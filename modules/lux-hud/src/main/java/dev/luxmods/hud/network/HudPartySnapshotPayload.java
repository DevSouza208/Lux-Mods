package dev.luxmods.hud.network;

import dev.luxmods.hud.LuxHud;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-authoritative roster, never a client-provided health estimate. */
public record HudPartySnapshotPayload(String data) implements CustomPacketPayload {
    public static final Type<HudPartySnapshotPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LuxHud.MOD_ID, "party_snapshot"));

    public static final StreamCodec<ByteBuf, HudPartySnapshotPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
                    HudPartySnapshotPayload::data, HudPartySnapshotPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
