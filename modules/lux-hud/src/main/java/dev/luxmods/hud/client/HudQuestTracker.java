package dev.luxmods.hud.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Quest status is always real; the decorative scroll icon is never a fake task. */
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
        int height = current == null ? 62 : 104;

        LuxGuiStyle.panel(g, x, top, width, height);

        // Scroll parchment with lavender gemstone seal; 14px internal padding.
        LuxGuiStyle.icon(g, "quest_scroll", x + 13, top + 9, 22, 22);
        g.drawString(mc.font, "MISSÕES", x + 42, top + 15,
                LuxGuiStyle.FOREGROUND, true);
        LuxGuiStyle.separator(g, x + 14, top + 35, width - 28);

        if (current == null) {
            g.drawString(mc.font, width < 175 ? "Sem missão ativa" : "Sem missão acompanhada",
                    x + 17, top + 44, LuxGuiStyle.MUTED_TEXT, false);
            return;
        }
        g.drawString(mc.font, elide(current.title(), width < 175 ? 16 : 25),
                x + 17, top + 45, LuxGuiStyle.AMBER, true);
        LuxGuiStyle.icon(g, "quest_check", x + 16, top + 64, 10, 10);
        g.drawString(mc.font, elide(current.objective(), width < 175 ? 15 : 23),
                x + 31, top + 65, LuxGuiStyle.MUTED_TEXT, false);
        LuxGuiStyle.bar(g, x + 16, top + 81, width - 32, 7,
                current.done(), current.total(), LuxGuiStyle.TOP_ACCENT);
        String count = current.done() + "/" + current.total();
        g.drawString(mc.font, count, x + width - 17 - mc.font.width(count),
                top + 92, LuxGuiStyle.AMBER, false);
    }

    private static String elide(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}
