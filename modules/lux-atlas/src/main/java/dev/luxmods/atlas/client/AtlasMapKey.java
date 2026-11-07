package dev.luxmods.atlas.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

/** Opens the atlas on M, only when gameplay has focus. */
public final class AtlasMapKey {
    private static boolean wasDown;
    private AtlasMapKey() {}

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getWindow() == null) {
            wasDown = false;
            return;
        }
        boolean down = GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_M) == GLFW.GLFW_PRESS;
        if (down && !wasDown && mc.screen == null) {
            mc.setScreen(new AtlasMapScreen());
        }
        wasDown = down;
    }
}
