package dev.luxmods.dialogues.client;

import dev.luxmods.dialogues.DialogueNpcTypes;
import dev.luxmods.dialogues.network.DialogueNetwork;
import dev.luxmods.engine.api.LuxQuestTracker;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Optional Guard Villagers and vanilla villagers share the Lux dialogue screen. */
public final class DialogueClient {
    private DialogueClient() {}
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

    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        LuxQuestTracker.clear();
    }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof LivingEntity npc)
                || !DialogueNpcTypes.supported(npc)
                || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.getEntity().isShiftKeyDown() || !event.getEntity().getMainHandItem().isEmpty()) return;
        if (DialogueNpcTypes.guardInCombat(npc) || !event.getLevel().isClientSide()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        mc.setScreen(new VillagerDialogueScreen(npc));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
