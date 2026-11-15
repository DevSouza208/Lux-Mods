package dev.luxmods.hud.client;

import dev.luxmods.engine.api.LuxIntroQuestState;

/** Quest status is always real; the decorative scroll icon is never a fake task. */
public final class HudQuestTracker {
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

    /** Called exclusively from the server snapshot; no fabricated client quests. */
    public static void updateStage(int stage, int progress) {
        LuxIntroQuestState.receive(stage, progress);
        if (stage <= 0) {
            track("Primeiros troncos", "Coletar madeira", progress, 16);
        } else if (stage == 1) {
            track("Pedras para construir", "Coletar pedra", progress, 16);
        } else {
            track("Primeiros passos", "2 missões concluídas", 2, 2);
        }
    }

    public static void track(String title, String objective, int done, int total) {
        tracked = new Quest(title, objective, done, total);
    }

    public static void clear() {
        tracked = null;
        LuxIntroQuestState.clear();
    }

    private static String trim(String text, int limit) {
        String safe = text == null ? "" : text.strip();
        return safe.length() <= limit ? safe : safe.substring(0, limit - 3) + "...";
    }

}
