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
        double scale = HudConfig.HUD_SCALE.get();
        int virtualWidth = (int) Math.floor(g.guiWidth() / scale);
        int x = LEFT, y = TOP;
        // Draw edge feedback in native GUI coordinates before scaling the panel.
        HudDangerVignette.render(g, mc);
        g.pose().pushPose();
        g.pose().scale((float) scale, (float) scale, 1f);
        try {

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
        Integer hydration = HudThirstCompat.readThirst();
        if (hydration != null) {
            drawThirst(g, mc, x + 63, y + 61, hydration);
        } else {
            // Never substitute XP for thirst. If the installed hydration mod
            // does not expose a readable value, keep its own overlay visible.
            g.drawString(mc.font, "Sede: --", x + 80, y + 63,
                    LuxGuiStyle.MUTED_TEXT, false);
        }

        if (HudConfig.SHOW_EFFECTS.get()) {
            // Align active conditions with the right-side quests panel.
            // Placing them below its maximum 104px height prevents overlap
            // on narrower resolutions where the quest panel shifts left.
            int virtualHeight = (int) Math.floor(g.guiHeight() / scale);
            int effectsX = virtualWidth - 170;
            drawEffects(g, mc, effectsX, y + 113, virtualWidth, virtualHeight);
        }
        int mountOffset = HudConfig.SHOW_MOUNT.get() && HudMountPanel.hasMount(mc) ? HudMountPanel.HEIGHT + 6 : 0;
        if (mountOffset > 0) HudMountPanel.render(g, mc, x, y + HEIGHT + 6, WIDTH);
        if (HudConfig.SHOW_TEAMMATES.get()) {
            HudPartyClientState.render(g, mc, x, y + HEIGHT + 7 + mountOffset, WIDTH);
        }
        if (HudConfig.SHOW_QUESTS.get()) {
            HudQuestTracker.render(g, mc, x + WIDTH + 14, y, virtualWidth);
        }
        } finally {
            g.pose().popPose();
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

    private static void drawThirst(GuiGraphics g, Minecraft mc, int x, int y, int hydration) {
        // A tiny procedural water-drop silhouette, no extra texture pack needed.
        int blue = 0xFF63B5E1;
        g.fill(x + 5, y, x + 7, y + 2, blue);
        g.fill(x + 4, y + 2, x + 8, y + 5, blue);
        g.fill(x + 3, y + 5, x + 9, y + 9, blue);
        g.fill(x + 4, y + 9, x + 8, y + 11, blue);
        g.fill(x + 5, y + 11, x + 7, y + 12, blue);
        LuxGuiStyle.bar(g, x + 17, y, 143, 11, hydration, 20f, blue);
        g.drawString(mc.font, hydration + "/20", x + 23, y + 1, 0xFFFFFFFF, true);
    }

    private static void drawEffects(GuiGraphics g, Minecraft mc, int x, int y, int virtualWidth, int virtualHeight) {
        var effects = mc.player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon)
                .sorted(Comparator.comparing(MobEffectInstance::getDescriptionId))
                .limit(4).toList();
        int maxCards = Math.max(0, (virtualHeight - y - 30) / 34);
        if (effects.isEmpty() || x < 8 || x + 158 > virtualWidth || maxCards == 0) return;

        for (int i = 0; i < Math.min(effects.size(), maxCards); i++) {
            MobEffectInstance effect = effects.get(i);
            int cy = y + i * 34;
            LuxGuiStyle.panel(g, x, cy, 158, 31);
            g.fill(x + 5, cy + 6, x + 25, cy + 26, 0xFF292334);
            LuxGuiStyle.icon(g, "effect_slot", x + 6, cy + 7, 18, 18);
            g.blit(x + 7, cy + 8, 0, 16, 16,
                    mc.getMobEffectTextures().get(effect.getEffect()));

            String name = effect.getEffect().value().getDisplayName().getString();
            if (name.length() > 17) name = name.substring(0, 16) + "…";
            if (effect.getAmplifier() > 0) name += " " + (effect.getAmplifier() + 1);
            g.drawString(mc.font, name, x + 30, cy + 6, LuxGuiStyle.FOREGROUND, false);
            int seconds = Math.max(0, effect.getDuration() / 20);
            String time = seconds / 60 + ":" + String.format("%02d", seconds % 60);
            g.drawString(mc.font, time, x + 30, cy + 18, LuxGuiStyle.MUTED_TEXT, false);
        }
    }
}
