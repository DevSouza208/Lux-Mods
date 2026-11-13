package dev.luxmods.hud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Contextual danger feedback without introducing new permanent HUD bars.
 * Uses gentle layered screen-edge tint so the center stays readable.
 */
public final class HudDangerVignette {
    private HudDangerVignette() {}

    public static void render(GuiGraphics g, Minecraft mc) {
        if (mc.player == null) return;
        float hp = mc.player.getHealth() / Math.max(1.0F, mc.player.getMaxHealth());
        float hunger = mc.player.getFoodData().getFoodLevel() / 20.0F;
        int maxAir = mc.player.getMaxAirSupply();
        float air = maxAir <= 0 ? 1F : (float) mc.player.getAirSupply() / maxAir;

        if (hp < 0.35F) {
            edge(g, 0xCE2835, (int) (105 * (0.35F - hp) / 0.35F));
        }
        if (hunger < 0.25F) {
            edge(g, 0xB28A29, (int) (75 * (0.25F - hunger) / 0.25F));
        }
        // Dark edges only during submersion or active lack of breathable air.
        if (air < 0.35F && mc.player.isUnderWater()) {
            edge(g, 0x04080D, (int) (160 * (0.35F - Math.max(0,air)) / 0.35F));
        }
    }

    private static void edge(GuiGraphics g, int rgb, int alpha) {
        if (alpha <= 0) return;
        int w = g.guiWidth(), h = g.guiHeight();
        int radius = Math.max(16, Math.min(w, h) / 6);
        for (int layer = 0; layer < 9; layer++) {
            int offset = layer * radius / 9;
            int thickness = Math.max(1, radius / 9);
            int a = Math.max(0, Math.min(255, alpha * (9 - layer) / 28));
            int c = (a << 24) | rgb;
            g.fill(offset, offset, offset + thickness, h - offset, c);
            g.fill(w - offset - thickness, offset, w - offset, h - offset, c);
            g.fill(offset, offset, w - offset, offset + thickness, c);
            g.fill(offset, h - offset - thickness, w - offset, h - offset, c);
        }
    }
}
