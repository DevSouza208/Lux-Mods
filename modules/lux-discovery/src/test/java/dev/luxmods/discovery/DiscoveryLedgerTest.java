package dev.luxmods.discovery;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class DiscoveryLedgerTest {
    @Test
    void recordsExactlyOnceAcrossSessionsAndWorldDimensions() {
        Set<String> saved = Set.of();
        var first = DiscoveryLedger.mark(saved, "minecraft:overworld", "minecraft:plains");
        assertTrue(first.firstVisit());
        assertTrue(saved.isEmpty(), "não modifica os dados anteriores");
        var repeated = DiscoveryLedger.mark(first.saved(), "minecraft:overworld", "minecraft:plains");
        assertFalse(repeated.firstVisit());
        assertSame(first.saved(), repeated.saved());

        var otherDimension = DiscoveryLedger.mark(first.saved(), "minecraft:the_nether", "minecraft:plains");
        assertTrue(otherDimension.firstVisit());
        assertEquals(2, otherDimension.saved().size());
        assertFalse(DiscoveryLedger.mark(otherDimension.saved(), "minecraft:overworld", "minecraft:plains").firstVisit());
    }

    @Test
    void rejectsInvalidNullInputs() {
        assertThrows(NullPointerException.class, () -> DiscoveryLedger.mark(null, "world", "biome"));
        assertThrows(NullPointerException.class, () -> DiscoveryLedger.mark(Set.of(), null, "biome"));
        assertThrows(NullPointerException.class, () -> DiscoveryLedger.mark(Set.of(), "world", null));
    }
}
