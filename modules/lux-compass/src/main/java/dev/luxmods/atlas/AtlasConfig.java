package dev.luxmods.atlas;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client compass options, anchored above the hotbar by default. */
public final class AtlasConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_COMPASS = BUILDER
            .comment("Mostrar a bussola na HUD?")
            .define("showCompass", true);

    public static final ModConfigSpec.BooleanValue SNAP_TO_HOTBAR = BUILDER
            .comment("Fixar a bussola acima da hotbar (Sim) ou usar distancia personalizada (Nao).")
            .define("snapToHotbar", true);

    public static final ModConfigSpec.IntValue DISTANCE_FROM_BOTTOM = BUILDER
            .comment("Distancia da base da tela quando Snap To Hotbar = Nao.")
            .defineInRange("distanceFromBottom", 47, 32, 200);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private AtlasConfig() { }
}
