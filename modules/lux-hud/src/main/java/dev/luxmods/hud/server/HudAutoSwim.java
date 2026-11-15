package dev.luxmods.hud.server;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** In sufficiently deep water, automatically use the vanilla swimming pose.
 * Never touch speed, oxygen, invulnerability, or movement on dry land.
 */
public final class HudAutoSwim {
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.isAlive() || player.isSpectator() || player.isPassenger()
                || player.getAbilities().flying || !player.isInWater()
                || player.onGround()) return;
        // The player should be floating in deep water, not wading in a puddle.
        // Vanilla controls remain available and surfacing restores standing.
        if (player.isUnderWater()
                || player.getFluidHeight(FluidTags.WATER) > 1.05D) {
            player.setSwimming(true);
        }
    }
}
