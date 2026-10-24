package dev.luxmods.hud.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Placeholder for real Lux Quests data; never fabricates progress. */
public final class HudQuestTracker {
    private static final int WIDTH = 194;
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
        int x = Math.max(minX, graphics.guiWidth() - WIDTH - 9);
        int width = Math.min(WIDTH, graphics.guiWidth() - x - 9);
        if (width < 128) return;
        Quest current = tracked;
        int h = current == null ? 40 : 69;

        LuxGuiStyle.panel(graphics, x, top, width, h);
        LuxGuiStyle.icon(graphics, "quest_badge", x + 7, top + 5, 16, 16);
        graphics.drawString(mc.font, "MISSÕES", x + 28, top + 10,
                LuxGuiStyle.AMBER, true);
        LuxGuiStyle.separator(graphics, x + 8, top + 23, width - 16);

        if (current == null) {
            graphics.drawString(mc.font,
                    width < 170 ? "Sem missão ativa" : "Nenhuma missão acompanhada",
                    x + 9, top + 28, LuxGuiStyle.MUTED_TEXT, false);
            return;
        }
        graphics.drawString(mc.font, elide(current.title(), width < 170 ? 19 : 29),
                x + 9, top + 28, LuxGuiStyle.FOREGROUND, true);
        graphics.drawString(mc.font, elide(current.objective(), width < 170 ? 19 : 29),
                x + 9, top + 41, LuxGuiStyle.MUTED_TEXT, false);
        LuxGuiStyle.bar(graphics, x + 9, top + 55, width - 18, 4,
                current.done(), current.total(), LuxGuiStyle.TOP_ACCENT);
        graphics.drawString(mc.font, current.done() + "/" + current.total(),
                x + width - 37, top + 61, LuxGuiStyle.AMBER, false);
    }

    private static String elide(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}
