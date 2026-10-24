package dev.luxmods.atlas.client;

import dev.luxmods.atlas.AtlasConfig;
import dev.luxmods.atlas.CompassMath;
import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Lux Clean Fantasy compass: fine bearings, thin border, no opaque bulky box. */
public final class AtlasCompassHud {
    private static final int WIDTH = 182;
    private static final int HEIGHT = 21;
    private static final int HALF_WINDOW_DEGREES = 65;
    private static final float PIXELS_PER_DEGREE = WIDTH / (2.0f * HALF_WINDOW_DEGREES);

    private AtlasCompassHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui || mc.screen != null
                || !AtlasConfig.SHOW_COMPASS.get()) return;

        int centerX = graphics.guiWidth() / 2;
        int y = Math.max(3, graphics.guiHeight() - AtlasConfig.DISTANCE_FROM_BOTTOM.get());
        int x = centerX - WIDTH / 2;
        float heading = CompassMath.fromMinecraftYaw(mc.player.getYRot());

        LuxGuiStyle.panel(graphics, x, y, WIDTH, HEIGHT);
        LuxGuiStyle.icon(graphics, "compass_end_left", x - 14, y, 17, 20);
        LuxGuiStyle.icon(graphics, "compass_end_right", x + WIDTH - 3, y, 17, 20);

        for (int bearing = 0; bearing < 360; bearing += 15) {
            float angle = CompassMath.signedOffset(bearing, heading);
            if (Math.abs(angle) > HALF_WINDOW_DEGREES) continue;
            int px = Math.round(centerX + angle * PIXELS_PER_DEGREE);
            boolean cardinal = bearing % 90 == 0;
            boolean major = bearing % 45 == 0;
            graphics.fill(px, y + 3, px + 1, y + (major ? 9 : 6),
                    cardinal ? LuxGuiStyle.AMBER : 0xFF817C99);
            if (cardinal) {
                String text = CompassMath.cardinalLabel(bearing);
                graphics.drawString(mc.font, text, px - mc.font.width(text) / 2,
                        y + 10, LuxGuiStyle.AMBER, true);
            }
        }
        graphics.fill(centerX - 1, y + 2, centerX + 2, y + 8, 0xFFE5D5FA);
        graphics.fill(centerX, y + 16, centerX + 1, y + 19, LuxGuiStyle.TOP_ACCENT);
    }
}
