package dev.luxmods.discovery.client;

import dev.luxmods.discovery.DiscoveryConfig;
import dev.luxmods.discovery.DiscoveryDebouncer;
import dev.luxmods.discovery.LuxDiscovery;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Locale;

/** Client-only: nunca importar esta classe a partir do mod comum no dedicated server. */
@EventBusSubscriber(modid = LuxDiscovery.MOD_ID, value = Dist.CLIENT)
public final class DiscoveryClientEvents {
    private static final DiscoveryDebouncer TRACKER = new DiscoveryDebouncer();
    private static ClientLevel lastLevel;

    private DiscoveryClientEvents() {
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            lastLevel = null;
            TRACKER.reset();
            return;
        }
        if (lastLevel != mc.level) {
            lastLevel = mc.level;
            TRACKER.reset();
        }
        if (!DiscoveryConfig.ENABLED.get()) {
            TRACKER.reset();
            return;
        }
        if (mc.screen != null) {
            return;
        }

        mc.level.getBiome(mc.player.blockPosition()).unwrapKey().ifPresent(biomeKey -> {
            ResourceLocation id = biomeKey.location();
            TRACKER.tick(
                    mc.level.dimension().location().toString(),
                    id.toString(),
                    DiscoveryConfig.STABLE_TICKS.get(),
                    DiscoveryConfig.TITLE_COOLDOWN_TICKS.get()
            ).ifPresent(ignored -> showTitle(mc, id));
        });
    }

    private static void showTitle(Minecraft mc, ResourceLocation id) {
        String fallback = humanize(id.getPath());
        Component name = Component.translatableWithFallback(
                "biome." + id.getNamespace() + "." + id.getPath(), fallback)
                .withStyle(ChatFormatting.GOLD);

        mc.gui.setTimes(12, 55, 22);
        mc.gui.setSubtitle(Component.translatable("message.lux_discovery.biome")
                .withStyle(ChatFormatting.GRAY));
        mc.gui.setTitle(name);
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
