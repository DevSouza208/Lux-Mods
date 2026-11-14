package dev.luxmods.atlas;

import dev.luxmods.atlas.client.AtlasClientLayers;
import dev.luxmods.atlas.client.AtlasMapKey;
import dev.luxmods.atlas.client.AtlasWalkDiscovery;
import dev.luxmods.atlas.client.AtlasHorseMemory;
import dev.luxmods.atlas.client.AtlasDiscoveryMarkers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Lux Atlas depends only on Engine; all rendering lives on the physical client. */
@Mod(LuxAtlas.MOD_ID)
public final class LuxAtlas {
    public static final String MOD_ID = "lux_atlas";
    private static final Logger LOGGER = LoggerFactory.getLogger(LuxAtlas.class);

    public LuxAtlas(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, AtlasConfig.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.register(modBus);
        }
        LOGGER.info("Lux Atlas carregado.");
    }

    private static final class ClientSetup {
        private static void register(IEventBus modBus) {
            modBus.addListener(AtlasClientLayers::register);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(AtlasMapKey::onTick);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(AtlasWalkDiscovery::onTick);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(AtlasWalkDiscovery::onLogout);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(AtlasHorseMemory::onTick);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(AtlasDiscoveryMarkers::onLocation);
        }
    }
}
