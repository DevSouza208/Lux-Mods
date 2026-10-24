package dev.luxmods.atlas;

import net.neoforged.fml.ModList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LuxAtlasModLoadingTest {
    @Test void requiresEngineWithoutDiscovery() {
        ModList list = ModList.get();
        assertNotNull(list);
        assertTrue(list.isLoaded("lux_engine"));
        assertTrue(list.isLoaded("lux_atlas"));
        // Lux Discovery is intentionally NOT asserted: Atlas works without it.
    }
}
