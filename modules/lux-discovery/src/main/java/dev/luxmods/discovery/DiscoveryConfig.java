package dev.luxmods.discovery;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DiscoveryConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Exibir título quando um bioma novo for descoberto durante a sessão?")
            .define("showBiomeTitles", true);

    public static final ModConfigSpec.IntValue STABLE_TICKS = BUILDER
            .comment("Ticks estáveis num bioma antes de exibir o título.")
            .defineInRange("stableBiomeTicks", 35, 10, 200);

    public static final ModConfigSpec.IntValue TITLE_COOLDOWN_TICKS = BUILDER
            .comment("Intervalo mínimo entre títulos (ticks).")
            .defineInRange("titleCooldownTicks", 120, 20, 1200);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private DiscoveryConfig() {
    }
}
