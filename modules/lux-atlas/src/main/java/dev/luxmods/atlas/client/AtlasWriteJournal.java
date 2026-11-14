package dev.luxmods.atlas.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
import java.util.concurrent.atomic.AtomicLong;
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
    private static final AtomicLong SERIAL = new AtomicLong();

    private AtlasWriteJournal() {}

    public static Path folder(Path scope) { return scope.resolve(".pending"); }

    public static Path create(Path scope, Map<Long, Integer> samples) throws IOException {
        if (scope == null || samples.isEmpty() || samples.size() > MAX_SAMPLES)
            throw new IOException("Invalid Atlas journal batch");
        Path directory = folder(scope);
        Files.createDirectories(directory);
        String filename = String.format("%013d-%016d-%s%s",
                System.currentTimeMillis(), SERIAL.incrementAndGet(), UUID.randomUUID(), EXT);
        Path target = directory.resolve(filename);
        Path temp = Files.createTempFile(directory, "pending-", ".tmp");
        try {
            // CRC protects against torn or truncated writes.
            CRC32 crc = new CRC32();
            for (var entry : samples.entrySet()) {
                ByteBuffer pair = ByteBuffer.allocate(12);
                pair.putLong(entry.getKey()).putInt(entry.getValue());
                crc.update(pair.array());
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
            return target;
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public static Map<Long, Integer> read(Path file) throws IOException {
        Map<Long, Integer> result = new HashMap<>();
        CRC32 crc = new CRC32();
        try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
            if (in.readInt() != MAGIC) throw new IOException("Invalid Atlas journal header");
            int count = in.readInt();
            long expectedCrc = in.readLong();
            if (count < 1 || count > MAX_SAMPLES)
                throw new IOException("Invalid Atlas journal sample count");
            for (int i = 0; i < count; i++) {
                long key = in.readLong();
                int value = in.readInt();
                ByteBuffer pair = ByteBuffer.allocate(12);
                pair.putLong(key).putInt(value);
                crc.update(pair.array());
                if (result.put(key, value) != null)
                    throw new IOException("Duplicate Atlas journal coordinate");
            }
            if (in.read() != -1 || crc.getValue() != expectedCrc)
                throw new IOException("Damaged Atlas journal");
        }
        return result;
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
