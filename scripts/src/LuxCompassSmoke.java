import dev.luxmods.atlas.CompassMath;

public class LuxCompassSmoke {
    private static void near(float actual, float expected) {
        if (Math.abs(actual - expected) > 0.0001f) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        near(CompassMath.fromMinecraftYaw(-180f), 0f);
        near(CompassMath.fromMinecraftYaw(-90f), 90f);
        near(CompassMath.fromMinecraftYaw(0f), 180f);
        near(CompassMath.fromMinecraftYaw(90f), 270f);
        near(CompassMath.signedOffset(10f, 350f), 20f);
        near(CompassMath.signedOffset(350f, 10f), -20f);
        for (int bearing = 0; bearing < 360; bearing += 90) {
            if (CompassMath.cardinalLabel(bearing).isEmpty()) {
                throw new AssertionError("Missing cardinal label for " + bearing);
            }
        }
        System.out.println("LUX ATLAS COMPASS SMOKE PASSOU");
    }
}
