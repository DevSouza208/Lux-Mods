package dev.luxmods.hud.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import dev.luxmods.hud.HudConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Comparator;

/**
 * Lux Clean Fantasy v2: pixel-framed portrait, inline stat icons/numbers,
 * compact XP, real potion-effects tray and optional genuine party + quests.
 */
public final class HudPanel {
    private static final int WIDTH = 230;
    private static final int HEIGHT = 84;
    private static final int LEFT = 10;
    private static final int TOP = 10;

    private HudPanel() { }

    public static boolean shouldDraw(Minecraft mc) {
        return mc.player != null && mc.level != null && !mc.player.isSpectator()
                && !mc.options.hideGui && mc.screen == null
                && HudConfig.MODE.get() == HudConfig.Mode.RPG;
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!shouldDraw(mc)) return;
        var player = mc.player;
        int x = LEFT, y = TOP;

        LuxGuiStyle.panel(g, x, y, WIDTH, HEIGHT);
        g.fill(x + 7, y + 15, x + 45, y + 53, 0xFF282234);
        PlayerFaceRenderer.draw(g, player.getSkin(), x + 10, y + 18, 32);
        LuxGuiStyle.icon(g, "portrait_frame", x + 7, y + 15, 38, 38);

        String name = player.getName().getString();
        if (name.length() > 16) name = name.substring(0, 13) + "...";
        g.drawString(mc.font, name, x + 49, y + 7, LuxGuiStyle.FOREGROUND, true);

        String level = "Nv. " + player.experienceLevel;
        g.drawString(mc.font, level, x + WIDTH - 11 - mc.font.width(level),
                y + 8, LuxGuiStyle.MUTED_TEXT, false);

        // Portrait frame stays unobstructed; values live inside their beveled bars.
        drawStat(g, mc, x + 49, y + 23, "stat_health", "VIDA", player.getHealth(),
                player.getMaxHealth(), LuxGuiStyle.HEALTH);
        drawStat(g, mc, x + 49, y + 44, "stat_hunger", "FOME",
                player.getFoodData().getFoodLevel(), 20f, LuxGuiStyle.HUNGER);

        LuxGuiStyle.separator(g, x + 8, y + 64, WIDTH - 16);
        g.drawString(mc.font, "XP", x + 10, y + 69, LuxGuiStyle.EXPERIENCE, false);
        LuxGuiStyle.icon(g, "stat_xp", x + 27, y + 68, 12, 12);
        LuxGuiStyle.bar(g, x + 43, y + 70, WIDTH - 96, 8,
                player.experienceProgress, 1f, LuxGuiStyle.EXPERIENCE);

        if (HudConfig.SHOW_EFFECTS.get()) {
            drawEffects(g, mc, x + WIDTH + 9, y + 2, WIDTH);
        }
        if (HudConfig.SHOW_TEAMMATES.get()) {
            HudPartyClientState.render(g, mc, x, y + HEIGHT + 7, WIDTH);
        }
        if (HudConfig.SHOW_QUESTS.get()) {
            HudQuestTracker.render(g, mc, x + WIDTH + 14, y);
        }
    }

    private static void drawStat(GuiGraphics g, Minecraft mc, int x, int y,
                                 String icon, String label, float value,
                                 float max, int color) {
        g.drawString(mc.font, label, x, y, LuxGuiStyle.MUTED_TEXT, false);
        LuxGuiStyle.icon(g, icon, x, y + 10, 11, 11);
        LuxGuiStyle.bar(g, x + 15, y + 10, 132, 11, value, max, color);
        String amount = Math.round(value) + "/" + Math.round(max);
        g.drawString(mc.font, amount, x + 22, y + 11, 0xFFFFFFFF, true);
    }

    private static void drawEffects(GuiGraphics g, Minecraft mc, int x, int y, int panelWidth) {
        var effects = mc.player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon)
                .sorted(Comparator.comparing(MobEffectInstance::getDescriptionId))
                .limit(4).toList();
        if (effects.isEmpty()) return;

        // Keep clear of the right-hand quest panel on narrower screens.
        int available = mc.getWindow().getGuiScaledWidth() - 212 - x;
        int count = Math.min(effects.size(), Math.max(0, (available - 8) / 25));
        if (count == 0) return;

        LuxGuiStyle.panel(g, x, y, count * 25 + 9, 36);
        for (int i = 0; i < count; i++) {
            MobEffectInstance effect = effects.get(i);
            int slotX = x + 6 + i * 25;
            g.fill(slotX - 1, y + 4, slotX + 18, y + 23, 0xFF292334);
            LuxGuiStyle.icon(g, "effect_slot", slotX, y + 5, 16, 16);
            g.blit(slotX, y + 5, 0, 16, 16,
                    mc.getMobEffectTextures().get(effect.getEffect()));

            int seconds = Math.max(0, effect.getDuration() / 20);
            String time = seconds / 60 + ":" + String.format("%02d", seconds % 60);
            g.drawString(mc.font, time, slotX - 1, y + 25, LuxGuiStyle.MUTED_TEXT, false);
        }
    }
}
