package dev.luxmods.discovery;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Controla fronteiras de bioma sem APIs Minecraft e sem side-effects de render.
 * Nesta primeira entrega, o conjunto de descobertas vale só para a sessão.
 */
public final class DiscoveryDebouncer {
    private final Set<String> discovered = new HashSet<>();
    private String candidate;
    private int candidateAge;
    private int sinceLastTitle = Integer.MAX_VALUE;

    public void reset() {
        discovered.clear();
        candidate = null;
        candidateAge = 0;
        sinceLastTitle = Integer.MAX_VALUE;
    }

    /**
     * @return ID de bioma elegível para um título, caso as restrições permitam.
     */
    public Optional<String> tick(String dimensionId, String biomeId, int stableTicks, int cooldownTicks) {
        Objects.requireNonNull(dimensionId, "dimensionId");
        Objects.requireNonNull(biomeId, "biomeId");
        if (stableTicks < 1 || cooldownTicks < 0) {
            throw new IllegalArgumentException("Intervalos inválidos");
        }

        String key = dimensionId + "|" + biomeId;
        if (sinceLastTitle < Integer.MAX_VALUE) sinceLastTitle++;

        if (!key.equals(candidate)) {
            candidate = key;
            candidateAge = 1;
        } else if (candidateAge < stableTicks) {
            candidateAge++;
        }
        if (candidateAge < stableTicks || sinceLastTitle < cooldownTicks || discovered.contains(key)) {
            return Optional.empty();
        }
        discovered.add(key);
        sinceLastTitle = 0;
        return Optional.of(biomeId);
    }
}
