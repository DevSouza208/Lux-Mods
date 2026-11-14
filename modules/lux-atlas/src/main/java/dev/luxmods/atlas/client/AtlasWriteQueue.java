package dev.luxmods.atlas.client;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Single-writer disk queue shared by Atlas background discovery and map UI.
 *
 * The game thread only snapshots dirty tiles into this queue. All region
 * read/modify/write operations run serially off-thread, so independent writers
 * cannot replace one another's updates. Failure retains the dirty entries
 * for a later retry; accepting a snapshot does NOT mean it is persisted.
 *
 * Queue is process-local: a crash or unwriteable disk may still lose unsaved
 * data. At disconnect, drain() makes a best-effort bounded wait and reports
 * failures rather than pretending every sample was committed.
 */
public final class AtlasWriteQueue {
    private static final Object LOCK = new Object();
    private static final Map<Path, Map<Long, Integer>> PENDING = new LinkedHashMap<>();
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "LuxAtlas-RegionWriter");
        thread.setDaemon(true);
        return thread;
    });

    private static boolean running;
    private static boolean failed;
    private static String error = "";
    private static long savedBatches;

    private AtlasWriteQueue() {}

    /** Non-blocking capture of the caller's dirty samples, partitioned by world/dimension. */
    public static void enqueue(Path scope, Map<Long, Integer> changes) {
        if (scope == null || changes.isEmpty()) return;
        synchronized (LOCK) {
            PENDING.computeIfAbsent(scope, ignored -> new HashMap<>()).putAll(changes);
            scheduleLocked();
        }
    }

    private static void scheduleLocked() {
        if (running || PENDING.isEmpty()) return;
        running = true;
        WRITER.execute(AtlasWriteQueue::process);
    }

    private static void process() {
        while (true) {
            Path scope;
            Map<Long, Integer> batch;
            synchronized (LOCK) {
                var first = PENDING.entrySet().iterator();
                if (!first.hasNext()) {
                    running = false;
                    LOCK.notifyAll();
                    return;
                }
                var entry = first.next();
                scope = entry.getKey();
                batch = entry.getValue();
                first.remove();
            }

            boolean ok = AtlasTerrainCache.saveToScope(scope, batch);
            synchronized (LOCK) {
                if (ok) {
                    savedBatches++;
                    failed = false;
                    error = "";
                } else {
                    // Newer queued values take precedence over failed older writes.
                    Map<Long, Integer> newer = PENDING.computeIfAbsent(scope, ignored -> new HashMap<>());
                    for (var item : batch.entrySet()) newer.putIfAbsent(item.getKey(), item.getValue());
                    failed = true;
                    error = "Falha ao gravar: " + scope.getFileName();
                    running = false;
                    LOCK.notifyAll();
                    return; // Do not busy-loop while storage is unavailable.
                }
            }
        }
    }

    /**
     * Periodic retry, normally called every few seconds while exploring.
     * Does not perform filesystem operations on the client thread.
     */
    public static void retry() {
        synchronized (LOCK) { scheduleLocked(); }
    }

    public record Status(int pendingSamples, boolean writing, boolean error,
                         String message, long successfulBatches) {}

    public static Status status() {
        synchronized (LOCK) {
            int count = 0;
            for (var batch : PENDING.values()) count += batch.size();
            return new Status(count, running, failed, error, savedBatches);
        }
    }

    /**
     * Drain pending writes at world disconnect. Waits for disk completion,
     * not merely for a task to be accepted. False means there are unsaved
     * samples or an IO error. The bounded wait avoids an infinite game hang.
     */
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
