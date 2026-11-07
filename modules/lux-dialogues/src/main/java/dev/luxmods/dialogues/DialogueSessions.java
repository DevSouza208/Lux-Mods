package dev.luxmods.dialogues;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Server-authoritative dialogue pause, restored on close, distance, or timeout. */
public final class DialogueSessions {
    private record Session(UUID player, long expires, double x, double y, double z) {}
    private static final Map<UUID, Session> ACTIVE = new HashMap<>();
    private DialogueSessions() {}

    public static void start(Villager villager, ServerPlayer player) {
        ACTIVE.put(villager.getUUID(), new Session(player.getUUID(),
                villager.level().getGameTime() + 1200L,
                villager.getX(), villager.getY(), villager.getZ()));
        hold(villager, player, ACTIVE.get(villager.getUUID()));
    }

    public static void finish(Villager villager, ServerPlayer player) {
        Session session = ACTIVE.get(villager.getUUID());
        if (session != null && session.player().equals(player.getUUID())) {
            ACTIVE.remove(villager.getUUID());
        }
    }

    private static void hold(Villager villager, ServerPlayer player, Session session) {
        villager.getNavigation().stop();
        villager.setDeltaMovement(0.0D, 0.0D, 0.0D);
        villager.setPos(session.x(), session.y(), session.z());
        double dx = player.getX() - villager.getX();
        double dz = player.getZ() - villager.getZ();
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
        villager.setYRot(yaw);
        villager.setYHeadRot(yaw);
        villager.setYBodyRot(yaw);
        villager.getLookControl().setLookAt(player, 30.0f, 30.0f);
    }

    public static void onTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Villager villager) || entity.level().isClientSide()) return;
        Session session = ACTIVE.get(villager.getUUID());
        if (session == null) return;
        if (villager.level().getGameTime() > session.expires() || !villager.isAlive()) {
            ACTIVE.remove(villager.getUUID());
            return;
        }
        ServerPlayer player = villager.level().getServer().getPlayerList().getPlayer(session.player());
        if (player == null || player.level() != villager.level() || player.distanceToSqr(villager) > 64.0D) {
            ACTIVE.remove(villager.getUUID());
            return;
        }
        hold(villager, player, session);
    }
}
