package dev.luxmods.hud.server;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Small, server-side prototype of Lux Rest. Requires no dependency on a seating mod.
 *
 * This feature does not change the world's naturalRegeneration gamerule automatically:
 * during tests, use /gamerule naturalRegeneration false to separate food from healing.
 * Sitz mods using invisible mount entities are recognized; animation-only mods require
 * a dedicated integration when the installed mod is identified.
 */
public final class HudRestServerEvents {
    private static final int CAMPFIRE_RADIUS = 4;
    private static final int CAMPFIRE_HEAL_INTERVAL = 120; // 6 seconds
    private static final int SITTING_HEAL_INTERVAL = 80;  // 4 seconds
    private static final int BOTH_HEAL_INTERVAL = 60;     // 3 seconds
    private static final int SLEEP_TICKS_REQUIRED = 80;   // prevent quick bed cancel exploitation
    private static final String SLEEP_TICKS = "lux_rest_sleep_ticks";

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return;
        var data = player.getPersistentData();

        if (player.isSleeping()) {
            data.putInt(SLEEP_TICKS, Math.min(SLEEP_TICKS_REQUIRED, data.getInt(SLEEP_TICKS) + 1));
            return;
        }
        int slept = data.getInt(SLEEP_TICKS);
        if (slept > 0) {
            data.remove(SLEEP_TICKS);
            if (slept >= SLEEP_TICKS_REQUIRED) {
                player.heal(player.getMaxHealth());
            }
        }

        // Survival healing requires some food; it never consumes hunger.
        if (player.isCreative() || player.isSpectator()
                || player.getFoodData().getFoodLevel() <= 0
                || player.getHealth() >= player.getMaxHealth()) return;

        // Avoid scanning blocks on every tick.
        if (player.tickCount % 20 != 0) return;

        boolean seated = isRestingOnSeat(player);
        boolean warm = hasLitCampfire(player);
        int interval = seated && warm ? BOTH_HEAL_INTERVAL
                : seated ? SITTING_HEAL_INTERVAL
                : warm ? CAMPFIRE_HEAL_INTERVAL : 0;
        if (interval != 0 && player.tickCount % interval == 0) {
            player.heal(1.0F); // Half a heart per pulse
        }
    }

    /** Generic compatibility for seating mods that mount an invisible seat entity. */
    private static boolean isRestingOnSeat(ServerPlayer player) {
        return player.isPassenger() && !(player.getVehicle() instanceof LivingEntity);
    }

    private static boolean hasLitCampfire(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-CAMPFIRE_RADIUS, -2, -CAMPFIRE_RADIUS),
                center.offset(CAMPFIRE_RADIUS, 2, CAMPFIRE_RADIUS))) {
            BlockState state = player.level().getBlockState(pos);
            if (state.getBlock() instanceof CampfireBlock
                    && state.getValue(CampfireBlock.LIT)
                    && pos.distSqr(center) <= CAMPFIRE_RADIUS * CAMPFIRE_RADIUS) {
                return true;
            }
        }
        return false;
    }
}
