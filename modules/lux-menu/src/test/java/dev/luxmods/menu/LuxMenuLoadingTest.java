package dev.luxmods.menu;

import net.neoforged.fml.ModList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LuxMenuLoadingTest {
    @Test void loadsAsAnIndependentOptionalMod() {
        var list = ModList.get();
        assertNotNull(list);
        assertTrue(list.isLoaded("lux_engine"));
        assertTrue(list.isLoaded("lux_menu"));
    }
}
