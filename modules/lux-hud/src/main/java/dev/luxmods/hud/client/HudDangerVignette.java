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

        // Persistent warnings grow gradually as the underlying need becomes critical.
        // Hydration is optional: never guess thirst when the third-party API
        // is unavailable, and never hide the original thirst meter in that case.
        Integer thirst = HudThirstCompat.readThirst();
        if (thirst != null && thirst < 5) {
            edge(g, 0x164D7A, (int) (85 * (5 - thirst) / 5.0F));
        }
        if (hp < 0.35F) {
            edge(g, 0x8E1524, (int) (115 * (0.35F - hp) / 0.35F));
        }
        if (hunger < 0.25F) {
            edge(g, 0xC16C19, (int) (85 * (0.25F - hunger) / 0.25F));
        }
        // No redundant air bar. Black edges appear only when air is low.
        if (air < 0.35F && mc.player.isUnderWater()) {
            edge(g, 0x04080D, (int) (160 * (0.35F - Math.max(0, air)) / 0.35F));
        }
        // Brief red flash when hit. Relies on the vanilla hit animation timer,
        // avoiding network packets or a new game mechanic.
        if (mc.player.hurtTime > 0) {
            edge(g, 0xD6293E, Math.min(125, mc.player.hurtTime * 11));
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
