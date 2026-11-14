package dev.luxmods.dialogues.client;

import dev.luxmods.dialogues.DialogueNpcTypes;
import dev.luxmods.dialogues.network.DialogueNetwork;
import dev.luxmods.engine.api.LuxQuestTracker;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Optional Guard Villagers and vanilla villagers share the Lux dialogue screen. */
public final class DialogueClient {
    private DialogueClient() {}
    private static ClientLevel syncedLevel;

    /**
     * Login events may fire before the client has finished installing its level.
     * Request a replay once per loaded level, without polling every tick.
     */
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            syncedLevel = null;
            return;
        }
        if (mc.level != syncedLevel) {
            syncedLevel = mc.level;
            PacketDistributor.sendToServer(new DialogueNetwork.RequestQuestLedger());
        }
    }

    public static void onContractSnapshot(DialogueNetwork.ContractSnapshot snapshot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getEntity(snapshot.entityId()) instanceof Villager villager) {
            LuxQuestTracker.observe(new LuxQuestTracker.Contract(
                    villager.getUUID(), mc.level.dimension().location().toString(),
                    villager.getBlockX(), villager.getBlockZ(), snapshot.objective(),
                    snapshot.quantity(), snapshot.expiresDay(), snapshot.status()),
                    snapshot.mine());
        }
        if (mc.screen instanceof VillagerDialogueScreen screen) {
            screen.receiveContractSnapshot(snapshot);
        }
    }

    public static void onDeliveryResult(DialogueNetwork.ContractDeliveryResult result) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof VillagerDialogueScreen screen) {
            screen.receiveDeliveryResult(result);
        }
    }

    /** Restore only server-owned quests, never locally inferred acceptance. */
    public static void onQuestLedgerSync(DialogueNetwork.QuestLedgerSync packet) {
        LuxQuestTracker.clear();
        for (var entry : packet.entries()) {
            if ((entry.status() != 1 && entry.status() != 4)
                    || entry.quantity() < 1 || entry.dimension().isBlank()
                    || entry.objective().isBlank()) continue;
            LuxQuestTracker.observe(new LuxQuestTracker.Contract(
                    entry.npc(), entry.dimension(), entry.x(), entry.z(),
                    entry.objective(), entry.quantity(), entry.expiresDay(),
                    entry.status()), true);
        }
    }

    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        syncedLevel = null;
        LuxQuestTracker.clear();
    }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof LivingEntity npc)
                || !DialogueNpcTypes.supported(npc)
                || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.getEntity().isShiftKeyDown()) return;
        if (DialogueNpcTypes.guardInCombat(npc) || !event.getLevel().isClientSide()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        mc.setScreen(new VillagerDialogueScreen(npc));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
