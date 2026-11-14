package dev.luxmods.hud.client;

import dev.luxmods.hud.HudConfig;
import dev.luxmods.hud.LuxHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Transparent purple/champagne pixel-art trim drawn ABOVE vanilla HOTBAR.
 * Minecraft still draws all its slots, actual inventory stacks and selected
 * slot on the original layer. No cancel, no fake items, no input handling.
 *
 * Exact PNGs: 196x28 and 24x26. Bottom-centered, aligned with the vanilla
 * 182x22 slot row. The existing compass ends just above this trim.
 */
public final class HudHotbar {
    private static final int WIDTH = 196;
    private static final int HEIGHT = 28;
    private static final ResourceLocation TRIM = ResourceLocation.fromNamespaceAndPath(
            LuxHud.MOD_ID, "textures/gui/hotbar/lux_hotbar_trim.png");
    private static final ResourceLocation SELECT = ResourceLocation.fromNamespaceAndPath(
            LuxHud.MOD_ID, "textures/gui/hotbar/lux_hotbar_selected.png");

    private HudHotbar() { }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!HudPanel.shouldDraw(mc) || !HudConfig.DECORATE_HOTBAR.get()) return;

        int x = g.guiWidth() / 2 - WIDTH / 2;
        int y = g.guiHeight() - HEIGHT;
        // Base image is TRANSPARENT in all item centers, preserving icons,
        // stack counts, durability and the vanilla selection hitbox.
        g.blit(TRIM, x, y, WIDTH, HEIGHT, 0f, 0f, WIDTH, HEIGHT, WIDTH, HEIGHT);

        int slot = mc.player.getInventory().selected;
        if (slot >= 0 && slot < 9) {
            int selectedX = x + 5 + slot * 20;
            g.blit(SELECT, selectedX, y + 2, 24, 26, 0f, 0f, 24, 26, 24, 26);
        }
    }
}
