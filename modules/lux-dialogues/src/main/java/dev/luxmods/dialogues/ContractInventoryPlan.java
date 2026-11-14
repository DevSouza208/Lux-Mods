package dev.luxmods.dialogues;

/**
 * Pure Java inventory removal plan. Never remove an item unless the full
 * contract quantity is available; the caller applies the plan server-side.
 */
public final class ContractInventoryPlan {
    private ContractInventoryPlan() {}

    /** Returns per-slot removal amounts, or null if the request cannot be fulfilled. */
    public static int[] forCounts(int[] counts, int quantity) {
        if (counts == null || quantity <= 0) return null;
        int[] removals = new int[counts.length];
        int remaining = quantity;
        for (int slot = 0; slot < counts.length; slot++) {
            if (counts[slot] < 0) return null;
            int amount = Math.min(counts[slot], remaining);
            removals[slot] = amount;
            remaining -= amount;
        }
        return remaining == 0 ? removals : null;
    }

    /** Verify that the live inventory still matches the planned removals. */
    public static boolean canApply(int[] available, int[] removals, int quantity) {
        if (available == null || removals == null
                || available.length != removals.length || quantity <= 0) return false;
        long total = 0;
        for (int i = 0; i < available.length; i++) {
            if (available[i] < 0 || removals[i] < 0 || removals[i] > available[i]) return false;
            total += removals[i];
        }
        return total == quantity;
    }
}
