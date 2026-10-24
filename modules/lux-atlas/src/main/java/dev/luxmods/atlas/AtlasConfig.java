package dev.luxmods.atlas;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Per-client GUI preferences, never stored in world saves. */
public final class AtlasConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_COMPASS = BUILDER
            .comment("Mostrar a bussola na HUD?")
            .define("showCompass", true);

    public static final ModConfigSpec.IntValue DISTANCE_FROM_BOTTOM = BUILDER
            .comment("Distancia em pixels da base da tela ate a bussola.")
            .defineInRange("distanceFromBottom", 84, 48, 200);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private AtlasConfig() {
    }
}
