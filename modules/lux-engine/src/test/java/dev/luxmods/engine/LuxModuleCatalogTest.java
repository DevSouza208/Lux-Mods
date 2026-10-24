package dev.luxmods.engine;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LuxModuleCatalogTest {
    @Test
    void reportsOnlyLoadedLuxModsInDeterministicOrder() {
        Set<String> loaded = Set.of("lux_engine", "lux_discovery", "unrelated_mod");
        assertEquals(List.of("lux_engine", "lux_discovery"),
                LuxModuleCatalog.installedModules(loaded::contains));
    }

    @Test
    void rejectsNullLoader() {
        assertThrows(NullPointerException.class,
                () -> LuxModuleCatalog.installedModules(null));
    }
}
