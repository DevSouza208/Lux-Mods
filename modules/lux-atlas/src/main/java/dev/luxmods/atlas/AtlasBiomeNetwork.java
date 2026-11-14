package dev.luxmods.atlas;

import dev.luxmods.atlas.client.AtlasBiomeClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Read-only, generator-backed biome preview. Uses the CURRENT server's biome
 * source (including modded worldgen); never creates or loads distant chunks.
 * This is NOT a precise topographic/structure map.
 */
public final class AtlasBiomeNetwork {
    public static final int CELL_BLOCKS = 16;
    public static final int SIDE = 32;
    public static final int REGION_BLOCKS = CELL_BLOCKS * SIDE;
    private static final int CELLS = SIDE * SIDE;
    private static final java.util.Map<ServerPlayer, Integer> REQUEST_TICKS =
            new java.util.WeakHashMap<>();

    private AtlasBiomeNetwork() {}

    public record Request(int originX, int originZ) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(LuxAtlas.MOD_ID, "biome_preview_request"));
        public static final StreamCodec<FriendlyByteBuf, Request> CODEC =
                StreamCodec.of((buf, value) -> {
                    buf.writeInt(value.originX());
                    buf.writeInt(value.originZ());
                }, buf -> new Request(buf.readInt(), buf.readInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Preview(int originX, int originZ, String dimension, int[] colors)
            implements CustomPacketPayload {
        public static final Type<Preview> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(LuxAtlas.MOD_ID, "biome_preview"));
        public static final StreamCodec<FriendlyByteBuf, Preview> CODEC =
                StreamCodec.of((buf, value) -> {
                    buf.writeInt(value.originX());
                    buf.writeInt(value.originZ());
                    buf.writeUtf(value.dimension(), 128);
                    for (int color : value.colors()) buf.writeInt(color);
                }, buf -> {
                    int x = buf.readInt(), z = buf.readInt();
                    String dimension = buf.readUtf(128);
                    int[] colors = new int[CELLS];
                    for (int i = 0; i < CELLS; i++) colors[i] = buf.readInt();
                    return new Preview(x, z, dimension, colors);
                });
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(Request.TYPE, Request.CODEC, AtlasBiomeNetwork::onRequest);
        registrar.playToClient(Preview.TYPE, Preview.CODEC, AtlasBiomeNetwork::onPreview);
    }

    private static void onRequest(Request request, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
            int ox = request.originX(), oz = request.originZ();
            if (ox % REGION_BLOCKS != 0 || oz % REGION_BLOCKS != 0) return;
            // Clients can inspect their surrounding landscape, not request an
            // arbitrary world-scale seed dump from an unsuspecting server.
            if (Math.abs((long) ox - player.getBlockX()) > 8192
                    || Math.abs((long) oz - player.getBlockZ()) > 8192) return;
            int tick = player.tickCount;
            Integer lastTick = REQUEST_TICKS.get(player);
            if (lastTick != null && tick >= lastTick && tick - lastTick < 10) return;
            REQUEST_TICKS.put(player, tick);
            ServerLevel level = player.serverLevel();
            var chunks = level.getChunkSource();
            var source = chunks.getGenerator().getBiomeSource();
            var sampler = chunks.randomState().sampler();
            int[] colors = new int[CELLS];
            for (int z = 0; z < SIDE; z++) {
                for (int x = 0; x < SIDE; x++) {
                    int wx = ox + x * CELL_BLOCKS + CELL_BLOCKS / 2;
                    int wz = oz + z * CELL_BLOCKS + CELL_BLOCKS / 2;
                    var biome = source.getNoiseBiome(Math.floorDiv(wx, 4), 16,
                            Math.floorDiv(wz, 4), sampler);
                    colors[z * SIDE + x] = biomeColor(biome.value(), biome.unwrapKey()
                            .map(key -> key.location().getPath()).orElse(""), wx, wz);
                }
            }
            PacketDistributor.sendToPlayer(player,
                    new Preview(ox, oz, level.dimension().location().toString(), colors));
        });
    }

    private static int biomeColor(Biome biome, String name, int x, int z) {
        int rgb;
        if (name.contains("ocean") || name.contains("river")) {
            rgb = biome.getWaterColor();
        } else if (name.contains("desert") || name.contains("beach")) {
            rgb = 0xDCC885;
        } else if (name.contains("badlands")) {
            rgb = 0xBE8057;
        } else if (name.contains("snow") || name.contains("frozen")
                || name.contains("ice") || name.contains("grove")) {
            rgb = 0xE0E7DD;
        } else {
            rgb = biome.getGrassColor(x, z);
        }
        // Biome preview is deliberately subdued; observed map colors drawn
        // above it remain visibly different from predicted terrain.
        int red = (((rgb >>> 16) & 255) * 3 + 55) / 4;
        int green = (((rgb >>> 8) & 255) * 3 + 55) / 4;
        int blue = ((rgb & 255) * 3 + 55) / 4;
        return 0xff000000 | red << 16 | green << 8 | blue;
    }

    private static void onPreview(Preview preview, IPayloadContext context) {
        context.enqueueWork(() -> AtlasBiomeClient.receive(preview));
    }
}
