package dev.luxmods.dialogues;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Server-side cancellation prevents the vanilla trade GUI opening immediately. */
public final class DialogueInteractions {
    private DialogueInteractions() {}

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager) || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.getEntity().isShiftKeyDown() || !event.getEntity().getMainHandItem().isEmpty()) return;
        if (!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof ServerPlayer player) DialogueSessions.start(villager, player);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
