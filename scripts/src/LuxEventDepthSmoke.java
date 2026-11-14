import dev.luxmods.engine.api.LuxEventBus;
import dev.luxmods.engine.api.LuxEventType;
import java.util.concurrent.atomic.AtomicInteger;

/** Regression against plugins recursively republishing the same event forever. */
public final class LuxEventDepthSmoke {
    public static void main(String[] args) {
        LuxEventBus bus = new LuxEventBus();
        LuxEventType<String> kind = new LuxEventType<>("lux_test:recursive", String.class);
        AtomicInteger counted = new AtomicInteger();
        bus.subscribe(kind, event -> bus.publish(kind, event));
        bus.subscribe(kind, event -> counted.incrementAndGet());
        int success = bus.publish(kind, "test");
        if (success != 2 || counted.get() != 16) {
            throw new AssertionError("Depth cap invalid: success=" + success
                    + ", count=" + counted.get());
        }
        if (bus.publish(new LuxEventType<>("lux_test:unused", String.class), "test") != 0) {
            throw new AssertionError("Unsubscribed event should reach zero listeners");
        }
        System.out.println("LUX EVENT DEPTH PASSOU: 16 callbacks, sem recursao infinita");
    }
}
