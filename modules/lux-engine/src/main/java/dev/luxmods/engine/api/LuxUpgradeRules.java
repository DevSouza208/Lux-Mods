package dev.luxmods.engine.api;

/**
 * Shared, side-effect-free progression rules. The Minecraft XP bar remains
 * vanilla; RPG character level is independent and purchased after completing
 * the four upgrades of the current character level.
 */
public final class LuxUpgradeRules {
    public static final int MAX_LEVEL = 20;
    public static final int PERKS = 4;
    public static final int PROMOTE = 4;
    private static final int[] BASE_COST = {10, 12, 11, 13};

    private LuxUpgradeRules() {}

    public static int clampLevel(int value) {
        return Math.max(1, Math.min(MAX_LEVEL, value));
    }

    public static int clampRank(int value) {
        return Math.max(0, Math.min(MAX_LEVEL, value));
    }

    /** Each new upgrade rank costs one XP level more, not +12 at once. */
    public static int upgradeCost(int perk, int rank, int characterLevel) {
        if (perk < 0 || perk >= PERKS || rank < 0 || rank >= MAX_LEVEL
                || rank >= clampLevel(characterLevel)) return -1;
        return BASE_COST[perk] + rank;
    }

    public static boolean stageComplete(int characterLevel, int[] ranks) {
        if (ranks == null || ranks.length != PERKS) return false;
        int needed = clampLevel(characterLevel);
        for (int rank : ranks) if (clampRank(rank) < needed) return false;
        return true;
    }

    /** Promotion also consumes real XP, starting at 15 and growing by one. */
    public static int promotionCost(int characterLevel, int[] ranks) {
        if (characterLevel < 1 || characterLevel >= MAX_LEVEL
                || !stageComplete(characterLevel, ranks)) return -1;
        return 14 + characterLevel;
    }
}
