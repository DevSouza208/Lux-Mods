package dev.luxmods.engine.api;

/**
 * Client display only. All upgrades and earned points are authoritative on the
 * server. Do not apply attributes or deduct points in this class.
 */
public final class LuxUpgradeState {
    private static boolean received;
    private static int highestLevel;
    private static boolean health;
    private static boolean speed;

    private LuxUpgradeState() {}

    public static void receive(int level, boolean healthUnlocked, boolean speedUnlocked) {
        highestLevel = Math.max(0, level);
        health = healthUnlocked;
        speed = speedUnlocked;
        received = true;
    }

    public static void clear() {
        received = false;
        highestLevel = 0;
        health = false;
        speed = false;
    }

    public static boolean received() { return received; }
    public static int highestLevel() { return highestLevel; }
    public static boolean health() { return health; }
    public static boolean speed() { return speed; }
    public static int earned() { return Math.min(2, highestLevel / 5); }
    public static int available() { return Math.max(0, earned() - (health ? 1 : 0) - (speed ? 1 : 0)); }
}
