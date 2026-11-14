package dev.luxmods.dialogues;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Server-side delivery for the first three NPC supply contracts.
 *
 * The NPC enters SETTLING before moving items and cannot settle twice.
 * A crash may still leave an ambiguous state: it is frozen for manual review,
 * NOT automatically refunded or paid. All steps run on the server thread.
 * This is not a durable atomic transaction across the player and NPC saves.
 */
public final class NpcContractDelivery {
    private NpcContractDelivery() {}

    public static void deliver(Villager villager, ServerPlayer player) {
        if (villager.level().isClientSide() || !villager.isAlive()
                || !player.isAlive() || player.isSpectator()
                || villager.distanceToSqr(player) > 64.0D) return;

        long day = Math.max(0L, villager.level().getDayTime() / 24000L);
        var offer = NpcContractStorage.current(villager, day);
        if (offer.status() == NpcContractLifecycle.Status.COMPLETED) {
            player.sendSystemMessage(Component.literal("Este contrato já foi concluído."));
            return;
        }
        if (offer.status() == NpcContractLifecycle.Status.SETTLING) {
            player.sendSystemMessage(Component.literal(
                    "Entrega interrompida ou em revisão. Nenhum item será cobrado novamente. "
                    + "Peça ajuda ao administrador do mundo."));
            return;
        }
        if (offer.status() != NpcContractLifecycle.Status.ACCEPTED
                || !player.getUUID().equals(offer.claimant())) {
            player.sendSystemMessage(Component.literal("Você não possui um contrato aceito com este aldeão."));
            return;
        }

        Item item = requestedItem(offer.objective());
        if (item == null) {
            player.sendSystemMessage(Component.literal("Tipo de contrato desconhecido. Nenhum item foi retirado."));
            return;
        }

        // Snapshot only matching items. Do not mark COMPLETED unless a plan
        // exists for the entire amount, including split stacks.
        int size = player.getInventory().getContainerSize();
        int[] counts = new int[size];
        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            counts[slot] = stack.is(item) ? stack.getCount() : 0;
        }
        int[] removal = ContractInventoryPlan.forCounts(counts, offer.quantity());
        if (removal == null) {
            int owned = 0;
            for (int count : counts) owned += count;
            player.sendSystemMessage(Component.literal("Faltam "
                    + Math.max(0, offer.quantity() - owned)
                    + " itens para concluir o contrato."));
            return;
        }

        // Validate that quantities still match the plan before writing NBT.
        int[] current = new int[size];
        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            current[slot] = stack.is(item) ? stack.getCount() : 0;
        }
        if (!ContractInventoryPlan.canApply(current, removal, offer.quantity())) {
            player.sendSystemMessage(Component.literal("Inventário alterado. Tente novamente."));
            return;
        }

        var settling = NpcContractLifecycle.beginSettlement(offer, player.getUUID());
        // Reserve a non-repeatable settlement before touching inventory.
        // Interrupted settlements require manual review; NEVER auto-pay them.
        if (!NpcContractStorage.store(villager, offer, settling)) {
            player.sendSystemMessage(Component.literal("Contrato alterado; tente novamente."));
            return;
        }

        for (int slot = 0; slot < size; slot++) {
            if (removal[slot] > 0) player.getInventory().getItem(slot).shrink(removal[slot]);
        }
        player.getInventory().setChanged();

        int emeralds = offer.objective().equals("minecraft:iron_ingot") ? 4 : 3;
        ItemStack reward = new ItemStack(Items.EMERALD, emeralds);
        player.addItem(reward);
        // addItem can leave a remainder when only part of the stack fits.
        if (!reward.isEmpty()) player.drop(reward, false);
        var completed = NpcContractLifecycle.complete(settling, player.getUUID(), true);
        if (!NpcContractStorage.store(villager, settling, completed)) {
            player.sendSystemMessage(Component.literal(
                    "Pagamento entregue, mas o registro exige revisão do administrador."));
            return;
        }
        player.sendSystemMessage(Component.literal("Contrato concluído! Recompensa: "
                + emeralds + " esmeraldas."));
    }

    private static Item requestedItem(String objective) {
        return switch (objective) {
            case "minecraft:coal" -> Items.COAL;
            case "minecraft:bread" -> Items.BREAD;
            case "minecraft:iron_ingot" -> Items.IRON_INGOT;
            default -> null;
        };
    }
}
