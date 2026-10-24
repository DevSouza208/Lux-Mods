package dev.luxmods.menu;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Local-only settings. Disable the effect without removing other Lux mods. */
public final class MenuConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Ativa a aparência Lux no menu principal e na tela de pausa.")
            .define("enabled", true);

    public static final ModConfigSpec.BooleanValue STYLE_BUTTONS = BUILDER
            .comment("Desenha os botoes comuns no tema Clean Fantasy; preserva as acoes vanilla.")
            .define("styleButtons", true);

    public static final ModConfigSpec.BooleanValue SHOW_BRANDING = BUILDER
            .comment("Exibe a assinatura discreta do Lux Mods sem alterar o logo Minecraft.")
            .define("showBranding", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private MenuConfig() { }
}
