package dev.luxmods.hud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Client-side rendering adapter for future Lux Quests integration.
 * No fabricated quests. The right panel displays a truthful empty state.
 */
public final class HudQuestTracker {
    private static final int WIDTH = 198;
    private static Quest tracked;

    public record Quest(String title, String objective, int done, int total) {
        public Quest {
            title = trim(title, 44);
            objective = trim(objective, 48);
            done = Math.max(0, done);
            total = Math.max(0, total);
            done = Math.min(done, total);
        }
    }

    private HudQuestTracker() {
    }

    public static void track(String title, String objective, int done, int total) {
        tracked = new Quest(title, objective, done, total);
    }

    public static void clear() {
        tracked = null;
    }

    private static String trim(String text, int limit) {
        String safe = text == null ? "" : text.strip();
        return safe.length() <= limit ? safe : safe.substring(0, limit - 3) + "...";
    }

    public static void render(GuiGraphics graphics, Minecraft mc, int minX, int top) {
        int x = graphics.guiWidth() - WIDTH - 8;
        // Never cover the left player panel on smaller GUI scales.
        if (x < minX) return;
        int h = tracked == null ? 43 : 69;
        graphics.fill(x - 2, top - 2, x + WIDTH + 2, top + h + 2, 0xA00B0B14);
        graphics.fill(x, top, x + WIDTH, top + 2, 0xFF8061D8);
        graphics.drawString(mc.font, "MISSÕES", x + 8, top + 9, 0xFFF0E4FF, true);
        Quest current = tracked;
        if (current == null) {
            graphics.drawString(mc.font, "Nenhuma missão acompanhada",
                    x + 8, top + 25, 0xFFB6B1C2, false);
            return;
        }
        graphics.drawString(mc.font, elide(current.title(), 31), x + 8, top + 23, 0xFFFFFFFF, false);
        graphics.drawString(mc.font, elide(current.objective(), 31), x + 8, top + 36, 0xFFBEC2D1, false);
        int filled = Math.min(WIDTH - 16,
                dev.luxmods.hud.HudBarMath.filledWidth(current.done(), current.total(), WIDTH - 16));
        graphics.fill(x + 8, top + 54, x + WIDTH - 8, top + 59, 0xFF2B2738);
        if (filled > 0) graphics.fill(x + 8, top + 54, x + 8 + filled, top + 59, 0xFF8B71E2);
        graphics.drawString(mc.font, current.done() + "/" + current.total(),
                x + WIDTH - 42, top + 62, 0xFFD4CEE8, false);
    }

    private static String elide(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}
