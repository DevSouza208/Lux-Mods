package dev.luxmods.atlas.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Passive exploration while walking: sample only terrain in loaded chunks.
 * Uses a separate on-disk cache so the map UI never overwrites exploration
 * collected in the background. Does not generate or request distant chunks.
 */
public final class AtlasWalkDiscovery {
    private static ClientLevel world;
    private static final Map<Long, Integer> known = new LinkedHashMap<>(2048, 0.75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, Integer> eldest) {
            return size() > 400_000;
        }
    };
    private static int sinceSave;
    private static final Map<Long, Integer> pending = new LinkedHashMap<>();

    private AtlasWalkDiscovery() {}

    /** Bring recently explored blocks into an already open Atlas, without disk IO. */
    public static void mergeInto(Map<Long, Integer> target) {
        target.putAll(known);
    }

    public static void flush() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level == world && !pending.isEmpty()) {
            if (AtlasTerrainCache.save(mc, world, pending)) {
                pending.clear();
                sinceSave = 0;
            }
        }
    }

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            // Preserve pending samples if the save cannot be written yet.
            world = null;
            known.clear();
            pending.clear();
            sinceSave = 0;
            return;
        }
        if (world != mc.level) {
            world = mc.level;
            known.clear();
            pending.clear();
            AtlasTerrainCache.migrate(mc, world);
            sinceSave = 0;
        }

        if (mc.player.tickCount % 40 != 0) return;
        // Explore up to 24 blocks around the player, sampling on a stable
        // 2x2 world-aligned grid regardless of the player position parity.
        final int step = 2;
        int px = mc.player.getBlockX();
        int pz = mc.player.getBlockZ();
        int added = 0;
        // The archive remains complete on disk; this bounded map is only a
        // runtime hot cache. Previously explored cells can be read by Atlas.
        int baseX = Math.floorDiv(px, step) * step;
        int baseZ = Math.floorDiv(pz, step) * step;
        for (int dz = -24; dz <= 24; dz += step) {
            for (int dx = -24; dx <= 24; dx += step) {
                int wx = baseX + dx, wz = baseZ + dz;
                long key = AtlasMapScreen.key(wx, wz, step);
                if (known.containsKey(key) || !world.getChunkSource().hasChunk(wx >> 4, wz >> 4)) continue;
                int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz);
                if (y <= world.getMinBuildHeight() || y >= world.getMaxBuildHeight()) continue;
                BlockPos pos = new BlockPos(wx, y - 1, wz);
                int color = world.getBlockState(pos).getMapColor(world, pos).col;
                if (color == 0) color = 0x576C52;
                int relief = (y - world.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz - 1)) * 5;
                int shade = Math.max(-38, Math.min(38, (y - 72) / 4 + relief));
                int red = Math.max(0, Math.min(255, ((color >> 16) & 255) + shade));
                int green = Math.max(0, Math.min(255, ((color >> 8) & 255) + shade));
                int blue = Math.max(0, Math.min(255, (color & 255) + shade));
                int sample = 0xFF000000 | (red << 16) | (green << 8) | blue;
                known.put(key, sample);
                pending.put(key, sample);
                added++;
            }
        }
        if (added > 0) sinceSave += 40;
        // Persist more frequently to avoid losing a short exploration session.
        if (sinceSave >= 80) flush();
    }
}
