package dev.luxmods.atlas.client;

import java.util.Map;
import java.util.Objects;

/**
 * Pure Java merge rule: add new samples and update changed samples.
 * No storage rewrite is needed when all coordinates and colors match.
 */
public final class AtlasRegionMerge {
    private AtlasRegionMerge() {}

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
