package dev.luxmods.hud.server;

import dev.luxmods.hud.network.HudSleepSnapshotPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Lux Sleep: fatigue measured in in-game day time (24000 = one Minecraft day).
 * Independent of real time and the vanilla Phantom insomnia stat.
 *
 * Day 2: soft sleepy vignette. Day 3: heavy exhaustion and one brief faint.
 * Sleep resets the counter. Faint never kills, changes health or forces sleep.
 */
public final class HudSleepServerEvents {
    private static final int DAY = 24_000;
    private static final int SLEEPY = DAY * 2;
    private static final int FAINT_AT = DAY * 3;
    private static final int FAINT_DURATION = 100; // 5 seconds
    private static final int VALID_SLEEP = 80;

    private static final String AWAKE = "lux_sleep_awake";
    private static final String LAST_DAY_TIME = "lux_sleep_last_time";
    private static final String INITIALIZED = "lux_sleep_initialized";
    private static final String SLEEPING = "lux_sleep_bed_ticks";
    private static final String FAINT_LEFT = "lux_sleep_faint_left";
    private static final String FAINT_MARK = "lux_sleep_last_faint_at";

    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var data = player.getPersistentData();
            // Discard disconnected time. No offline exhaustion while the player is absent.
            data.putLong(LAST_DAY_TIME, player.serverLevel().getDayTime());
            data.putBoolean(INITIALIZED, true);
            data.putInt(FAINT_LEFT, 0);
            sync(player);
        }
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var previous = event.getOriginal().getPersistentData();
        var current = player.getPersistentData();
        current.putInt(AWAKE, previous.getInt(AWAKE));
        current.putInt(FAINT_MARK, previous.getInt(FAINT_MARK));
        current.putBoolean(INITIALIZED, false);
        current.remove(SLEEPING);
        current.putInt(FAINT_LEFT, 0);
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return;
        var data = player.getPersistentData();
        long dayTime = player.serverLevel().getDayTime();
        if (!data.getBoolean(INITIALIZED)) {
            data.putLong(LAST_DAY_TIME, dayTime);
            data.putBoolean(INITIALIZED, true);
        }

        long last = data.getLong(LAST_DAY_TIME);
        data.putLong(LAST_DAY_TIME, dayTime);
        // Time manipulation backwards does not count as fatigue. A forward
        // /time set or sleep skip of more than one day is safely capped.
        long delta = Math.max(0L, Math.min(DAY, dayTime - last));

        if (player.isSleeping()) {
            int slept = Math.min(VALID_SLEEP, data.getInt(SLEEPING) + 1);
            data.putInt(SLEEPING, slept);
            if (slept >= VALID_SLEEP) {
                reset(data);
            }
        } else {
            // Ignore very short bed interactions; meaningful sleep resets fully.
            data.remove(SLEEPING);
            if (!player.isCreative() && !player.isSpectator()) {
                int awake = (int) Math.min(FAINT_AT + DAY * 10L, data.getInt(AWAKE) + delta);
                data.putInt(AWAKE, awake);
                int faint = data.getInt(FAINT_LEFT);
                if (faint > 0) {
                    data.putInt(FAINT_LEFT, faint - 1);
                } else if (awake >= FAINT_AT && (data.getInt(FAINT_MARK) == 0
                        || awake - data.getInt(FAINT_MARK) >= DAY)) {
                    // Prevent a faint every tick; later collapses only after
                    // another complete Minecraft day without sleep.
                    data.putInt(FAINT_MARK, awake);
                    data.putInt(FAINT_LEFT, FAINT_DURATION);
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                            FAINT_DURATION, 6, false, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
                            60, 0, false, false, false));
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Exaustão extrema! Você desmaiou."), true);
                }
            }
        }
        if (player.tickCount % 40 == 0 || data.getInt(FAINT_LEFT) == FAINT_DURATION) sync(player);
    }

    private static void reset(net.minecraft.nbt.CompoundTag data) {
        data.putInt(AWAKE, 0);
        data.putInt(FAINT_MARK, 0);
        data.putInt(FAINT_LEFT, 0);
    }

    private static void sync(ServerPlayer player) {
        var data = player.getPersistentData();
        PacketDistributor.sendToPlayer(player, new HudSleepSnapshotPayload(
                data.getInt(AWAKE), data.getInt(FAINT_LEFT)));
    }
}
