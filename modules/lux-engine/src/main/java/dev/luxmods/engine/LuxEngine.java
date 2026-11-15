package dev.luxmods.engine;

import dev.luxmods.engine.api.LuxEventBus;
import dev.luxmods.engine.api.LuxServiceRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal, stable Lux platform.
 *
 * Engine does not own gameplay, player ticks, HUD layers, keybindings,
 * world generation, quest purchases or third-party mod integrations.
 * Modules may call the established EVENTS and SERVICES contracts without
 * having to load any other optional Lux module.
 */
@Mod(LuxEngine.MOD_ID)
public final class LuxEngine {
    public static final String MOD_ID = "lux_engine";

    // Keep these public instances for compatibility with installed modules.
    public static final LuxServiceRegistry SERVICES = new LuxServiceRegistry();
    public static final LuxEventBus EVENTS = new LuxEventBus();

    private static final Logger LOGGER = LoggerFactory.getLogger(LuxEngine.class);

    public LuxEngine(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, EngineConfig.SPEC);
        // Diagnostics only once when the logical server starts; never per tick.
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener(
                    dev.luxmods.engine.client.LuxProgressClientEvents::onLogout);
        }
        LOGGER.info("Lux Engine inicializado: base compartilhada, sem gameplay.");
    }

    private void onServerStarting(ServerStartingEvent event) {
        if (!EngineConfig.LOG_LOADED_MODULES.get()) return;
        var installed = LuxModuleCatalog.installedModules(ModList.get()::isLoaded);
        LOGGER.info("Lux Mods ativos ({}/{}): {}",
                installed.size(), LuxModuleCatalog.KNOWN_MODULE_IDS.size(), installed);
        LOGGER.info("Servicos Lux registrados: {}", SERVICES.registeredIds());
    }
}
