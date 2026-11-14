package dev.luxmods.dialogues.network;

import dev.luxmods.dialogues.LuxDialogues;
import dev.luxmods.dialogues.DialogueSessions;
import dev.luxmods.dialogues.DialogueNpcTypes;
import dev.luxmods.dialogues.GuardDialogueSessions;
import dev.luxmods.dialogues.NpcContractLifecycle;
import dev.luxmods.dialogues.NpcContractStorage;
import dev.luxmods.dialogues.NpcContractDelivery;
import dev.luxmods.dialogues.PlayerQuestLedger;
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

    // A malicious client must not request the full 128-entry quest index every tick.
    // Weak player keys are discarded after disconnection.
    private static final java.util.Map<ServerPlayer, Integer> QUEST_SYNC_COOLDOWN =
            new java.util.WeakHashMap<>();

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
            sendContractSnapshot(villager, player);
            return;
        }
        if (old.status() != NpcContractLifecycle.Status.AVAILABLE) {
            player.sendSystemMessage(Component.literal("Não há contrato disponível neste momento."));
            sendContractSnapshot(villager, player);
            return;
        }
        NpcContractLifecycle.Offer updated =
                NpcContractLifecycle.accept(old, player.getUUID(), day);
        if (!NpcContractStorage.store(villager, old, updated)) {
            player.sendSystemMessage(Component.literal("Contrato mudou. Converse novamente com o aldeão."));
            sendContractSnapshot(villager, player);
            return;
        }
        PlayerQuestLedger.record(player, villager, updated);
        sendQuestLedger(player);
        player.sendSystemMessage(Component.literal("Contrato aceito: entregar "
                + updated.quantity() + " de " + updated.objective()
                + ". Volte ao aldeão quando reunir os itens."));
        sendContractSnapshot(villager, player);
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
            boolean success = NpcContractDelivery.deliver(villager, player);
            if (villager.isAlive() && villager.distanceToSqr(player) <= 64.0D) {
                sendContractSnapshot(villager, player);
                sendQuestLedger(player);
                // Send AFTER the refreshed snapshot, so the result stays visible.
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                        player, new ContractDeliveryResult(villager.getId(), success));
            }
        }
    }

    /** Explicit result of a delivery, distinct from the NPC's next offer. */
    public record ContractDeliveryResult(int entityId, boolean success) implements CustomPacketPayload {
        public static final Type<ContractDeliveryResult> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(LuxDialogues.MOD_ID, "contract_delivery_result"));
        public static final StreamCodec<FriendlyByteBuf, ContractDeliveryResult> CODEC =
                StreamCodec.of((buf, p) -> {
                    buf.writeVarInt(p.entityId());
                    buf.writeBoolean(p.success());
                }, buf -> new ContractDeliveryResult(buf.readVarInt(), buf.readBoolean()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private static void receiveDeliveryResult(ContractDeliveryResult result, IPayloadContext context) {
        context.enqueueWork(() ->
                dev.luxmods.dialogues.client.DialogueClient.onDeliveryResult(result));
    }

    /** Read-only contract query; the server sends the authoritative snapshot. */
    public record RequestContract(int entityId) implements CustomPacketPayload {
        public static final Type<RequestContract> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(LuxDialogues.MOD_ID, "request_contract"));
        public static final StreamCodec<FriendlyByteBuf, RequestContract> CODEC =
                StreamCodec.of((buf, payload) -> buf.writeVarInt(payload.entityId()),
                        buf -> new RequestContract(buf.readVarInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Bounded server-owned display values; no client-trusted contract mutations. */
    public record ContractSnapshot(int entityId, String objective, int quantity,
                                   long expiresDay, int status, boolean mine) implements CustomPacketPayload {
        public static final Type<ContractSnapshot> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(LuxDialogues.MOD_ID, "contract_snapshot"));
        public static final StreamCodec<FriendlyByteBuf, ContractSnapshot> CODEC =
                StreamCodec.of((buf, p) -> {
                    buf.writeVarInt(p.entityId());
                    buf.writeUtf(p.objective(), 64);
                    buf.writeVarInt(p.quantity());
                    buf.writeVarLong(p.expiresDay());
                    buf.writeVarInt(p.status());
                    buf.writeBoolean(p.mine());
                }, buf -> new ContractSnapshot(buf.readVarInt(), buf.readUtf(64),
                        buf.readVarInt(), buf.readVarLong(), buf.readVarInt(), buf.readBoolean()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private static void requestContract(RequestContract payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
        Entity entity = player.level().getEntity(payload.entityId());
        if (!(entity instanceof Villager villager) || !villager.isAlive()
                || villager.isBaby() || player.distanceToSqr(villager) > 64.0D) return;
        sendContractSnapshot(villager, player);
    }

    private static void sendContractSnapshot(Villager villager, ServerPlayer player) {
        long day = Math.max(0L, villager.level().getDayTime() / 24000L);
        NpcContractLifecycle.Offer offer = NpcContractStorage.current(villager, day);
        boolean mine = player.getUUID().equals(offer.claimant());
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new ContractSnapshot(villager.getId(), offer.objective(), offer.quantity(),
                        offer.expiresDay(), offer.status().ordinal(), mine));
    }

    private static void receiveContractSnapshot(ContractSnapshot snapshot, IPayloadContext context) {
        context.enqueueWork(() ->
                dev.luxmods.dialogues.client.DialogueClient.onContractSnapshot(snapshot));
    }

    /** Client asks for a fresh server-owned quest index when its level is ready. */
    public record RequestQuestLedger() implements CustomPacketPayload {
        public static final Type<RequestQuestLedger> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(LuxDialogues.MOD_ID, "request_quest_ledger"));
        public static final StreamCodec<FriendlyByteBuf, RequestQuestLedger> CODEC =
                StreamCodec.unit(new RequestQuestLedger());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private static void requestQuestLedger(RequestQuestLedger payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
        int now = player.tickCount;
        Integer previous = QUEST_SYNC_COOLDOWN.get(player);
        if (previous != null && now >= previous && now - previous < 40) return;
        QUEST_SYNC_COOLDOWN.put(player, now);
        sendQuestLedger(player);
    }

    /** A bounded, server-authored list of accepted contracts for reconnection. */
    public record QuestLedgerSync(java.util.List<PlayerQuestLedger.Entry> entries)
            implements CustomPacketPayload {
        public static final Type<QuestLedgerSync> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(LuxDialogues.MOD_ID, "quest_ledger_sync"));
        public static final StreamCodec<FriendlyByteBuf, QuestLedgerSync> CODEC =
                StreamCodec.of((buf, payload) -> {
                    buf.writeVarInt(payload.entries().size());
                    for (var quest : payload.entries()) {
                        buf.writeUUID(quest.npc());
                        buf.writeUtf(quest.dimension(), 128);
                        buf.writeInt(quest.x());
                        buf.writeInt(quest.z());
                        buf.writeUtf(quest.objective(), 64);
                        buf.writeVarInt(quest.quantity());
                        buf.writeVarLong(quest.expiresDay());
                        buf.writeVarInt(quest.status());
                    }
                }, buf -> {
                    int count = buf.readVarInt();
                    if (count < 0 || count > 128)
                        throw new IllegalArgumentException("Too many quest entries");
                    var entries = new java.util.ArrayList<PlayerQuestLedger.Entry>(count);
                    for (int i = 0; i < count; i++) {
                        entries.add(new PlayerQuestLedger.Entry(buf.readUUID(), buf.readUtf(128),
                                buf.readInt(), buf.readInt(), buf.readUtf(64),
                                buf.readVarInt(), buf.readVarLong(), buf.readVarInt()));
                    }
                    return new QuestLedgerSync(java.util.List.copyOf(entries));
                });
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void sendQuestLedger(ServerPlayer player) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                player, new QuestLedgerSync(PlayerQuestLedger.read(player)));
    }

    private static void receiveQuestLedger(QuestLedgerSync payload, IPayloadContext context) {
        context.enqueueWork(() ->
                dev.luxmods.dialogues.client.DialogueClient.onQuestLedgerSync(payload));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(OpenShop.TYPE, OpenShop.CODEC, DialogueNetwork::openShop);
        registrar.playToServer(EndDialogue.TYPE, EndDialogue.CODEC, DialogueNetwork::endDialogue);
        registrar.playToServer(AcceptContract.TYPE, AcceptContract.CODEC, DialogueNetwork::acceptContract);
        registrar.playToServer(DeliverContract.TYPE, DeliverContract.CODEC, DialogueNetwork::deliverContract);
        registrar.playToServer(RequestContract.TYPE, RequestContract.CODEC, DialogueNetwork::requestContract);
        registrar.playToClient(ContractSnapshot.TYPE, ContractSnapshot.CODEC, DialogueNetwork::receiveContractSnapshot);
        registrar.playToClient(ContractDeliveryResult.TYPE, ContractDeliveryResult.CODEC, DialogueNetwork::receiveDeliveryResult);
        registrar.playToClient(QuestLedgerSync.TYPE, QuestLedgerSync.CODEC, DialogueNetwork::receiveQuestLedger);
        registrar.playToServer(RequestQuestLedger.TYPE, RequestQuestLedger.CODEC, DialogueNetwork::requestQuestLedger);
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
