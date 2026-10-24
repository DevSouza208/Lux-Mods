package dev.luxmods.discovery;

import dev.luxmods.discovery.network.DiscoveryNetwork;
import dev.luxmods.discovery.server.DiscoveryServerEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Mod de descoberta: o servidor persiste eventos; o cliente só os apresenta. */
@Mod(LuxDiscovery.MOD_ID)
public final class LuxDiscovery {
    public static final String MOD_ID = "lux_discovery";
    private static final Logger LOGGER = LoggerFactory.getLogger(LuxDiscovery.class);

    public LuxDiscovery(IEventBus modBus, ModContainer container) {
        DiscoveryAttachments.TYPES.register(modBus);
        modBus.addListener(DiscoveryNetwork::register);

        container.registerConfig(ModConfig.Type.CLIENT, DiscoveryConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, DiscoveryServerConfig.SPEC);

        NeoForge.EVENT_BUS.register(new DiscoveryServerEvents());
        LOGGER.info("Lux Discovery carregado (histórico persistente por personagem).");
    }
}
