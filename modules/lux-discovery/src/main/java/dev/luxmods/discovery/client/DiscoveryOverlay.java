package dev.luxmods.discovery.client;

import dev.luxmods.discovery.DiscoveryConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A short, cinematic, top-centered RPG discovery title.
 * Server-side discovery persistence and notification frequency are unchanged.
 * No textures, sounds or GUI screens are required.
 */
public final class DiscoveryOverlay {
    private static Component title;
    private static Component subtitle;
    private static int remaining;
    private static final int DURATION = 110;
    private static final int FADE_IN = 13;
    private static final int FADE_OUT = 23;

    private DiscoveryOverlay() {}

    public static void show(Component discovered, Component detail) {
        title = discovered;
        subtitle = detail;
        remaining = DURATION;
    }

    public static void onTick(ClientTickEvent.Post event) {
        if (remaining > 0) remaining--;
    }

    public static void onRender(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (remaining <= 0 || title == null || mc.options.hideGui || mc.screen != null
                || !DiscoveryConfig.ENABLED.get()) return;

        GuiGraphics g = event.getGuiGraphics();
        int center = g.guiWidth() / 2;
        // Fade into view, then back out. Keep the animation frame-independent.
        int elapsed = DURATION - remaining;
        float fadeIn = Math.min(1F, elapsed / (float) FADE_IN);
        float fadeOut = Math.min(1F, remaining / (float) FADE_OUT);
        float opacity = Math.min(fadeIn, fadeOut);
        opacity = opacity * opacity * (3F - 2F * opacity);
        int alpha = Math.max(0, Math.min(255, (int) (255F * opacity)));
        if (alpha < 3) return;

        float titleScale = 1.45F;
        int titleWidth = (int) Math.ceil(mc.font.width(title) * titleScale);
        int subtitleWidth = subtitle == null ? 0 : mc.font.width(subtitle);
        int width = Math.min(g.guiWidth() - 32, Math.max(172,
                Math.max(titleWidth + 44, subtitleWidth + 48)));
        int half = width / 2;
        int y = 28 + (int) (5 * (1F - fadeIn));
        int top = y - 12;
        int bottom = y + 38;

        // A subtle translucent plaque with a restrained gold + violet border.
        g.fill(center - half, top, center + half, bottom, color(0x100F1C, (int) (118 * opacity)));
        g.fill(center - half + 12, top, center + half - 12, top + 1,
                color(0xCDA86B, (int) (155 * opacity)));
        g.fill(center - half + 12, bottom - 1, center + half - 12, bottom,
                color(0x8E6CB7, (int) (135 * opacity)));

        // Split heraldic accent above the place name. The central diamond
        // is intentionally pixel-art-friendly (no imported fonts/assets).
        int accentY = y - 7;
        g.fill(center - 56, accentY, center - 9, accentY + 1,
                color(0xAD8A58, (int) (140 * opacity)));
        g.fill(center + 9, accentY, center + 56, accentY + 1,
                color(0xAD8A58, (int) (140 * opacity)));
        g.fill(center - 2, accentY - 1, center + 2, accentY + 3,
                color(0xE7CB90, (int) (215 * opacity)));

        // Center a slightly enlarged headline, retaining the normal game font.
        float maxScale = (g.guiWidth() - 48F) / Math.max(1, mc.font.width(title));
        float scale = Math.min(titleScale, maxScale);
        g.pose().pushPose();
        g.pose().translate(center, y + 3, 0);
        g.pose().scale(scale, scale, 1F);
        g.drawCenteredString(mc.font, title, 0, 0, color(0xF6D28B, alpha));
        g.pose().popPose();

        if (subtitle != null) {
            g.drawCenteredString(mc.font, subtitle, center, y + 22,
                    color(0xD5C5E7, (int) (220 * opacity)));
        }
    }

    private static int color(int rgb, int alpha) {
        return (Math.max(0, Math.min(alpha, 255)) << 24) | rgb;
    }
}
