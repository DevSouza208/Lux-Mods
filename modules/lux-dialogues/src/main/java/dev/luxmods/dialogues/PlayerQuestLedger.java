package dev.luxmods.dialogues;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Server-owned, bounded index of contracts seen by each player.
 * The NPC contract remains authoritative for acceptance and rewards.
 * This index exists solely to restore the Atlas quest list after reconnecting.
 */
public final class PlayerQuestLedger {
    private static final String KEY = "lux_dialogues_player_quests_v1";
    private static final int MAX_ENTRIES = 128;

    private PlayerQuestLedger() {}

    public record Entry(UUID npc, String dimension, int x, int z,
                        String objective, int quantity, long expiresDay, int status) {}

    public static void record(ServerPlayer player, Villager villager, NpcContractLifecycle.Offer offer) {
        if (player.level().isClientSide() || offer == null
                || !player.getUUID().equals(offer.claimant())) return;
        ListTag previous = player.getPersistentData().getList(KEY, Tag.TAG_COMPOUND);
        ListTag updated = new ListTag();
        for (int i = 0; i < previous.size(); i++) {
            CompoundTag item = previous.getCompound(i);
            if (!item.hasUUID("npc") || item.getUUID("npc").equals(offer.npcId())) continue;
            if (updated.size() < MAX_ENTRIES - 1) updated.add(item.copy());
        }
        if (offer.status() == NpcContractLifecycle.Status.ACCEPTED
                || offer.status() == NpcContractLifecycle.Status.SETTLING) {
            CompoundTag item = new CompoundTag();
            item.putUUID("npc", offer.npcId());
            item.putString("dimension", villager.level().dimension().location().toString());
            item.putInt("x", villager.getBlockX());
            item.putInt("z", villager.getBlockZ());
            item.putString("objective", offer.objective());
            item.putInt("quantity", offer.quantity());
            item.putLong("expiresDay", offer.expiresDay());
            item.putInt("status", offer.status().ordinal());
            updated.add(item);
        }
        player.getPersistentData().put(KEY, updated);
    }

    public static List<Entry> read(ServerPlayer player) {
        ListTag source = player.getPersistentData().getList(KEY, Tag.TAG_COMPOUND);
        List<Entry> result = new ArrayList<>();
        for (int i = 0; i < source.size() && result.size() < MAX_ENTRIES; i++) {
            CompoundTag tag = source.getCompound(i);
            if (!tag.hasUUID("npc")) continue;
            String dimension = tag.getString("dimension");
            String objective = tag.getString("objective");
            int quantity = tag.getInt("quantity");
            int status = tag.getInt("status");
            if (dimension.isBlank() || dimension.length() > 128
                    || objective.isBlank() || objective.length() > 64
                    || quantity < 1 || (status != 1 && status != 4)) continue;
            result.add(new Entry(tag.getUUID("npc"), dimension,
                    tag.getInt("x"), tag.getInt("z"), objective, quantity,
                    tag.getLong("expiresDay"), status));
        }
        return List.copyOf(result);
    }

    /** Preserve the index when the server replaces the player entity on death. */
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData();
        if (original.contains(KEY, Tag.TAG_LIST)) {
            event.getEntity().getPersistentData().put(KEY, original.getList(KEY, Tag.TAG_COMPOUND).copy());
        }
    }
}
