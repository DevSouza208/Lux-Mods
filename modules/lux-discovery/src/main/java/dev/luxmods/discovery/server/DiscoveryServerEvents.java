package dev.luxmods.discovery.server;

import dev.luxmods.discovery.DiscoveryAttachments;
import dev.luxmods.discovery.DiscoveryDebouncer;
import dev.luxmods.discovery.DiscoveryLedger;
import dev.luxmods.discovery.DiscoveryServerConfig;
import dev.luxmods.discovery.LuxDiscoveryEvents;
import dev.luxmods.engine.LuxEngine;
import dev.luxmods.discovery.network.BiomeDiscoveredPayload;
import dev.luxmods.discovery.network.StructureDiscoveredPayload;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
        if (player.tickCount % 80 == 0) discoverStructure(player);

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
        if (!result.firstVisit()) return;

        // Grava antes de emitir o evento; outros módulos observam um fato confirmado.
        player.setData(DiscoveryAttachments.SEEN_BIOMES, result.saved());
        LuxEngine.EVENTS.publish(
                LuxDiscoveryEvents.FIRST_BIOME,
                new LuxDiscoveryEvents.BiomeFirstDiscovered(player.getUUID(), dimensionId, biomeId)
        );
        PacketDistributor.sendToPlayer(player, new BiomeDiscoveredPayload(biomeId));
    }

    /**
     * Check structures already referenced by the player's current chunk only.
     * This never invokes /locate, searches unloaded chunks, or invents places.
     * Distinct generated instances are deduplicated by start chunk + id.
     */
    private static void discoverStructure(ServerPlayer player) {
        var level = player.serverLevel();
        var position = player.blockPosition();
        var structures = level.structureManager().getAllStructuresAt(position);
        if (structures.isEmpty()) return;
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (var structure : structures.keySet()) {
            var start = level.structureManager().getStructureAt(position, structure);
            if (!start.isValid() || !start.getBoundingBox().isInside(position)) continue;
            ResourceLocation id = registry.getKey(structure);
            if (id == null) continue;
            var chunk = start.getChunkPos();
            String occurrence = "structure:" + id + "@" + chunk.x + "," + chunk.z;
            String dimension = level.dimension().location().toString();
            var seen = player.getData(DiscoveryAttachments.SEEN_BIOMES);
            var result = DiscoveryLedger.mark(seen, dimension, occurrence);
            if (!result.firstVisit()) continue;
            player.setData(DiscoveryAttachments.SEEN_BIOMES, result.saved());
            PacketDistributor.sendToPlayer(player, new StructureDiscoveredPayload(
                    id.toString(), chunk.getMiddleBlockX(), chunk.getMiddleBlockZ()));
            break; // one title per sampling period, avoiding overlapping banners
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        sessions.remove(event.getEntity().getUUID());
    }
}
