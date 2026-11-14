package dev.luxmods.engine.api;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Registro mínimo de provedores opcionais.
 *
 * Regras:
 * - Um ID só pode ser registrado uma vez (falha explícita em duplicidade).
 * - Lookup sem provedor retorna Optional.empty() e não obriga instalar outro mod.
 * - Um ID consultado com classe diferente da registrada falha claramente.
 * - O servidor determina estado de jogo; serviços aqui não autorizam clientes.
 *
 * Este registro não substitui o barramento de eventos nem o ModList do NeoForge.
 */
public final class LuxServiceRegistry {
    private final ConcurrentMap<String, Entry> providers = new ConcurrentHashMap<>();

    public <T> void register(LuxServiceKey<T> key, T service) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(service, "service");
        if (!key.contract().isInstance(service)) {
            throw new IllegalArgumentException("Implementação incompatível com " + key.id());
        }
        var previous = providers.putIfAbsent(key.id(), new Entry(key.contract(), service));
        if (previous != null) {
            throw new IllegalStateException("Serviço já registrado: " + key.id());
        }
    }

    public <T> Optional<T> find(LuxServiceKey<T> key) {
        Objects.requireNonNull(key, "key");
        Entry entry = providers.get(key.id());
        if (entry == null) {
            return Optional.empty();
        }
        if (!key.contract().equals(entry.contract())) {
            throw new IllegalStateException("Contrato incompatível para " + key.id()
                    + " (registrado: " + entry.contract().getName() + ")");
        }
        return Optional.of(key.contract().cast(entry.service()));
    }

    public Set<String> registeredIds() {
        return Set.copyOf(providers.keySet());
    }

    private record Entry(Class<?> contract, Object service) {
    }
}
