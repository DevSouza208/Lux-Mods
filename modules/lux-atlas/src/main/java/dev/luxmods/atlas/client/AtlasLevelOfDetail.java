package dev.luxmods.atlas.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Screen-only level-of-detail index built from already recorded map samples.
 *
 * The one-block archive is authoritative: this index is NEVER written to the
 * world's region files. A coarse cell is shown only when it contains a real
 * discovered sample; unknown world terrain is not guessed or generated.
 */
public final class AtlasLevelOfDetail {
    private static final int[] STEPS = {2, 4, 8, 16};
    private final Map<Integer, Map<Long, Integer>> colors = new HashMap<>();

    private static final class Average {
        long red, green, blue, weight;
        void add(int color, int sampleWeight) {
            red += (color >>> 16 & 255L) * sampleWeight;
            green += (color >>> 8 & 255L) * sampleWeight;
            blue += (color & 255L) * sampleWeight;
            weight += sampleWeight;
        }
        int color() {
            return 0xff000000 | ((int) (red / weight) << 16)
                    | ((int) (green / weight) << 8) | (int) (blue / weight);
        }
    }

    /**
     * Recompute only when visible terrain changed, not on every render.
     * Old 2x2 samples are included as an approximated 2x2 footprint, while
     * new samples contribute an actual single block.
     */
    public void rebuild(Map<Long, Integer> source) {
        Map<Integer, Map<Long, Average>> sums = new HashMap<>();
        for (int step : STEPS) sums.put(step, new HashMap<>());
        for (var entry : source.entrySet()) {
            long id = entry.getKey();
            int resolution = (int) (id & 0xffL);
            if (resolution != 1 && resolution != 2) continue;
            int wx = AtlasViewport.unpackX(id), wz = AtlasViewport.unpackZ(id);
            // Existing LXT1 2x2 colors are fallback only. Once any newer
            // single-block sample exists in that 2x2 footprint, do not blend
            // the outdated coarse color into regional summaries.
            if (resolution == 2) {
                boolean replaced = false;
                for (int dz = 0; dz < 2 && !replaced; dz++) {
                    for (int dx = 0; dx < 2; dx++) {
                        if (source.containsKey(AtlasViewport.sampleKey(wx + dx, wz + dz, 1))) {
                            replaced = true;
                            break;
                        }
                    }
                }
                if (replaced) continue;
            }
            int weight = resolution == 1 ? 1 : 4;
            for (int step : STEPS) {
                int x = Math.floorDiv(wx, step) * step;
                int z = Math.floorDiv(wz, step) * step;
                long bucket = AtlasViewport.sampleKey(x, z, step);
                sums.get(step).computeIfAbsent(bucket, unused -> new Average())
                        .add(entry.getValue(), weight);
            }
        }
        colors.clear();
        for (int step : STEPS) {
            Map<Long, Integer> compact = new HashMap<>(sums.get(step).size());
            for (var entry : sums.get(step).entrySet()) {
                compact.put(entry.getKey(), entry.getValue().color());
            }
            colors.put(step, compact);
        }
    }

    /**
     * Return a real stored sample at full zoom or a downsampled color at
     * regional zoom. Returns null for genuinely unexplored cells.
     */
    public Integer color(int worldX, int worldZ, int step, Map<Long, Integer> source) {
        if (step <= 1) {
            Integer detailed = source.get(AtlasViewport.sampleKey(worldX, worldZ, 1));
            if (detailed != null) return detailed;
            return source.get(AtlasViewport.sampleKey(
                    Math.floorDiv(worldX, 2) * 2, Math.floorDiv(worldZ, 2) * 2, 2));
        }
        Map<Long, Integer> level = colors.get(step);
        if (level != null) {
            Integer color = level.get(AtlasViewport.sampleKey(
                    Math.floorDiv(worldX, step) * step,
                    Math.floorDiv(worldZ, step) * step, step));
            if (color != null) return color;
        }
        // Old saves and tiles acquired since the last index rebuild remain
        // visible immediately, but never invent unknown surroundings.
        Integer fallback = source.get(AtlasViewport.sampleKey(worldX, worldZ, 1));
        if (fallback != null) return fallback;
        return source.get(AtlasViewport.sampleKey(
                Math.floorDiv(worldX, 2) * 2, Math.floorDiv(worldZ, 2) * 2, 2));
    }
}
