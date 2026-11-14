package dev.luxmods.atlas.client;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Single region writer, backed by durable per-batch journals (LXJ1).
 * Disk operations never run concurrently. Each scope is a world+dimension.
 *
 * Important: enqueue synchronously writes its SMALL journal and can take
 * filesystem time. Compression of big terrain regions remains off-thread.
 */
public final class AtlasWriteQueue {
    private static final Object LOCK = new Object();
    private record Job(Path scope, int samples) {}
    private static final Map<Path, Job> PENDING = new LinkedHashMap<>();
    private static final Set<Path> REGISTERED = new HashSet<>();
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "LuxAtlas-RegionWriter");
        thread.setDaemon(true);
        return thread;
    });

    private static boolean running;
    private static boolean failed;
    private static String error = "";
    private static long savedBatches;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!drain(5000))
                System.err.println("[Lux Atlas] Unfinished journals remain: " + status().message());
        }, "LuxAtlas-DrainOnExit"));
    }

    private AtlasWriteQueue() {}

    /** Discover previous interrupted journal files before accepting newer updates. */
    public static boolean registerScope(Path scope) {
        if (scope == null) return false;
        synchronized (LOCK) {
            if (REGISTERED.contains(scope)) return true;
            try {
                // Validate the complete recovery set before inserting ANY of
                // its entries. Otherwise a corrupt later file can leave an
                // incomplete prefix queued and be silently skipped on retry.
                Map<Path, Job> discovered = new LinkedHashMap<>();
                for (Path journal : AtlasWriteJournal.find(scope)) {
                    int count = AtlasWriteJournal.read(journal).size();
                    discovered.put(journal, new Job(scope, count));
                }
                // Replay pre-existing disk journals before new session writes.
                Map<Path, Job> merged = new LinkedHashMap<>(discovered);
                merged.putAll(PENDING);
                PENDING.clear();
                PENDING.putAll(merged);
                REGISTERED.add(scope);
                scheduleLocked();
                return true;
            } catch (IOException failure) {
                failed = true;
                error = "Falha ao recuperar diário: " + scope.getFileName();
                return false;
            }
        }
    }

    /**
     * Durable acknowledgement: returns false if no journal was written.
     * Caller MUST retain its own dirty map when this method returns false.
     */
    public static boolean enqueue(Path scope, Map<Long, Integer> changes) {
        if (scope == null || changes.isEmpty()) return changes.isEmpty();
        synchronized (LOCK) {
            if (!registerScope(scope)) return false;
            try {
                Path journal = AtlasWriteJournal.create(scope, changes);
                PENDING.put(journal, new Job(scope, changes.size()));
                // Do not clear a previous save failure just because another
                // journal was created successfully.
                scheduleLocked();
                return true;
            } catch (IOException failure) {
                failed = true;
                error = "Falha ao criar diário: " + scope.getFileName();
                return false;
            }
        }
    }

    private static void scheduleLocked() {
        if (running || PENDING.isEmpty()) return;
        running = true;
        WRITER.execute(AtlasWriteQueue::process);
    }

    private static void process() {
        while (true) {
            Path journal;
            Job job;
            synchronized (LOCK) {
                var iterator = PENDING.entrySet().iterator();
                if (!iterator.hasNext()) {
                    running = false;
                    LOCK.notifyAll();
                    return;
                }
                var next = iterator.next();
                journal = next.getKey();
                job = next.getValue();
                iterator.remove();
            }
            boolean ok = false;
            try {
                Map<Long, Integer> updates = AtlasWriteJournal.read(journal);
                if (AtlasTerrainCache.saveToScope(job.scope(), updates)) {
                    AtlasWriteJournal.complete(journal);
                    ok = true;
                }
            } catch (IOException | RuntimeException failure) {
                ok = false;
            }
            synchronized (LOCK) {
                if (ok) {
                    savedBatches++;
                    failed = false;
                    error = "";
                } else {
                    // Reinsert failed journal at FRONT to preserve update
                    // ordering; never process later writes ahead of it.
                    Map<Path, Job> reordered = new LinkedHashMap<>();
                    reordered.put(journal, job);
                    reordered.putAll(PENDING);
                    PENDING.clear();
                    PENDING.putAll(reordered);
                    failed = true;
                    error = "Falha ao aplicar diário: " + job.scope().getFileName();
                    running = false;
                    LOCK.notifyAll();
                    return;
                }
            }
        }
    }

    public static void retry() {
        synchronized (LOCK) { scheduleLocked(); }
    }

    public record Status(int pendingSamples, boolean writing, boolean error,
                         String message, long successfulBatches) {}

    public static Status status() {
        synchronized (LOCK) {
            int count = 0;
            for (Job job : PENDING.values()) count += job.samples();
            return new Status(count, running, failed, error, savedBatches);
        }
    }

    /** Best-effort bounded drain. Never claims success while work is unfinished. */
    public static boolean drain(long timeoutMillis) {
        retry();
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        synchronized (LOCK) {
            while (running && System.nanoTime() < deadline) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) break;
                try {
                    TimeUnit.NANOSECONDS.timedWait(LOCK, remaining);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return !running && PENDING.isEmpty() && !failed;
        }
    }
}
