package dev.luxmods.hud.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import dev.luxmods.hud.HudServerConfig;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Lux Rest: resting is not a substitute for medicine.
 *
 * Sitting, standing still and campfires do NOT heal. They may affect stamina
 * once Lux Stamina has been implemented. The only healing implemented by this
 * class is a full recovery after actually sleeping in a bed.
 *
 * Natural food/saturation regeneration is controlled separately by the world's
 * naturalRegeneration gamerule. Until Lux Healing is ready, use:
 * /gamerule naturalRegeneration false
 */
public final class HudRestServerEvents {
    private static final int MINIMUM_SLEEP_TICKS = 80;
    private static final String SLEEP_TICKS = "lux_rest_sleep_ticks";

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !HudServerConfig.DISABLE_NATURAL_HEALING.get()) return;
        // Explicit world rule, not a blanket cancellation of healing events.
        // Potions, curatives and bed recovery continue to work.
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
            return;
        }

        int ticks = data.getInt(SLEEP_TICKS);
        if (ticks <= 0) return;
        data.remove(SLEEP_TICKS);
        // Require a meaningful sleep duration; quick bed interactions do not heal.
        if (ticks >= MINIMUM_SLEEP_TICKS && !player.isCreative() && !player.isSpectator()) {
            player.heal(player.getMaxHealth());
        }
    }
}
