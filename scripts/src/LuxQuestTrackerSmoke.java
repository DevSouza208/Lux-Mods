import dev.luxmods.engine.api.LuxQuestTracker;
import java.util.UUID;

public final class LuxQuestTrackerSmoke {
    private static void check(boolean ok) {
        if (!ok) throw new AssertionError("Lux quest tracker");
    }
    public static void main(String[] args) {
        UUID a = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID b = UUID.fromString("00000000-0000-0000-0000-000000000002");
        LuxQuestTracker.clear();
        var first = new LuxQuestTracker.Contract(a, "minecraft:overworld", 10, -20,
                "minecraft:coal", 24, 30, 1);
        var second = new LuxQuestTracker.Contract(b, "minecraft:the_nether", 20, 30,
                "minecraft:bread", 12, 31, 4);
        LuxQuestTracker.observe(first, true);
        LuxQuestTracker.observe(second, true);
        check(LuxQuestTracker.all().size() == 2);
        check(LuxQuestTracker.inDimension("minecraft:overworld").size() == 1);
        check(LuxQuestTracker.inDimension("minecraft:the_nether").get(0).status() == 4);
        LuxQuestTracker.observe(new LuxQuestTracker.Contract(a, "minecraft:overworld", 10, -20,
                "minecraft:coal", 24, 30, 2), true);
        check(LuxQuestTracker.all().size() == 1);
        LuxQuestTracker.observe(second, false);
        check(LuxQuestTracker.all().isEmpty());
        LuxQuestTracker.observe(first, true);
        LuxQuestTracker.clear();
        check(LuxQuestTracker.all().isEmpty());
        System.out.println("Lux Quest Tracker smoke OK");
    }
}
