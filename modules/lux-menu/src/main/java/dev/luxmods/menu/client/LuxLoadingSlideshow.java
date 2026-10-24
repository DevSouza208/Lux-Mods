package dev.luxmods.menu.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import dev.luxmods.menu.LuxMenu;
import dev.luxmods.menu.MenuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional slideshow on the NORMAL client world-loading screens.
 *
 * Important: the FML early startup window and the Mojang LoadingOverlay appear
 * before / outside normal Screen events and are deliberately NOT changed here.
 * If no slide PNG exists, the unmodified vanilla world-loading screen is shown.
 */
public final class LuxLoadingSlideshow {
    private static final int IMAGE_WIDTH = 1920;
    private static final int IMAGE_HEIGHT = 1080;
    private static final int MAX_SLIDES = 8;
    private static Screen previousScreen;
    private static long startNanos;

    private LuxLoadingSlideshow() { }

    public static void onRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (!MenuConfig.ENABLED.get() || !MenuConfig.LOADING_SLIDESHOW.get()
                || !isWorldLoadingScreen(screen)) {
            previousScreen = null;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        List<ResourceLocation> images = discoverImages(mc);
        if (images.isEmpty()) return; // No assets => vanilla, no purple checkerboards.

        if (previousScreen != screen) {
            previousScreen = screen;
            startNanos = System.nanoTime();
        }
        long elapsedMs = Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);
        int holdMs = MenuConfig.SLIDE_SECONDS.get() * 1000;
        int fadeMs = MenuConfig.FADE_MILLIS.get();
        SlideTiming.Frame frame = SlideTiming.frame(elapsedMs, images.size(), holdMs, fadeMs);

        GuiGraphics g = event.getGuiGraphics();
        int w = g.guiWidth();
        int h = g.guiHeight();
        if (w <= 0 || h <= 0) return;

        // Content layer uses opaque first photo, then fades the next onto it.
        drawCover(g, images.get(frame.current()), w, h, 1f);
        if (frame.nextOpacity() > 0f && frame.current() != frame.next()) {
            drawCover(g, images.get(frame.next()), w, h, frame.nextOpacity());
        }

        // Gentle lower gradient for high contrast in snowy/daytime photos.
        g.fillGradient(0, Math.max(0, h - 96), w, h, 0x00161524, 0xD9151224);
        String text = SlideTiming.loadingText(elapsedMs);
        int tx = Math.max(30, w - 27 - mc.font.width("CARREGANDO..."));
        int ty = Math.max(14, h - 23);
        g.drawString(mc.font, text, tx, ty, LuxGuiStyle.FOREGROUND, true);
        LuxGuiStyle.icon(g, "ornament_diamond", tx - 19, ty - 2, 12, 12);

        // Decorative footer; intentionally NO bogus percent / fake progress bar.
        g.fill(0, h - 3, w, h - 2, 0x66AF84EE);
    }

    private static boolean isWorldLoadingScreen(Screen screen) {
        return screen instanceof LevelLoadingScreen
                || screen instanceof ReceivingLevelScreen
                || screen instanceof ProgressScreen;
    }

    private static List<ResourceLocation> discoverImages(Minecraft mc) {
        List<ResourceLocation> result = new ArrayList<>(MAX_SLIDES);
        for (int number = 1; number <= MAX_SLIDES; number++) {
            String name = String.format(java.util.Locale.ROOT, "slide_%02d.png", number);
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    LuxMenu.MOD_ID, "textures/gui/loading/" + name);
            if (mc.getResourceManager().getResource(id).isPresent()) {
                result.add(id);
            }
        }
        return result;
    }

    /** Maintain original photo proportions and crop only the sides or height. */
    private static void drawCover(GuiGraphics g, ResourceLocation id,
                                  int targetW, int targetH, float alpha) {
        int cropW = IMAGE_WIDTH;
        int cropH = IMAGE_HEIGHT;
        if ((long) targetW * IMAGE_HEIGHT < (long) targetH * IMAGE_WIDTH) {
            cropW = (int) Math.round((double) targetW * IMAGE_HEIGHT / targetH);
        } else {
            cropH = (int) Math.round((double) targetH * IMAGE_WIDTH / targetW);
        }
        cropW = Math.max(1, Math.min(cropW, IMAGE_WIDTH));
        cropH = Math.max(1, Math.min(cropH, IMAGE_HEIGHT));
        int u = (IMAGE_WIDTH - cropW) / 2;
        int v = (IMAGE_HEIGHT - cropH) / 2;
        try {
            g.setColor(1f, 1f, 1f, Math.max(0f, Math.min(1f, alpha)));
            g.blit(id, 0, 0, targetW, targetH, (float) u, (float) v,
                    cropW, cropH, IMAGE_WIDTH, IMAGE_HEIGHT);
        } finally {
            g.setColor(1f, 1f, 1f, 1f);
        }
    }
}
