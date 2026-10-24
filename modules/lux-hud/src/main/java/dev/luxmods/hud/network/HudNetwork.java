package dev.luxmods.hud.network;

import dev.luxmods.hud.client.HudPartyClientState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Dedicated servers register the codec but never load client rendering code. */
public final class HudNetwork {
    private HudNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                HudPartySnapshotPayload.TYPE,
                HudPartySnapshotPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ClientHandler.receive(payload.data());
                    }
                }
        );
    }

    private static final class ClientHandler {
        private static void receive(String data) {
            HudPartyClientState.update(data);
        }
    }
}
