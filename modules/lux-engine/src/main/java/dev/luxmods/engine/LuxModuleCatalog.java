package dev.luxmods.engine;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Exact mod IDs currently shipped as standalone JARs by assembleTestPack.
 * A catalog entry does not force the module to be installed.
 *
 * This list is deliberately small: planned modules must not be reported as
 * implemented, and the removed lux_atlas must never return.
 */
public final class LuxModuleCatalog {
    public static final List<String> KNOWN_MODULE_IDS = List.of(
            "lux_engine",
            "lux_discovery",
            "lux_compass",
            "lux_quests",
            "lux_upgrades",
            "lux_hud",
            "lux_menu",
            "lux_borderless",
            "lux_dialogues"
    );

    private LuxModuleCatalog() {}

    /** Pure query; no Minecraft dependency, reflection, or mod loading. */
    public static List<String> installedModules(Predicate<String> isInstalled) {
        Objects.requireNonNull(isInstalled, "isInstalled");
        return KNOWN_MODULE_IDS.stream().filter(isInstalled).toList();
    }
}
