package dev.luxmods.dialogues;

import java.util.UUID;

/** Pure contract lifecycle; persistence and inventory transaction remain server responsibilities. */
public final class NpcContractLifecycle {
    private NpcContractLifecycle() {}

    public enum Status { AVAILABLE, ACCEPTED, COMPLETED, EXPIRED }

    public record Offer(UUID npcId, long issuedDay, long expiresDay, String objective,
                        int quantity, Status status, UUID claimant) {
        public Offer {
            if (npcId == null || objective == null || objective.isBlank() || status == null)
                throw new IllegalArgumentException("invalid contract");
            if (issuedDay < 0 || expiresDay <= issuedDay || quantity < 1)
                throw new IllegalArgumentException("invalid dates or quantity");
            if ((status == Status.ACCEPTED || status == Status.COMPLETED) != (claimant != null))
                throw new IllegalArgumentException("claimant inconsistent with status");
        }
    }

    /** Stable per NPC and issue day: opening dialogue does not regenerate an offer. */
    public static Offer create(UUID npc, long day) {
        if (npc == null || day < 0) throw new IllegalArgumentException("invalid identity or day");
        long mix = npc.getMostSignificantBits() ^ Long.rotateLeft(npc.getLeastSignificantBits(), 19)
                ^ (day * 0x9E3779B97F4A7C15L);
        int kind = Math.floorMod(mix, 3);
        String objective = switch (kind) {
            case 0 -> "minecraft:coal";
            case 1 -> "minecraft:bread";
            default -> "minecraft:iron_ingot";
        };
        int amount = switch (kind) { case 0 -> 24; case 1 -> 12; default -> 8; };
        long duration = 2L + Math.floorMod(mix >>> 32, 2);
        return new Offer(npc, day, day + duration, objective, amount, Status.AVAILABLE, null);
    }

    /** An accepted contract is reserved until completed, including after the offer expires. */
    public static Offer refresh(Offer offer, long day) {
        if (day < 0) throw new IllegalArgumentException("invalid day");
        if (offer.status() != Status.AVAILABLE || day < offer.expiresDay()) return offer;
        return new Offer(offer.npcId(), offer.issuedDay(), offer.expiresDay(),
                offer.objective(), offer.quantity(), Status.EXPIRED, null);
    }

    public static Offer accept(Offer offer, UUID player, long day) {
        if (player == null) throw new IllegalArgumentException("missing player");
        Offer current = refresh(offer, day);
        if (current.status() != Status.AVAILABLE) return current;
        return new Offer(current.npcId(), current.issuedDay(), current.expiresDay(),
                current.objective(), current.quantity(), Status.ACCEPTED, player);
    }

    /**
     * Only accept the two state transitions used by the server's compare-and-store.
     * Never permit changing objectives, quantities, deadlines, NPCs or claimants
     * by accidentally passing an unrelated Offer to the persistence layer.
     */
    public static boolean validTransition(Offer before, Offer after) {
        if (before == null || after == null
                || !before.npcId().equals(after.npcId())
                || before.issuedDay() != after.issuedDay()
                || before.expiresDay() != after.expiresDay()
                || !before.objective().equals(after.objective())
                || before.quantity() != after.quantity()) return false;
        if (before.status() == Status.AVAILABLE) {
            return after.status() == Status.ACCEPTED && after.claimant() != null;
        }
        return before.status() == Status.ACCEPTED
                && after.status() == Status.COMPLETED
                && before.claimant().equals(after.claimant());
    }

    /** Call ONLY after a server-authoritative inventory transfer has succeeded. */
    public static Offer complete(Offer offer, UUID player, boolean itemsConsumed) {
        if (!itemsConsumed || player == null || offer.status() != Status.ACCEPTED
                || !player.equals(offer.claimant())) return offer;
        return new Offer(offer.npcId(), offer.issuedDay(), offer.expiresDay(),
                offer.objective(), offer.quantity(), Status.COMPLETED, player);
    }
}
