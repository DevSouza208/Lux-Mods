package dev.luxmods.engine.api;

/** Read-only client snapshot. Purchases and all XP deductions run server-side. */
public final class LuxUpgradeState {
    private static final int[] RANKS = new int[LuxUpgradeRules.PERKS];
    private static boolean received;
    private static int xpLevel;
    private static int characterLevel = 1;
    private static int revision;

    private LuxUpgradeState() {}

    public static void receive(int xp, int level, int health, int strength, int speed, int armor) {
        xpLevel = Math.max(0, xp);
        characterLevel = LuxUpgradeRules.clampLevel(level);
        RANKS[0] = LuxUpgradeRules.clampRank(health);
        RANKS[1] = LuxUpgradeRules.clampRank(strength);
        RANKS[2] = LuxUpgradeRules.clampRank(speed);
        RANKS[3] = LuxUpgradeRules.clampRank(armor);
        received = true;
        revision++;
    }

    public static void clear() {
        received = false;
        xpLevel = 0;
        characterLevel = 1;
        java.util.Arrays.fill(RANKS, 0);
        revision++;
    }

    public static boolean received() { return received; }
    public static int xpLevel() { return xpLevel; }
    public static int characterLevel() { return characterLevel; }
    public static int revision() { return revision; }
    public static int rank(int perk) {
        return perk < 0 || perk >= RANKS.length ? 0 : RANKS[perk];
    }
    public static int price(int perk) {
        return perk < 0 || perk >= RANKS.length ? -1
                : LuxUpgradeRules.upgradeCost(perk, RANKS[perk], characterLevel);
    }
    public static boolean canBuy(int perk) {
        int cost = price(perk);
        return received && cost >= 0 && xpLevel >= cost;
    }
    public static boolean stageComplete() {
        return LuxUpgradeRules.stageComplete(characterLevel, RANKS);
    }
    public static int promotionCost() {
        return LuxUpgradeRules.promotionCost(characterLevel, RANKS);
    }
    public static boolean canPromote() {
        int cost = promotionCost();
        return received && cost >= 0 && xpLevel >= cost;
    }
}
