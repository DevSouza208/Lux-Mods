package dev.luxmods.atlas.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Bounded local cache of already explored client-side map samples.
 * Coordinates and colors are stored, never copied from unknown chunks.
 * Scopes cache to the same singleplayer save/server and dimension as markers.
 */
public final class AtlasTerrainCache {
    private static final int MAGIC = 0x4C585431; // LXT1
    private static final int MAX_TILES = 120_000;

    private AtlasTerrainCache() {}

    private static Path path(Minecraft mc, Level level, String kind) {
        String world = AtlasMarkers.worldKey(mc);
        String dimension = AtlasMarkers.dimension(level).replaceAll("[^a-zA-Z0-9._-]", "_");
        return mc.gameDirectory.toPath().resolve("lux-atlas").resolve(world + "_" + dimension + "_" + kind + ".bin.gz");
    }

    public static void load(Minecraft mc, Level level, Map<Long, Integer> target) {
        load(mc, level, target, "map");
    }

    public static void load(Minecraft mc, Level level, Map<Long, Integer> target, String kind) {
        Path p = path(mc, level, kind);
        if (!Files.isRegularFile(p)) return;
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(Files.newInputStream(p)))) {
            if (in.readInt() != MAGIC) return;
            int count = in.readInt();
            if (count < 0 || count > MAX_TILES) return;
            target.clear();
            for (int i = 0; i < count; i++) {
                target.put(in.readLong(), in.readInt());
            }
        } catch (Exception ignored) {
            // Broken or old cache should never break gameplay.
        }
    }

    public static void save(Minecraft mc, Level level, Map<Long, Integer> tiles) {
        save(mc, level, tiles, "map");
    }

    public static void save(Minecraft mc, Level level, Map<Long, Integer> tiles, String kind) {
        if (tiles.isEmpty()) return;
        try {
            Path p = path(mc, level);
            Files.createDirectories(p.getParent());
            try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(p)))) {
                out.writeInt(MAGIC);
                out.writeInt(Math.min(MAX_TILES, tiles.size()));
                int i = 0;
                for (var entry : tiles.entrySet()) {
                    if (i++ >= MAX_TILES) break;
                    out.writeLong(entry.getKey());
                    out.writeInt(entry.getValue());
                }
            }
        } catch (Exception ignored) {
            // Read-only client directory: keep in-memory map working.
        }
    }
}
