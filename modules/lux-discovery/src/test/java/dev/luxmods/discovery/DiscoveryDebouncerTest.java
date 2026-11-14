package dev.luxmods.discovery;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscoveryDebouncerTest {
    @Test
    void requiresStableBiome() {
        DiscoveryDebouncer d = new DiscoveryDebouncer();
        for (int i = 0; i < 3; i++) assertTrue(d.tick("overworld", "plains", 4, 10).isEmpty());
        assertEquals("plains", d.tick("overworld", "plains", 4, 10).orElseThrow());
        assertTrue(d.tick("overworld", "plains", 4, 10).isEmpty());
    }

    @Test
    void cancelsUnstableCrossingAndNeverRepeatsVisitedBiome() {
        DiscoveryDebouncer d = new DiscoveryDebouncer();
        d.tick("world", "plains", 3, 0);
        d.tick("world", "forest", 3, 0);
        assertTrue(d.tick("world", "plains", 3, 0).isEmpty());
        assertTrue(d.tick("world", "plains", 3, 0).isEmpty());
        assertEquals("plains", d.tick("world", "plains", 3, 0).orElseThrow());
        d.tick("world", "forest", 3, 0);
        assertTrue(d.tick("world", "plains", 3, 0).isEmpty());
    }

    @Test
    void respectsCooldownAndSeparatesDimensions() {
        DiscoveryDebouncer d = new DiscoveryDebouncer();
        assertEquals("plains", d.tick("overworld", "plains", 1, 4).orElseThrow());
        assertTrue(d.tick("overworld", "forest", 1, 4).isEmpty());
        assertTrue(d.tick("nether", "plains", 1, 4).isEmpty());
        assertTrue(d.tick("nether", "plains", 1, 4).isEmpty());
        assertEquals("plains", d.tick("nether", "plains", 1, 4).orElseThrow());
        // Voltar ao Overworld não repete a descoberta da mesma sessão.
        assertTrue(d.tick("overworld", "plains", 1, 4).isEmpty());
        d.reset();
        assertEquals("plains", d.tick("overworld", "plains", 1, 4).orElseThrow());
    }

    @Test
    void sameNameInTwoDimensionsIsNotSameDiscovery() {
        DiscoveryDebouncer d = new DiscoveryDebouncer();
        assertEquals("forest", d.tick("overworld", "forest", 1, 0).orElseThrow());
        assertEquals("forest", d.tick("custom_dim", "forest", 1, 0).orElseThrow());
        assertTrue(d.tick("overworld", "forest", 1, 0).isEmpty());
    }

    @Test
    void invalidInputFailsInsteadOfCorruptingState() {
        DiscoveryDebouncer d = new DiscoveryDebouncer();
        assertThrows(IllegalArgumentException.class,
                () -> d.tick("world", "plains", 0, 5));
        assertThrows(NullPointerException.class,
                () -> d.tick(null, "plains", 1, 0));
    }
}
