package dev.luxmods.engine.api;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Synchronous, local fan-out of typed events. Only publish committed game facts
 * on the owning logical thread (normally the Minecraft server thread).
 * A failing observer does not interrupt other observers or undo committed data.
 * Recursive event chains have a hard depth cap to prevent runaway addon loops.
 */
public final class LuxEventBus {
    private static final System.Logger LOGGER = System.getLogger(LuxEventBus.class.getName());
    private static final int MAX_PUBLISH_DEPTH = 16;

    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();
    private final ThreadLocal<Integer> publishDepth = ThreadLocal.withInitial(() -> 0);

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
     * Returns the number of listeners successfully invoked. Exceptions in
     * individual observers are logged without aborting subsequent observers.
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

        int previousDepth = publishDepth.get();
        if (previousDepth >= MAX_PUBLISH_DEPTH) {
            throw new IllegalStateException("Lux event recursion limit exceeded: " + type.id());
        }
        publishDepth.set(previousDepth + 1);
        try {
            int invoked = 0;
            for (Consumer<Object> listener : channel.listeners) {
                try {
                    listener.accept(event);
                    invoked++;
                } catch (RuntimeException exception) {
                    LOGGER.log(System.Logger.Level.ERROR,
                            "Lux event handler failed: " + type.id(), exception);
                }
            }
            return invoked;
        } finally {
            if (previousDepth == 0) publishDepth.remove();
            else publishDepth.set(previousDepth);
        }
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
        private Channel(Class<?> payloadType) { this.payloadType = payloadType; }
    }
}
