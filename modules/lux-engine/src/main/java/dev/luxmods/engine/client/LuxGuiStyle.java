package dev.luxmods.engine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Lux Clean Fantasy — shared lightweight visual system for HUD and Atlas.
 *
 * Core shapes render without image assets. The optional original pixel-art PNGs
 * add corners/ornaments after being added to the Engine resources and rebuilt.
 * This client-only class must not be referenced by common entrypoints.
 */
public final class LuxGuiStyle {
    public static final int BACKGROUND = 0xBC151720;
    public static final int EDGE = 0xFF534864;
    public static final int TOP_ACCENT = 0xFF8467B4;
    public static final int FOREGROUND = 0xFFF1ECED;
    public static final int MUTED_TEXT = 0xFFD2C9DB;
    public static final int AMBER = 0xFFE1C69A;
    public static final int HEALTH = 0xFFCF5061;
    public static final int HUNGER = 0xFFD39B54;
    public static final int EXPERIENCE = 0xFF68B690;
    public static final int TRACK = 0xD9262934;

    private static final Map<ResourceLocation, Boolean> AVAILABLE = new HashMap<>();

    private LuxGuiStyle() {
    }

    public static void panel(GuiGraphics g, int x, int y, int width, int height) {
        if (width < 8 || height < 8) return;
        g.fill(x, y, x + width, y + height, BACKGROUND);
        g.fill(x, y, x + width, y + 1, TOP_ACCENT);
        g.fill(x, y + height - 1, x + width, y + height, EDGE);
        g.fill(x, y, x + 1, y + height, EDGE);
        g.fill(x + width - 1, y, x + width, y + height, EDGE);

        icon(g, "frame_corner_tl", x - 1, y - 1, 12, 12);
        icon(g, "frame_corner_tr", x + width - 11, y - 1, 12, 12);
        icon(g, "frame_corner_bl", x - 1, y + height - 11, 12, 12);
        icon(g, "frame_corner_br", x + width - 11, y + height - 11, 12, 12);
    }

    /** Draw a bar using only rectangles, regardless of whether textures exist. */
    public static void bar(GuiGraphics g, int x, int y, int width, int height,
                           float value, float max, int color) {
        if (width <= 0 || height <= 0) return;
        g.fill(x, y, x + width, y + height, TRACK);
        int filled = 0;
        if (Float.isFinite(value) && Float.isFinite(max) && max > 0) {
            float ratio = Math.max(0f, Math.min(1f, value / max));
            filled = Math.round(width * ratio);
        }
        if (filled > 0) {
            g.fill(x, y, x + filled, y + height, color);
            g.fill(x, y, x + filled, y + 1, 0x55FFFFFF);
        }
    }

    public static void separator(GuiGraphics g, int x, int y, int width) {
        g.fill(x, y, x + Math.max(0, width), y + 1, 0x88564D64);
    }

    /**
     * PNG is located in lux_engine:textures/gui/lux/<name>.png.
     * If absent, do not draw a missing-texture checkerboard.
     */
    public static void icon(GuiGraphics g, String name, int x, int y, int width, int height) {
        ResourceLocation resource = ResourceLocation.fromNamespaceAndPath(
                "lux_engine", "textures/gui/lux/" + name + ".png");
        if (!exists(resource)) return;
        g.blit(resource, x, y, width, height, 0.0f, 0.0f, width, height, width, height);
    }

    private static boolean exists(ResourceLocation resource) {
        // Each texture is resolved once per game process; rebuilt jars load on restart.
        return AVAILABLE.computeIfAbsent(resource,
                key -> Minecraft.getInstance().getResourceManager().getResource(key).isPresent());
    }
}
