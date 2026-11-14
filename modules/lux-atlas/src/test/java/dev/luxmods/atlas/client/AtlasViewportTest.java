package dev.luxmods.atlas.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Pure coordinate tests; no game instance required. */
class AtlasViewportTest {
    @Test void sampleKeysRoundTripNegativeAndPositive() {
        int[] values = {-100_000, -2049, -129, -128, -1, 0, 1, 127, 128, 2048, 99_999};
        for (int x : values) for (int z : values) {
            long packed = AtlasViewport.sampleKey(x, z, 1);
            assertEquals(x, AtlasViewport.unpackX(packed));
            assertEquals(z, AtlasViewport.unpackZ(packed));
            assertNotEquals(packed, AtlasViewport.sampleKey(x, z, 2));
        }
    }

    @Test void negativeRegionBoundariesAreFlooredNotTruncated() {
        assertEquals(-2, AtlasViewport.region(-129, 128));
        assertEquals(-1, AtlasViewport.region(-128, 128));
        assertEquals(-1, AtlasViewport.region(-1, 128));
        assertEquals(0, AtlasViewport.region(0, 128));
        assertEquals(0, AtlasViewport.region(127, 128));
        assertEquals(1, AtlasViewport.region(128, 128));
    }

    @Test void clicksSelectTheExactBlockRenderedAtThatCell() {
        for (int center : new int[]{-105, -1, 0, 1, 203}) {
            for (int step : new int[]{1, 2, 4, 8, 16}) {
                int origin = AtlasViewport.start(center, 299, step);
                for (int cell = 0; cell < 140; cell++) {
                    int world = origin + cell * step;
                    int pixel = AtlasViewport.worldToPixel(world, 33, origin, step);
                    assertEquals(33 + cell * 2, pixel);
                    assertEquals(world, AtlasViewport.pixelToWorld(pixel, 33, origin, step));
                }
            }
        }
    }

    @Test void zoomKeepsWorldCoordinateUnderCursorWithinOneCell() {
        int center = -117;
        int cursorPx = 167, mapStart = 41, width = 310;
        int worldBefore = AtlasViewport.pixelToWorld(cursorPx, mapStart,
                AtlasViewport.start(center, width, 8), 8);
        int newOrigin = AtlasViewport.start(center, width, 2);
        int worldAfter = AtlasViewport.pixelToWorld(cursorPx, mapStart, newOrigin, 2);
        center += worldBefore - worldAfter;
        int after = AtlasViewport.pixelToWorld(cursorPx, mapStart,
                AtlasViewport.start(center, width, 2), 2);
        assertTrue(Math.abs(worldBefore - after) <= 2);
    }
}
