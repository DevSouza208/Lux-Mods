package dev.luxmods.menu.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import dev.luxmods.menu.MenuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.List;

/**
 * Small noninvasive styling layer for Minecraft 1.21.1 title/pause menus.
 * No Screen.Opening redirection, no cancellation, no changes to world saves.
 */
public final class LuxMenuScreens {
    private LuxMenuScreens() { }

    private static boolean isPrimaryScreen(Screen screen) {
        return screen instanceof TitleScreen || screen instanceof PauseScreen;
    }

    private static boolean isButtonStyleScreen(Screen screen) {
        if (isPrimaryScreen(screen)) return true;
        if (!MenuConfig.STYLE_SUBMENUS.get()) return false;
        // Keep these screens' lists, file/hostname fields and special controls untouched.
        return screen instanceof OptionsScreen || screen instanceof JoinMultiplayerScreen
                || screen instanceof SelectWorldScreen;
    }

    /** Run after vanilla has added its controls. Preserve all nonstandard widgets. */
    public static void onInit(ScreenEvent.Init.Post event) {
        if (!MenuConfig.ENABLED.get() || !MenuConfig.STYLE_BUTTONS.get()
                || !isButtonStyleScreen(event.getScreen())) return;

        // Only replace exact vanilla Button objects: leave subclass-specific
        // buttons, sliders, modded widgets and event-provided controls alone.
        for (var item : List.copyOf(event.getListenersList())) {
            if (item.getClass() != Button.class) continue;
            Button button = (Button) item;
            if (button.getWidth() < 62 || button.getHeight() < 16) continue;
            event.removeListener(button);
            event.addListener(new LuxMenuButton(button));
        }
    }

    /** Branding is drawn after the screen but outside interactive controls. */
    public static void onRender(ScreenEvent.Render.Post event) {
        if (!MenuConfig.ENABLED.get() || !MenuConfig.SHOW_BRANDING.get()
                || !isPrimaryScreen(event.getScreen())) return;
        GuiGraphics g = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int width = g.guiWidth(), height = g.guiHeight();
        if (width < 240 || height < 150) return;

        // Always position away from title logo and central vanilla widgets.
        int x = width - 112;
        int y = 12;
        g.fill(x, y, x + 102, y + 29, 0xD7191623);
        g.fill(x, y, x + 102, y + 1, LuxGuiStyle.TOP_ACCENT);
        g.fill(x, y + 28, x + 102, y + 29, LuxGuiStyle.EDGE);
        LuxGuiStyle.icon(g, "lux_mark", x + 5, y + 5, 17, 17);
        g.drawString(mc.font, "LUX MODS", x + 26, y + 6, LuxGuiStyle.FOREGROUND, true);
        g.drawString(mc.font, "CLEAN FANTASY", x + 26, y + 17, LuxGuiStyle.MUTED_TEXT, false);

        // Bottom-center caption; not an overlay over the main buttons.
        String footer = event.getScreen() instanceof TitleScreen
                ? "LUX • MINECRAFT 1.21.1" : "LUX • AVENTURA EM PAUSA";
        int labelWidth = mc.font.width(footer);
        int fx = Math.max(5, (width - labelWidth) / 2);
        if (height > 195) {
            g.drawString(mc.font, footer, fx, height - 16, LuxGuiStyle.AMBER, true);
        }
    }
}
