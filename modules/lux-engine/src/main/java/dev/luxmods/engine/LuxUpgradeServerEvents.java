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
 * Two tiny permanent upgrades earned through gameplay, not fake GUI points:
 * reach levels 5 and 10 to earn at most two spendable points. Highest level
 * achieved is persisted so death/enchanting cannot farm points repeatedly.
 */
public final class LuxUpgradeServerEvents {
    private static final String PEAK = "lux_upgrades_highest_level_v1";
    private static final String HEALTH = "lux_upgrades_health_v1";
    private static final String SPEED = "lux_upgrades_speed_v1";
    private static final ResourceLocation HEALTH_ID =
            ResourceLocation.fromNamespaceAndPath(LuxEngine.MOD_ID, "permanent_health");
    private static final ResourceLocation SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(LuxEngine.MOD_ID, "permanent_speed");

    private static int earned(CompoundTag data) {
        return Math.min(2, Math.max(0, data.getInt(PEAK)) / 5);
    }

    private static void sync(ServerPlayer player) {
        var data = player.getPersistentData();
        PacketDistributor.sendToPlayer(player, new LuxUpgradeNetwork.Snapshot(
                Math.max(0, data.getInt(PEAK)), data.getBoolean(HEALTH), data.getBoolean(SPEED)));
    }

    private static void updateLevels(ServerPlayer player) {
        var data = player.getPersistentData();
        int highest = Math.max(0, player.experienceLevel);
        if (highest > data.getInt(PEAK)) {
            data.putInt(PEAK, highest);
            sync(player);
        }
    }

    private static void apply(ServerPlayer player) {
        var data = player.getPersistentData();
        var health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && data.getBoolean(HEALTH) && !health.hasModifier(HEALTH_ID)) {
            health.addOrReplacePermanentModifier(new AttributeModifier(
                    HEALTH_ID, 2.0, AttributeModifier.Operation.ADD_VALUE));
        }
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && data.getBoolean(SPEED) && !speed.hasModifier(SPEED_ID)) {
            speed.addOrReplacePermanentModifier(new AttributeModifier(
                    SPEED_ID, 0.005, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void buy(ServerPlayer player, int perk) {
        if (!player.isAlive() || player.isSpectator() || player.isCreative()
                || (perk != 0 && perk != 1)) return;
        updateLevels(player);
        var data = player.getPersistentData();
        String key = perk == 0 ? HEALTH : SPEED;
        int spent = (data.getBoolean(HEALTH) ? 1 : 0)
                + (data.getBoolean(SPEED) ? 1 : 0);
        if (data.getBoolean(key) || earned(data) <= spent) {
            sync(player);
            return;
        }
        data.putBoolean(key, true);
        apply(player);
        sync(player);
    }

    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            updateLevels(player);
            apply(player);
            sync(player);
        }
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0) return;
        updateLevels(player);
        apply(player);
        if (player.tickCount % 200 == 0) sync(player);
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var old = event.getOriginal().getPersistentData();
        var data = player.getPersistentData();
        for (String key : new String[]{PEAK, HEALTH, SPEED}) {
            if (old.contains(key)) data.put(key, old.get(key).copy());
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
