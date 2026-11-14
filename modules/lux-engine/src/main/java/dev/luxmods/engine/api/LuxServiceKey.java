package dev.luxmods.engine.api;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Identificador tipado e namespaced para serviços oferecidos por módulos Lux.
 * Exemplo: "lux_atlas:waypoints" com uma interface Waypoints.class.
 *
 * O contrato Java deve ser compartilhado via uma API pública versionada.
 */
public record LuxServiceKey<T>(String id, Class<T> contract) {
    private static final Pattern ID_PATTERN =
            Pattern.compile("[a-z][a-z0-9_]*:[a-z][a-z0-9_./-]*");

    public LuxServiceKey {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(contract, "contract");
        if (!ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Identificador de serviço inválido: " + id);
        }
        if (contract.isPrimitive()) {
            throw new IllegalArgumentException("Contrato deve ser uma interface/classe de referência");
        }
    }
}
