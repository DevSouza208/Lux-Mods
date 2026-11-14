package dev.luxmods.atlas.client;

import java.util.Map;
import java.util.Objects;

/**
 * Pure Java merge rule: add new samples and update changed samples.
 * No storage rewrite is needed when all coordinates and colors match.
 */
public final class AtlasRegionMerge {
    private AtlasRegionMerge() {}

    /**
     * Restore historical samples into holes only. Never replace terrain from
     * a newer scan with an older LXT1 import.
     */
    public static boolean mergeMissing(Map<Long, Integer> stored,
                                       Map<Long, Integer> legacy) {
        Objects.requireNonNull(stored, "stored");
        Objects.requireNonNull(legacy, "legacy");
        boolean changed = false;
        for (var entry : legacy.entrySet()) {
            if (!stored.containsKey(entry.getKey())) {
                stored.put(entry.getKey(), entry.getValue());
                changed = true;
            }
        }
        return changed;
    }

    public static boolean merge(Map<Long, Integer> stored, Map<Long, Integer> updates) {
        Objects.requireNonNull(stored, "stored");
        Objects.requireNonNull(updates, "updates");
        boolean changed = false;
        for (var entry : updates.entrySet()) {
            Integer previous = stored.put(entry.getKey(), entry.getValue());
            if (!Objects.equals(previous, entry.getValue())) changed = true;
        }
        return changed;
    }
}
