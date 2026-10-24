package dev.luxmods.hud;

import net.neoforged.fml.ModList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LuxHudModLoadingTest {
    @Test void requiresEngineButNotDiscoveryOrAtlas() {
        var mods = ModList.get();
        assertNotNull(mods);
        assertTrue(mods.isLoaded("lux_engine"));
        assertTrue(mods.isLoaded("lux_hud"));
    }
}
