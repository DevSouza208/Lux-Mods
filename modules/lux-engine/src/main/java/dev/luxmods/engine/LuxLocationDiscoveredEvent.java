package dev.luxmods.engine;

import net.neoforged.bus.api.Event;

/**
 * Client-side notification after a world/server-confirmed structure discovery.
 * Does not create a compile dependency between Lux Discovery and Lux Atlas.
 * Other optional Lux modules may also listen to the same event.
 */
public final class LuxLocationDiscoveredEvent extends Event {
    private final String structureId;
    private final String dimensionId;
    private final int x;
    private final int z;

    public LuxLocationDiscoveredEvent(String structureId, String dimensionId, int x, int z) {
        this.structureId = structureId;
        this.dimensionId = dimensionId;
        this.x = x;
        this.z = z;
    }

    public String structureId() { return structureId; }
    public String dimensionId() { return dimensionId; }
    public int x() { return x; }
    public int z() { return z; }
}
