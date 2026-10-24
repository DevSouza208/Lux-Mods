package dev.luxmods.menu;

import dev.luxmods.menu.client.LuxMenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client-side cosmetic enhancement of vanilla title and pause menus.
 * Completely optional except for Lux Engine. Never replaces the screen itself.
 */
@Mod(LuxMenu.MOD_ID)
public final class LuxMenu {
    public static final String MOD_ID = "lux_menu";
    private static final Logger LOGGER = LoggerFactory.getLogger(LuxMenu.class);

    public LuxMenu(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, MenuConfig.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.register();
        }
        LOGGER.info("Lux Menu carregado (menus vanilla preservados).");
    }

    private static final class ClientSetup {
        private static void register() {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(LuxMenuScreens::onInit);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(LuxMenuScreens::onRender);
        }
    }
}
