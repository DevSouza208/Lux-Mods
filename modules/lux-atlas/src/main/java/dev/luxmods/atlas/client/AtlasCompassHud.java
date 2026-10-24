package dev.luxmods.atlas.client;

import dev.luxmods.atlas.AtlasConfig;
import dev.luxmods.atlas.CompassMath;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Compact 2D compass above the hotbar; no exploration data or networking. */
public final class AtlasCompassHud {
    private static final int WIDTH = 182;
    private static final int HEIGHT = 26;
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

        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, 0xA00B0B16);
        graphics.fill(x, y, x + WIDTH, y + 1, 0xFF7955C9);
        graphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0x886C54AA);

        for (int bearing = 0; bearing < 360; bearing += 15) {
            float angle = CompassMath.signedOffset(bearing, heading);
            if (Math.abs(angle) > HALF_WINDOW_DEGREES) continue;
            int px = Math.round(centerX + angle * PIXELS_PER_DEGREE);
            boolean cardinal = bearing % 90 == 0;
            boolean major = bearing % 45 == 0;

            graphics.fill(px, y + 4, px + 1, y + (major ? 11 : 8),
                    cardinal ? 0xFFFFCB6A : 0xFFADB0C2);
            if (cardinal) {
                String text = CompassMath.cardinalLabel(bearing);
                graphics.drawString(mc.font, text, px - mc.font.width(text) / 2,
                        y + 13, 0xFFFFD68D, true);
            }
        }
        // Vertical marker indicates current look direction.
        graphics.fill(centerX - 1, y + 2, centerX + 2, y + 12, 0xFFE5D8FF);
        graphics.fill(centerX - 1, y + 20, centerX + 2, y + 24, 0xFF926FE1);
    }
}
