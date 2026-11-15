package dev.luxmods.atlas;

/** Heading math is pure Java, with no Minecraft dependency. */
public final class CompassMath {
    private CompassMath() {
    }

    public static float normalize(float degrees) {
        if (!Float.isFinite(degrees)) throw new IllegalArgumentException("Heading must be finite");
        float result = degrees % 360.0f;
        return result < 0.0f ? result + 360.0f : result;
    }

    /** Minecraft yaw: 0 = South, -90 = East, +90 = West, +/-180 = North. */
    public static float fromMinecraftYaw(float yaw) {
        return normalize(yaw + 180.0f);
    }

    /** Signed delta: positive markers are clockwise (right of player heading). */
    public static float signedOffset(float marker, float heading) {
        return normalize(marker - heading + 180.0f) - 180.0f;
    }

    public static String cardinalLabel(int bearing) {
        return switch (Math.floorMod(bearing, 360)) {
            case 0 -> "N";
            case 90 -> "L";
            case 180 -> "S";
            case 270 -> "O";
            default -> "";
        };
    }
}
