package dev.luxmods.atlas.client;

import dev.luxmods.atlas.AtlasConfig;
import dev.luxmods.atlas.CompassMath;
import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** The Lux Atlas mini-compass: narrow, slim and snapped just above the hotbar. */
public final class AtlasCompassHud {
    private static final int WIDTH = 162;
    private static final int HEIGHT = 18;
    private static final int HALF_WINDOW_DEGREES = 58;
    private static final float PIXELS_PER_DEGREE = WIDTH / (2.0f * HALF_WINDOW_DEGREES);

    private AtlasCompassHud() { }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui || mc.screen != null
                || !AtlasConfig.SHOW_COMPASS.get()) return;

        int centerX = g.guiWidth() / 2;
        int bottomDistance = AtlasConfig.SNAP_TO_HOTBAR.get() ?
                46 : AtlasConfig.DISTANCE_FROM_BOTTOM.get();
        int y = Math.max(4, g.guiHeight() - bottomDistance);
        int x = centerX - WIDTH / 2;
        float heading = CompassMath.fromMinecraftYaw(mc.player.getYRot());

        LuxGuiStyle.panel(g, x, y, WIDTH, HEIGHT);
        LuxGuiStyle.icon(g, "compass_end_left", x - 12, y - 1, 15, 19);
        LuxGuiStyle.icon(g, "compass_end_right", x + WIDTH - 3, y - 1, 15, 19);

        for (int bearing = 0; bearing < 360; bearing += 15) {
            float offset = CompassMath.signedOffset(bearing, heading);
            if (Math.abs(offset) > HALF_WINDOW_DEGREES) continue;
            int px = Math.round(centerX + offset * PIXELS_PER_DEGREE);
            boolean cardinal = bearing % 90 == 0;
            boolean major = bearing % 45 == 0;
            g.fill(px, y + 3, px + 1, y + (major ? 8 : 6),
                    cardinal ? LuxGuiStyle.AMBER : 0xFFA095B8);
            if (cardinal) {
                String letter = CompassMath.cardinalLabel(bearing);
                g.drawString(mc.font, letter, px - mc.font.width(letter) / 2,
                        y + 9, LuxGuiStyle.AMBER, true);
            }
        }
        // Fine stationary needle at current heading, plus pixel jewel.
        g.fill(centerX, y + 2, centerX + 1, y + 8, 0xFFF3DAFF);
        LuxGuiStyle.icon(g, "compass_needle", centerX - 3, y - 3, 7, 9);
    }
}
