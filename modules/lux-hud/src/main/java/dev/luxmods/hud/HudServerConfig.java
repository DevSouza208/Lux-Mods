package dev.luxmods.hud;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Explicit gameplay preference for Lux survival worlds. */
public final class HudServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue DISABLE_NATURAL_HEALING = BUILDER
            .comment("Lux Healing: disallow natural food/saturation healing. Medicine and sleep still restore health.")
            .define("disableNaturalHealing", true);
    public static final ModConfigSpec SPEC = BUILDER.build();
    private HudServerConfig() {}
}
