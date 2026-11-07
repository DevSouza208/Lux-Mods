package dev.luxmods.dialogues.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Open a local conversation only for ordinary empty-hand right-clicks. */
public final class DialogueClient {
    private DialogueClient() {}
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager) || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.getEntity().isShiftKeyDown() || !event.getEntity().getMainHandItem().isEmpty()) return;
        if (!event.getLevel().isClientSide()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        mc.setScreen(new VillagerDialogueScreen(villager));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
