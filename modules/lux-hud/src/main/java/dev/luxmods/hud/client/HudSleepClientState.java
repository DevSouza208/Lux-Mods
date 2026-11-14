package dev.luxmods.hud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

/**
 * Last server-provided sleep state; never predicts fatigue from local ticks.
 * Clears when the client switches worlds/players.
 */
public final class HudSleepClientState {
    private static int awakeTicks;
    private static int faintTicks;
    private static ClientLevel world;
    private static LocalPlayer player;

    private HudSleepClientState() {}

    public static void receive(int awake, int faint) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        world = mc.level;
        player = mc.player;
        awakeTicks = Math.max(0, awake);
        faintTicks = Math.max(0, faint);
    }

    public static int awakeTicks() {
        Minecraft mc = Minecraft.getInstance();
        if (world != mc.level || player != mc.player) {
            awakeTicks = 0;
            faintTicks = 0;
            return 0;
        }
        return awakeTicks;
    }

    public static int faintTicks() {
        awakeTicks();
        return faintTicks;
    }
}
