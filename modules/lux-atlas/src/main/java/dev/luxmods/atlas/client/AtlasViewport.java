package dev.luxmods.atlas.client;

/**
 * Pure Java viewport math. Pixel/world conversions share the same integer
 * cell origin for cursor selection, waypoint icons and terrain rendering.
 * Handles negative world coordinates with floorDiv/floorMod semantics.
 */
public final class AtlasViewport {
    private AtlasViewport() {}

    public static int cells(int pixels) {
        return Math.max(1, pixels / 2);
    }

    public static int start(int centerBlock, int pixelLength, int step) {
        return centerBlock - cells(pixelLength) * step / 2;
    }

    public static int pixelToWorld(double pixel, int startPixel, int startBlock, int step) {
        return startBlock + (int) Math.floor((pixel - startPixel) / 2.0) * step;
    }

    public static int worldToPixel(int world, int startPixel, int startBlock, int step) {
        return startPixel + (int) Math.floor((world - (double) startBlock) * 2.0 / step);
    }

    public static long sampleKey(int x, int z, int sampleStep) {
        long a = (long) x & 0x0fffffffL;
        long b = (long) z & 0x0fffffffL;
        return (a << 36) ^ (b << 8) ^ (sampleStep & 0xff);
    }

    public static int unpackX(long key) {
        int v = (int) ((key >>> 36) & 0x0fffffffL);
        return (v << 4) >> 4;
    }

    public static int unpackZ(long key) {
        int v = (int) ((key >>> 8) & 0x0fffffffL);
        return (v << 4) >> 4;
    }

    public static int region(int coordinate, int regionBlocks) {
        return Math.floorDiv(coordinate, regionBlocks);
    }
}
