package dev.luxmods.menu.client;

import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * Maps only well-known vanilla interstitials. Never match arbitrary
 * text ("loading", "saving") since failures/confirmation screens may
 * contain similar wording and must remain readable and clickable.
 */
public final class LoadingScreenStatus {
    private LoadingScreenStatus() { }

    public static boolean isKnownInterstitial(String translationKey) {
        return "menu.savingLevel".equals(translationKey)
                || "menu.savingChunks".equals(translationKey)
                || "selectWorld.resource_load".equals(translationKey);
    }

    public static String labelFor(Screen screen) {
        if (screen instanceof GenericMessageScreen
                && screen.getTitle().getContents() instanceof TranslatableContents contents) {
            return switch (contents.getKey()) {
                case "menu.savingLevel", "menu.savingChunks" -> "SALVANDO MUNDO";
                case "selectWorld.resource_load" -> "PREPARANDO RECURSOS";
                default -> "CARREGANDO";
            };
        }
        return "CARREGANDO";
    }
}
