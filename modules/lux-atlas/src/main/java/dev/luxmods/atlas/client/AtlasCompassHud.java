package dev.luxmods.atlas.client;

import dev.luxmods.atlas.AtlasConfig;
import dev.luxmods.atlas.CompassMath;
import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.AABB;

/** Direction compass at the upper center; legacy manual-bottom placement remains optional. */
public final class AtlasCompassHud {
    private static final int WIDTH = 162;
    private static final int HEIGHT = 18;
    private static final int HALF_WINDOW_DEGREES = 58;
    private static final float PIXELS_PER_DEGREE = WIDTH / (2.0f * HALF_WINDOW_DEGREES);

    private AtlasCompassHud() { }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui || mc.screen != null
                || !AtlasConfig.SHOW_COMPASS.get()) return;

        int centerX = g.guiWidth() / 2;
        // The primary exploration compass belongs at the TOP of the screen.
        // Keep legacy manual positioning only for users who disabled snapping.
        int y = AtlasConfig.SNAP_TO_HOTBAR.get() ? 8
                : Math.max(4, g.guiHeight() - AtlasConfig.DISTANCE_FROM_BOTTOM.get());
        int x = centerX - WIDTH / 2;
        float heading = CompassMath.fromMinecraftYaw(mc.player.getYRot());

        LuxGuiStyle.panel(g, x, y, WIDTH, HEIGHT);
        LuxGuiStyle.icon(g, "compass_end_left", x - 12, y - 1, 15, 19);
        LuxGuiStyle.icon(g, "compass_end_right", x + WIDTH - 3, y - 1, 15, 19);

        for (int bearing = 0; bearing < 360; bearing += 15) {
            float offset = CompassMath.signedOffset(bearing, heading);
            if (Math.abs(offset) > HALF_WINDOW_DEGREES) continue;
            int px = Math.round(centerX + offset * PIXELS_PER_DEGREE);
            boolean cardinal = bearing % 90 == 0;
            boolean major = bearing % 45 == 0;
            g.fill(px, y + 3, px + 1, y + (major ? 8 : 6),
                    cardinal ? LuxGuiStyle.AMBER : 0xFFA095B8);
            if (cardinal) {
                String letter = CompassMath.cardinalLabel(bearing);
                g.drawString(mc.font, letter, px - mc.font.width(letter) / 2,
                        y + 9, LuxGuiStyle.AMBER, true);
            }
        }
        drawNearbyEntities(g, mc, centerX, y, heading);
        drawLastHorseSighting(g, mc, centerX, y, heading);
        // Fine stationary needle at current heading, plus pixel jewel.
        g.fill(centerX, y + 2, centerX + 1, y + 8, 0xFFF3DAFF);
        LuxGuiStyle.icon(g, "compass_needle", centerX - 3, y - 3, 7, 9);
    }

    /**
     * Only client-known entities are drawn, never hidden/unloaded enemies.
     * Red dots = hostile monsters. Gold H = a tamed horse owned by the
     * player; other players' horses and wild horses are omitted.
     */
    private static void drawNearbyEntities(GuiGraphics g, Minecraft mc,
                                           int centerX, int y, float heading) {
        var player = mc.player;
        if (player == null || mc.level == null) return;
        AABB region = player.getBoundingBox().inflate(32.0D);
        int threats = 0;
        for (Entity entity : mc.level.getEntities(player, region, e ->
                e instanceof Monster || e instanceof AbstractHorse)) {
            boolean hostile = entity instanceof Monster monster && monster.isAlive();
            boolean ownedHorse = entity instanceof AbstractHorse horse
                    && horse.isAlive() && horse.isTamed()
                    && (player.getUUID().equals(horse.getOwnerUUID())
                        || player.getVehicle() == horse);
            if (!hostile && !ownedHorse) continue;
            if (hostile && ++threats > 24) continue; // avoid unreadable dot clusters
            double dx = entity.getX() - player.getX();
            double dz = entity.getZ() - player.getZ();
            if (dx * dx + dz * dz > 32.0D * 32.0D) continue;
            // Absolute compass bearing: N=0, E=90, S=180, W=270.
            float bearing = CompassMath.normalize(
                    (float) Math.toDegrees(Math.atan2(dx, -dz)));
            float offset = CompassMath.signedOffset(bearing, heading);
            if (Math.abs(offset) > HALF_WINDOW_DEGREES) continue;
            int px = Math.round(centerX + offset * PIXELS_PER_DEGREE);
            if (hostile) {
                g.fill(px - 2, y + 1, px + 3, y + 6, 0xFF33121A);
                g.fill(px - 1, y + 2, px + 2, y + 5, 0xFFF04C59);
            } else {
                drawHorseIcon(g, px, y, 0xFFECC37C);
            }
        }
    }
    private static void drawLastHorseSighting(GuiGraphics g, Minecraft mc,
                                              int centerX, int y, float heading) {
        AtlasHorseMemory.Sighting sighting = AtlasHorseMemory.get(mc);
        if (sighting == null || sighting.current() || mc.player == null) return;
        double dx = sighting.x() - mc.player.getX();
        double dz = sighting.z() - mc.player.getZ();
        if (dx * dx + dz * dz < 16D) return; // player is at the stale location
        float bearing = CompassMath.normalize((float) Math.toDegrees(Math.atan2(dx, -dz)));
        float offset = CompassMath.signedOffset(bearing, heading);
        if (Math.abs(offset) > HALF_WINDOW_DEGREES) return;
        int px = Math.round(centerX + offset * PIXELS_PER_DEGREE);
        // Muted color + '?' means historical position, not a live GPS signal.
        drawHorseIcon(g, px - 3, y, 0xFF957C9F);
        g.drawString(mc.font, "?", px + 2, y, 0xFFC2A4D1, true);
    }

    /** Tiny pixel horse-head silhouette, with no external texture dependency. */
    private static void drawHorseIcon(GuiGraphics g, int cx, int y, int color) {
        g.fill(cx - 4, y, cx + 4, y + 11, 0xE021192F);
        g.fill(cx - 2, y + 1, cx, y + 4, color);      // ear
        g.fill(cx + 1, y + 2, cx + 3, y + 5, color);  // mane
        g.fill(cx - 2, y + 4, cx + 2, y + 8, color);  // head
        g.fill(cx - 3, y + 7, cx + 1, y + 10, color); // muzzle
        g.fill(cx - 1, y + 5, cx, y + 6, 0xFF1D1526);
    }

}
