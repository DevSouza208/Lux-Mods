package dev.luxmods.hud;

import net.neoforged.neoforge.common.ModConfigSpec;

/** All display preferences are local; no server packets or save data. */
public final class HudConfig {
    public enum Mode { CLASSIC, RPG }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<Mode> MODE = BUILDER
            .comment("CLASSIC usa as barras do Minecraft. RPG usa o painel de personagem Lux.")
            .defineEnum("mode", Mode.RPG);

    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_BARS = BUILDER
            .comment("No modo RPG, esconde barras de vida, fome e XP vanilla para evitar duplicacao.")
            .define("replaceVanillaBars", true);

    public static final ModConfigSpec.SPEC SPEC = null;
}
