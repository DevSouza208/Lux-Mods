package dev.luxmods.discovery;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Regra pura de deduplicação de descobertas; sem acesso a Minecraft ou IO.
 * Usar resultado imutável em setData para persistir alterações corretamente.
 */
public final class DiscoveryLedger {
    private DiscoveryLedger() {
    }

    public record MarkResult(Set<String> saved, boolean firstVisit) {
    }

    public static MarkResult mark(Set<String> saved, String dimensionId, String biomeId) {
        Objects.requireNonNull(saved, "saved");
        Objects.requireNonNull(dimensionId, "dimensionId");
        Objects.requireNonNull(biomeId, "biomeId");

        String key = dimensionId + "|" + biomeId;
        if (saved.contains(key)) {
            return new MarkResult(saved, false);
        }
        Set<String> updated = new HashSet<>(saved);
        updated.add(key);
        return new MarkResult(Set.copyOf(updated), true);
    }
}
