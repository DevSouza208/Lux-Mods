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
 * Lux Rest: food never causes vanilla natural healing.
 *
 * Only a lit campfire plus actual rest heals:
 * - motionless and not sitting: 1 HP each 200 ticks (10 seconds)
 * - seated on a recognized seat: 1 HP each 120 ticks (6 seconds)
 * - after taking damage: eight-second lockout, no combat healing
 * - sleep: full recovery after 80 sleeping ticks
 *
 * No healing merely from standing still away from campfires, running past them,
 * or being mounted on a vehicle. Potion and medicine healing is unaffected.
 */
public final class HudRestServerEvents {
    private static final int MINIMUM_SLEEP_TICKS = 80;
    private static final int CAMPFIRE_RADIUS = 4;
    private static final int STANDING_HEAL_TICKS = 200;
    private static final int SEATED_HEAL_TICKS = 120;
    private static final int COMBAT_COOLDOWN_TICKS = 160;
    private static final int STATIONARY_REQUIRED_TICKS = 20;

    private static final String SLEEP_TICKS = "lux_rest_sleep_ticks";
    private static final String FIRE_REST_TICKS = "lux_rest_fire_ticks";
    private static final String SAFE_TICKS = "lux_rest_safe_ticks";
    private static final String STILL_TICKS = "lux_rest_still_ticks";
    private static final String LAST_X = "lux_rest_last_x";
    private static final String LAST_Y = "lux_rest_last_y";
    private static final String LAST_Z = "lux_rest_last_z";
    private static final String LAST_PRESENT = "lux_rest_last_present";

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var data = player.getPersistentData();
        clearRest(data);
        data.remove(LAST_PRESENT);
        data.remove(SAFE_TICKS);
        if (!HudServerConfig.DISABLE_NATURAL_HEALING.get()) return;
        player.serverLevel().getGameRules()
                .getRule(GameRules.RULE_NATURAL_REGENERATION)
                .set(false, player.getServer());
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return;
        var data = player.getPersistentData();

        if (player.isSleeping()) {
            data.putInt(SLEEP_TICKS, Math.min(MINIMUM_SLEEP_TICKS, data.getInt(SLEEP_TICKS) + 1));
            clearRest(data);
            return;
        }

        int slept = data.getInt(SLEEP_TICKS);
        if (slept > 0) {
            data.remove(SLEEP_TICKS);
            if (slept >= MINIMUM_SLEEP_TICKS && !player.isCreative() && !player.isSpectator()) {
                player.heal(player.getMaxHealth());
            }
        }

        boolean seated = isSitting(player);
        boolean still = updateStationary(player);
        // hurtTime catches the most recent hit even when multiple hits occur.
        int safeTicks = player.hurtTime > 0 ? 0
                : Math.min(COMBAT_COOLDOWN_TICKS, data.getInt(SAFE_TICKS) + 1);
        data.putInt(SAFE_TICKS, safeTicks);

        if (player.isCreative() || player.isSpectator()
                || player.getHealth() >= player.getMaxHealth()
                || safeTicks < COMBAT_COOLDOWN_TICKS
                || !(seated || still) || (player.isPassenger() && !seated)) {
            data.remove(FIRE_REST_TICKS);
            return;
        }

        // The small campfire search runs once per second, not every tick.
        if (player.tickCount % 20 != 0) return;
        if (!nearLitCampfire(player)) {
            data.remove(FIRE_REST_TICKS);
            return;
        }

        int progress = data.getInt(FIRE_REST_TICKS) + 20;
        int threshold = seated ? SEATED_HEAL_TICKS : STANDING_HEAL_TICKS;
        if (progress >= threshold) {
            player.heal(1.0F);
            progress = 0;
        }
        data.putInt(FIRE_REST_TICKS, progress);
    }

    private static void clearRest(net.minecraft.nbt.CompoundTag data) {
        data.remove(FIRE_REST_TICKS);
        data.remove(STILL_TICKS);
    }

    private static boolean updateStationary(ServerPlayer player) {
        var data = player.getPersistentData();
        double x = player.getX(), y = player.getY(), z = player.getZ();
        boolean wasPresent = data.getBoolean(LAST_PRESENT);
        double dx = x - data.getDouble(LAST_X);
        double dy = y - data.getDouble(LAST_Y);
        double dz = z - data.getDouble(LAST_Z);
        data.putDouble(LAST_X, x);
        data.putDouble(LAST_Y, y);
        data.putDouble(LAST_Z, z);
        data.putBoolean(LAST_PRESENT, true);

        // Small sub-pixel jitter is allowed, but walking or jumping breaks rest.
        boolean motionless = wasPresent && dx * dx + dy * dy + dz * dz <= 0.0016D;
        int ticks = motionless ? Math.min(STATIONARY_REQUIRED_TICKS, data.getInt(STILL_TICKS) + 1) : 0;
        data.putInt(STILL_TICKS, ticks);
        return ticks >= STATIONARY_REQUIRED_TICKS;
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
