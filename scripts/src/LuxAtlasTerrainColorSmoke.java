import dev.luxmods.atlas.client.AtlasTerrainColor;

/** Pure Java surface color consistency regressions. */
public final class LuxAtlasTerrainColorSmoke {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        int flat = AtlasTerrainColor.shade(0x005080a0, 72, 72, 72, false);
        check(flat == 0xff5080a0, "flat surface should keep original color");
        int hills = AtlasTerrainColor.shade(0x005080a0, 90, 70, 70, false);
        check(hills != flat, "height and relief must shade the landscape");
        int water = AtlasTerrainColor.shade(0x005080a0, 90, 70, 70, true);
        check(water == 0xff447494, "water shade is stable across collector and screen");
        int fallback = AtlasTerrainColor.shade(0, 72, 72, 72, false);
        check(fallback == 0xff576c52, "missing map color uses documented fallback");
        int saturated = AtlasTerrainColor.shade(0x00ffffff, 200, 0, 0, false);
        check(saturated == 0xffffffff, "channel values must be clamped");
        System.out.println("LUX ATLAS COLORS: OK");
    }
}
