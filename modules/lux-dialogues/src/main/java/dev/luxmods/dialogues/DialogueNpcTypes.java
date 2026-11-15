package dev.luxmods.dialogues;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.Monster;

/** Optional, registry-only Guard Villagers compatibility: no class dependency. */
public final class DialogueNpcTypes {
    private DialogueNpcTypes() {}

    public static boolean isGuard(Entity entity) {
        if (!(entity instanceof Mob)) return false;
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return "guardvillagers".equals(id.getNamespace())
                && ("guard".equals(id.getPath()) || id.getPath().contains("guard"));
    }

    /**
     * Optional Companions! compatibility. Resolve by registry only, so worlds
     * without the mod do not acquire a hard dependency. Hostile monsters keep
     * their native interaction, as do companions currently in combat.
     */
    public static boolean isCompanion(Entity entity) {
        if (!(entity instanceof Mob mob) || entity instanceof Monster
                || mob.getTarget() != null) return false;
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return "companions".equals(id.getNamespace())
                || "companions_mod".equals(id.getNamespace());
    }

    public static boolean supported(Entity entity) {
        return entity instanceof Villager || isGuard(entity) || isCompanion(entity);
    }

    public static boolean guardInCombat(Entity entity) {
        return entity instanceof Mob mob && isGuard(entity) && mob.getTarget() != null;
    }
}
