package dev.luxmods.atlas.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Last CONFIRMED sighting of the player's own tamed horse. A previous position
 * is a memory, not a real-time tracker of unloaded entities.
 *
 * No network packets, no horse teleports, no map/minimap layer.
 */
public final class AtlasHorseMemory {
    public record Sighting(int x, int z, String dimension, boolean current) {}

    private static ClientLevel world;
    private static String worldId;
    private static Sighting last;
    private static int lastSeenTick;
    private static int lastWriteTick;

    private AtlasHorseMemory() {}

    private static Path path(Minecraft mc) {
        return mc.gameDirectory.toPath().resolve("lux-atlas")
                .resolve(AtlasMarkers.worldKey(mc) + "_horse.json");
    }

    private static void load(Minecraft mc) {
        last = null;
        try {
            Path file = path(mc);
            if (!Files.isRegularFile(file)) return;
            JsonObject data = JsonParser.parseString(
                    Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            last = new Sighting(data.get("x").getAsInt(), data.get("z").getAsInt(),
                    data.get("dimension").getAsString(), false);
        } catch (Exception ignored) {
            // Old/corrupt optional horse memory must not prevent gameplay.
        }
    }

    private static void save(Minecraft mc) {
        if (last == null) return;
        try {
            Path file = path(mc);
            Files.createDirectories(file.getParent());
            JsonObject data = new JsonObject();
            data.addProperty("x", last.x());
            data.addProperty("z", last.z());
            data.addProperty("dimension", last.dimension());
            Files.writeString(file, data.toString(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            // Keep live memory working even in a read-only game directory.
        }
    }

    public static Sighting get(Minecraft mc) {
        if (mc.level == null || last == null
                || !AtlasMarkers.dimension(mc.level).equals(last.dimension())) return null;
        boolean current = mc.player != null && mc.player.tickCount - lastSeenTick < 40;
        return new Sighting(last.x(), last.z(), last.dimension(), current);
    }

    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            world = null;
            worldId = null;
            last = null;
            return;
        }
        String id = AtlasMarkers.worldKey(mc);
        if (world != mc.level || !id.equals(worldId)) {
            world = mc.level;
            worldId = id;
            lastSeenTick = Integer.MIN_VALUE;
            lastWriteTick = Integer.MIN_VALUE;
            load(mc);
        }
        if (mc.player.tickCount % 20 != 0) return;
        var player = mc.player;
        String dimension = AtlasMarkers.dimension(mc.level);
        for (var entity : mc.level.getEntities(player, player.getBoundingBox().inflate(64D),
                e -> e instanceof AbstractHorse)) {
            if (!(entity instanceof AbstractHorse horse) || !horse.isAlive()
                    || !horse.isTamed() || !player.getUUID().equals(horse.getOwnerUUID())) continue;
            int x = horse.getBlockX(), z = horse.getBlockZ();
            boolean moved = last == null || !last.dimension().equals(dimension)
                    || Math.abs(last.x() - x) + Math.abs(last.z() - z) >= 3;
            last = new Sighting(x, z, dimension, true);
            lastSeenTick = player.tickCount;
            if (moved || player.tickCount - lastWriteTick >= 600) {
                save(mc);
                lastWriteTick = player.tickCount;
            }
            return; // one player-owned horse icon; not a herd tracker
        }
    }
}
