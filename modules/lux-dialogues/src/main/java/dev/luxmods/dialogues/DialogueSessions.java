package dev.luxmods.dialogues;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Temporary sessions: no permanent AI disable or persistent villager changes. */
public final class DialogueSessions {
    private record Session(UUID player, long expires) {}
    private static final Map<UUID, Session> ACTIVE = new HashMap<>();
    private DialogueSessions() {}

    public static void start(Villager villager, ServerPlayer player) {
        ACTIVE.put(villager.getUUID(), new Session(player.getUUID(), villager.level().getGameTime() + 200L));
        villager.getNavigation().stop();
        villager.getLookControl().setLookAt(player, 30f, 30f);
    }

    public static void finish(Villager villager, ServerPlayer player) {
        Session session = ACTIVE.get(villager.getUUID());
        if (session != null && session.player().equals(player.getUUID())) {
            ACTIVE.remove(villager.getUUID());
        }
    }

    public static void onTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Villager villager) || entity.level().isClientSide()) return;
        Session session = ACTIVE.get(villager.getUUID());
        if (session == null) return;
        if (villager.level().getGameTime() > session.expires() || !villager.isAlive()) {
            ACTIVE.remove(villager.getUUID()); return;
        }
        if (!(villager.level().getServer().getPlayerList().getPlayer(session.player()) instanceof ServerPlayer player)
                || player.level() != villager.level() || player.distanceToSqr(villager) > 64.0D) {
            ACTIVE.remove(villager.getUUID()); return;
        }
        villager.getNavigation().stop();
        villager.getLookControl().setLookAt(player, 30f, 30f);
    }
}
