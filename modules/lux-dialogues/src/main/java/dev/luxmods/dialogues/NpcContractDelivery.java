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
 * A completion is persisted before items/rewards are transferred so a repeated
 * packet cannot mint another reward. All steps must run on the server thread.
 * This is not a cross-entity disk transaction: an abrupt crash mid-delivery
 * still requires recovery logic before production use.
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

        // Count every storage slot, including offhand and armor where applicable.
        int owned = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) owned += stack.getCount();
        }
        if (owned < offer.quantity()) {
            player.sendSystemMessage(Component.literal("Faltam "
                    + (offer.quantity() - owned) + " itens para concluir o contrato."));
            return;
        }

        var completed = NpcContractLifecycle.complete(offer, player.getUUID(), true);
        // Guard against repeated requests and stale offers before changing inventory.
        if (!NpcContractStorage.store(villager, offer, completed)) {
            player.sendSystemMessage(Component.literal("Contrato alterado; tente novamente."));
            return;
        }

        int remaining = offer.quantity();
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item)) continue;
            int used = Math.min(stack.getCount(), remaining);
            stack.shrink(used);
            remaining -= used;
        }
        player.getInventory().setChanged();

        int emeralds = offer.objective().equals("minecraft:iron_ingot") ? 4 : 3;
        ItemStack reward = new ItemStack(Items.EMERALD, emeralds);
        if (!player.addItem(reward) && !reward.isEmpty()) {
            player.drop(reward, false);
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
