package dev.luxmods.discovery;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Descobertas por personagem: persistência no save do servidor.
 * A estrutura é imutável: sempre usamos setData para substituir um Set novo.
 */
public final class DiscoveryAttachments {
    private static final Codec<Set<String>> DISCOVERIES_CODEC = Codec.STRING.listOf()
            .xmap(Set::copyOf, List::copyOf);

    public static final DeferredRegister<AttachmentType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LuxDiscovery.MOD_ID);

    public static final Supplier<AttachmentType<Set<String>>> SEEN_BIOMES = TYPES.register(
            "seen_biomes",
            // Zero argumentos => Supplier; evita ambiguidade com builder(Function).
            () -> AttachmentType.builder(() -> Set.<String>of())
                    .serialize(DISCOVERIES_CODEC)
                    .copyOnDeath()
                    .build()
    );

    private DiscoveryAttachments() {
    }
}
