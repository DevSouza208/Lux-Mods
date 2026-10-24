package dev.luxmods.atlas.client;

import dev.luxmods.atlas.LuxAtlas;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Loaded only on physical clients via LuxAtlas.ClientSetup. */
public final class AtlasClientLayers {
    private AtlasClientLayers() {
    }

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LuxAtlas.MOD_ID, "compass"),
                AtlasCompassHud::render
        );
    }
}
