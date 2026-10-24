package dev.luxmods.engine.api;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class LuxEventBusTest {
    record Found(String biome) { }

    @Test
    void publishesTypedEventsAndSupportsUnsubscribe() {
        var bus = new LuxEventBus();
        var event = new LuxEventType<>("lux_discovery:first_biome", Found.class);
        assertEquals(0, bus.publish(event, new Found("minecraft:plains")));

        AtomicInteger received = new AtomicInteger();
        var first = bus.subscribe(event, payload -> received.incrementAndGet());
        var second = bus.subscribe(event, payload -> received.addAndGet(2));
        assertEquals(2, bus.publish(event, new Found("minecraft:plains")));
        assertEquals(3, received.get());

        first.close();
        assertEquals(1, bus.publish(event, new Found("minecraft:forest")));
        assertEquals(5, received.get());
        second.close();
        assertEquals(0, bus.publish(event, new Found("minecraft:plains")));
    }

    @Test
    void rejectsDuplicateIdsWithIncompatibleContract() {
        var bus = new LuxEventBus();
        bus.subscribe(new LuxEventType<>("lux_discovery:first_biome", Found.class), ignored -> {});
        assertThrows(IllegalStateException.class, () ->
                bus.publish(new LuxEventType<>("lux_discovery:first_biome", String.class), "wrong"));
        assertThrows(IllegalStateException.class, () ->
                bus.subscribe(new LuxEventType<>("lux_discovery:first_biome", String.class), ignored -> {}));
        assertThrows(IllegalArgumentException.class, () ->
                new LuxEventType<>("INVALID NAME", Found.class));
    }
}
