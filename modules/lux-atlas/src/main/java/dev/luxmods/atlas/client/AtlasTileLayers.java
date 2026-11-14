package dev.luxmods.atlas.client;

import java.util.Map;
import java.util.Objects;

/**
 * Merges live snapshots over previously loaded disk tiles.
 * Precedence is intentional: disk < walking observations < screen edits.
 * Never mutates the supplied snapshot maps.
 */
public final class AtlasTileLayers {
    private AtlasTileLayers() {}

    public static void overlay(Map<Long, Integer> tiles,
                               Map<Long, Integer> walking,
                               Map<Long, Integer> screenEdits) {
        Objects.requireNonNull(tiles, "tiles");
        Objects.requireNonNull(walking, "walking");
        Objects.requireNonNull(screenEdits, "screenEdits");
        tiles.putAll(walking);
        tiles.putAll(screenEdits);
    }
}
