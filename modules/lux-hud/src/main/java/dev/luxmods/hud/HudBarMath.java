package dev.luxmods.hud;

/** Pure, resilient bar math shared by health, hunger and XP. */
public final class HudBarMath {
    private HudBarMath() {
    }

    public static int filledWidth(float value, float maximum, int width) {
        if (width <= 0 || !Float.isFinite(value) || !Float.isFinite(maximum) || maximum <= 0) {
            return 0;
        }
        float percentage = Math.max(0.0f, Math.min(1.0f, value / maximum));
        return Math.max(0, Math.min(width, Math.round(width * percentage)));
    }
}
