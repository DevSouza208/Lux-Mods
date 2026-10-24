package dev.luxmods.atlas;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompassMathTest {
    private static final float EPS = 0.0001f;

    @Test void vanillaYawMapsToCardinals() {
        assertEquals(0f, CompassMath.fromMinecraftYaw(-180f), EPS);
        assertEquals(90f, CompassMath.fromMinecraftYaw(-90f), EPS);
        assertEquals(180f, CompassMath.fromMinecraftYaw(0f), EPS);
        assertEquals(270f, CompassMath.fromMinecraftYaw(90f), EPS);
        assertEquals(0f, CompassMath.fromMinecraftYaw(180f), EPS);
    }

    @Test void wrapsAcrossNorth() {
        assertEquals(20f, CompassMath.signedOffset(10f, 350f), EPS);
        assertEquals(-20f, CompassMath.signedOffset(350f, 10f), EPS);
        assertEquals(0f, CompassMath.signedOffset(0f, 360f), EPS);
    }

    @Test void labelsUseBrazilianCardinals() {
        assertEquals("N", CompassMath.cardinalLabel(0));
        assertEquals("L", CompassMath.cardinalLabel(90));
        assertEquals("S", CompassMath.cardinalLabel(180));
        assertEquals("O", CompassMath.cardinalLabel(270));
        assertEquals("", CompassMath.cardinalLabel(15));
        assertEquals("N", CompassMath.cardinalLabel(360));
    }

    @Test void rejectsNonFiniteDirections() {
        assertThrows(IllegalArgumentException.class, () -> CompassMath.normalize(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> CompassMath.normalize(Float.POSITIVE_INFINITY));
    }
}
