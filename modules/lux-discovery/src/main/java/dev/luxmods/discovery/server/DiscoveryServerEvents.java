package dev.luxmods.discovery.server;

import dev.luxmods.discovery.DiscoveryAttachments;
import dev.luxmods.discovery.DiscoveryDebouncer;
import dev.luxmods.discovery.DiscoveryLedger;
import dev.luxmods.discovery.DiscoveryServerConfig;
import dev.luxmods.discovery.network.BiomeDiscoveredPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * O servidor decide o que existe e o que o jogador descobriu.
 * Tick é somente de jogador online; não busca chunks distantes.
 */
public final class DiscoveryServerEvents {
    private final Map<UUID, DiscoveryDebouncer> sessions = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var biome = player.serverLevel().getBiome(player.blockPosition()).unwrapKey();
        if (biome.isEmpty()) return;

        String dimensionId = player.serverLevel().dimension().location().toString();
        String biomeId = biome.orElseThrow().location().toString();

        var tracker = sessions.computeIfAbsent(player.getUUID(), id -> new DiscoveryDebouncer());
        tracker.tick(
                dimensionId, biomeId,
                DiscoveryServerConfig.STABLE_TICKS.get(),
                DiscoveryServerConfig.COOLDOWN_TICKS.get()
        ).ifPresent(id -> recordAndAnnounce(player, dimensionId, id));
    }

    private void recordAndAnnounce(ServerPlayer player, String dimensionId, String biomeId) {
        Set<String> old = player.getData(DiscoveryAttachments.SEEN_BIOMES);
        DiscoveryLedger.MarkResult result = DiscoveryLedger.mark(old, dimensionId, biomeId);

        // O servidor guarda a primeira descoberta. Itens conhecidos não anunciam de novo.
        if (!result.firstVisit()) return;

        player.setData(DiscoveryAttachments.SEEN_BIOMES, result.saved());
        PacketDistributor.sendToPlayer(player, new BiomeDiscoveredPayload(biomeId));
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        sessions.remove(event.getEntity().getUUID());
    }
}
