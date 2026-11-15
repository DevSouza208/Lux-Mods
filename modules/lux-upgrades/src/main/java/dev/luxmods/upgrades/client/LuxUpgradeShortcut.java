package dev.luxmods.upgrades.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

/** Backspace opens upgrades during gameplay; no pause menu or J/U shortcut. */
public final class LuxUpgradeShortcut {
    private static final KeyMapping OPEN = new KeyMapping(
            "key.lux_upgrades.open", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_BACKSPACE, "key.categories.lux_mods");

    private LuxUpgradeShortcut() {}

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN);
    }

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        while (OPEN.consumeClick()) {
            mc.setScreen(new LuxUpgradesScreen(null));
        }
    }
}
