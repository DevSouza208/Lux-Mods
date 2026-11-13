package dev.luxmods.hud;

import dev.luxmods.hud.client.HudClientLayers;
import dev.luxmods.hud.client.HudScaleKey;
import dev.luxmods.hud.client.HudHorseCamera;
import dev.luxmods.hud.client.HudVanillaLayers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Player-centric HUD; optional alongside Discovery, Atlas, and all future modules.
 * This common entrypoint does not initialize any client UI on dedicated servers.
 */
@Mod(LuxHud.MOD_ID)
public final class LuxHud {
    public static final String MOD_ID = "lux_hud";
    private static final Logger LOGGER = LoggerFactory.getLogger(LuxHud.class);

    public LuxHud(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, HudConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, HudServerConfig.SPEC);
        dev.luxmods.hud.server.HudHealingItems.ITEMS.register(modBus);
        modBus.addListener(dev.luxmods.hud.network.HudNetwork::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(
                new dev.luxmods.hud.server.HudPartyServerEvents());
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(
                new dev.luxmods.hud.server.HudQuestServerEvents());
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(
                new dev.luxmods.hud.server.HudRestServerEvents());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.register(modBus);
        }
        LOGGER.info("Lux HUD carregado.");
    }

    private static final class ClientSetup {
        private static void register(IEventBus modBus) {
            modBus.addListener(HudClientLayers::register);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(HudScaleKey::onTick);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(HudHorseCamera::onTick);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(HudHorseCamera::onCamera);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(HudVanillaLayers::onPre);
        }
    }
}
