package dev.luxmods.atlas.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

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
    private static int scanCursor;
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

    private static boolean sampleOne(int wx, int wz) {
        long key = AtlasMapScreen.key(wx, wz, 1);
        if (known.containsKey(key) || world == null
                || !world.getChunkSource().hasChunk(wx >> 4, wz >> 4)) return false;
        int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz);
        if (y <= world.getMinBuildHeight() || y >= world.getMaxBuildHeight()) return false;
        BlockPos pos = new BlockPos(wx, y - 1, wz);
        int color = world.getBlockState(pos).getMapColor(world, pos).col;
        if (color == 0) color = 0x576C52;
        int northY = world.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz - 1);
        int relief = (y - northY) * 5;
        int shade = Math.max(-38, Math.min(38, (y - 72) / 4 + relief));
        int red = Math.max(0, Math.min(255, ((color >> 16) & 255) + shade));
        int green = Math.max(0, Math.min(255, ((color >> 8) & 255) + shade));
        int blue = Math.max(0, Math.min(255, (color & 255) + shade));
        int sample = 0xFF000000 | (red << 16) | (green << 8) | blue;
        known.put(key, sample);
        pending.put(key, sample);
        return true;
    }

    /** Persist the last few seconds of exploration before leaving the server. */
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        flush();
    }

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            // Preserve pending samples if the save cannot be written yet.
            world = null;
            known.clear();
            pending.clear();
            sinceSave = 0;
            scanCursor = 0;
            return;
        }
        if (world != mc.level) {
            world = mc.level;
            known.clear();
            pending.clear();
            AtlasTerrainCache.migrate(mc, world);
            sinceSave = 0;
            scanCursor = 0;
        }

        if (mc.player.tickCount % 40 != 0) return;
        // Each stored cell now corresponds to one exact world block.
        // Immediate surroundings fill reliably; outer loaded terrain is
        // sampled progressively with a bounded per-tick budget.
        final int step = 1;
        int baseX = mc.player.getBlockX();
        int baseZ = mc.player.getBlockZ();
        int added = 0;
        for (int dz = -16; dz <= 16; dz += step) {
            for (int dx = -16; dx <= 16; dx += step) {
                if (sampleOne(baseX + dx, baseZ + dz)) added++;
            }
        }
        final int radius = 96;
        final int side = radius / step * 2 + 1;
        final int total = side * side;
        final int budget = 900;
        for (int i = 0; i < budget; i++) {
            int index = scanCursor++ % total;
            int dx = (index % side - side / 2) * step;
            int dz = (index / side - side / 2) * step;
            if (sampleOne(baseX + dx, baseZ + dz)) added++;
        }
        if (added > 0) sinceSave += 40;
        // Persist more frequently to avoid losing a short exploration session.
        if (sinceSave >= 80) flush();
    }
}
