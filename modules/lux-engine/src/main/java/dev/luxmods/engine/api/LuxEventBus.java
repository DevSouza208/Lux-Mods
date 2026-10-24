package dev.luxmods.engine.api;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Synchronous event fan-out, separate from the NeoForge lifecycle/event bus.
 * The calling thread must be the correct logical side (normally server thread).
 * Publish only AFTER an authoritative game-state transaction was committed.
 * Listener exceptions propagate to the publisher; listeners must not mutate
 * game state unless the publisher explicitly permits this.
 */
public final class LuxEventBus {
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    public interface Subscription extends AutoCloseable {
        @Override void close();
    }

    public <T> Subscription subscribe(LuxEventType<T> type, Consumer<? super T> listener) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(listener, "listener");
        Channel channel = channelFor(type);
        Consumer<Object> bridge = value -> listener.accept(type.payloadType().cast(value));
        channel.listeners.add(bridge);
        return () -> channel.listeners.remove(bridge);
    }

    public <T> int publish(LuxEventType<T> type, T event) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(event, "event");
        if (!type.payloadType().isInstance(event)) {
            throw new IllegalArgumentException("Incorrect payload for " + type.id());
        }
        Channel channel = channels.get(type.id());
        if (channel == null) return 0;
        verifyContract(type, channel);
        int invoked = 0;
        for (Consumer<Object> listener : channel.listeners) {
            listener.accept(event);
            invoked++;
        }
        return invoked;
    }

    private <T> Channel channelFor(LuxEventType<T> type) {
        return channels.compute(type.id(), (id, existing) -> {
            if (existing == null) return new Channel(type.payloadType());
            verifyContract(type, existing);
            return existing;
        });
    }

    private static void verifyContract(LuxEventType<?> type, Channel channel) {
        if (!type.payloadType().equals(channel.payloadType)) {
            throw new IllegalStateException("Incompatible Lux event contract: " + type.id());
        }
    }

    private static final class Channel {
        private final Class<?> payloadType;
        private final CopyOnWriteArrayList<Consumer<Object>> listeners = new CopyOnWriteArrayList<>();

        private Channel(Class<?> payloadType) {
            this.payloadType = payloadType;
        }
    }
}
