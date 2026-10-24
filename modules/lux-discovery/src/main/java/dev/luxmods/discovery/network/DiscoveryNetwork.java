package dev.luxmods.discovery.network;

import dev.luxmods.discovery.client.DiscoveryClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Registro de payload servidor → cliente, com guarda explícita de lado físico.
 * Evita resolver renderizadores quando o servidor dedicado carrega o mod.
 */
public final class DiscoveryNetwork {
    private DiscoveryNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                BiomeDiscoveredPayload.TYPE,
                BiomeDiscoveredPayload.STREAM_CODEC,
                (payload, context) -> DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT, () -> () -> DiscoveryClientEvents.handleBiome(payload, context))
        );
    }
}
