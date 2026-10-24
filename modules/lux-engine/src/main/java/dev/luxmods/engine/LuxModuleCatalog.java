package dev.luxmods.engine;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Catálogo estático de IDs Lux reconhecidos na primeira fase.
 * isInstalled só depende de um Predicado para ser testável sem Minecraft.
 */
public final class LuxModuleCatalog {
    public static final List<String> KNOWN_MODULE_IDS = List.of(
            "lux_engine", "lux_discovery", "lux_atlas", "lux_hud",
            "lux_npc", "lux_lore", "lux_quests", "lux_villages",
            "lux_progression", "lux_inventory", "lux_items", "lux_food",
            "lux_materials");

    private LuxModuleCatalog() {
    }

    public static List<String> installedModules(Predicate<String> isInstalled) {
        Objects.requireNonNull(isInstalled, "isInstalled");
        return KNOWN_MODULE_IDS.stream().filter(isInstalled).toList();
    }
}
