package dev.luxmods.engine;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class EngineConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_LOADED_MODULES = BUILDER
            .comment("Lista os módulos Lux quando um servidor inicia.")
            .define("logLoadedModules", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private EngineConfig() {
    }
}
