package dev.luxmods.hud.network;

import dev.luxmods.hud.LuxHud;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-owned accumulated Minecraft-day awake ticks and brief faint duration. */
public record HudSleepSnapshotPayload(int awakeTicks, int faintTicks) implements CustomPacketPayload {
    public static final Type<HudSleepSnapshotPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LuxHud.MOD_ID, "sleep_snapshot"));
    public static final StreamCodec<ByteBuf, HudSleepSnapshotPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, HudSleepSnapshotPayload::awakeTicks,
                    ByteBufCodecs.VAR_INT, HudSleepSnapshotPayload::faintTicks,
                    HudSleepSnapshotPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
