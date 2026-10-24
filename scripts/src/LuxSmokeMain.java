import dev.luxmods.engine.api.LuxServiceKey;
import dev.luxmods.engine.api.LuxServiceRegistry;
import dev.luxmods.discovery.DiscoveryDebouncer;

/**
 * Smoke check executável com apenas JDK 21: não requer Gradle nem Minecraft.
 * Usa exceptions explícitas, não depende da flag -ea.
 */
public final class LuxSmokeMain {
    interface Compass {
        int direction();
    }

    public static void main(String[] args) {
        LuxServiceRegistry registry = new LuxServiceRegistry();
        LuxServiceKey<Compass> key = new LuxServiceKey<>("lux_atlas:compass", Compass.class);
        check(registry.find(key).isEmpty(), "serviço ausente deveria retornar Optional.empty");
        registry.register(key, () -> 180);
        check(registry.find(key).orElseThrow().direction() == 180, "registro tipado");

        boolean duplicateRejected = false;
        try {
            registry.register(key, () -> 90);
        } catch (IllegalStateException expected) {
            duplicateRejected = true;
        }
        check(duplicateRejected, "registro duplicado deveria falhar");

        DiscoveryDebouncer d = new DiscoveryDebouncer();
        check(d.tick("overworld", "plains", 3, 4).isEmpty(), "deve esperar 3 ticks");
        check(d.tick("overworld", "plains", 3, 4).isEmpty(), "deve esperar");
        check("plains".equals(d.tick("overworld", "plains", 3, 4).orElseThrow()),
                "deve anunciar após estabilidade");
        check(d.tick("overworld", "plains", 3, 4).isEmpty(), "não duplica em mesmo bioma");
        check(d.tick("the_nether", "plains", 1, 4).isEmpty(), "cooldown respeitado");
        check(d.tick("the_nether", "plains", 1, 4).isEmpty(), "cooldown respeitado");
        check("plains".equals(d.tick("the_nether", "plains", 1, 4).orElseThrow()),
                "dimensão tem descoberta própria");
        check(d.tick("overworld", "plains", 1, 4).isEmpty(),
                "voltar dimensão não deve duplicar descoberta");
        d.reset();
        check("plains".equals(d.tick("overworld", "plains", 1, 4).orElseThrow()),
                "nova sessão deve permitir descobrir novamente");

        System.out.println("LUX SMOKE PASSOU — service registry + discovery debounce/cooldown");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
