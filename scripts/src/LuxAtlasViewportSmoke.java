import dev.luxmods.atlas.client.AtlasViewport;

/**
 * Standalone Atlas coordinate regression test. Runs via javac/java without
 * NeoForge, Gradle, downloaded assets or a Minecraft client.
 */
public final class LuxAtlasViewportSmoke {
    private static void eq(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        int[] places = {-100000, -129, -128, -1, 0, 1, 127, 128, 99999};
        for (int x : places) for (int z : places) {
            long packed = AtlasViewport.sampleKey(x, z, 1);
            eq(x, AtlasViewport.unpackX(packed), "X packing");
            eq(z, AtlasViewport.unpackZ(packed), "Z packing");
            if (packed == AtlasViewport.sampleKey(x, z, 2))
                throw new AssertionError("Zoom samples collided");
        }
        eq(-2, AtlasViewport.region(-129, 128), "negative region -129");
        eq(-1, AtlasViewport.region(-1, 128), "negative region -1");
        eq(0, AtlasViewport.region(127, 128), "region 127");
        eq(1, AtlasViewport.region(128, 128), "region 128");

        for (int step : new int[]{1, 2, 4, 8, 16}) {
            for (int center : new int[]{-1001, -1, 0, 1, 1001}) {
                int origin = AtlasViewport.start(center, 301, step);
                for (int cell = 0; cell < 150; cell++) {
                    int world = origin + cell * step;
                    int px = AtlasViewport.worldToPixel(world, 41, origin, step);
                    eq(41 + cell * 2, px, "map pixel position");
                    eq(world, AtlasViewport.pixelToWorld(px, 41, origin, step),
                            "click aligns to displayed tile");
                }
            }
        }
        System.out.println("LUX ATLAS: coordenadas e amostras OK");
    }
}
