package dev.luxmods.discovery.network;

import dev.luxmods.discovery.client.DiscoveryClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Registra o payload em ambos os lados físicos, sem carregar classes do
 * Minecraft.client no servidor dedicado. A notificação visual só roda no cliente.
 */
public final class DiscoveryNetwork {
    private DiscoveryNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(
                BiomeDiscoveredPayload.TYPE,
                BiomeDiscoveredPayload.STREAM_CODEC,
                (payload, context) -> {
                    // O payload também precisa ser conhecido pelo servidor dedicado.
                    // O holder interno de ClientHandler não é carregado lá.
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ClientHandler.handle(payload, context);
                    }
                }
        );
        registrar.playToClient(StructureDiscoveredPayload.TYPE,
                StructureDiscoveredPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) ClientHandler.handleStructure(payload, context);
                });
    }

    /** Separação deliberada: a JVM só resolve esta classe no cliente físico. */
    private static final class ClientHandler {
        private static void handleStructure(StructureDiscoveredPayload payload,
                net.neoforged.neoforge.network.handling.IPayloadContext context) {
            DiscoveryClientEvents.handleStructure(payload, context);
        }

        private static void handle(
                BiomeDiscoveredPayload payload,
                net.neoforged.neoforge.network.handling.IPayloadContext context
        ) {
            DiscoveryClientEvents.handleBiome(payload, context);
        }
    }
}
