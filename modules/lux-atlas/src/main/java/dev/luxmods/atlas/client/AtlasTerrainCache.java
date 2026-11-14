package dev.luxmods.atlas.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Persistent, disk-backed terrain archive. Memory limits are NOT world limits:
 * each 128x128-block region is saved separately, and only regions in view
 * need to be read into RAM. The archive never deletes explored regions.
 *
 * Old LXT1 map/walk snapshots are imported once, without deleting originals.
 */
public final class AtlasTerrainCache {
    private static final int LEGACY_MAGIC = 0x4C585431; // LXT1
    private static final int REGION_MAGIC = 0x4C585432; // LXT2
    public static final int REGION_BLOCKS = 128;
    private static final int MAX_REGION_SAMPLES = 65536;
    private static final int MAX_LEGACY_SAMPLES = 400_000;
    private static final Set<Path> MIGRATED_THIS_SESSION = new HashSet<>();

    private AtlasTerrainCache() {}

    private static Path oldPath(Minecraft mc, Level level, String kind) {
        String world = AtlasMarkers.worldKey(mc);
        String dimension = AtlasMarkers.dimension(level).replaceAll("[^a-zA-Z0-9._-]", "_");
        return mc.gameDirectory.toPath().resolve("lux-atlas")
                .resolve(world + "_" + dimension + "_" + kind + ".bin.gz");
    }

    private static Path regionFolder(Minecraft mc, Level level) {
        String world = AtlasMarkers.worldKey(mc);
        String dimension = AtlasMarkers.dimension(level).replaceAll("[^a-zA-Z0-9._-]", "_");
        return mc.gameDirectory.toPath().resolve("lux-atlas")
                .resolve(world + "_" + dimension + "_regions");
    }

    /**
     * Freeze the save scope while the player is inside a specific world.
     * Needed to save pending exploration safely after a dimension transfer.
     */
    static Path scope(Minecraft mc, Level level) {
        return regionFolder(mc, level);
    }

    private static Path regionPath(Path folder, int rx, int rz) {
        return folder.resolve("r." + rx + "." + rz + ".lxt.gz");
    }

    static int region(int coordinate) {
        return Math.floorDiv(coordinate, REGION_BLOCKS);
    }

    static long regionKey(int rx, int rz) {
        return ((long) rx << 32) ^ (rz & 0xffffffffL);
    }

    // The original 28-bit X/Z packed format used by AtlasMapScreen.
    private static int unpackX(long key) {
        int value = (int) ((key >>> 36) & 0x0fffffffL);
        return (value << 4) >> 4;
    }

    private static int unpackZ(long key) {
        int value = (int) ((key >>> 8) & 0x0fffffffL);
        return (value << 4) >> 4;
    }

    private static void readRegion(Path file, Map<Long, Integer> target) throws IOException {
        if (!Files.isRegularFile(file)) return;
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))) {
            if (in.readInt() != REGION_MAGIC) throw new IOException("Invalid Atlas region header");
            int count = in.readInt();
            if (count < 0 || count > MAX_REGION_SAMPLES) throw new IOException("Invalid Atlas region count");
            for (int i = 0; i < count; i++) {
                target.put(in.readLong(), in.readInt());
            }
        }
    }

    private static void writeRegion(Path file, Map<Long, Integer> data) throws IOException {
        Files.createDirectories(file.getParent());
        Path temp = Files.createTempFile(file.getParent(), "atlas-", ".tmp");
        try {
            try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(temp)))) {
                out.writeInt(REGION_MAGIC);
                out.writeInt(data.size());
                for (var entry : data.entrySet()) {
                    out.writeLong(entry.getKey());
                    out.writeInt(entry.getValue());
                }
            }
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void persist(Path folder, Map<Long, Integer> updates) throws IOException {
        Map<Long, Map<Long, Integer>> batches = new HashMap<>();
        for (var sample : updates.entrySet()) {
            int rx = region(unpackX(sample.getKey()));
            int rz = region(unpackZ(sample.getKey()));
            batches.computeIfAbsent(regionKey(rx, rz), ignored -> new HashMap<>())
                    .put(sample.getKey(), sample.getValue());
        }
        for (var batch : batches.entrySet()) {
            int rx = (int) (batch.getKey() >> 32);
            int rz = (int) (long) batch.getKey();
            Path path = regionPath(folder, rx, rz);
            Map<Long, Integer> contents = new HashMap<>();
            readRegion(path, contents);
            contents.putAll(batch.getValue());
            if (contents.size() > MAX_REGION_SAMPLES) throw new IOException("Atlas region overflow");
            writeRegion(path, contents);
        }
    }

    private static Map<Long, Integer> legacySnapshot(Path path) throws IOException {
        Map<Long, Integer> result = new HashMap<>();
        if (!Files.isRegularFile(path)) return result;
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))) {
            if (in.readInt() != LEGACY_MAGIC) throw new IOException("Invalid old Atlas header");
            int count = in.readInt();
            if (count < 0 || count > MAX_LEGACY_SAMPLES) throw new IOException("Invalid old Atlas count");
            for (int i = 0; i < count; i++) result.put(in.readLong(), in.readInt());
        }
        return result;
    }

    /** Imports previous monolithic maps once, retaining their original backups. */
    public static void migrate(Minecraft mc, Level level) {
        Path folder = regionFolder(mc, level);
        if (!MIGRATED_THIS_SESSION.add(folder)) return;
        Path marker = folder.resolve(".legacy-imported");
        if (Files.isRegularFile(marker)) return;
        try {
            Map<Long, Integer> old = legacySnapshot(oldPath(mc, level, "map"));
            old.putAll(legacySnapshot(oldPath(mc, level, "walk")));
            if (!old.isEmpty()) persist(folder, old);
            Files.createDirectories(folder);
            Files.writeString(marker, "LXT1 imported");
        } catch (IOException ignored) {
            // Retry next session; original files remain untouched.
            MIGRATED_THIS_SESSION.remove(folder);
        }
    }

    /**
     * Read one 128-block region on demand. Caller tracks which regions have
     * already been loaded for its current view; the method does no chunk IO.
     */
    public static void loadRegion(Minecraft mc, Level level, int rx, int rz,
                                  Map<Long, Integer> target) {
        migrate(mc, level);
        try {
            Map<Long, Integer> region = new HashMap<>();
            readRegion(regionPath(regionFolder(mc, level), rx, rz), region);
            target.putAll(region);
        } catch (IOException ignored) {
            // Corrupt region affects only this area, not other explored regions.
        }
    }

    /**
     * Persist into a scope captured before a world/dimension switch.
     * Never derive a changed server name from a new active connection.
     */
    static boolean saveToScope(Path folder, Map<Long, Integer> updates) {
        if (updates.isEmpty()) return true;
        if (folder == null) return false;
        try {
            persist(folder, updates);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /** Commit discovered samples by region; older regions are never truncated. */
    public static boolean save(Minecraft mc, Level level, Map<Long, Integer> updates) {
        if (updates.isEmpty()) return true;
        migrate(mc, level);
        return saveToScope(regionFolder(mc, level), updates);
    }
}
