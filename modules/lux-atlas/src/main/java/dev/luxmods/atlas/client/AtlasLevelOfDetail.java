package dev.luxmods.atlas.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary screen-level regional index of terrain the player has observed.
 * Unknown blocks are never inferred, and aggregated data is never persisted
 * over the authoritative one-block regional archive.
 */
public final class AtlasLevelOfDetail {
    private static final int[] STEPS = {2, 4, 8, 16};

    /** known/total counts recorded coverage, not just the averaged color. */
    public record Cell(int color, int known, int total) {
        public Cell {
            if (known < 0 || total <= 0 || known > total)
                throw new IllegalArgumentException("Invalid explored fraction");
        }
        public double fraction() { return known / (double) total; }
    }

    private final Map<Integer, Map<Long, Cell>> levels = new HashMap<>();

    private static final class Accumulator {
        long red, green, blue;
        int count;
        void add(int argb, int footprint) {
            red += ((argb >>> 16) & 255L) * footprint;
            green += ((argb >>> 8) & 255L) * footprint;
            blue += (argb & 255L) * footprint;
            count += footprint;
        }
        Cell finish(int total) {
            int color = 0xff000000 | ((int)(red / count) << 16)
                    | ((int)(green / count) << 8) | (int)(blue / count);
            return new Cell(color, Math.min(count, total), total);
        }
    }

    /**
     * Aggregate real explored blocks. Old 2x2 snapshots contribute only
     * uncovered blocks; a new 1x1 measurement supersedes the old approximation
     * for its exact coordinate, without throwing out the other three blocks.
     */
    public void rebuild(Map<Long, Integer> source) {
        Map<Integer, Map<Long, Accumulator>> sums = new HashMap<>();
        for (int step : STEPS) sums.put(step, new HashMap<>());
        for (var entry : source.entrySet()) {
            long packed = entry.getKey();
            int resolution = (int) (packed & 0xff);
            if (resolution != 1 && resolution != 2) continue;
            int x = AtlasViewport.unpackX(packed);
            int z = AtlasViewport.unpackZ(packed);
            int footprint = 1;
            if (resolution == 2) {
                footprint = 4;
                for (int dz = 0; dz < 2; dz++) {
                    for (int dx = 0; dx < 2; dx++) {
                        if (source.containsKey(AtlasViewport.sampleKey(x + dx, z + dz, 1)))
                            footprint--;
                    }
                }
                if (footprint == 0) continue;
            }
            for (int step : STEPS) {
                int bx = Math.floorDiv(x, step) * step;
                int bz = Math.floorDiv(z, step) * step;
                long key = AtlasViewport.sampleKey(bx, bz, step);
                sums.get(step).computeIfAbsent(key, unused -> new Accumulator())
                        .add(entry.getValue(), footprint);
            }
        }
        levels.clear();
        for (int step : STEPS) {
            Map<Long, Cell> values = new HashMap<>(sums.get(step).size());
            for (var entry : sums.get(step).entrySet())
                values.put(entry.getKey(), entry.getValue().finish(step * step));
            levels.put(step, values);
        }
    }

    /** Null = not observed; non-null carries the true sampled coverage. */
    public Cell cell(int x, int z, int step, Map<Long, Integer> source) {
        if (step <= 1) {
            Integer color = source.get(AtlasViewport.sampleKey(x, z, 1));
            if (color == null) color = source.get(AtlasViewport.sampleKey(
                    Math.floorDiv(x, 2) * 2, Math.floorDiv(z, 2) * 2, 2));
            return color == null ? null : new Cell(color, 1, 1);
        }
        Map<Long, Cell> level = levels.get(step);
        if (level != null) {
            Cell sampled = level.get(AtlasViewport.sampleKey(
                    Math.floorDiv(x, step) * step,
                    Math.floorDiv(z, step) * step, step));
            if (sampled != null) return sampled;
        }
        // Recently observed terrain not yet incorporated into the index.
        Integer fresh = source.get(AtlasViewport.sampleKey(x, z, 1));
        if (fresh != null) return new Cell(fresh, 1, step * step);
        Integer legacy = source.get(AtlasViewport.sampleKey(
                Math.floorDiv(x, 2) * 2, Math.floorDiv(z, 2) * 2, 2));
        return legacy == null ? null : new Cell(legacy, Math.min(4, step * step), step * step);
    }

    /** Compatibility accessor for other rendering and existing smoke tests. */
    public Integer color(int x, int z, int step, Map<Long, Integer> source) {
        Cell sampled = cell(x, z, step, source);
        return sampled == null ? null : sampled.color();
    }
}
