package dev.luxmods.discovery;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Validação de descobertas ocorre no servidor, mesmo com HUD desativado. */
public final class DiscoveryServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue STABLE_TICKS = BUILDER
            .comment("Quantos ticks dentro do mesmo bioma antes de registrar uma descoberta.")
            .defineInRange("stableBiomeTicks", 35, 5, 200);

    public static final ModConfigSpec.IntValue COOLDOWN_TICKS = BUILDER
            .comment("Intervalo mínimo entre anúncios para o mesmo jogador.")
            .defineInRange("titleCooldownTicks", 120, 0, 1200);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private DiscoveryServerConfig() {
    }
}
