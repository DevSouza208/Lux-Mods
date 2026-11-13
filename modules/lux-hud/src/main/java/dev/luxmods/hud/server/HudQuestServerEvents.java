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

/** Two permanent introductory quests: gather 16 logs, then 16 stone blocks.
 *  Tracks the highest number held during pickup, preventing drop/pickup loops.
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

    @SubscribeEvent
    public void onPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var data = player.getPersistentData();
        int stage = data.getInt(STAGE);
        if (stage >= 2) return;
        ItemStack picked = event.getOriginalStack();
        boolean matches = stage == 0 ? picked.is(ItemTags.LOGS) :
                picked.is(Items.COBBLESTONE) || picked.is(Items.COBBLED_DEEPSLATE)
                || picked.is(Items.STONE);
        if (!matches) return;

        // Track the highest actual inventory quantity, not cumulative pickup
        // events: dropping and re-picking the same logs cannot farm progress.
        int held = 0;
        for (ItemStack stack : player.getInventory().items) {
            boolean relevant = stage == 0 ? stack.is(ItemTags.LOGS) :
                    stack.is(Items.COBBLESTONE) || stack.is(Items.COBBLED_DEEPSLATE)
                    || stack.is(Items.STONE);
            if (relevant) held += stack.getCount();
        }
        int progress = Math.min(TARGET, Math.max(data.getInt(PROGRESS), held));
        if (progress == TARGET) {
            data.putInt(STAGE, stage + 1);
            data.putInt(PROGRESS, 0);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    stage == 0 ? "Missão concluída: Primeiros troncos!" :
                            "Missão concluída: Pedras para construir!"));
        } else {
            data.putInt(PROGRESS, progress);
        }
        sync(player);
    }

    @SubscribeEvent
    public void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 40 == 0) {
            sync(player);
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
