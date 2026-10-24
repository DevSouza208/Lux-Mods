package dev.luxmods.hud;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Preferences are local to each client and never change server gameplay. */
public final class HudConfig {
    public enum Mode { CLASSIC, RPG }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.EnumValue<Mode> MODE = BUILDER
            .comment("CLASSIC: interface original do Minecraft; RPG: painel Lux.")
            .defineEnum("mode", Mode.RPG);

    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_BARS = BUILDER
            .comment("Ocultar barras vanilla de vida, fome e XP quando o painel RPG estiver visivel.")
            .define("replaceVanillaBars", true);

    public static final ModConfigSpec.BooleanValue SHOW_EFFECTS = BUILDER
            .comment("Exibir icones dos efeitos ativos perto da barra de vida.")
            .define("showEffects", true);

    public static final ModConfigSpec.BooleanValue SHOW_TEAMMATES = BUILDER
            .comment("Mostrar somente colegas pertencentes ao mesmo time do scoreboard.")
            .define("showTeammates", true);

    public static final ModConfigSpec.BooleanValue SHOW_QUESTS = BUILDER
            .comment("Mostrar rastreador de missoes a direita; sem tarefas ficticias.")
            .define("showQuests", true);

    public static final ModConfigSpec.BooleanValue DECORATE_HOTBAR = BUILDER
            .comment("Moldura pixel art Clean Fantasy na hotbar, sem substituir os slots/items vanilla.")
            .define("decorateHotbar", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private HudConfig() {
    }
}
