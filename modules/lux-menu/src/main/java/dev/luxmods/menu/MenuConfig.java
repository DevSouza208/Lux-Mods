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

    public static final ModConfigSpec.BooleanValue STYLE_SUBMENUS = BUILDER
            .comment("Aplica somente o estilo dos botoes vanilla nas telas Opcoes, Mundos e Multiplayer.")
            .define("styleSubmenus", true);

    public static final ModConfigSpec.BooleanValue SHOW_BRANDING = BUILDER
            .comment("Exibe a assinatura discreta do Lux Mods sem alterar o logo Minecraft.")
            .define("showBranding", true);

    public static final ModConfigSpec.BooleanValue LOADING_SLIDESHOW = BUILDER
            .comment("Fotos com crossfade ao entrar/criar mundos. Nao altera a tela vermelha FML inicial.")
            .define("loadingSlideshow", true);

    public static final ModConfigSpec.IntValue SLIDE_SECONDS = BUILDER
            .comment("Segundos por foto, sem incluir o tempo de transicao.")
            .defineInRange("slideSeconds", 5, 2, 20);

    public static final ModConfigSpec.IntValue FADE_MILLIS = BUILDER
            .comment("Duracao da transicao suave entre fotos, em milissegundos.")
            .defineInRange("fadeMillis", 1200, 200, 3000);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private MenuConfig() { }
}
