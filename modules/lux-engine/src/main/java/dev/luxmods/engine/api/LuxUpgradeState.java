package dev.luxmods.engine.api;

/** Read-only client snapshot: the server spends XP and owns the attribute ranks. */
public final class LuxUpgradeState {
    private static final int[] BASE = {15, 20, 18, 25};
    private static final int[] RANKS = new int[4];
    private static boolean received;
    private static int xpLevel;
    private static int revision;

    private LuxUpgradeState() {}

    public static void receive(int level, int health, int strength, int speed, int armor) {
        xpLevel = Math.max(0, level);
        RANKS[0] = clamp(health);
        RANKS[1] = clamp(strength);
        RANKS[2] = clamp(speed);
        RANKS[3] = clamp(armor);
        received = true;
        revision++;
    }

    private static int clamp(int rank) { return Math.max(0, Math.min(3, rank)); }

    public static void clear() {
        received = false;
        xpLevel = 0;
        java.util.Arrays.fill(RANKS, 0);
        revision++;
    }

    public static boolean received() { return received; }
    public static int xpLevel() { return xpLevel; }
    public static int revision() { return revision; }
    public static int rank(int perk) { return perk < 0 || perk >= 4 ? 0 : RANKS[perk]; }
    public static int price(int perk) {
        return perk < 0 || perk >= 4 || RANKS[perk] >= 3 ? -1
                : BASE[perk] + 12 * RANKS[perk];
    }
    public static boolean canBuy(int perk) {
        int cost = price(perk);
        return received && cost > 0 && xpLevel >= cost;
    }
}
