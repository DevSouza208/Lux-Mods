package dev.luxmods.engine.api;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class LuxEventBusDepthTest {
    @Test
    void capsRecursivePublicationWithoutBreakingOtherObservers() {
        LuxEventBus bus = new LuxEventBus();
        LuxEventType<String> type = new LuxEventType<>("lux_test:recursive", String.class);
        AtomicInteger otherObservers = new AtomicInteger();

        bus.subscribe(type, payload -> bus.publish(type, payload));
        bus.subscribe(type, payload -> otherObservers.incrementAndGet());

        assertEquals(2, bus.publish(type, "discovered"));
        assertEquals(16, otherObservers.get(), "Should execute other observer at every allowed depth");

        // ThreadLocal depth must be cleared after nested execution.
        assertEquals(2, bus.publish(type, "discovered-again"));
        assertEquals(32, otherObservers.get());
    }
}
