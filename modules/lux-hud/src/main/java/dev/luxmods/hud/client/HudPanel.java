package dev.luxmods.hud.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import dev.luxmods.hud.HudConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Comparator;

/** Functional HUD with Lux Clean Fantasy styling; all stats remain genuine. */
public final class HudPanel {
    private static final int WIDTH = 230;
    private static final int HEIGHT = 89;
    private static final int LEFT = 9;
    private static final int TOP = 9;

    private HudPanel() {
    }

    public static boolean shouldDraw(Minecraft mc) {
        return mc.player != null && mc.level != null && !mc.player.isSpectator()
                && !mc.options.hideGui && mc.screen == null
                && HudConfig.MODE.get() == HudConfig.Mode.RPG;
    }

    public static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!shouldDraw(mc)) return;
        var player = mc.player;
        int x = LEFT;
        int y = TOP;

        LuxGuiStyle.panel(graphics, x, y, WIDTH, HEIGHT);
        graphics.fill(x + 8, y + 16, x + 42, y + 50, 0xFF3A3445);
        PlayerFaceRenderer.draw(graphics, player.getSkin(), x + 9, y + 17, 32);
        LuxGuiStyle.icon(graphics, "lux_mark", x + WIDTH - 24, y + 6, 16, 16);

        String name = player.getName().getString();
        if (name.length() > 16) name = name.substring(0, 13) + "...";
        graphics.drawString(mc.font, name, x + 49, y + 7, LuxGuiStyle.FOREGROUND, true);

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        graphics.drawString(mc.font, "VIDA " + Math.round(health) + "/" + Math.round(maxHealth),
                x + 49, y + 19, 0xFFFFD3DA, false);
        LuxGuiStyle.bar(graphics, x + 49, y + 31, 118, 5,
                health, maxHealth, LuxGuiStyle.HEALTH);

        if (HudConfig.SHOW_EFFECTS.get()) {
            drawEffects(graphics, mc, x + 176, y + 26);
        }

        float food = player.getFoodData().getFoodLevel();
        graphics.drawString(mc.font, "FOME " + Math.round(food) + "/20",
                x + 49, y + 42, LuxGuiStyle.AMBER, false);
        LuxGuiStyle.bar(graphics, x + 49, y + 54, 118, 5,
                food, 20f, LuxGuiStyle.HUNGER);

        LuxGuiStyle.separator(graphics, x + 8, y + 65, WIDTH - 16);
        graphics.drawString(mc.font, "XP", x + 9, y + 72,
                LuxGuiStyle.EXPERIENCE, false);
        graphics.drawString(mc.font, "Nv. " + player.experienceLevel,
                x + WIDTH - 47, y + 72, LuxGuiStyle.MUTED_TEXT, false);
        LuxGuiStyle.bar(graphics, x + 34, y + 76, 139, 5,
                player.experienceProgress, 1f, LuxGuiStyle.EXPERIENCE);

        if (HudConfig.SHOW_TEAMMATES.get()) {
            HudPartyClientState.render(graphics, mc, x, y + HEIGHT + 7, WIDTH);
        }
        if (HudConfig.SHOW_QUESTS.get()) {
            HudQuestTracker.render(graphics, mc, x + WIDTH + 14, y);
        }
    }

    private static void drawEffects(GuiGraphics graphics, Minecraft mc, int x, int y) {
        // Leave original Minecraft potion icon texture intact.
        var effects = mc.player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon)
                .sorted(Comparator.comparing(MobEffectInstance::getDescriptionId))
                .limit(2).toList();

        for (int i = 0; i < effects.size(); i++) {
            var effect = effects.get(i);
            int slotX = x + i * 21;
            graphics.fill(slotX - 1, y - 1, slotX + 18, y + 18, 0xAA242431);
            LuxGuiStyle.icon(graphics, "effect_slot", slotX, y, 16, 16);
            graphics.blit(slotX, y, 0, 16, 16,
                    mc.getMobEffectTextures().get(effect.getEffect()));
        }
        long overflow = mc.player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon).count() - effects.size();
        if (overflow > 0) {
            graphics.drawString(mc.font, "+" + overflow, x + 3, y + 21,
                    LuxGuiStyle.MUTED_TEXT, true);
        }
    }
}
