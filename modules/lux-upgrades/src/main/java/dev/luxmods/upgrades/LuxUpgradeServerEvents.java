package dev.luxmods.upgrades;

import dev.luxmods.engine.api.LuxUpgradeRules;
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
 * Permanent, character-owned progression; not related to vanilla experience
 * level. The vanilla XP levels are the currency spent for each upgrade and
 * for promotion after all four ranks at the current character level are done.
 *
 * Modifiers use vanilla attributes with Lux-specific IDs: they ADD to mods'
 * equipment/Curios/Better Combat effects rather than overwriting them.
 */
public final class LuxUpgradeServerEvents {
    private static final String LEVEL_KEY = "lux_character_level_v3";
    private static final String[] KEYS = {
            "lux_upgrades_health_rank_v3", "lux_upgrades_strength_rank_v3",
            "lux_upgrades_speed_rank_v3", "lux_upgrades_armor_rank_v3"
    };
    private static final String[] OLD_KEYS = {
            "lux_upgrades_health_rank_v2", "lux_upgrades_strength_rank_v2",
            "lux_upgrades_speed_rank_v2", "lux_upgrades_armor_rank_v2"
    };
    private static final String OLD_HEALTH = "lux_upgrades_health_v1";
    private static final String OLD_SPEED = "lux_upgrades_speed_v1";
    // Each v2 step must retain its actual effect magnitude after migration.
    private static final int[] V2_MULTIPLIER = {2, 2, 4, 4};
    private static final ResourceLocation[] IDS = {
            id("permanent_health"), id("permanent_strength"),
            id("permanent_speed"), id("permanent_armor")
    };

    private LuxUpgradeServerEvents() {}

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath("lux_engine", name);
    }

    /** Import saved v1/v2 purchases without charging XP or weakening bonuses. */
    private static void migrate(CompoundTag data) {
        if (data.contains(LEVEL_KEY)) return;
        int highest = 1;
        for (int i = 0; i < KEYS.length; i++) {
            int oldRank = Math.max(0, data.getInt(OLD_KEYS[i]));
            if (oldRank == 0 && i == 0 && data.getBoolean(OLD_HEALTH)) oldRank = 1;
            if (oldRank == 0 && i == 2 && data.getBoolean(OLD_SPEED)) oldRank = 1;
            int converted = LuxUpgradeRules.clampRank(oldRank * V2_MULTIPLIER[i]);
            data.putInt(KEYS[i], converted);
            highest = Math.max(highest, converted);
        }
        // A migrated rank must never be hidden behind a lower character level.
        data.putInt(LEVEL_KEY, LuxUpgradeRules.clampLevel(highest));
    }

    private static int level(CompoundTag data) {
        migrate(data);
        return LuxUpgradeRules.clampLevel(data.getInt(LEVEL_KEY));
    }

    private static int[] ranks(CompoundTag data) {
        int[] ranks = new int[LuxUpgradeRules.PERKS];
        for (int i = 0; i < ranks.length; i++)
            ranks[i] = LuxUpgradeRules.clampRank(data.getInt(KEYS[i]));
        return ranks;
    }

    private static void sync(ServerPlayer player) {
        var data = player.getPersistentData();
        int characterLevel = level(data);
        int[] r = ranks(data);
        PacketDistributor.sendToPlayer(player, new LuxUpgradeNetwork.Snapshot(
                player.experienceLevel, characterLevel, r[0], r[1], r[2], r[3]));
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
            case 0 -> 1.0 * rank;
            case 1 -> 0.25 * rank;
            case 2 -> 0.001 * rank;
            default -> 0.25 * rank;
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
        level(data);
        int[] r = ranks(data);
        for (int i = 0; i < r.length; i++) modifier(player, i, r[i]);
    }

    /** Per-player requests are serialized on the server's main thread. */
    public static void buy(ServerPlayer player, int perk) {
        if (!player.isAlive() || player.isSpectator()
                || perk < 0 || perk > LuxUpgradeRules.PROMOTE) return;
        CompoundTag data = player.getPersistentData();
        int characterLevel = level(data);
        int[] current = ranks(data);
        int cost;
        if (perk == LuxUpgradeRules.PROMOTE) {
            cost = LuxUpgradeRules.promotionCost(characterLevel, current);
        } else {
            cost = LuxUpgradeRules.upgradeCost(perk, current[perk], characterLevel);
        }
        if (cost < 0 || player.experienceLevel < cost) {
            sync(player);
            return;
        }
        // This is the vanilla XP balance, NOT a fictitious skill point counter.
        player.giveExperienceLevels(-cost);
        if (perk == LuxUpgradeRules.PROMOTE) {
            data.putInt(LEVEL_KEY, characterLevel + 1);
        } else {
            data.putInt(KEYS[perk], current[perk] + 1);
        }
        apply(player);
        if (perk == 0) player.heal(1.0F);
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
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount % 200 != 0) return;
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
        if (old.contains(LEVEL_KEY)) data.putInt(LEVEL_KEY, old.getInt(LEVEL_KEY));
        // Existing pre-v3 players may die before a regular tick migrated them.
        for (String key : OLD_KEYS) {
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
