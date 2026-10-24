package dev.luxmods.hud.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Real quest data only; rich pixel framing even when no quest is tracked. */
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

    private HudQuestTracker() { }

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

    public static void render(GuiGraphics g, Minecraft mc, int minX, int top) {
        int x = Math.max(minX, g.guiWidth() - WIDTH - 10);
        int width = Math.min(WIDTH, g.guiWidth() - x - 10);
        if (width < 128) return;
        Quest current = tracked;
        int height = current == null ? 46 : 81;

        LuxGuiStyle.panel(g, x, top, width, height);
        LuxGuiStyle.icon(g, "quest_badge", x + 7, top + 5, 18, 18);
        g.drawString(mc.font, "MISSÕES", x + 29, top + 10,
                LuxGuiStyle.FOREGROUND, true);
        LuxGuiStyle.separator(g, x + 9, top + 25, width - 18);

        if (current == null) {
            g.drawString(mc.font,
                    width < 170 ? "Sem missão ativa" : "Nenhuma missão acompanhada",
                    x + 9, top + 32, LuxGuiStyle.MUTED_TEXT, false);
            return;
        }
        g.drawString(mc.font, elide(current.title(), width < 170 ? 18 : 27),
                x + 9, top + 32, LuxGuiStyle.AMBER, true);
        LuxGuiStyle.icon(g, "quest_check", x + 9, top + 48, 10, 10);
        g.drawString(mc.font, elide(current.objective(), width < 170 ? 17 : 24),
                x + 23, top + 49, LuxGuiStyle.MUTED_TEXT, false);
        LuxGuiStyle.bar(g, x + 9, top + 64, width - 18, 6,
                current.done(), current.total(), LuxGuiStyle.TOP_ACCENT);
        String count = current.done() + "/" + current.total();
        g.drawString(mc.font, count, x + width - 10 - mc.font.width(count),
                top + 72, LuxGuiStyle.AMBER, false);
    }

    private static String elide(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}
