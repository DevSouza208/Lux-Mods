package dev.luxmods.discovery;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LuxDiscovery.MOD_ID)
public final class LuxDiscovery {
    public static final String MOD_ID = "lux_discovery";
    private static final Logger LOGGER = LoggerFactory.getLogger(LuxDiscovery.class);

    public LuxDiscovery(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, DiscoveryConfig.SPEC);
        LOGGER.info("Lux Discovery carregado (protótipo de biomas).");
    }
}
