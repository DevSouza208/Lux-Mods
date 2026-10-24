package dev.luxmods.hud.client;

import dev.luxmods.hud.HudConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Cancel only bars already represented by the Lux HUD.
 * Preserve armor, breath, mounts, boss bars, subtitles, actionbar and hotbar.
 */
public final class HudVanillaLayers {
    private HudVanillaLayers() {
    }

    public static void onPre(RenderGuiLayerEvent.Pre event) {
        if (!HudPanel.shouldDraw(Minecraft.getInstance())) return;
        var name = event.getName();
        if (HudConfig.REPLACE_VANILLA_BARS.get()
                && (VanillaGuiLayers.PLAYER_HEALTH.equals(name)
                || VanillaGuiLayers.FOOD_LEVEL.equals(name)
                || VanillaGuiLayers.EXPERIENCE_BAR.equals(name)
                || VanillaGuiLayers.EXPERIENCE_LEVEL.equals(name))) {
            event.setCanceled(true);
        }
        // Potion effects have moved next to health. They remain vanilla
        // if the optional effects section is disabled.
        if (HudConfig.SHOW_EFFECTS.get() && VanillaGuiLayers.EFFECTS.equals(name)) {
            event.setCanceled(true);
        }
    }
}
