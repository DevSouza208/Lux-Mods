package dev.luxmods.hud.client;

import dev.luxmods.hud.HudConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * RPG HUD owns the player's status indicators. The lower center of the screen
 * belongs exclusively to the decorated hotbar: no stray vanilla status icons.
 *
 * Hide vanilla status layers independently of the legacy replaceVanillaBars
 * switch: an old client config with that switch disabled must not leak hunger
 * or other stats beside the hotbar.
 *
 * Keep the hotbar itself, crosshair, chat, action bar, boss bars and overlays.
 * CLASSIC mode retains the vanilla HUD as advertised.
 */
public final class HudVanillaLayers {
    private HudVanillaLayers() {
    }

    public static void onPre(RenderGuiLayerEvent.Pre event) {
        if (!HudPanel.shouldDraw(Minecraft.getInstance())) return;

        var name = event.getName();
        // Third-party thirst indicator is hidden ONLY after reading a real
        // hydration value. Until then it stays visible for player safety.
        if ("thirst".equals(name.getNamespace())
                && name.getPath().contains("thirst")
                && HudThirstCompat.readThirst() != null) {
            event.setCanceled(true);
            return;
        }
        if (VanillaGuiLayers.PLAYER_HEALTH.equals(name)
                || VanillaGuiLayers.ARMOR_LEVEL.equals(name)
                || VanillaGuiLayers.FOOD_LEVEL.equals(name)
                || VanillaGuiLayers.AIR_LEVEL.equals(name)
                || VanillaGuiLayers.EXPERIENCE_BAR.equals(name)
                || VanillaGuiLayers.EXPERIENCE_LEVEL.equals(name)
                || VanillaGuiLayers.VEHICLE_HEALTH.equals(name)
                || VanillaGuiLayers.JUMP_METER.equals(name)
                || VanillaGuiLayers.SELECTED_ITEM_NAME.equals(name)) {
            event.setCanceled(true);
            return;
        }

        // Active status effects are represented in the Lux panel when enabled.
        if (HudConfig.SHOW_EFFECTS.get() && VanillaGuiLayers.EFFECTS.equals(name)) {
            event.setCanceled(true);
        }
    }
}
