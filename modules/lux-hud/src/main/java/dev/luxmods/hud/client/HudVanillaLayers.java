package dev.luxmods.hud.client;

import dev.luxmods.hud.HudConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Avoid duplicated status bars; preserve armor, air, mounts and other vanilla UI. */
public final class HudVanillaLayers {
    private HudVanillaLayers() {
    }

    public static void onPre(RenderGuiLayerEvent.Pre event) {
        if (!HudPanel.shouldDraw(Minecraft.getInstance())
                || !HudConfig.REPLACE_VANILLA_BARS.get()) {
            return;
        }
        var id = event.getName();
        if (VanillaGuiLayers.PLAYER_HEALTH.equals(id)
                || VanillaGuiLayers.FOOD_LEVEL.equals(id)
                || VanillaGuiLayers.EXPERIENCE_BAR.equals(id)
                || VanillaGuiLayers.EXPERIENCE_LEVEL.equals(id)) {
            event.setCanceled(true);
        }
    }
}
