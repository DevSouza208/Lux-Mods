package dev.luxmods.discovery.network;

import dev.luxmods.discovery.LuxDiscovery;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Apenas um ID de bioma, enviado pelo servidor ao dono da descoberta. */
public record BiomeDiscoveredPayload(String biomeId) implements CustomPacketPayload {
    public static final Type<BiomeDiscoveredPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LuxDiscovery.MOD_ID, "biome_discovered"));

    public static final StreamCodec<ByteBuf, BiomeDiscoveredPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, BiomeDiscoveredPayload::biomeId,
                    BiomeDiscoveredPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
