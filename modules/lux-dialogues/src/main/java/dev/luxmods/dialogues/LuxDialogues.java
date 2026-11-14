package dev.luxmods.dialogues;

import dev.luxmods.dialogues.network.DialogueNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/** Vanilla villager interaction, with a client conversation and server-authorized shop. */
@Mod(LuxDialogues.MOD_ID)
public final class LuxDialogues {
    public static final String MOD_ID = "lux_dialogues";

    public LuxDialogues(IEventBus modBus, ModContainer container) {
        modBus.addListener(DialogueNetwork::register);
        NeoForge.EVENT_BUS.addListener(DialogueInteractions::onInteract);
        NeoForge.EVENT_BUS.addListener(DialogueSessions::onTick);
        NeoForge.EVENT_BUS.addListener(GuardDialogueSessions::onTick);
        if (FMLEnvironment.dist == Dist.CLIENT) ClientSetup.init();
    }

    private static final class ClientSetup {
        private static void init() {
            // UI types are kept away from dedicated server class loading.
            NeoForge.EVENT_BUS.addListener(dev.luxmods.dialogues.client.DialogueClient::onInteract);
        }
    }
}
