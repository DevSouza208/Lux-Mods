package dev.luxmods.atlas.client;

/**
 * Shared surface shading for background exploration and full-screen rendering.
 * Inputs are world observations; this class performs no chunk loads or IO.
 */
public final class AtlasTerrainColor {
    private AtlasTerrainColor() {}

    public static int shade(int mapColor, int height, int northHeight,
                            int westHeight, boolean water) {
        int base = mapColor == 0 ? 0x576c52 : mapColor;
        int relief = (height - northHeight) * 5 + (height - westHeight) * 3;
        int shade = Math.max(-37, Math.min(42, (height - 72) / 4 + relief));
        if (water) shade = -12;
        int r = clamp(((base >>> 16) & 255) + shade);
        int g = clamp(((base >>> 8) & 255) + shade);
        int b = clamp((base & 255) + shade);
        return 0xff000000 | r << 16 | g << 8 | b;
    }

    private static int clamp(int color) {
        return Math.max(0, Math.min(255, color));
    }
}
