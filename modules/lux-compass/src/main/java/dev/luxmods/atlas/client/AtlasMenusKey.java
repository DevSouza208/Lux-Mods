package dev.luxmods.atlas.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/** Separate, remappable quest and upgrade menus. M belongs to external maps. */
public final class AtlasMenusKey {
    private static final String CATEGORY = "key.categories.lux_compass";
    private static final KeyMapping MISSIONS = new KeyMapping(
            "key.lux_compass.missions", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
    private static final KeyMapping UPGRADES = new KeyMapping(
            "key.lux_compass.upgrades", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, CATEGORY);

    private AtlasMenusKey() {}

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(MISSIONS);
        event.register(UPGRADES);
    }

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        while (MISSIONS.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new AtlasMissionsScreen());
        }
        while (UPGRADES.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new AtlasUpgradesScreen());
        }
    }
}
