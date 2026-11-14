package dev.luxmods.hud.client;

import dev.luxmods.hud.LuxHud;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Register a dedicated client-only layer above the hotbar. */
public final class HudClientLayers {
    private HudClientLayers() {
    }

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LuxHud.MOD_ID, "hotbar_trim"),
                HudHotbar::render
        );
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LuxHud.MOD_ID, "player_panel"),
                HudPanel::render
        );
    }
}
