package dev.luxmods.discovery;

import dev.luxmods.engine.api.LuxEventType;
import java.util.UUID;

/** Server-side signals available to Atlas, Lore, Quests and other opt-in mods. */
public final class LuxDiscoveryEvents {
    public static final LuxEventType<BiomeFirstDiscovered> FIRST_BIOME =
            new LuxEventType<>("lux_discovery:first_biome", BiomeFirstDiscovered.class);

    public record BiomeFirstDiscovered(UUID playerId, String dimensionId, String biomeId) { }

    private LuxDiscoveryEvents() { }
}
