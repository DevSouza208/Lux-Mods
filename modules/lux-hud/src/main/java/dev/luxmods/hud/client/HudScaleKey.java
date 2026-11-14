package dev.luxmods.hud.client;

import dev.luxmods.hud.HudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

/** F8 cycles the local HUD scale without changing Minecraft's GUI scale. */
public final class HudScaleKey {
    private static final double[] SCALES = {0.60D, 0.75D, 0.85D, 1.00D};
    private static boolean wasDown;

    private HudScaleKey() {}

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null || mc.player == null) return;
        long window = mc.getWindow().getWindow();
        boolean down = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_F8) == GLFW.GLFW_PRESS;
        if (down && !wasDown && mc.screen == null) {
            double current = HudConfig.HUD_SCALE.get();
            int selected = -1;
            for (int i = 0; i < SCALES.length; i++) {
                if (Math.abs(SCALES[i] - current) < 0.001D) {
                    selected = i;
                    break;
                }
            }
            double next = SCALES[(selected + 1) % SCALES.length];
            HudConfig.HUD_SCALE.set(next);
            mc.player.displayClientMessage(Component.literal("Lux HUD: " + Math.round(next * 100) + "% (F8)"), true);
        }
        wasDown = down;
    }
}
