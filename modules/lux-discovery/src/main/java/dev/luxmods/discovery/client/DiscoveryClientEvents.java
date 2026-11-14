package dev.luxmods.discovery.client;

import dev.luxmods.discovery.DiscoveryConfig;
import dev.luxmods.discovery.network.BiomeDiscoveredPayload;
import dev.luxmods.discovery.network.StructureDiscoveredPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Locale;

/**
 * Somente cliente: apresentador de títulos.
 * O servidor envia o evento apenas após registrar a descoberta no save.
 */
public final class DiscoveryClientEvents {
    private DiscoveryClientEvents() {
    }

    public static void handleBiome(BiomeDiscoveredPayload payload, IPayloadContext context) {
        if (!DiscoveryConfig.ENABLED.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        ResourceLocation id = ResourceLocation.tryParse(payload.biomeId());
        if (id == null) return;

        Component name = Component.translatableWithFallback(
                        "biome." + id.getNamespace() + "." + id.getPath(), humanize(id.getPath()))
                .withStyle(ChatFormatting.GOLD);
        DiscoveryOverlay.show(name, Component.translatable("message.lux_discovery.biome")
                .withStyle(ChatFormatting.GRAY));
    }

    /** Only announce locations verified by the world server. */
    public static void handleStructure(StructureDiscoveredPayload payload, IPayloadContext context) {
        if (!DiscoveryConfig.ENABLED.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        ResourceLocation id = ResourceLocation.tryParse(payload.structureId());
        if (id == null) return;
        String key = id.getPath();
        String title = key.contains("village") ? "Vila descoberta"
                : key.contains("ruined_portal") ? "Portal arruinado"
                : key.contains("stronghold") ? "Fortaleza"
                : key.contains("mansion") ? "Mansão da floresta"
                : key.contains("temple") ? "Templo"
                : key.contains("outpost") ? "Posto de saqueadores"
                : humanize(key);
        DiscoveryOverlay.show(Component.literal(title).withStyle(ChatFormatting.GOLD),
                Component.literal("ESTRUTURA DESCOBERTA").withStyle(ChatFormatting.GRAY));
    }

    private static String humanize(String path) {
        String[] words = path.replace('_', ' ').replace('-', ' ').split(" ");
        StringBuilder title = new StringBuilder();
        for (String word : words) {
            if (!word.isBlank()) {
                if (!title.isEmpty()) title.append(' ');
                title.append(word.substring(0, 1).toUpperCase(Locale.ROOT))
                        .append(word.substring(1));
            }
        }
        return title.toString();
    }
}
