package dev.luxmods.hud.client;

import dev.luxmods.hud.HudBarMath;
import dev.luxmods.hud.HudConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Comparator;

/**
 * Functional MVP: real player skin, vanilla health/hunger/XP, potion sprites,
 * small teammates underneath, and a truthful quest tracker on the right.
 * Styling, transitions and quest synchronization will follow after playtesting.
 */
public final class HudPanel {
    private static final int WIDTH = 252;
    private static final int HEIGHT = 100;
    private static final int BAR_WIDTH = 146;
    private static final int LEFT = 8;
    private static final int TOP = 8;

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

        graphics.fill(x - 2, y - 2, x + WIDTH + 2, y + HEIGHT + 2, 0xA00B0B14);
        graphics.fill(x, y, x + WIDTH, y + 2, 0xFF8061D8);
        graphics.fill(x + 5, y + 18, x + 43, y + 56, 0xFF3B3447);
        PlayerFaceRenderer.draw(graphics, player.getSkin(), x + 8, y + 21, 32);

        String name = player.getName().getString();
        if (name.length() > 21) name = name.substring(0, 18) + "...";
        graphics.drawString(mc.font, name, x + 49, y + 8, 0xFFFFFFFF, true);

        float hp = player.getHealth();
        float max = player.getMaxHealth();
        graphics.drawString(mc.font, "VIDA " + Math.round(hp) + "/" + Math.round(max),
                x + 49, y + 21, 0xFFFFC1CB, false);
        bar(graphics, x + 49, y + 32, hp, max, 0xFFDB4E62);

        if (HudConfig.SHOW_EFFECTS.get()) drawEffects(graphics, mc, x + 197, y + 23);

        float hunger = player.getFoodData().getFoodLevel();
        graphics.drawString(mc.font, "FOME " + Math.round(hunger) + "/20",
                x + 49, y + 45, 0xFFFFD89B, false);
        bar(graphics, x + 49, y + 56, hunger, 20, 0xFFE2A951);

        graphics.drawString(mc.font, "XP", x + 8, y + 74, 0xFF9DE0AA, false);
        graphics.drawString(mc.font, "Nv. " + player.experienceLevel,
                x + WIDTH - 47, y + 74, 0xFFDCF5DC, false);
        bar(graphics, x + 49, y + 85, player.experienceProgress, 1f, 0xFF69C584);

        if (HudConfig.SHOW_TEAMMATES.get()) {
            HudPartyClientState.render(graphics, mc, x, y + HEIGHT + 7, WIDTH);
        }
        if (HudConfig.SHOW_QUESTS.get()) {
            HudQuestTracker.render(graphics, mc, x + WIDTH + 10, y);
        }
    }

    private static void drawEffects(GuiGraphics graphics, Minecraft mc, int x, int y) {
        // Nearby, never overlay the health bar. Only genuine active potion effects.
        var effects = mc.player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon)
                .sorted(Comparator.comparing(MobEffectInstance::getDescriptionId))
                .limit(3).toList();
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance effect = effects.get(i);
            graphics.blit(x + i * 17, y, 0, 15, 15,
                    mc.getMobEffectTextures().get(effect.getEffect()));
        }
        long overflow = mc.player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon).count() - effects.size();
        if (overflow > 0) {
            graphics.drawString(mc.font, "+" + overflow, x + 2, y + 18, 0xFFE8E8F2, true);
        }
    }

    static void bar(GuiGraphics graphics, int x, int y,
                    float value, float max, int color) {
        graphics.fill(x, y, x + BAR_WIDTH, y + 6, 0xFF292733);
        int filled = HudBarMath.filledWidth(value, max, BAR_WIDTH);
        if (filled > 0) graphics.fill(x, y, x + filled, y + 6, color);
    }
}
