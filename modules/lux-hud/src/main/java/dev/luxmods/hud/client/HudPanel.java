package dev.luxmods.hud.client;

import dev.luxmods.hud.HudBarMath;
import dev.luxmods.hud.HudConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;

/**
 * First functional RPG panel; final styling, animations and party HUD come later.
 * One render path, reading only local player's vanilla data; works with Atlas absent.
 */
public final class HudPanel {
    private static final int WIDTH = 194;
    private static final int HEIGHT = 84;
    private static final int BAR_WIDTH = 115;
    private static final int LEFT = 8;
    private static final int TOP = 8;

    private HudPanel() {
    }

    public static boolean shouldDraw(Minecraft mc) {
        return mc.player != null && mc.level != null && !mc.player.isSpectator()
                && !mc.options.hideGui && mc.screen == null
                && HudConfig.MODE.get() == HudConfig.Mode.RPG;
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!shouldDraw(mc)) return;

        var player = mc.player;
        int x = LEFT;
        int y = TOP;

        graphics.fill(x - 2, y - 2, x + WIDTH + 2, y + HEIGHT + 2, 0xA00B0B14);
        graphics.fill(x, y, x + WIDTH, y + 2, 0xFF8061D8);
        graphics.fill(x + 5, y + 16, x + 41, y + 52, 0xFF393241);
        PlayerFaceRenderer.draw(graphics, player.getSkin(), x + 7, y + 18, 32);

        String name = player.getName().getString();
        if (name.length() > 17) name = name.substring(0, 16) + "...";
        graphics.drawString(mc.font, name, x + 47, y + 8, 0xFFFFFFFF, true);

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float food = player.getFoodData().getFoodLevel();
        float xp = player.experienceProgress;

        String hpLabel = "VIDA " + Math.round(health) + "/" + Math.round(maxHealth);
        graphics.drawString(mc.font, hpLabel, x + 47, y + 21, 0xFFFFB6BF, false);
        drawBar(graphics, x + 47, y + 32, health, maxHealth, 0xFFDE4E64);

        graphics.drawString(mc.font, "FOME " + Math.round(food) + "/20",
                x + 47, y + 44, 0xFFFFD899, false);
        drawBar(graphics, x + 47, y + 55, food, 20f, 0xFFE2AC56);

        graphics.drawString(mc.font, "XP", x + 7, y + 69, 0xFF9BEEA7, false);
        graphics.drawString(mc.font, "Nv. " + player.experienceLevel,
                x + WIDTH - 43, y + 69, 0xFFDAF9D9, false);
        drawBar(graphics, x + 31, y + 73, xp, 1.0f, 0xFF69C584);
    }

    private static void drawBar(GuiGraphics graphics, int x, int y,
                                float value, float maximum, int fillColor) {
        graphics.fill(x, y, x + BAR_WIDTH, y + 6, 0xFF292733);
        int filled = HudBarMath.filledWidth(value, maximum, BAR_WIDTH);
        if (filled > 0) {
            graphics.fill(x, y, x + filled, y + 6, fillColor);
        }
    }
}
