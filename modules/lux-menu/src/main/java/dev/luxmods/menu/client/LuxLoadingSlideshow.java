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
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Slideshow during normal world loading screens.
 *
 * Discover ALL packaged slide_<number>.png resources instead of looking up
 * a hard-coded maximum. Randomize once per round: no repeated slide within
 * a round, and avoid repeating the previous round's final image.
 *
 * FML's early red window and Mojang's LoadingOverlay are not Screen objects:
 * they need separate early-bootstrap / overlay integrations.
 */
public final class LuxLoadingSlideshow {
    private static final int IMAGE_WIDTH = 1920;
    private static final int IMAGE_HEIGHT = 1080;

    private static final Random RANDOM = new Random();
    private static ResourceManager indexedManager;
    private static List<ResourceLocation> available = List.of();
    private static List<ResourceLocation> playlist = List.of();
    private static List<ResourceLocation> nextPlaylist = List.of();
    private static Screen previousScreen;
    private static long startNanos;
    private static long currentRound = -1L;

    private LuxLoadingSlideshow() { }

    public static void onRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (!MenuConfig.ENABLED.get() || !MenuConfig.LOADING_SLIDESHOW.get()
                || !isWorldLoadingScreen(screen)) {
            previousScreen = null;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ResourceManager manager = mc.getResourceManager();
        if (manager != indexedManager) {
            indexedManager = manager;
            available = discoverImages(manager);
            previousScreen = null;
        }
        if (available.isEmpty()) return;

        if (previousScreen != screen) {
            previousScreen = screen;
            startNanos = System.nanoTime();
            currentRound = 0L;
            playlist = new ArrayList<>(available);
            SlideShuffle.shuffleInPlace(playlist, RANDOM, null);
            nextPlaylist = shuffledAfter(playlist);

        }

        long elapsedMs = Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);
        int hold = MenuConfig.SLIDE_SECONDS.get() * 1000;
        int fade = MenuConfig.FADE_MILLIS.get();
        long duration = (long) hold + fade;
        long roundLength = Math.max(1L, duration * playlist.size());
        long round = elapsedMs / roundLength;
        if (round < currentRound) {
            currentRound = 0L;
        }
        // Prepare next order in advance so the final crossfade of a round
        // is continuous into the first photo of the next randomized round.
        while (round > currentRound) {
            playlist = nextPlaylist;
            nextPlaylist = shuffledAfter(playlist);
            currentRound++;
        }

        // Local elapsed time per round. The last image fades into the first
        // on the same round; after re-shuffle, the boundary is a new scene.
        long localTime = elapsedMs % roundLength;
        SlideTiming.Frame frame = SlideTiming.frame(localTime, playlist.size(), hold, fade);
        GuiGraphics g = event.getGuiGraphics();
        int width = g.guiWidth(), height = g.guiHeight();
        if (width <= 0 || height <= 0) return;

        drawCover(g, playlist.get(frame.current()), width, height, 1f);
        ResourceLocation nextImage = frame.current() == playlist.size() - 1
                ? nextPlaylist.get(0) : playlist.get(frame.next());
        if (frame.nextOpacity() > 0f && !playlist.get(frame.current()).equals(nextImage)) {
            drawCover(g, nextImage, width, height, frame.nextOpacity());
        }

        g.fillGradient(0, Math.max(0, height - 96), width, height, 0x00161524, 0xD9151224);
        String text = SlideTiming.loadingText(elapsedMs);
        int tx = Math.max(30, width - 27 - mc.font.width("CARREGANDO..."));
        int ty = Math.max(14, height - 23);
        g.drawString(mc.font, text, tx, ty, LuxGuiStyle.FOREGROUND, true);
        LuxGuiStyle.icon(g, "ornament_diamond", tx - 19, ty - 2, 12, 12);
        g.fill(0, height - 3, width, height - 2, 0x66AF84EE);
    }

    private static List<ResourceLocation> shuffledAfter(List<ResourceLocation> current) {
        List<ResourceLocation> next = new ArrayList<>(available);
        SlideShuffle.shuffleInPlace(next, RANDOM, current.get(current.size() - 1));
        return next;
    }

    private static boolean isWorldLoadingScreen(Screen screen) {
        return screen instanceof LevelLoadingScreen
                || screen instanceof ReceivingLevelScreen
                || screen instanceof ProgressScreen;
    }

    private static List<ResourceLocation> discoverImages(ResourceManager manager) {
        return manager.listResources("textures/gui/loading", id ->
                        id.getNamespace().equals(LuxMenu.MOD_ID)
                        && id.getPath().matches("textures/gui/loading/slide_[0-9]+\\.png"))
                .keySet().stream()
                .sorted(Comparator.comparing(ResourceLocation::getPath))
                .toList();
    }

    /** Center-cover images while preserving the original aspect ratio. */
    private static void drawCover(GuiGraphics g, ResourceLocation id,
                                  int targetW, int targetH, float alpha) {
        int cropW = IMAGE_WIDTH, cropH = IMAGE_HEIGHT;
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
