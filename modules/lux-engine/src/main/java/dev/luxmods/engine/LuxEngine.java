package dev.luxmods.engine;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lux Engine 0.1: serviço mínimo de detecção. Não registra gameplay.
 */
@Mod(LuxEngine.MOD_ID)
public final class LuxEngine {
    public static final String MOD_ID = "lux_engine";
    private static final Logger LOGGER = LoggerFactory.getLogger(LuxEngine.class);

    public LuxEngine(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, EngineConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        LOGGER.info("Lux Engine carregado.");
    }

    private void onServerStarting(ServerStartingEvent event) {
        if (EngineConfig.LOG_LOADED_MODULES.get()) {
            LOGGER.info("Módulos Lux detectados: {}",
                    LuxModuleCatalog.installedModules(
                            id -> net.neoforged.fml.ModList.get().isLoaded(id)));
        }
    }
}
