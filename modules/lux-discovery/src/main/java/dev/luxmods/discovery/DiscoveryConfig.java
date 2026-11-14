package dev.luxmods.discovery;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Apenas apresentação visual. A descoberta é armazenada pelo servidor. */
public final class DiscoveryConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Exibir o título visual das descobertas? Desativar não apaga o histórico.")
            .define("showBiomeTitles", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private DiscoveryConfig() {
    }
}
