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
 * A failing listener is logged and cannot stop other listeners or the gameplay
 * transaction. Callbacks are observers: they must be short and side-correct.
 */
public final class LuxEventBus {
    private static final System.Logger LOGGER = System.getLogger(LuxEventBus.class.getName());
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

    /**
     * @return number of observers successfully invoked. Listener failures
     * are logged and do not abort the remaining listeners.
     */
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
            try {
                listener.accept(event);
                invoked++;
            } catch (RuntimeException exception) {
                LOGGER.log(System.Logger.Level.ERROR, "Lux event handler failed: " + type.id(), exception);
            }
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
