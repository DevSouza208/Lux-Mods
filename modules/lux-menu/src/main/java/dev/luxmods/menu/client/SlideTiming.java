package dev.luxmods.menu.client;

/**
 * Pure math for slide timing. No fake load percentage: the animation reflects
 * elapsed display time only, not Minecraft's real progress.
 */
public final class SlideTiming {
    private SlideTiming() { }

    public record Frame(int current, int next, float nextOpacity) { }

    public static Frame frame(long elapsedMs, int slideCount, int holdMs, int fadeMs) {
        if (slideCount <= 0) return new Frame(-1, -1, 0f);
        if (slideCount == 1) return new Frame(0, 0, 0f);

        long hold = Math.max(100L, holdMs);
        long fade = Math.max(100L, fadeMs);
        long cycle = hold + fade;
        long time = Math.max(0L, elapsedMs);
        int slide = (int) ((time / cycle) % slideCount);
        long position = time % cycle;
        float linear = position < hold ? 0f : Math.min(1f, (position - hold) / (float) fade);
        // Cubic smoothstep eliminates the hard speed change at fade boundaries.
        float eased = linear * linear * (3f - 2f * linear);
        return new Frame(slide, (slide + 1) % slideCount, eased);
    }

    public static String loadingText(long elapsedMs) {
        int dots = (int) ((Math.max(0L, elapsedMs) / 500L) % 4L);
        return "CARREGANDO" + ".".repeat(dots);
    }
}
