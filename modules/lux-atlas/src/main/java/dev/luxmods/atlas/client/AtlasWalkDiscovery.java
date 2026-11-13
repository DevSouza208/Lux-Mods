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
            return size() > 120_000;
        }
    };
    private static int sinceSave;

    private AtlasWalkDiscovery() {}

    public static void flush() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level == world && sinceSave > 0) {
            AtlasTerrainCache.save(mc, world, known, "walk");
            sinceSave = 0;
        }
    }

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            world = null;
            known.clear();
            sinceSave = 0;
            return;
        }
        if (world != mc.level) {
            world = mc.level;
            known.clear();
            AtlasTerrainCache.load(mc, world, known, "walk");
            sinceSave = 0;
        }

        if (mc.player.tickCount % 40 != 0) return;
        // 9x9 samples around the player at step=2; include both sample
        // parities so map panning does not leave alternating blank cells.
        final int step = 2;
        int px = mc.player.getBlockX();
        int pz = mc.player.getBlockZ();
        int added = 0;
        for (int dz = -8; dz <= 8; dz += step) {
            for (int dx = -8; dx <= 8; dx += step) {
                for (int oz = 0; oz < 2; oz++) {
                    for (int ox = 0; ox < 2; ox++) {
                        int wx = px + dx + ox, wz = pz + dz + oz;
                        long key = AtlasMapScreen.key(wx, wz, step);
                        if (known.containsKey(key) || !world.getChunkSource().hasChunk(wx >> 4, wz >> 4)) continue;
                        int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz);
                        if (y <= world.getMinBuildHeight() || y >= world.getMaxBuildHeight()) continue;
                        BlockPos pos = new BlockPos(wx, y - 1, wz);
                        int color = world.getBlockState(pos).getMapColor(world, pos).col;
                        if (color == 0) color = 0x576C52;
                        known.put(key, 0xFF000000 | color);
                        added++;
                    }
                }
            }
        }
        if (added > 0) sinceSave += 40;
        // Persist more frequently to avoid losing a short exploration session.
        if (sinceSave >= 80) flush();
    }
}
