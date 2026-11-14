package dev.luxmods.dialogues;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Empty-hand interaction opens Lux conversation; shift-click keeps the original mod behavior. */
public final class DialogueInteractions {
    private DialogueInteractions() {}

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        var npc = event.getTarget();
        if (!DialogueNpcTypes.supported(npc) || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.getEntity().isShiftKeyDown()) return;
        if (DialogueNpcTypes.guardInCombat(npc)) return;
        if (!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof ServerPlayer player && player.distanceToSqr(npc) <= 64D) {
                if (npc instanceof Villager villager) {
                    // Initialize/read a stable server-owned offer; viewing it never
                    // accepts the contract, consumes items, or rerolls a live offer.
                    NpcContractStorage.current(villager, Math.max(0L, villager.level().getDayTime() / 24000L));
                    DialogueSessions.start(villager, player);
                }
                else if (npc instanceof Mob guard) GuardDialogueSessions.start(guard, player);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
}
