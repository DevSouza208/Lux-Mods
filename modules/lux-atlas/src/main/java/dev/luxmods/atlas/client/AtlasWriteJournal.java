package dev.luxmods.atlas.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.zip.CRC32;

/**
 * Write-ahead journal. One small immutable file per batch; records are made
 * durable before they are acknowledged to the game thread.
 *
 * A journal is removed ONLY after its region updates have completed. On the
 * next visit to this world/dimension, uncommitted files can be replayed.
 */
public final class AtlasWriteJournal {
    private static final int MAGIC = 0x4c584a31; // LXJ1
    private static final int MAX_SAMPLES = 4_000_000;
    private static final String EXT = ".lxj";
    private static final Map<Path, Long> LAST_TIMESTAMP = new HashMap<>();
    private static final Map<Path, Long> LAST_SERIAL = new HashMap<>();

    private AtlasWriteJournal() {}

    public static Path folder(Path scope) { return scope.resolve(".pending"); }

    public static synchronized Path create(Path scope, Map<Long, Integer> samples)
            throws IOException {
        if (scope == null || samples.isEmpty() || samples.size() > MAX_SAMPLES)
            throw new IOException("Invalid Atlas journal batch");
        Path directory = folder(scope);
        Files.createDirectories(directory);
        // A computer clock can jump backwards between game launches. Seed
        // the logical timestamp and serial from surviving journals so that
        // newly journaled changes always sort AFTER older pending changes.
        if (!LAST_TIMESTAMP.containsKey(scope)) {
            long maxMillis = 0;
            long maxSerial = 0;
            for (Path existing : find(scope)) {
                String name = existing.getFileName().toString();
                String[] parts = name.split("-", 3);
                if (parts.length != 3) continue;
                try {
                    long stamp = Long.parseLong(parts[0]);
                    long serial = Long.parseLong(parts[1]);
                    if (stamp > maxMillis || stamp == maxMillis && serial > maxSerial) {
                        maxMillis = stamp;
                        maxSerial = serial;
                    }
                } catch (NumberFormatException ignored) {
                    // Old or manually renamed journals are still handled by
                    // recovery; they do not determine the next sequence.
                }
            }
            LAST_TIMESTAMP.put(scope, maxMillis);
            LAST_SERIAL.put(scope, maxSerial);
        }
        long millis = Math.max(System.currentTimeMillis(), LAST_TIMESTAMP.get(scope));
        long serial = millis == LAST_TIMESTAMP.get(scope)
                ? LAST_SERIAL.get(scope) + 1 : 1;
        String filename = String.format("%013d-%016d-%s%s",
                millis, serial, UUID.randomUUID(), EXT);
        Path target = directory.resolve(filename);
        Path temp = Files.createTempFile(directory, "pending-", ".tmp");
        try {
            // CRC protects against torn or truncated writes.
            CRC32 crc = new CRC32();
            byte[] pair = new byte[12];
            for (var entry : samples.entrySet()) {
                updateCrc(crc, pair, entry.getKey(), entry.getValue());
            }
            try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE);
                 DataOutputStream out = new DataOutputStream(
                         java.nio.channels.Channels.newOutputStream(channel))) {
                out.writeInt(MAGIC);
                out.writeInt(samples.size());
                out.writeLong(crc.getValue());
                for (var entry : samples.entrySet()) {
                    out.writeLong(entry.getKey());
                    out.writeInt(entry.getValue());
                }
                out.flush();
                channel.force(true);
            }
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temp, target);
            }
            syncDirectory(directory); // best effort across filesystems
            LAST_TIMESTAMP.put(scope, millis);
            LAST_SERIAL.put(scope, serial);
            return target;
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public static Map<Long, Integer> read(Path file) throws IOException {
        Map<Long, Integer> result = new HashMap<>();
        CRC32 crc = new CRC32();
        byte[] pair = new byte[12];
        try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
            if (in.readInt() != MAGIC) throw new IOException("Invalid Atlas journal header");
            int count = in.readInt();
            long expectedCrc = in.readLong();
            if (count < 1 || count > MAX_SAMPLES)
                throw new IOException("Invalid Atlas journal sample count");
            // LXJ1 has a fixed 16-byte header and exactly 12 bytes per
            // sample. Reject truncated/oversized files before allocating a
            // map or replaying any updates.
            long expectedBytes = 16L + 12L * count;
            if (Files.size(file) != expectedBytes)
                throw new IOException("Invalid Atlas journal length");
            for (int i = 0; i < count; i++) {
                long key = in.readLong();
                int value = in.readInt();
                updateCrc(crc, pair, key, value);
                if (result.put(key, value) != null)
                    throw new IOException("Duplicate Atlas journal coordinate");
            }
            if (in.read() != -1 || crc.getValue() != expectedCrc)
                throw new IOException("Damaged Atlas journal");
        }
        return result;
    }

    /** Reuse one 12-byte buffer per batch instead of allocating per sample. */
    private static void updateCrc(CRC32 crc, byte[] pair, long key, int value) {
        for (int i = 7; i >= 0; i--) {
            pair[i] = (byte) key;
            key >>>= 8;
        }
        for (int i = 11; i >= 8; i--) {
            pair[i] = (byte) value;
            value >>>= 8;
        }
        crc.update(pair, 0, pair.length);
    }

    public static List<Path> find(Path scope) throws IOException {
        Path directory = folder(scope);
        if (!Files.isDirectory(directory)) return List.of();
        try (var entries = Files.list(directory)) {
            return entries.filter(path -> path.getFileName().toString().endsWith(EXT))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }

    public static void complete(Path file) throws IOException {
        Files.delete(file);
        syncDirectory(file.getParent());
    }

    /** Directory fsync is unsupported on some platforms (notably Windows). */
    private static void syncDirectory(Path directory) {
        try (FileChannel channel = FileChannel.open(directory, StandardOpenOption.READ)) {
            channel.force(true);
        } catch (IOException | UnsupportedOperationException ignored) {
            // The data file was forced already; directory durability is best effort.
        }
    }
}
