package dev.luxmods.engine.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LuxServiceRegistryTest {
    interface Compass {
        int direction();
    }

    interface WrongContract {
        String value();
    }

    @Test
    void storesTypedServiceAndReportsMissingServiceWithoutCrashing() {
        var registry = new LuxServiceRegistry();
        var key = new LuxServiceKey<>("lux_atlas:compass", Compass.class);
        assertTrue(registry.find(key).isEmpty());
        registry.register(key, () -> 270);
        assertEquals(270, registry.find(key).orElseThrow().direction());
        assertTrue(registry.registeredIds().contains("lux_atlas:compass"));
    }

    @Test
    void preventsDuplicateRegistrationAndTypeMismatch() {
        var registry = new LuxServiceRegistry();
        registry.register(new LuxServiceKey<>("lux_atlas:compass", Compass.class), () -> 90);
        assertThrows(IllegalStateException.class,
                () -> registry.register(new LuxServiceKey<>("lux_atlas:compass", Compass.class), () -> 45));
        assertThrows(IllegalStateException.class,
                () -> registry.find(new LuxServiceKey<>("lux_atlas:compass", WrongContract.class)));
    }

    @Test
    void validatesNamespacedIds() {
        assertThrows(IllegalArgumentException.class,
                () -> new LuxServiceKey<>("wrong id", Compass.class));
        assertDoesNotThrow(() -> new LuxServiceKey<>("lux_npc:dialogue", Compass.class));
    }
}
