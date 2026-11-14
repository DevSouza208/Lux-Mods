import dev.luxmods.engine.api.LuxQuestPage;
import java.util.List;

public final class LuxQuestPageSmoke {
    private static void check(boolean valid) {
        if (!valid) throw new AssertionError("Quest pager");
    }

    public static void main(String[] args) {
        var quests = List.of("a", "b", "c", "d", "e", "f", "g");
        check(LuxQuestPage.pages(0, 3) == 1);
        check(LuxQuestPage.pages(7, 3) == 3);
        check(LuxQuestPage.clamp(-10, 7, 3) == 0);
        check(LuxQuestPage.clamp(100, 7, 3) == 2);
        check(LuxQuestPage.slice(quests, 0, 3).equals(List.of("a", "b", "c")));
        check(LuxQuestPage.slice(quests, 1, 3).equals(List.of("d", "e", "f")));
        check(LuxQuestPage.slice(quests, 2, 3).equals(List.of("g")));
        check(LuxQuestPage.slice(quests, 100, 3).equals(List.of("g")));
        check(LuxQuestPage.slice(List.of(), 50, 3).isEmpty());
        try {
            LuxQuestPage.pages(1, 0);
            throw new AssertionError("Should reject pageSize=0");
        } catch (IllegalArgumentException expected) { }
        System.out.println("Lux quest pagination smoke OK");
    }
}
