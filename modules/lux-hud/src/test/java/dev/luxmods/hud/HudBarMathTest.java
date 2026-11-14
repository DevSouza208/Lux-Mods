package dev.luxmods.hud;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HudBarMathTest {
    @Test void clampsToVisibleRange() {
        assertEquals(50, HudBarMath.filledWidth(10, 20, 100));
        assertEquals(0, HudBarMath.filledWidth(-20, 20, 100));
        assertEquals(100, HudBarMath.filledWidth(30, 20, 100));
    }

    @Test void rejectsInvalidSizes() {
        assertEquals(0, HudBarMath.filledWidth(10, 0, 100));
        assertEquals(0, HudBarMath.filledWidth(Float.NaN, 20, 100));
        assertEquals(0, HudBarMath.filledWidth(10, 20, -5));
    }
}
