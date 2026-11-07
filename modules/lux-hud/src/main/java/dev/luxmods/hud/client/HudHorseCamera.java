package dev.luxmods.hud.client;

import dev.luxmods.hud.HudConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.lwjgl.glfw.GLFW;

/** Horse camera: auto third person; F9 switches shoulder; restore on dismount. */
public final class HudHorseCamera {
    private static boolean mounted;
    private static boolean previousKey;
    private static CameraType previousCamera;
    private static int side = 1;
    private HudHorseCamera() {}

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getWindow() == null) {
            mounted = false;
            previousCamera = null;
            previousKey = false;
            return;
        }
        boolean riding = HudMountPanel.hasMount(mc);
        if (riding && !mounted) {
            mounted = true;
            previousCamera = mc.options.getCameraType();
            if (HudConfig.HORSE_SHOULDER_CAMERA.get()) {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
        } else if (!riding && mounted) {
            mounted = false;
            if (previousCamera != null && HudConfig.RESTORE_HORSE_CAMERA.get()) {
                mc.options.setCameraType(previousCamera);
            }
            previousCamera = null;
        }
        long window = mc.getWindow().getWindow();
        boolean down = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_F9) == GLFW.GLFW_PRESS;
        if (riding && down && !previousKey && mc.screen == null) {
            side = -side;
            mc.player.displayClientMessage(
                Component.literal("Camera: ombro " + (side > 0 ? "direito" : "esquerdo")), true);
        }
        previousKey = down;
    }

    public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (!mounted || !HudConfig.HORSE_SHOULDER_CAMERA.get()
                || !HudMountPanel.hasMount(mc)
                || mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) return;
        // Shift the active camera sideways without changing the entity's rotation.
        double yaw = Math.toRadians(event.getCamera().getYRot());
        double amount = HudConfig.HORSE_SHOULDER_OFFSET.get() * side;
        Vec3 pos = event.getCamera().getPosition();
        event.getCamera().setPosition(pos.add(Math.cos(yaw) * amount, 0, Math.sin(yaw) * amount));
    }
}
