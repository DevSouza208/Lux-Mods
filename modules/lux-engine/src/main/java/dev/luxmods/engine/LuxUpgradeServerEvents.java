package dev.luxmods.engine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Player-only, server-authoritative upgrades. Every purchase consumes actual
 * Minecraft XP levels; each next rank costs more. Uses vanilla attribute
 * modifiers so equipment/Curios/Better Combat retain their own bonuses.
 */
public final class LuxUpgradeServerEvents {
    private static final String[] KEYS = {
            "lux_upgrades_health_rank_v2", "lux_upgrades_strength_rank_v2",
            "lux_upgrades_speed_rank_v2", "lux_upgrades_armor_rank_v2"
    };
    private static final int[] BASE_COST = {15, 20, 18, 25};
    private static final int MAX_RANK = 3;
    private static final String OLD_HEALTH = "lux_upgrades_health_v1";
    private static final String OLD_SPEED = "lux_upgrades_speed_v1";
    private static final ResourceLocation[] IDS = {
            id("permanent_health"), id("permanent_strength"),
            id("permanent_speed"), id("permanent_armor")
    };

    private LuxUpgradeServerEvents() {}

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(LuxEngine.MOD_ID, name);
    }

    public static int price(int perk, int currentRank) {
        if (perk < 0 || perk >= KEYS.length || currentRank < 0 || currentRank >= MAX_RANK)
            return -1;
        return BASE_COST[perk] + 12 * currentRank;
    }

    private static void migrate(CompoundTag data) {
        // Previously purchased one-time upgrades must stay purchased without
        // another XP charge when upgrading from the v1 save format.
        if (!data.contains(KEYS[0]) && data.getBoolean(OLD_HEALTH)) data.putInt(KEYS[0], 1);
        if (!data.contains(KEYS[2]) && data.getBoolean(OLD_SPEED)) data.putInt(KEYS[2], 1);
    }

    private static int rank(CompoundTag data, int perk) {
        return Math.max(0, Math.min(MAX_RANK, data.getInt(KEYS[perk])));
    }

    private static void sync(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        migrate(data);
        PacketDistributor.sendToPlayer(player, new LuxUpgradeNetwork.Snapshot(
                player.experienceLevel, rank(data, 0), rank(data, 1),
                rank(data, 2), rank(data, 3)));
    }

    private static void modifier(ServerPlayer player, int perk, int rank) {
        var attribute = switch (perk) {
            case 0 -> player.getAttribute(Attributes.MAX_HEALTH);
            case 1 -> player.getAttribute(Attributes.ATTACK_DAMAGE);
            case 2 -> player.getAttribute(Attributes.MOVEMENT_SPEED);
            default -> player.getAttribute(Attributes.ARMOR);
        };
        if (attribute == null) return;
        ResourceLocation id = IDS[perk];
        double amount = switch (perk) {
            case 0 -> 2.0 * rank;
            case 1 -> 0.5 * rank;
            case 2 -> 0.004 * rank;
            default -> 1.0 * rank;
        };
        var existing = attribute.getModifier(id);
        if (rank == 0) {
            if (existing != null) attribute.removeModifier(id);
        } else if (existing == null || existing.amount() != amount) {
            attribute.addOrReplacePermanentModifier(
                    new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void apply(ServerPlayer player) {
        var data = player.getPersistentData();
        migrate(data);
        for (int perk = 0; perk < KEYS.length; perk++) modifier(player, perk, rank(data, perk));
    }

    public static void buy(ServerPlayer player, int perk) {
        if (!player.isAlive() || player.isSpectator() || perk < 0 || perk >= KEYS.length) return;
        var data = player.getPersistentData();
        migrate(data);
        int current = rank(data, perk);
        int cost = price(perk, current);
        if (cost < 0 || player.experienceLevel < cost) {
            sync(player);
            return;
        }
        // Must succeed on the server in one tick. Never subtract client-side.
        player.giveExperienceLevels(-cost);
        data.putInt(KEYS[perk], current + 1);
        apply(player);
        if (perk == 0) player.heal(2.0F);
        sync(player);
    }

    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            apply(player);
            sync(player);
        }
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 200 != 0) return;
        apply(player);
        sync(player);
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var old = event.getOriginal().getPersistentData();
        var data = player.getPersistentData();
        for (String key : KEYS) {
            if (old.contains(key)) data.putInt(key, old.getInt(key));
        }
        for (String key : new String[]{OLD_HEALTH, OLD_SPEED}) {
            if (old.contains(key)) data.putBoolean(key, old.getBoolean(key));
        }
        apply(player);
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            apply(player);
            sync(player);
        }
    }
}
