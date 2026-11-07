package dev.luxmods.discovery.client;

import dev.luxmods.discovery.DiscoveryConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Small top-centered discovery label, independent of vanilla's giant title overlay. */
public final class DiscoveryOverlay {
    private static Component title;
    private static Component subtitle;
    private static int remaining;
    private static final int DURATION = 90;

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
        // Keep the notification above gameplay without taking over the center of the screen.
        int top = 12;
        int textWidth = Math.max(mc.font.width(title), subtitle == null ? 0 : mc.font.width(subtitle));
        int width = Math.min(g.guiWidth() - 16, textWidth + 24);
        int left = center - width / 2;
        int alpha = Math.min(255, remaining * 255 / 15);
        int bg = (Math.min(160, alpha * 160 / 255) << 24) | 0x181322;
        g.fill(left, top - 4, left + width, top + 25, bg);
        g.drawCenteredString(mc.font, title, center, top, (alpha << 24) | 0xFFBD50);
        if (subtitle != null) {
            g.drawCenteredString(mc.font, subtitle, center, top + 11, (alpha << 24) | 0xCBC6D4);
        }
    }
}
