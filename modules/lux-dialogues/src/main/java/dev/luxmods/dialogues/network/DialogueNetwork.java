package dev.luxmods.dialogues.network;

import dev.luxmods.dialogues.LuxDialogues;
import dev.luxmods.dialogues.DialogueSessions;
import dev.luxmods.dialogues.DialogueNpcTypes;
import dev.luxmods.dialogues.GuardDialogueSessions;
import dev.luxmods.dialogues.NpcContractLifecycle;
import dev.luxmods.dialogues.NpcContractStorage;
import dev.luxmods.dialogues.NpcContractDelivery;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
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

    /** Client request only; NPC, current offer and player are verified server-side. */
    public record AcceptContract(int entityId) implements CustomPacketPayload {
        public static final Type<AcceptContract> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(LuxDialogues.MOD_ID, "accept_contract"));
        public static final StreamCodec<FriendlyByteBuf, AcceptContract> CODEC =
                StreamCodec.of((buf, packet) -> buf.writeVarInt(packet.entityId()),
                        buf -> new AcceptContract(buf.readVarInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private static void acceptContract(AcceptContract payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
        Entity entity = player.level().getEntity(payload.entityId());
        if (!(entity instanceof Villager villager) || !villager.isAlive()
                || villager.distanceToSqr(player) > 64.0D || villager.isBaby()) return;
        long day = Math.max(0L, villager.level().getDayTime() / 24000L);
        NpcContractLifecycle.Offer old = NpcContractStorage.current(villager, day);
        if (old.status() == NpcContractLifecycle.Status.ACCEPTED) {
            player.sendSystemMessage(Component.literal(old.claimant().equals(player.getUUID())
                    ? "Você já aceitou o contrato deste aldeão."
                    : "Este contrato já foi reservado por outro aventureiro."));
            return;
        }
        if (old.status() != NpcContractLifecycle.Status.AVAILABLE) {
            player.sendSystemMessage(Component.literal("Não há contrato disponível neste momento."));
            return;
        }
        NpcContractLifecycle.Offer updated =
                NpcContractLifecycle.accept(old, player.getUUID(), day);
        if (!NpcContractStorage.store(villager, old, updated)) {
            player.sendSystemMessage(Component.literal("Contrato mudou. Converse novamente com o aldeão."));
            return;
        }
        player.sendSystemMessage(Component.literal("Contrato aceito: entregar "
                + updated.quantity() + " de " + updated.objective()
                + ". Volte ao aldeão quando reunir os itens."));
    }

    /** The client only identifies the NPC; all inventory and contract rules stay server-side. */
    public record DeliverContract(int entityId) implements CustomPacketPayload {
        public static final Type<DeliverContract> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(LuxDialogues.MOD_ID, "deliver_contract"));
        public static final StreamCodec<FriendlyByteBuf, DeliverContract> CODEC =
                StreamCodec.of((buf, packet) -> buf.writeVarInt(packet.entityId()),
                        buf -> new DeliverContract(buf.readVarInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private static void deliverContract(DeliverContract payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
        Entity entity = player.level().getEntity(payload.entityId());
        if (entity instanceof Villager villager) {
            NpcContractDelivery.deliver(villager, player);
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(OpenShop.TYPE, OpenShop.CODEC, DialogueNetwork::openShop);
        registrar.playToServer(EndDialogue.TYPE, EndDialogue.CODEC, DialogueNetwork::endDialogue);
        registrar.playToServer(AcceptContract.TYPE, AcceptContract.CODEC, DialogueNetwork::acceptContract);
        registrar.playToServer(DeliverContract.TYPE, DeliverContract.CODEC, DialogueNetwork::deliverContract);
    }

    private static void endDialogue(EndDialogue payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        Entity entity = player.level().getEntity(payload.entityId());
        if (entity == null || entity.distanceToSqr(player) > 64D) return;
        if (entity instanceof Villager villager) DialogueSessions.finish(villager, player);
        else if (entity instanceof Mob guard && DialogueNpcTypes.isGuard(guard))
            GuardDialogueSessions.finish(guard, player);
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
