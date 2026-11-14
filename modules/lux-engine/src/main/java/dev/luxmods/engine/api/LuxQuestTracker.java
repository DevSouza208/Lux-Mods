package dev.luxmods.engine.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Client-only, session-scoped read model for NPC contracts observed via
 * server snapshots. No authority: accepting/completing remains server-side.
 * Stored in Engine so Atlas and Dialogues have no hard dependency.
 */
public final class LuxQuestTracker {
    private static final int LIMIT = 128;
    private static final Map<UUID, Contract> KNOWN = new LinkedHashMap<>();
    private LuxQuestTracker() {}

    public record Contract(UUID npc, String dimension, int x, int z,
                           String objective, int quantity, long expiresDay,
                           int status) {
        public Contract {
            Objects.requireNonNull(npc, "npc");
            Objects.requireNonNull(dimension, "dimension");
            Objects.requireNonNull(objective, "objective");
            if (dimension.isBlank() || objective.isBlank() || quantity < 1)
                throw new IllegalArgumentException("Invalid quest snapshot");
        }
    }

    /** Only player-owned accepted or review-required contracts are tracked. */
    public static synchronized void observe(Contract value, boolean mine) {
        Objects.requireNonNull(value, "value");
        if (!mine || (value.status() != 1 && value.status() != 4)) {
            KNOWN.remove(value.npc());
            return;
        }
        KNOWN.remove(value.npc());
        KNOWN.put(value.npc(), value);
        if (KNOWN.size() > LIMIT) KNOWN.remove(KNOWN.keySet().iterator().next());
    }

    public static synchronized List<Contract> inDimension(String dimension) {
        List<Contract> result = new ArrayList<>();
        for (Contract contract : KNOWN.values())
            if (contract.dimension().equals(dimension)) result.add(contract);
        return List.copyOf(result);
    }

    /** Nearest-first for the compact Atlas mission panel; avoids overflow hiding local quests. */
    public static synchronized List<Contract> nearest(String dimension, int playerX, int playerZ) {
        List<Contract> result = new ArrayList<>(inDimension(dimension));
        result.sort(Comparator
                .comparingDouble((Contract contract) -> distanceSquared(contract, playerX, playerZ))
                .thenComparing(contract -> contract.npc().toString()));
        return List.copyOf(result);
    }

    private static double distanceSquared(Contract contract, int x, int z) {
        // Integer-coordinate extremes can overflow even 64-bit squared sums.
        double dx = (double) contract.x() - x;
        double dz = (double) contract.z() - z;
        return dx * dx + dz * dz;
    }

    public static synchronized List<Contract> all() {
        return List.copyOf(KNOWN.values());
    }

    /** Clear on disconnect to prevent leaking missions between worlds/users. */
    public static synchronized void clear() {
        KNOWN.clear();
    }
}
