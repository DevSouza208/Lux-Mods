import dev.luxmods.engine.api.LuxServiceKey;
import dev.luxmods.engine.api.LuxServiceRegistry;
import dev.luxmods.engine.api.LuxEventBus;
import dev.luxmods.discovery.DiscoveryDebouncer;
import dev.luxmods.discovery.DiscoveryLedger;
import dev.luxmods.discovery.LuxDiscoveryEvents;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/** Teste sem Gradle ou Minecraft, executável somente com JDK 21. */
public final class LuxSmokeMain {
    interface Compass { int direction(); }

    public static void main(String[] args) {
        LuxServiceRegistry registry = new LuxServiceRegistry();
        LuxServiceKey<Compass> key = new LuxServiceKey<>("lux_atlas:compass", Compass.class);
        check(registry.find(key).isEmpty(), "serviço ausente");
        registry.register(key, () -> 180);
        check(registry.find(key).orElseThrow().direction() == 180, "registro tipado");

        boolean duplicateRejected = false;
        try { registry.register(key, () -> 90); }
        catch (IllegalStateException expected) { duplicateRejected = true; }
        check(duplicateRejected, "registro duplicado deveria falhar");

        DiscoveryDebouncer d = new DiscoveryDebouncer();
        check(d.tick("overworld", "plains", 3, 4).isEmpty(), "espera 3 ticks");
        check(d.tick("overworld", "plains", 3, 4).isEmpty(), "espera");
        check("plains".equals(d.tick("overworld", "plains", 3, 4).orElseThrow()),
                "primeiro título após estabilidade");
        check(d.tick("overworld", "plains", 3, 4).isEmpty(), "não repete");

        var first = DiscoveryLedger.mark(Set.of(), "overworld", "plains");
        check(first.firstVisit(), "primeiro registro deve ser inédito");
        var second = DiscoveryLedger.mark(first.saved(), "overworld", "plains");
        check(!second.firstVisit(), "relog não pode repetir bioma salvo");
        check(second.saved().size() == 1, "não duplicar itens salvos");
        var third = DiscoveryLedger.mark(second.saved(), "the_nether", "plains");
        check(third.firstVisit() && third.saved().size() == 2, "dimensão distinta");

        var events = new LuxEventBus();
        var found = new LuxDiscoveryEvents.BiomeFirstDiscovered(
                UUID.randomUUID(), "minecraft:overworld", "minecraft:plains");
        check(events.publish(LuxDiscoveryEvents.FIRST_BIOME, found) == 0, "sem listeners");
        AtomicInteger seen = new AtomicInteger();
        var subscription = events.subscribe(LuxDiscoveryEvents.FIRST_BIOME, payload ->
                seen.incrementAndGet());
        check(events.publish(LuxDiscoveryEvents.FIRST_BIOME, found) == 1,
                "evento deveria ter um listener");
        check(seen.get() == 1, "listener processou uma vez");
        subscription.close();
        check(events.publish(LuxDiscoveryEvents.FIRST_BIOME, found) == 0,
                "listener removido");
        check(seen.get() == 1, "unsubscribe não pode processar");

        System.out.println("LUX SMOKE PASSOU: services, discovery ledger e events");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
