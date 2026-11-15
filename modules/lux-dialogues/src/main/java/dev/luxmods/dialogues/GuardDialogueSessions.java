package dev.luxmods.dialogues;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Guards hold their position and face the player only during an active dialogue.
 * Combat, distance, logout and closing the screen release the guard immediately.
 * Patrol/navigation resumes normally after the session; no AI goals are removed.
 */
public final class GuardDialogueSessions {
    private record Session(UUID player, long expires) {}
    private static final Map<UUID, Session> ACTIVE = new HashMap<>();
    private GuardDialogueSessions() {}

    public static void start(Mob guard, ServerPlayer player) {
        if (guard.getTarget() != null || player.distanceToSqr(guard) > 64D) return;
        ACTIVE.put(guard.getUUID(), new Session(player.getUUID(), guard.level().getGameTime() + 600L));
        hold(guard, player);
    }

    private static void hold(Mob guard, ServerPlayer player) {
        // Stop navigation without teleporting the guard or disabling its AI.
        // Preserve vertical velocity for gravity and swimming.
        guard.getNavigation().stop();
        var velocity = guard.getDeltaMovement();
        guard.setDeltaMovement(0.0D, velocity.y, 0.0D);

        double dx = player.getX() - guard.getX();
        double dz = player.getZ() - guard.getZ();
        if (dx * dx + dz * dz > 0.0001D) {
            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
            guard.setYRot(yaw);
            guard.setYHeadRot(yaw);
            guard.setYBodyRot(yaw);
        }
        guard.getLookControl().setLookAt(player, 30.0F, 30.0F);
    }

    public static void finish(Mob guard, ServerPlayer player) {
        Session session = ACTIVE.get(guard.getUUID());
        if (session != null && session.player().equals(player.getUUID())) {
            ACTIVE.remove(guard.getUUID());
        }
    }

    public static void onTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Mob guard) || !DialogueNpcTypes.isGuard(guard)
                || entity.level().isClientSide()) return;
        Session session = ACTIVE.get(guard.getUUID());
        if (session == null) return;

        if (!guard.isAlive() || guard.getTarget() != null
                || guard.level().getGameTime() > session.expires()) {
            ACTIVE.remove(guard.getUUID());
            return;
        }
        var server = guard.level().getServer();
        if (server == null) return;
        ServerPlayer player = server.getPlayerList().getPlayer(session.player());
        if (player == null || player.level() != guard.level()
                || player.distanceToSqr(guard) > 64D) {
            ACTIVE.remove(guard.getUUID());
            return;
        }
        hold(guard, player);
    }
}
