package dev.luxmods.dialogues.network;

import dev.luxmods.dialogues.LuxDialogues;
import dev.luxmods.dialogues.DialogueSessions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class DialogueNetwork {
    private DialogueNetwork() {}

    public record OpenShop(int entityId) implements CustomPacketPayload {
        public static final Type<OpenShop> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LuxDialogues.MOD_ID, "open_shop"));
        public static final StreamCodec<FriendlyByteBuf, OpenShop> CODEC =
            StreamCodec.of((buf, packet) -> buf.writeVarInt(packet.entityId()),
                buf -> new OpenShop(buf.readVarInt()));

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record EndDialogue(int entityId) implements CustomPacketPayload {
        public static final Type<EndDialogue> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LuxDialogues.MOD_ID, "end_dialogue"));
        public static final StreamCodec<FriendlyByteBuf, EndDialogue> CODEC =
            StreamCodec.of((buf, packet) -> buf.writeVarInt(packet.entityId()),
                buf -> new EndDialogue(buf.readVarInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(OpenShop.TYPE, OpenShop.CODEC, DialogueNetwork::openShop);
        registrar.playToServer(EndDialogue.TYPE, EndDialogue.CODEC, DialogueNetwork::endDialogue);
    }

    private static void endDialogue(EndDialogue payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        Entity entity = player.level().getEntity(payload.entityId());
        if (entity instanceof Villager villager) DialogueSessions.finish(villager, player);
    }

    private static void openShop(OpenShop payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        Entity entity = player.level().getEntity(payload.entityId());
        if (!(entity instanceof Villager villager) || !entity.isAlive()) return;
        if (entity.distanceToSqr(player) > 64.0D || player.isSpectator()) return;
        if (villager.isBaby() || villager.getVillagerData().getProfession() ==
                net.minecraft.world.entity.npc.VillagerProfession.NONE ||
            villager.getVillagerData().getProfession() ==
                net.minecraft.world.entity.npc.VillagerProfession.NITWIT) return;
        if (villager.getOffers().isEmpty()) return;
        DialogueSessions.finish(villager, player);
        villager.setTradingPlayer(player);
        villager.openTradingScreen(player, villager.getDisplayName(),
            villager.getVillagerData().getLevel());
    }
}
