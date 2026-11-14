package dev.luxmods.atlas.client;

import dev.luxmods.atlas.AtlasBiomeNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bounded in-memory biome preview for the currently connected world only.
 * No seed guesses or disk persistence: never mix two servers with identical
 * dimension names. True observed surface colors take precedence in the map.
 */
public final class AtlasBiomeClient {
    private static ClientLevel currentLevel;
    private static String dimension;
    private static final int MAX_REGIONS = 64;
    private static final Map<Long, int[]> REGIONS = new LinkedHashMap<>(32, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, int[]> entry) {
            return size() > MAX_REGIONS;
        }
    };
    private static final Map<Long, Integer> REQUESTED =
            new LinkedHashMap<>(128, .75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<Long, Integer> entry) {
                    return size() > 128;
                }
            };

    private AtlasBiomeClient() {}

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private static void checkWorld() {
        Minecraft mc = Minecraft.getInstance();
        String now = mc.level == null ? null : mc.level.dimension().location().toString();
        if (mc.level != currentLevel || !java.util.Objects.equals(dimension, now)) {
            currentLevel = mc.level;
            dimension = now;
            REGIONS.clear();
            REQUESTED.clear();
        }
    }

    public static void requestVisible(int minX, int minZ, int maxX, int maxZ, int ticks) {
        checkWorld();
        if (currentLevel == null || Minecraft.getInstance().player == null) return;
        int startX = Math.floorDiv(minX, AtlasBiomeNetwork.REGION_BLOCKS);
        int startZ = Math.floorDiv(minZ, AtlasBiomeNetwork.REGION_BLOCKS);
        int endX = Math.floorDiv(maxX, AtlasBiomeNetwork.REGION_BLOCKS);
        int endZ = Math.floorDiv(maxZ, AtlasBiomeNetwork.REGION_BLOCKS);
        // Only one request every 12 client ticks. A server can throttle as well.
        if (ticks % 12 != 0) return;
        for (int z = startZ; z <= endZ; z++) {
            for (int x = startX; x <= endX; x++) {
                long id = key(x, z);
                if (REGIONS.containsKey(id)) continue;
                Integer last = REQUESTED.get(id);
                if (last != null && ticks >= last && ticks - last < 200) continue;
                REQUESTED.put(id, ticks);
                PacketDistributor.sendToServer(new AtlasBiomeNetwork.Request(
                        x * AtlasBiomeNetwork.REGION_BLOCKS,
                        z * AtlasBiomeNetwork.REGION_BLOCKS));
                return;
            }
        }
    }

    public static void receive(AtlasBiomeNetwork.Preview response) {
        checkWorld();
        if (currentLevel == null || dimension == null
                || !dimension.equals(response.dimension())
                || response.colors().length != AtlasBiomeNetwork.SIDE * AtlasBiomeNetwork.SIDE
                || response.originX() % AtlasBiomeNetwork.REGION_BLOCKS != 0
                || response.originZ() % AtlasBiomeNetwork.REGION_BLOCKS != 0) return;
        int rx = Math.floorDiv(response.originX(), AtlasBiomeNetwork.REGION_BLOCKS);
        int rz = Math.floorDiv(response.originZ(), AtlasBiomeNetwork.REGION_BLOCKS);
        long key = key(rx, rz);
        // Only accept a reply to a request made in this ClientLevel. A delayed
        // packet from a previous connection using the same dimension name
        // must not populate the new world's biome preview.
        if (!REQUESTED.containsKey(key)) return;
        REGIONS.put(key, response.colors().clone());
        REQUESTED.remove(key);
    }

    public static Integer color(int x, int z) {
        int rx = Math.floorDiv(x, AtlasBiomeNetwork.REGION_BLOCKS);
        int rz = Math.floorDiv(z, AtlasBiomeNetwork.REGION_BLOCKS);
        int[] region = REGIONS.get(key(rx, rz));
        if (region == null) return null;
        int ix = Math.floorMod(x, AtlasBiomeNetwork.REGION_BLOCKS)
                / AtlasBiomeNetwork.CELL_BLOCKS;
        int iz = Math.floorMod(z, AtlasBiomeNetwork.REGION_BLOCKS)
                / AtlasBiomeNetwork.CELL_BLOCKS;
        return region[iz * AtlasBiomeNetwork.SIDE + ix];
    }
}
