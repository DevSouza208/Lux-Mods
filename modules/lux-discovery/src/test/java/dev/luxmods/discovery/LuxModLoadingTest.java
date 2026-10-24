package dev.luxmods.discovery;

import net.neoforged.fml.ModList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test running under ModDevGradle's unitTest harness.
 * A compilation-only project dependency is insufficient if FML does not
 * actually load the Engine as a mod in this environment.
 */
class LuxModLoadingTest {
    @Test
    void discoversBothModsInFml() {
        ModList loaded = ModList.get();
        assertNotNull(loaded, "NeoForge ModList must be initialized in mod-aware tests");
        assertTrue(loaded.isLoaded("lux_engine"), "Lux Engine missing from FML mod list");
        assertTrue(loaded.isLoaded("lux_discovery"), "Lux Discovery missing from FML mod list");
    }
}
