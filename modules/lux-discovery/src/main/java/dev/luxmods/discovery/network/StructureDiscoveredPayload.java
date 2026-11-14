package dev.luxmods.discovery.network;

import dev.luxmods.discovery.LuxDiscovery;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Structure id and exact start-chunk center, confirmed by the server. */
public record StructureDiscoveredPayload(String structureId, int x, int z)
        implements CustomPacketPayload {
    public static final Type<StructureDiscoveredPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LuxDiscovery.MOD_ID, "structure_discovered"));
    public static final StreamCodec<ByteBuf, StructureDiscoveredPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, StructureDiscoveredPayload::structureId,
                    ByteBufCodecs.VAR_INT, StructureDiscoveredPayload::x,
                    ByteBufCodecs.VAR_INT, StructureDiscoveredPayload::z,
                    StructureDiscoveredPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
