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
 * Lux Clean Fantasy v3. A 3-row stat grid aligned with a single tall portrait.
 * Distinct icons are sufficient: no VIDA/FOME/XP labels crowd the bars.
 */
public final class HudPanel {
    private static final int WIDTH = 230;
    private static final int HEIGHT = 80;
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

        // Portrait height is precisely the three rows: y+24 through y+72.
        g.fill(x + 8, y + 23, x + 58, y + 73, 0xFF282234);
        PlayerFaceRenderer.draw(g, player.getSkin(), x + 10, y + 25, 46);
        LuxGuiStyle.icon(g, "portrait_frame", x + 7, y + 22, 52, 52);

        String level = "Nv. " + player.experienceLevel;
        int levelX = x + WIDTH - 10 - mc.font.width(level);
        g.drawString(mc.font, level, levelX, y + 8, LuxGuiStyle.MUTED_TEXT, false);

        String name = player.getName().getString();
        int maxNameWidth = Math.max(38, levelX - (x + 65) - 8);
        if (mc.font.width(name) > maxNameWidth) {
            while (!name.isEmpty() && mc.font.width(name + "...") > maxNameWidth) {
                name = name.substring(0, name.length() - 1);
            }
            name += "...";
        }
        g.drawString(mc.font, name, x + 65, y + 8, LuxGuiStyle.FOREGROUND, true);

        // Three icons, three bars, identical x alignment and row spacing.
        drawStat(g, mc, x + 63, y + 25, "stat_health", player.getHealth(),
                player.getMaxHealth(), LuxGuiStyle.HEALTH, false);
        drawStat(g, mc, x + 63, y + 43, "stat_hunger",
                player.getFoodData().getFoodLevel(), 20f, LuxGuiStyle.HUNGER, false);
        drawStat(g, mc, x + 63, y + 61, "stat_xp",
                player.experienceProgress, 1f, LuxGuiStyle.EXPERIENCE, true);

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
                                 String icon, float value, float max, int color,
                                 boolean percent) {
        LuxGuiStyle.icon(g, icon, x, y, 12, 12);
        int bx = x + 17;
        LuxGuiStyle.bar(g, bx, y, 143, 11, value, max, color);
        String amount = percent ? Math.round(value * 100f) + "%"
                : Math.round(value) + "/" + Math.round(max);
        // Keep the numeric value readable even when the fill is empty.
        g.drawString(mc.font, amount, bx + 6, y + 1, 0xFFFFFFFF, true);
    }

    private static void drawEffects(GuiGraphics g, Minecraft mc, int x, int y, int panelWidth) {
        var effects = mc.player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon)
                .sorted(Comparator.comparing(MobEffectInstance::getDescriptionId))
                .limit(4).toList();
        if (effects.isEmpty()) return;

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
