package dev.luxmods.hud.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import dev.luxmods.hud.HudConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Comparator;

/** Lux HUD MVP: player status from vanilla data only, no optional thirst mod. */
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

        // Portrait occupies the same compact panel; just two status bars remain.
        g.fill(x + 8, y + 23, x + 58, y + 73, 0xFF282234);
        PlayerFaceRenderer.draw(g, player.getSkin(), x + 10, y + 25, 46);
        LuxGuiStyle.icon(g, "portrait_frame", x + 7, y + 22, 52, 52);

        // Only the real Minecraft experience level, never an RPG copy.
        String level = Integer.toString(player.experienceLevel);
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

        // Two live player stats: air replaces health only while underwater.
        // Draw our own small pixel sprites; no third-party HUD state.
        if (player.isUnderWater()) {
            drawBreath(g, mc, x + 63, y + 31,
                    player.getAirSupply(), player.getMaxAirSupply());
        } else {
            drawStat(g, mc, x + 63, y + 31, "health",
                    player.getHealth(), player.getMaxHealth(), LuxGuiStyle.HEALTH);
        }
        drawStat(g, mc, x + 63, y + 51, "hunger",
                player.getFoodData().getFoodLevel(), 20f, LuxGuiStyle.HUNGER);

        if (HudConfig.SHOW_EFFECTS.get()) {
            // Keep status effects clear of the player panel.
            int virtualHeight = (int) Math.floor(g.guiHeight() / scale);
            int effectsX = virtualWidth - 170;
            drawEffects(g, mc, effectsX, y + 113, virtualWidth, virtualHeight);
        }
        int mountOffset = HudConfig.SHOW_MOUNT.get() && HudMountPanel.hasMount(mc) ? HudMountPanel.HEIGHT + 6 : 0;
        if (mountOffset > 0) HudMountPanel.render(g, mc, x, y + HEIGHT + 6, WIDTH);
        if (HudConfig.SHOW_TEAMMATES.get()) {
            HudPartyClientState.render(g, mc, x, y + HEIGHT + 7 + mountOffset, WIDTH);
        }
        // Quest HUD display moved to lux-quests and frozen by default.
        } finally {
            g.pose().popPose();
        }
    }

    private static void drawStat(GuiGraphics g, Minecraft mc, int x, int y,
                                 String icon, float value, float max, int color) {
        drawStatusIcon(g, x, y, icon);
        final int barX = x + 17;
        LuxGuiStyle.bar(g, barX, y, 143, 11, value, max, color);
        // The bars deliberately show real vanilla data, including modded health.
        int shown = Math.max(0, Math.round(value));
        int total = Math.max(1, Math.round(max));
        g.drawString(mc.font, shown + "/" + total, barX + 6, y + 1,
                0xFFFFFFFF, true);
    }

    private static void drawStatusIcon(GuiGraphics g, int x, int y, String icon) {
        if ("health".equals(icon)) {
            // Crisp 11x10 heart, replacing the fragile optional PNG.
            int edge = 0xFF5B1F33, red = 0xFFE65A79, shine = 0xFFFFA3B4;
            g.fill(x + 1, y + 2, x + 5, y + 5, edge);
            g.fill(x + 7, y + 2, x + 11, y + 5, edge);
            g.fill(x + 2, y + 3, x + 10, y + 7, red);
            g.fill(x + 3, y + 7, x + 9, y + 9, red);
            g.fill(x + 4, y + 9, x + 8, y + 10, edge);
            g.fill(x + 2, y + 3, x + 4, y + 4, shine);
        } else {
            // Pixel-art drumstick: bone, meat, rim and specular highlight.
            int edge = 0xFF5D392B, meat = 0xFFD99B50, light = 0xFFFFC779;
            g.fill(x + 1, y + 2, x + 8, y + 9, edge);
            g.fill(x + 2, y + 2, x + 7, y + 8, meat);
            g.fill(x + 3, y + 3, x + 5, y + 5, light);
            g.fill(x + 7, y + 7, x + 11, y + 10, 0xFFE6D8BF);
            g.fill(x + 9, y + 6, x + 12, y + 8, 0xFFF6E6C9);
            g.fill(x + 9, y + 9, x + 12, y + 11, 0xFFAC9683);
        }
    }

    private static void drawBreath(GuiGraphics g, Minecraft mc, int x, int y,
                                   int airTicks, int maxAirTicks) {
        int current = Math.max(0, airTicks);
        int maximum = Math.max(1, maxAirTicks);
        int blue = 0xFF63B5E1;
        drawBubble(g, x + 1, y, true);
        LuxGuiStyle.bar(g, x + 17, y, 143, 11, current, maximum, blue);
        // Show exact ticks so the player knows the remaining time underwater.
        int seconds = (int) Math.ceil(current / 20.0);
        g.drawString(mc.font, seconds + "s", x + 23, y + 1,
                0xFFFFFFFF, true);
    }

    private static void drawBubble(GuiGraphics g, int x, int y, boolean full) {
        int outline = 0xFF326889;
        int fill = full ? 0xFF73C8F0 : 0xFF365169;
        g.fill(x + 3, y, x + 8, y + 1, outline);
        g.fill(x + 1, y + 2, x + 10, y + 8, outline);
        g.fill(x + 2, y + 2, x + 9, y + 8, fill);
        g.fill(x + 3, y + 2, x + 5, y + 4, 0xFFDDF6FF);
        g.fill(x + 3, y + 9, x + 8, y + 10, outline);
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
