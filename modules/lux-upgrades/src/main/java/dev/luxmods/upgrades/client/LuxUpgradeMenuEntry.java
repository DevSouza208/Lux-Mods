package dev.luxmods.upgrades.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Pause-menu entry; no custom keybind or changes to other mods' attribute pages. */
public final class LuxUpgradeMenuEntry {
    private LuxUpgradeMenuEntry() {}

    public static void onPauseInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen screen)) return;
        // Under the main vanilla pause controls, without obscuring them.
        int x = screen.width / 2 - 100;
        int y = screen.height / 4 + 168;
        if (y + 20 >= screen.height - 14) return;
        event.addListener(Button.builder(Component.literal("Melhorias"), b ->
                Minecraft.getInstance().setScreen(new LuxUpgradesScreen(screen)))
                .bounds(x, y, 200, 20).build());
    }
}
