package dev.luxmods.engine;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LuxModuleCatalogTest {
    @Test
    void listsOnlyCurrentMvpJarsInBuildOrder() {
        assertEquals(List.of(
                "lux_engine", "lux_discovery", "lux_compass", "lux_quests",
                "lux_upgrades", "lux_hud", "lux_menu", "lux_borderless",
                "lux_dialogues"), LuxModuleCatalog.KNOWN_MODULE_IDS);
        assertFalse(LuxModuleCatalog.KNOWN_MODULE_IDS.contains("lux_atlas"));
        assertFalse(LuxModuleCatalog.KNOWN_MODULE_IDS.contains("lux_npc"));
    }

    @Test
    void reportsOnlyLoadedLuxModsInDeterministicOrder() {
        Set<String> loaded = Set.of("lux_dialogues", "lux_engine",
                "lux_compass", "unrelated_mod");
        assertEquals(List.of("lux_engine", "lux_compass", "lux_dialogues"),
                LuxModuleCatalog.installedModules(loaded::contains));
    }

    @Test
    void acceptsMissingOptionalMods() {
        assertEquals(List.of("lux_engine"),
                LuxModuleCatalog.installedModules("lux_engine"::equals));
    }

    @Test
    void rejectsNullLoader() {
        assertThrows(NullPointerException.class,
                () -> LuxModuleCatalog.installedModules(null));
    }
}
