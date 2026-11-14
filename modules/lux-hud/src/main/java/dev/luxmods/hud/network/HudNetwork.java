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
        var registrar = event.registrar("1");
        registrar.playToClient(
                HudPartySnapshotPayload.TYPE,
                HudPartySnapshotPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ClientHandler.receive(payload.data());
                    }
                }
        );
        registrar.playToClient(
                HudQuestSnapshotPayload.TYPE,
                HudQuestSnapshotPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        QuestClientHandler.receive(payload.stage(), payload.progress());
                    }
                }
        );
        registrar.playToClient(
                HudSleepSnapshotPayload.TYPE,
                HudSleepSnapshotPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        SleepClientHandler.receive(payload.awakeTicks(), payload.faintTicks());
                    }
                }
        );
    }

    private static final class SleepClientHandler {
        private static void receive(int awakeTicks, int faintTicks) {
            dev.luxmods.hud.client.HudSleepClientState.receive(awakeTicks, faintTicks);
        }
    }

    private static final class QuestClientHandler {
        private static void receive(int stage, int progress) {
            dev.luxmods.hud.client.HudQuestTracker.updateStage(stage, progress);
        }
    }

    private static final class ClientHandler {
        private static void receive(String data) {
            HudPartyClientState.update(data);
        }
    }
}
