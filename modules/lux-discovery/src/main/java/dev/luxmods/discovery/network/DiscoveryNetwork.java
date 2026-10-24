package dev.luxmods.discovery.network;

import dev.luxmods.discovery.client.DiscoveryClientEvents;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Registro somente de payload servidor → cliente. Não aceita dados de descoberta do cliente. */
public final class DiscoveryNetwork {
    private DiscoveryNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                BiomeDiscoveredPayload.TYPE,
                BiomeDiscoveredPayload.STREAM_CODEC,
                DiscoveryClientEvents::handleBiome
        );
    }
}
