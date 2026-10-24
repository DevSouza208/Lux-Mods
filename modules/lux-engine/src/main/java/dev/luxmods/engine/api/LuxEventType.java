package dev.luxmods.engine.api;

import java.util.Objects;
import java.util.regex.Pattern;

/** A versionable namespaced event identifier with a Java payload contract. */
public record LuxEventType<T>(String id, Class<T> payloadType) {
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_]*:[a-z][a-z0-9_./-]*");

    public LuxEventType {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(payloadType, "payloadType");
        if (!ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid Lux event ID: " + id);
        }
        if (payloadType.isPrimitive()) {
            throw new IllegalArgumentException("Event payload type must be a reference type");
        }
    }
}
