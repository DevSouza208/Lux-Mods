package dev.luxmods.hud.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Optional, client-only compatibility with Thirst Was Taken.
 *
 * The older public source exposes a PLAYER_THIRST object in ThirstBarRenderer,
 * whose getThirst() method returns a 0..20 hydration value. The code for the
 * 1.21.1 port is not publicly available, so this adapter MUST fail open:
 * when that entry point is missing, the Lux HUD leaves the original thirst
 * indicator alone and continues to display XP, never a fabricated value.
 *
 * No hard dependency or reflective writes to another mod.
 */
public final class HudThirstCompat {
    private static final String[] RENDERERS = {
            "dev.ghen.thirst.foundation.gui.ThirstBarRenderer",
            "dev.ghen.thirst.client.gui.ThirstBarRenderer"
    };
    private static volatile boolean lookedUp;
    private static Field thirstField;
    private static Method thirstGetter;
    private static ClientLevel lastLevel;
    private static int warmupTicks;

    private HudThirstCompat() {}

    public static Integer readThirst() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;
        if (mc.level != lastLevel) {
            lastLevel = mc.level;
            // Allow the original mod's overlay to initialize its cached
            // thirst reference when joining or switching worlds.
            warmupTicks = mc.player.tickCount + 60;
            thirstGetter = null;
        }
        if (mc.player.tickCount < warmupTicks) return null;
        if (!lookedUp) resolve();
        if (thirstField == null) return null;
        try {
            Object state = thirstField.get(null);
            if (state == null) return null;
            if (thirstGetter == null || !thirstGetter.getDeclaringClass().isInstance(state)) {
                thirstGetter = state.getClass().getMethod("getThirst");
            }
            Object value = thirstGetter.invoke(state);
            if (value instanceof Number number) {
                int level = number.intValue();
                if (level >= 0 && level <= 20) return level;
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Missing or changed third-party API: keep the original thirst HUD.
        }
        return null;
    }

    private static synchronized void resolve() {
        if (lookedUp) return;
        for (String name : RENDERERS) {
            try {
                Class<?> owner = Class.forName(name, false, HudThirstCompat.class.getClassLoader());
                Field field = owner.getField("PLAYER_THIRST");
                thirstField = field;
                break;
            } catch (ClassNotFoundException | NoSuchFieldException | LinkageError ignored) {
                // An installed 1.21.1 port may use a different implementation.
            }
        }
        lookedUp = true;
    }
}
