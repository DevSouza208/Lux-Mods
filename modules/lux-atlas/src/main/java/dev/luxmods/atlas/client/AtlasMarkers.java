package dev.luxmods.atlas.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Locally persisted waypoints, scoped by server/save and dimension. */
public final class AtlasMarkers {
    public record Marker(String label, int x, int z, String dimension, String icon, int color) {
        // Backwards-compatible with legacy and automatic discovery markers.
        public Marker(String label, int x, int z, String dimension) {
            this(label, x, z, dimension,
                    label.startsWith("◆ ") ? "Estrutura" : "Marcador",
                    label.startsWith("◆ ") ? 0xFFEBC17A : 0xFFD39CF5);
        }
    }
    private AtlasMarkers() {}

    static String worldKey(Minecraft mc) {
        String key;
        if (mc.getCurrentServer() != null) key = mc.getCurrentServer().ip;
        else if (mc.getSingleplayerServer() != null) key = mc.getSingleplayerServer().getWorldData().getLevelName();
        else key = "local";
        return key.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static Path path(Minecraft mc) {
        return mc.gameDirectory.toPath().resolve("lux-atlas").resolve(worldKey(mc) + ".json");
    }

    public static String dimension(Level level) {
        return level.dimension().location().toString();
    }

    public static List<Marker> load(Minecraft mc) {
        List<Marker> result = new ArrayList<>();
        try {
            Path p = path(mc);
            if (!Files.isRegularFile(p)) return result;
            JsonArray array = JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8)).getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject o = element.getAsJsonObject();
                String label = o.get("name").getAsString();
                String dimension = o.get("dimension").getAsString();
                int x = o.get("x").getAsInt(), z = o.get("z").getAsInt();
                Marker defaults = new Marker(label, x, z, dimension);
                String icon = o.has("icon") ? o.get("icon").getAsString() : defaults.icon();
                int color = o.has("color") ? o.get("color").getAsInt() : defaults.color();
                result.add(new Marker(label, x, z, dimension, icon, color));
            }
        } catch (Exception ignored) {
            // Corrupt user data must never stop game startup or opening the map.
        }
        return result;
    }

    public static void save(Minecraft mc, List<Marker> markers) {
        try {
            Path p = path(mc);
            Files.createDirectories(p.getParent());
            JsonArray array = new JsonArray();
            for (Marker marker : markers) {
                JsonObject o = new JsonObject();
                o.addProperty("name", marker.label());
                o.addProperty("x", marker.x());
                o.addProperty("z", marker.z());
                o.addProperty("dimension", marker.dimension());
                o.addProperty("icon", marker.icon());
                o.addProperty("color", marker.color());
                array.add(o);
            }
            Files.writeString(p, array.toString(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            // Keep temporary waypoints even if local file is read-only.
        }
    }
}
