package dev.luxmods.dialogues;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Server-side cancellation prevents the vanilla trade GUI opening immediately. */
public final class DialogueInteractions {
    private DialogueInteractions() {}

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (event.getEntity().isShiftKeyDown() || !event.getEntity().getMainHandItem().isEmpty()) return;
        if (!event.getLevel().isClientSide()) {
            villager.getLookControl().setLookAt(event.getEntity(), 30f, 30f);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
