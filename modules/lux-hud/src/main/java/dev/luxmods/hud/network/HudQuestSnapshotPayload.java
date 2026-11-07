package dev.luxmods.hud.network;

import dev.luxmods.hud.LuxHud;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-owned quest stage and collection progress. */
public record HudQuestSnapshotPayload(int stage, int progress) implements CustomPacketPayload {
    public static final Type<HudQuestSnapshotPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LuxHud.MOD_ID, "quest_snapshot"));
    public static final StreamCodec<ByteBuf, HudQuestSnapshotPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, HudQuestSnapshotPayload::stage,
                    ByteBufCodecs.VAR_INT, HudQuestSnapshotPayload::progress, HudQuestSnapshotPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
