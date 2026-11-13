package dev.luxmods.hud.server;

import dev.luxmods.hud.HudServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Lux Rest: passive regeneration is disabled.
 *
 * Health may recover from curatives, potions, sleep, and resting while SEATED
 * next to a LIT campfire. Neither sitting alone nor campfire proximity alone
 * heals; standing still never heals.
 *
 * The seating test targets seat entities from sitting mods, not boats, aircraft
 * or other vehicles. This must be checked against the installed Sit! mod.
 */
public final class HudRestServerEvents {
    private static final int MINIMUM_SLEEP_TICKS = 80;
    private static final int CAMPFIRE_RADIUS = 4;
    private static final int CAMPFIRE_REST_TICKS = 120; // half a heart every six seconds
    private static final String SLEEP_TICKS = "lux_rest_sleep_ticks";
    private static final String FIRE_REST_TICKS = "lux_rest_fire_ticks";

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !HudServerConfig.DISABLE_NATURAL_HEALING.get()) return;
        player.serverLevel().getGameRules()
                .getRule(GameRules.RULE_NATURAL_REGENERATION)
                .set(false, player.getServer());
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return;
        var data = player.getPersistentData();

        if (player.isSleeping()) {
            data.putInt(SLEEP_TICKS, Math.min(MINIMUM_SLEEP_TICKS,
                    data.getInt(SLEEP_TICKS) + 1));
            data.remove(FIRE_REST_TICKS);
            return;
        }

        // Process sleep recovery, but do not return early when there was no sleep.
        int slept = data.getInt(SLEEP_TICKS);
        if (slept > 0) {
            data.remove(SLEEP_TICKS);
            if (slept >= MINIMUM_SLEEP_TICKS && !player.isCreative() && !player.isSpectator()) {
                player.heal(player.getMaxHealth());
            }
        }

        if (player.isCreative() || player.isSpectator()
                || player.getHealth() >= player.getMaxHealth()
                || !isSitting(player)) {
            data.remove(FIRE_REST_TICKS);
            return;
        }

        // Scan the small radius once a second, never every game tick.
        if (player.tickCount % 20 != 0) return;
        if (!nearLitCampfire(player)) {
            data.remove(FIRE_REST_TICKS);
            return;
        }

        int ticks = data.getInt(FIRE_REST_TICKS) + 20;
        if (ticks >= CAMPFIRE_REST_TICKS) {
            player.heal(1.0F);
            ticks = 0;
        }
        data.putInt(FIRE_REST_TICKS, ticks);
    }

    private static boolean isSitting(ServerPlayer player) {
        if (!player.isPassenger() || player.getVehicle() == null) return false;
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(player.getVehicle().getType());
        String path = id.getPath();
        String namespace = id.getNamespace();
        return path.contains("seat") || path.contains("sitting")
                || namespace.equals("sit") || namespace.equals("sitmod");
    }

    private static boolean nearLitCampfire(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-CAMPFIRE_RADIUS, -2, -CAMPFIRE_RADIUS),
                center.offset(CAMPFIRE_RADIUS, 2, CAMPFIRE_RADIUS))) {
            if (pos.distSqr(center) > CAMPFIRE_RADIUS * CAMPFIRE_RADIUS) continue;
            BlockState state = player.level().getBlockState(pos);
            if (state.getBlock() instanceof CampfireBlock
                    && state.getValue(CampfireBlock.LIT)) return true;
        }
        return false;
    }
}
