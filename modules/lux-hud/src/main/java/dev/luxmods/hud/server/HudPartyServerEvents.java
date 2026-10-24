package dev.luxmods.hud.server;

import dev.luxmods.hud.HudPartyCodec;
import dev.luxmods.hud.network.HudPartySnapshotPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.List;

/**
 * Explicit existing scoreboard teams stand in for parties during MVP.
 * This never exposes all online players' private health information.
 */
public final class HudPartyServerEvents {
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer viewer) || viewer.tickCount % 20 != 0) {
            return;
        }

        Team group = viewer.getTeam();
        List<HudPartyCodec.Member> roster = group == null ? List.of() :
                viewer.server.getPlayerList().getPlayers().stream()
                        .filter(other -> other != viewer && other.getTeam() == group)
                        .sorted(Comparator.comparing(other -> other.getName().getString()))
                        .limit(HudPartyCodec.MAX_MEMBERS)
                        .map(other -> new HudPartyCodec.Member(
                                other.getUUID(),
                                other.getGameProfile().getName(),
                                other.getHealth(), other.getMaxHealth(), other.experienceLevel
                        ))
                        .toList();

        // Empty roster explicitly clears any team that was left or disbanded.
        PacketDistributor.sendToPlayer(viewer,
                new HudPartySnapshotPayload(HudPartyCodec.encode(roster)));
    }
}
