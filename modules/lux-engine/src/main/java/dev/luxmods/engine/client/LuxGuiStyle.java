package dev.luxmods.engine.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Lux Clean Fantasy v2. Shared pixel-precise styling. Layout and essential
 * information are procedural: missing optional PNGs can never hide game data.
 *
 * Only reference this class from client-side renderers.
 */
public final class LuxGuiStyle {
    public static final int BACKGROUND = 0xD4191724;
    public static final int EDGE = 0xFF65507F;
    public static final int TOP_ACCENT = 0xFFAF84EE;
    public static final int FOREGROUND = 0xFFF7F1FF;
    public static final int MUTED_TEXT = 0xFFC9C0D4;
    public static final int AMBER = 0xFFF4CE87;
    public static final int HEALTH = 0xFFD44D66;
    public static final int HUNGER = 0xFFE1A64F;
    public static final int EXPERIENCE = 0xFF65B786;
    public static final int TRACK = 0xE5242230;

    private static final Map<ResourceLocation, Boolean> AVAILABLE = new HashMap<>();

    private LuxGuiStyle() { }

    /** Pixel frame: continuous double border and optional jewel corner sprites. */
    public static void panel(GuiGraphics g, int x, int y, int width, int height) {
        if (width < 12 || height < 12) return;
        // Offset shadow, base and inset: ornamental but stays true to vanilla pixels.
        g.fill(x + 2, y + 3, x + width + 3, y + height + 3, 0x6004000D);
        g.fill(x, y, x + width, y + height, BACKGROUND);

        g.fill(x, y, x + width, y + 1, TOP_ACCENT);
        g.fill(x, y + height - 1, x + width, y + height, TOP_ACCENT);
        g.fill(x, y, x + 1, y + height, TOP_ACCENT);
        g.fill(x + width - 1, y, x + width, y + height, TOP_ACCENT);

        // A muted inner contour provides continuity between the ornamental corners.
        g.fill(x + 2, y + 2, x + width - 2, y + 3, EDGE);
        g.fill(x + 2, y + height - 3, x + width - 2, y + height - 2, EDGE);
        g.fill(x + 2, y + 2, x + 3, y + height - 2, EDGE);
        g.fill(x + width - 3, y + 2, x + width - 2, y + height - 2, EDGE);
        g.fill(x + 12, y + 1, x + width - 12, y + 2, 0x667D5B9F);

        icon(g, "frame_corner_tl", x - 2, y - 2, 12, 12);
        icon(g, "frame_corner_tr", x + width - 10, y - 2, 12, 12);
        icon(g, "frame_corner_bl", x - 2, y + height - 10, 12, 12);
        icon(g, "frame_corner_br", x + width - 10, y + height - 10, 12, 12);
    }

    /** A beveled track + clipped fill + highlight, compatible with any stat. */
    public static void bar(GuiGraphics g, int x, int y, int width, int height,
                           float value, float max, int color) {
        if (width < 4 || height < 4) return;
        g.fill(x, y, x + width, y + height, 0xFF705D81);
        g.fill(x + 1, y + 1, x + width - 1, y + height - 1, TRACK);

        int innerWidth = width - 2;
        int filled = 0;
        if (Float.isFinite(value) && Float.isFinite(max) && max > 0f) {
            filled = Math.round(innerWidth * Math.max(0f, Math.min(1f, value / max)));
        }
        if (filled > 0) {
            g.fill(x + 1, y + 1, x + 1 + filled, y + height - 1, color);
            g.fill(x + 1, y + 1, x + 1 + filled, y + 2, 0x55FFFFFF);
            if (filled >= 2) {
                g.fill(x + filled, y + 2, x + filled + 1, y + height - 2, 0x33FFFFFF);
            }
        }
    }

    public static void separator(GuiGraphics g, int x, int y, int width) {
        if (width <= 0) return;
        g.fill(x, y, x + width, y + 1, 0x9976658B);
        icon(g, "divider", x + Math.max(0, width / 2 - 18), y - 2, 36, 3);
    }

    /** Coordinates and size in GUI pixels; image optional. */
    public static void icon(GuiGraphics g, String name, int x, int y, int width, int height) {
        ResourceLocation resource = ResourceLocation.fromNamespaceAndPath(
                "lux_engine", "textures/gui/lux/" + name + ".png");
        if (!exists(resource)) return;
        g.blit(resource, x, y, width, height, 0f, 0f, width, height, width, height);
    }

    private static boolean exists(ResourceLocation resource) {
        return AVAILABLE.computeIfAbsent(resource,
                key -> Minecraft.getInstance().getResourceManager().getResource(key).isPresent());
    }
}
