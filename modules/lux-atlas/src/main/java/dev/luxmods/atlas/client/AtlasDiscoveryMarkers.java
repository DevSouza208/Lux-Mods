package dev.luxmods.atlas.client;

import dev.luxmods.engine.LuxLocationDiscoveredEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/** Save verified structure discoveries as persistent markers, without revealing other chunks. */
public final class AtlasDiscoveryMarkers {
    private AtlasDiscoveryMarkers() {}

    public static void onLocation(LuxLocationDiscoveredEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        ResourceLocation id = ResourceLocation.tryParse(event.structureId());
        if (id == null) return;
        List<AtlasMarkers.Marker> markers = new ArrayList<>(AtlasMarkers.load(mc));
        // Tolerate several structure instances of the same type, while avoiding
        // repeated markers when a client receives the same discovery again.
        for (var existing : markers) {
            if (existing.dimension().equals(event.dimensionId())
                    && Math.abs(existing.x() - event.x()) <= 8
                    && Math.abs(existing.z() - event.z()) <= 8
                    && existing.label().startsWith("◆ ")) return;
        }
        String label = "◆ " + label(id.getPath());
        markers.add(new AtlasMarkers.Marker(label, event.x(), event.z(), event.dimensionId()));
        AtlasMarkers.save(mc, markers);
    }

    private static String label(String path) {
        if (path.contains("village")) return "Vila";
        if (path.contains("ruined_portal")) return "Portal arruinado";
        if (path.contains("stronghold")) return "Fortaleza";
        if (path.contains("mansion")) return "Mansão";
        if (path.contains("outpost")) return "Posto de saqueadores";
        if (path.contains("temple")) return "Templo";
        String[] words = path.replace('_', ' ').replace('-', ' ').split(" ");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return out.toString();
    }
}
