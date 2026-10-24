package dev.luxmods.menu.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoadingScreenStatusTest {
    @Test void matchesOnlyTheVanillaInterstititalTranslationKeys() {
        assertTrue(LoadingScreenStatus.isKnownInterstitial("menu.savingLevel"));
        assertTrue(LoadingScreenStatus.isKnownInterstitial("menu.savingChunks"));
        assertTrue(LoadingScreenStatus.isKnownInterstitial("selectWorld.resource_load"));
        assertFalse(LoadingScreenStatus.isKnownInterstitial("multiplayer.disconnect.generic"));
        assertFalse(LoadingScreenStatus.isKnownInterstitial("selectWorld.datapackFailure"));
        assertFalse(LoadingScreenStatus.isKnownInterstitial(""));
        assertFalse(LoadingScreenStatus.isKnownInterstitial("menu.quit"));
    }
}
