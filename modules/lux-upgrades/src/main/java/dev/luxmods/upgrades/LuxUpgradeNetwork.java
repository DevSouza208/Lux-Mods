package dev.luxmods.upgrades;

import dev.luxmods.engine.api.LuxUpgradeState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Server-authoritative XP purchases and character progression sync. */
public final class LuxUpgradeNetwork {
    private LuxUpgradeNetwork() {}

    public record Purchase(int perk) implements CustomPacketPayload {
        public static final Type<Purchase> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("lux_engine", "upgrade_purchase"));
        public static final StreamCodec<FriendlyByteBuf, Purchase> CODEC =
                StreamCodec.of((buf, p) -> buf.writeVarInt(p.perk()),
                        buf -> new Purchase(buf.readVarInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Snapshot(int xpLevel, int characterLevel, int healthRank,
                           int strengthRank, int speedRank, int armorRank)
            implements CustomPacketPayload {
        public static final Type<Snapshot> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("lux_engine", "upgrade_snapshot"));
        public static final StreamCodec<FriendlyByteBuf, Snapshot> CODEC =
                StreamCodec.of((buf, p) -> {
                    buf.writeVarInt(p.xpLevel());
                    buf.writeVarInt(p.characterLevel());
                    buf.writeVarInt(p.healthRank());
                    buf.writeVarInt(p.strengthRank());
                    buf.writeVarInt(p.speedRank());
                    buf.writeVarInt(p.armorRank());
                }, buf -> new Snapshot(buf.readVarInt(), buf.readVarInt(),
                        buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(Purchase.TYPE, Purchase.CODEC, LuxUpgradeNetwork::onPurchase);
        registrar.playToClient(Snapshot.TYPE, Snapshot.CODEC, LuxUpgradeNetwork::onSnapshot);
    }

    private static void onPurchase(Purchase packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player)
                LuxUpgradeServerEvents.buy(player, packet.perk());
        });
    }

    private static void onSnapshot(Snapshot packet, IPayloadContext context) {
        context.enqueueWork(() ->
                LuxUpgradeState.receive(packet.xpLevel(), packet.characterLevel(), packet.healthRank(), packet.strengthRank(), packet.speedRank(), packet.armorRank()));
    }
}
