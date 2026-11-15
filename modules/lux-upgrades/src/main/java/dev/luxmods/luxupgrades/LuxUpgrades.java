package dev.luxmods.luxupgrades;

import dev.luxmods.upgrades.LuxUpgradeNetwork;
import dev.luxmods.upgrades.LuxUpgradeServerEvents;
import dev.luxmods.upgrades.client.LuxUpgradeShortcut;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/** Optional upgrades. No J/U shortcuts, and no HUD overlay. */
@Mod("lux_upgrades")
public final class LuxUpgrades {
    public LuxUpgrades(IEventBus modBus) {
        modBus.addListener(LuxUpgradeNetwork::register);
        NeoForge.EVENT_BUS.register(new LuxUpgradeServerEvents());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(LuxUpgradeShortcut::register);
            NeoForge.EVENT_BUS.addListener(LuxUpgradeShortcut::onTick);
        }
    }
}
