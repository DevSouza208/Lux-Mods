package dev.luxmods.dialogues;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;

/**
 * Server-only per-NPC offer store. Entity persistent data survives world saves.
 * Never derive a new offer from an opened dialogue while an existing one is active.
 * Only the public, unclaimed offer is rolled over after expiry.
 */
public final class NpcContractStorage {
    private static final String KEY = "lux_dialogues_contract_v1";
    private static final int SCHEMA = 1;

    private NpcContractStorage() {}

    public static NpcContractLifecycle.Offer current(Villager villager, long gameDay) {
        if (villager.level().isClientSide()) {
            throw new IllegalStateException("Contracts must be read on the server");
        }
        CompoundTag root = villager.getPersistentData();
        NpcContractLifecycle.Offer offer = read(root.getCompound(KEY), villager.getUUID());
        if (offer == null) {
            offer = NpcContractLifecycle.create(villager.getUUID(), Math.max(0, gameDay));
            write(root, offer);
            return offer;
        }

        // Offers stay identical while active. An old AVAILABLE offer is replaced
        // once, on next access after its expiry; accepted offers are preserved.
        if (offer.status() == NpcContractLifecycle.Status.AVAILABLE
                && gameDay >= offer.expiresDay()) {
            offer = NpcContractLifecycle.create(villager.getUUID(), Math.max(0, gameDay));
            write(root, offer);
        }
        return offer;
    }

    /** Atomically persist a permitted state change on the server thread. */
    public static boolean store(Villager villager, NpcContractLifecycle.Offer expected,
                                NpcContractLifecycle.Offer updated) {
        if (villager.level().isClientSide() || expected == null || updated == null
                || !villager.getUUID().equals(updated.npcId())) return false;
        CompoundTag root = villager.getPersistentData();
        var persisted = read(root.getCompound(KEY), villager.getUUID());
        if (!expected.equals(persisted)) return false;
        write(root, updated);
        return true;
    }

    private static NpcContractLifecycle.Offer read(CompoundTag tag, UUID npcId) {
        if (tag.getInt("schema") != SCHEMA || !tag.hasUUID("npc")
                || !npcId.equals(tag.getUUID("npc"))) return null;
        try {
            String statusName = tag.getString("status");
            var status = NpcContractLifecycle.Status.valueOf(statusName);
            UUID claimant = tag.hasUUID("claimant") ? tag.getUUID("claimant") : null;
            return new NpcContractLifecycle.Offer(npcId,
                    tag.getLong("issuedDay"), tag.getLong("expiresDay"),
                    tag.getString("objective"), tag.getInt("quantity"), status, claimant);
        } catch (IllegalArgumentException ex) {
            // Corrupt/older tag: a fresh offer is created by current().
            return null;
        }
    }

    private static void write(CompoundTag root, NpcContractLifecycle.Offer offer) {
        CompoundTag data = new CompoundTag();
        data.putInt("schema", SCHEMA);
        data.putUUID("npc", offer.npcId());
        data.putLong("issuedDay", offer.issuedDay());
        data.putLong("expiresDay", offer.expiresDay());
        data.putString("objective", offer.objective());
        data.putInt("quantity", offer.quantity());
        data.putString("status", offer.status().name());
        if (offer.claimant() != null) data.putUUID("claimant", offer.claimant());
        root.put(KEY, data);
    }
}
