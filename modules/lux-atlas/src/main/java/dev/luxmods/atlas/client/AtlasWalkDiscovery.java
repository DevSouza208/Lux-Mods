package dev.luxmods.atlas.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.LinkedHashMap;
import java.nio.file.Path;
import java.util.Map;

/**
 * Passive exploration while walking: sample only terrain in loaded chunks.
 * Uses a separate on-disk cache so the map UI never overwrites exploration
 * collected in the background. Does not generate or request distant chunks.
 */
public final class AtlasWalkDiscovery {
    private static ClientLevel world;
    // Do not resume scanning a disconnected level that remains attached to
    // Minecraft for a few client ticks during the logout transition.
    private static ClientLevel disconnectedWorld;
    private static final Map<Long, Integer> known = new LinkedHashMap<>(2048, 0.75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, Integer> eldest) {
            return size() > 400_000;
        }
    };
    private static int sinceSave;
    private static int scanCursor;
    private static int refreshCursor;
    private static Path saveScope;
    private static final Map<Long, Integer> pending = new LinkedHashMap<>();
    // If even the durable journal cannot be created, keep the dirty samples
    // associated with the original world and retry on subsequent ticks.
    private static final Map<Path, Map<Long, Integer>> UNSUBMITTED = new LinkedHashMap<>();

    private AtlasWalkDiscovery() {}

    /** Bring recently explored blocks into an already open Atlas, without disk IO. */
    public static void mergeInto(Map<Long, Integer> target) {
        target.putAll(known);
    }

    /** Apply the live walk snapshot, then preserve newer map-screen edits. */
    public static void mergeInto(Map<Long, Integer> target,
                                 Map<Long, Integer> screenEdits) {
        AtlasTileLayers.overlay(target, known, screenEdits);
    }

    /**
     * Transfer unjournaled map-screen samples before that screen is destroyed.
     * This is a session fallback, not a successful disk save.
     */
    public static void keepUnsubmitted(Path scope, Map<Long, Integer> changes) {
        if (scope == null || changes.isEmpty()) return;
        UNSUBMITTED.computeIfAbsent(scope, ignored -> new LinkedHashMap<>())
                .putAll(changes);
    }

    public static void flush() {
        // Retry older world samples before the current world. Never relabel
        // failed observations as belonging to the new dimension.
        var iterator = UNSUBMITTED.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (AtlasWriteQueue.enqueue(entry.getKey(), entry.getValue()))
                iterator.remove();
        }
        // An older unjournaled batch for this same world must be accepted
        // first. Otherwise a later successful batch can be replayed before
        // the older one, reverting newly explored terrain after recovery.
        if (!pending.isEmpty() && saveScope != null
                && !UNSUBMITTED.containsKey(saveScope)) {
            // Returning true means journaled, not yet applied to regions.
            if (AtlasWriteQueue.enqueue(saveScope, pending)) {
                pending.clear();
                sinceSave = 0;
            }
        }
    }

    private static void rotateWorld() {
        flush();
        if (!pending.isEmpty() && saveScope != null) {
            // Failed screen saves may already contain newer edits for the
            // same coordinates. Preserve them when rotating dimensions.
            Map<Long, Integer> older = new LinkedHashMap<>(pending);
            Map<Long, Integer> newer = UNSUBMITTED.get(saveScope);
            if (newer != null) older.putAll(newer);
            UNSUBMITTED.put(saveScope, older);
        }
        pending.clear();
        known.clear();
        world = null;
        saveScope = null;
        sinceSave = 0;
        scanCursor = 0;
        refreshCursor = 0;
    }

    private static boolean sampleOne(int wx, int wz, boolean refresh) {
        long key = AtlasMapScreen.key(wx, wz, 1);
        if (world == null || !world.getChunkSource().hasChunk(wx >> 4, wz >> 4)
                || (!refresh && known.containsKey(key))) return false;
        int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz);
        if (y <= world.getMinBuildHeight() || y >= world.getMaxBuildHeight()) return false;
        BlockPos pos = new BlockPos(wx, y - 1, wz);
        var state = world.getBlockState(pos);
        // Height lookups at chunk borders must not request unloaded
        // neighbors: passive exploration must never trigger world generation.
        int northY = world.getChunkSource().hasChunk(wx >> 4, (wz - 1) >> 4)
                ? world.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz - 1) : y;
        int westY = world.getChunkSource().hasChunk((wx - 1) >> 4, wz >> 4)
                ? world.getHeight(Heightmap.Types.WORLD_SURFACE, wx - 1, wz) : y;
        int sample = AtlasTerrainColor.shade(state.getMapColor(world, pos).col,
                y, northY, westY, state.is(Blocks.WATER));
        Integer previous = known.put(key, sample);
        // Revisits detect modifications, but unchanged terrain never gets
        // pointlessly re-journaled. Colors are stable across the same sampler.
        if (previous != null && previous == sample) return false;
        pending.put(key, sample);
        return true;
    }

    /** Persist the last few seconds of exploration before leaving the server. */
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        flush();
        if (!AtlasWriteQueue.drain(3000)) {
            System.err.println("[Lux Atlas] Gravações pendentes ao desconectar: "
                    + AtlasWriteQueue.status().message());
        }
        // The client can retain its old ClientLevel briefly after logout.
        // Clear its live samples immediately so opening the map during the
        // transition cannot merge another server's exploration into it.
        disconnectedWorld = world;
        rotateWorld();
    }

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == disconnectedWorld && disconnectedWorld != null) return;
        if (mc.level != disconnectedWorld) disconnectedWorld = null;
        if (mc.level == null || mc.player == null) {
            if (world != null) rotateWorld();
            return;
        }
        if (world != mc.level) {
            if (world != null) rotateWorld();
            world = mc.level;
            AtlasTerrainCache.migrate(mc, world);
            saveScope = AtlasTerrainCache.scope(mc, world);
            AtlasWriteQueue.registerScope(saveScope);
            AtlasWriteQueue.retry(); // recover interrupted journals for this world
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
                if (sampleOne(baseX + dx, baseZ + dz, false)) added++;
            }
        }
        // Refresh 64 nearby coordinates per sampling cycle. This discovers
        // roads, bridges and buildings erected AFTER the terrain was mapped,
        // without recalculating the entire 33x33 neighborhood each time.
        final int revisitSide = 33;
        for (int i = 0; i < 64; i++) {
            int index = Math.floorMod(refreshCursor++, revisitSide * revisitSide);
            int dx = index % revisitSide - revisitSide / 2;
            int dz = index / revisitSide - revisitSide / 2;
            if (sampleOne(baseX + dx, baseZ + dz, true)) added++;
        }
        final int radius = 96;
        final int side = radius / step * 2 + 1;
        final int total = side * side;
        final int budget = 900;
        for (int i = 0; i < budget; i++) {
            // Keep the scan cursor bounded: integer overflow after long
            // sessions would otherwise produce negative sample offsets.
            int index = scanCursor;
            scanCursor = (scanCursor + 1) % total;
            int dx = (index % side - side / 2) * step;
            int dz = (index / side - side / 2) * step;
            if (sampleOne(baseX + dx, baseZ + dz, false)) added++;
        }
        if (added > 0) sinceSave += 40;
        // Persist more frequently to avoid losing a short exploration session.
        if (sinceSave >= 80) flush();
        if (mc.player.tickCount % 200 == 0) {
            flush();
            AtlasWriteQueue.retry();
        }
    }
}
