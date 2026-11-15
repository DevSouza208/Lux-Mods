package dev.luxmods.engine.client;

import dev.luxmods.engine.api.LuxIntroQuestState;
import dev.luxmods.engine.api.LuxUpgradeState;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/** Clear both read-only client snapshots when changing world or server. */
public final class LuxProgressClientEvents {
    private LuxProgressClientEvents() {}

    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        LuxIntroQuestState.clear();
        LuxUpgradeState.clear();
    }
}
