import dev.luxmods.dialogues.ContractInventoryPlan;
import java.util.Arrays;

/** Runs without Minecraft, Gradle or GitHub Actions. */
public final class LuxContractInventorySmoke {
    private LuxContractInventorySmoke() {}

    private static void check(boolean condition) {
        if (!condition) throw new AssertionError("contract inventory plan");
    }

    public static void main(String[] args) {
        int[] plan = ContractInventoryPlan.forCounts(new int[] {4, 0, 7, 20}, 16);
        check(Arrays.equals(plan, new int[] {4, 0, 7, 5}));
        check(ContractInventoryPlan.canApply(new int[] {4, 0, 7, 20}, plan, 16));
        check(!ContractInventoryPlan.canApply(new int[] {4, 0, 7, 4}, plan, 16));
        check(ContractInventoryPlan.forCounts(new int[] {2, 3}, 6) == null);
        check(ContractInventoryPlan.forCounts(new int[] {2, -1, 10}, 4) == null);
        check(ContractInventoryPlan.forCounts(new int[] {1}, 0) == null);
        check(ContractInventoryPlan.forCounts(null, 4) == null);
        check(!ContractInventoryPlan.canApply(new int[] {3}, new int[] {4}, 4));
        check(!ContractInventoryPlan.canApply(new int[] {3}, new int[] {2}, 3));
        check(!ContractInventoryPlan.canApply(new int[] {3, 1}, new int[] {1}, 1));
        check(ContractInventoryPlan.canApply(new int[] {8}, new int[] {8}, 8));
        System.out.println("Lux contract inventory plan smoke OK");
    }
}
