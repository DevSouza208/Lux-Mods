package dev.luxmods.engine.api;

/**
 * Client-only copy of the two introductory quests, populated exclusively from
 * the existing HUD server snapshot. Never grants rewards or advances progress.
 */
public final class LuxIntroQuestState {
    private static int stage;
    private static int progress;
    private static boolean received;

    private LuxIntroQuestState() {}

    public static void receive(int serverStage, int serverProgress) {
        stage = Math.max(0, Math.min(2, serverStage));
        progress = stage >= 2 ? 16 : Math.max(0, Math.min(16, serverProgress));
        received = true;
    }

    public static void clear() {
        stage = 0;
        progress = 0;
        received = false;
    }

    public static boolean received() { return received; }
    public static int stage() { return stage; }
    public static int progress() { return progress; }
}
