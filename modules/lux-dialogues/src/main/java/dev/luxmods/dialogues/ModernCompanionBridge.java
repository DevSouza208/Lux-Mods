package dev.luxmods.dialogues;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter for ModernCompanions v4.45. The optional mod is never linked at
 * class-load time. The MOD's own mobInteract performs each recruitment step,
 * including real item consumption, bonding, taming, sound and save data.
 * No duplicate Lux quest, fake acceptance or locally changed tame state.
 */
public final class ModernCompanionBridge {
    public record Material(String item, int remaining) {}
    public record State(boolean recruited, boolean owner,
                        boolean following, boolean patrolling, boolean guarding,
                        List<Material> materials) {}

    private ModernCompanionBridge() {}

    public static boolean supported(Entity entity) {
        if (!(entity instanceof TamableAnimal) || !DialogueNpcTypes.isCompanion(entity)) return false;
        return "modern_companions".equals(
                BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace());
    }

    private static Method method(Class<?> type, String name, Class<?>... parameters)
            throws NoSuchMethodException {
        for (Class<?> cursor = type; cursor != null; cursor = cursor.getSuperclass()) {
            try {
                Method method = cursor.getDeclaredMethod(name, parameters);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                // Method can be inherited from an intermediate companion type.
            }
        }
        throw new NoSuchMethodException(name);
    }

    @SuppressWarnings("unchecked")
    private static Map<Item, Integer> requirements(Entity npc) throws ReflectiveOperationException {
        return (Map<Item, Integer>) method(npc.getClass(), "getFoodRequirements").invoke(npc);
    }

    /** Initializes real recruitment requirements without sending chat messages. */
    private static void ensureRequirements(Entity npc, ServerPlayer player)
            throws ReflectiveOperationException {
        if (!(npc instanceof TamableAnimal animal) || animal.isTame()) return;
        if (requirements(npc).isEmpty()) {
            method(npc.getClass(), "assignFoodRequirements",
                    net.minecraft.world.entity.player.Player.class).invoke(npc, player);
        }
    }

    public static State state(Entity npc, ServerPlayer player) {
        if (!supported(npc) || !npc.isAlive() || npc.distanceToSqr(player) > 64D)
            return null;
        try {
            TamableAnimal animal = (TamableAnimal) npc;
            ensureRequirements(npc, player);
            List<Material> materials = new ArrayList<>();
            if (!animal.isTame()) {
                for (var entry : requirements(npc).entrySet()) {
                    if (materials.size() >= 16) break; // bounded packet size
                    materials.add(new Material(
                            BuiltInRegistries.ITEM.getKey(entry.getKey()).toString(),
                            Math.max(0, entry.getValue())));
                }
            }
            boolean owner = animal.isTame() && player.getUUID().equals(animal.getOwnerUUID());
            boolean following = (boolean) method(npc.getClass(), "isFollowing").invoke(npc);
            boolean patrolling = (boolean) method(npc.getClass(), "isPatrolling").invoke(npc);
            boolean guarding = (boolean) method(npc.getClass(), "isGuarding").invoke(npc);
            return new State(animal.isTame(), owner, following, patrolling, guarding,
                    List.copyOf(materials));
        } catch (ReflectiveOperationException | ClassCastException ex) {
            // Keep the factual ownership state even if an optional mod method
            // changed. The Lux dialogue will indicate unknown requirements
            // instead of hanging indefinitely on "loading".
            TamableAnimal animal = (TamableAnimal) npc;
            return new State(animal.isTame(),
                    animal.isTame() && player.getUUID().equals(animal.getOwnerUUID()),
                    false, false, false, List.of());
        }
    }

    /**
     * Real single-item delivery from player inventory. A temporary server-side
     * selected-slot swap delegates the item use to the installed companion's
     * own interaction method, then restores the original selected item.
     * Executed only from a validated network handler on the server main thread.
     */
    public static boolean deliverOne(Entity npc, ServerPlayer player, int materialIndex) {
        if (!supported(npc) || player.isSpectator() || !npc.isAlive()
                || npc.distanceToSqr(player) > 64D || materialIndex < 0) return false;
        State current = state(npc, player);
        if (current == null || current.recruited()
                || materialIndex >= current.materials().size()) return false;
        Material material = current.materials().get(materialIndex);
        if (material.remaining() <= 0) return false;

        var items = player.getInventory();
        int source = -1;
        for (int slot = 0; slot < items.getContainerSize(); slot++) {
            ItemStack stack = items.getItem(slot);
            if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem())
                    .toString().equals(material.item())) {
                source = slot;
                break;
            }
        }
        if (source < 0) return false;
        int selected = items.selected;
        ItemStack priorHand = items.getItem(selected);
        ItemStack originalSource = items.getItem(source);
        if (source != selected) {
            items.setItem(selected, originalSource);
            items.setItem(source, ItemStack.EMPTY);
        }
        int before = originalSource.getCount();
        try {
            // No forged resources: the native mod shrinks this exact stack.
            method(npc.getClass(), "mobInteract",
                    net.minecraft.world.entity.player.Player.class,
                    InteractionHand.class).invoke(npc, player, InteractionHand.MAIN_HAND);
            return originalSource.getCount() < before;
        } catch (ReflectiveOperationException ex) {
            return false;
        } finally {
            if (source != selected) {
                items.setItem(source, items.getItem(selected));
                items.setItem(selected, priorHand);
            }
            items.setChanged();
        }
    }

    public static boolean setOrder(Entity npc, ServerPlayer player, int order) {
        State current = state(npc, player);
        if (current == null || !current.owner() || !(npc instanceof Mob mob)) return false;
        if (order < 0 || order > 2) return false;
        try {
            boolean follow = order == 0, guard = order == 1, patrol = order == 2;
            method(npc.getClass(), "setFollowing", boolean.class).invoke(npc, follow);
            method(npc.getClass(), "setGuarding", boolean.class).invoke(npc, guard);
            method(npc.getClass(), "setPatrolling", boolean.class).invoke(npc, patrol);
            mob.getNavigation().stop();
            if (npc instanceof TamableAnimal animal) animal.setOrderedToSit(false);
            return true;
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    /** Equipment stays native until its inventory slots can be reskinned safely. */
    public static boolean openNativeInventory(Entity npc, ServerPlayer player) {
        State current = state(npc, player);
        if (current == null || !current.owner()) return false;
        try {
            method(npc.getClass(), "openGui", ServerPlayer.class).invoke(npc, player);
            return true;
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }
}
