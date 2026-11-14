package dev.luxmods.hud.server;

import dev.luxmods.hud.network.HudQuestSnapshotPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Introductory inventory quests: 16 logs, then 16 stone blocks.
 * Uses the highest inventory quantity seen, never cumulative pickup events.
 * The server owns progress and checks inventory periodically, including items
 * already held or moved into the inventory without a world-item pickup.
 */
public final class HudQuestServerEvents {
    private static final String STAGE = "lux_quest_stage";
    private static final String PROGRESS = "lux_quest_progress";
    private static final int TARGET = 16;

    private static void sync(ServerPlayer player) {
        var data = player.getPersistentData();
        PacketDistributor.sendToPlayer(player, new HudQuestSnapshotPayload(
                data.getInt(STAGE), data.getInt(PROGRESS)));
    }

    private static int countRelevant(ServerPlayer player, int stage) {
        int held = 0;
        for (ItemStack stack : player.getInventory().items) {
            boolean relevant = stage == 0 ? stack.is(ItemTags.LOGS) :
                    stack.is(Items.COBBLESTONE) || stack.is(Items.COBBLED_DEEPSLATE)
                    || stack.is(Items.STONE);
            if (relevant) held += stack.getCount();
        }
        return Math.min(TARGET, held);
    }

    private static void updateFromInventory(ServerPlayer player) {
        var data = player.getPersistentData();
        int stage = data.getInt(STAGE);
        if (stage >= 2) return;
        int previous = data.getInt(PROGRESS);
        int progress = Math.min(TARGET, Math.max(previous, countRelevant(player, stage)));
        if (progress == TARGET) {
            data.putInt(STAGE, stage + 1);
            data.putInt(PROGRESS, 0);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    stage == 0 ? "Missão concluída: Primeiros troncos!" :
                            "Missão concluída: Pedras para construir!"));
            // Evaluate the next stage on a subsequent tick. Completion is
            // persisted first, so re-entrance/relogging cannot reward twice.
        } else if (progress != previous) {
            data.putInt(PROGRESS, progress);
        } else {
            return; // No state change; regular heartbeat handles initial sync.
        }
        sync(player);
    }

    @SubscribeEvent
    public void onPickup(ItemEntityPickupEvent.Post event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            updateFromInventory(player);
        }
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 40 == 0) {
            updateFromInventory(player);
            sync(player); // A low-frequency heartbeat for reconnect/UI initialization.
        }
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var old = event.getOriginal().getPersistentData();
        var data = player.getPersistentData();
        data.putInt(STAGE, old.getInt(STAGE));
        data.putInt(PROGRESS, old.getInt(PROGRESS));
    }
}
