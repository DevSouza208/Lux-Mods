package dev.luxmods.dialogues;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;

/** Optional, registry-only Guard Villagers compatibility: no class dependency. */
public final class DialogueNpcTypes {
    private DialogueNpcTypes() {}

    public static boolean isGuard(Entity entity) {
        if (!(entity instanceof Mob)) return false;
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return "guardvillagers".equals(id.getNamespace())
                && ("guard".equals(id.getPath()) || id.getPath().contains("guard"));
    }

    public static boolean supported(Entity entity) {
        return entity instanceof Villager || isGuard(entity);
    }

    public static boolean guardInCombat(Entity entity) {
        return entity instanceof Mob mob && isGuard(entity) && mob.getTarget() != null;
    }
}
