package dev.luxmods.menu.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlideTimingTest {
    @Test void emptyAndSingleAreSafe() {
        assertEquals(-1, SlideTiming.frame(120L, 0, 5000, 1200).current());
        assertEquals(0, SlideTiming.frame(9999L, 1, 5000, 1200).current());
    }

    @Test void holdsFadesAndWraps() {
        var first = SlideTiming.frame(1000L, 3, 5000, 1200);
        assertEquals(0, first.current());
        assertEquals(0f, first.nextOpacity());
        var middle = SlideTiming.frame(5600L, 3, 5000, 1200);
        assertEquals(0, middle.current());
        assertEquals(1, middle.next());
        assertEquals(0.5f, middle.nextOpacity(), 0.001f);
        assertEquals(1, SlideTiming.frame(6200L, 3, 5000, 1200).current());
        assertEquals(0, SlideTiming.frame(18600L, 3, 5000, 1200).current());
    }

    @Test void savingAndPreparingAnimateWithoutChangingStatus() {
        assertEquals("SALVANDO MUNDO..", SlideTiming.animatedLabel("SALVANDO MUNDO", 1000L));
        assertEquals("PREPARANDO RECURSOS...", SlideTiming.animatedLabel("PREPARANDO RECURSOS", 1500L));
    }

    @Test void cyclesDots() {
        assertEquals("CARREGANDO", SlideTiming.loadingText(0));
        assertEquals("CARREGANDO.", SlideTiming.loadingText(500));
        assertEquals("CARREGANDO..", SlideTiming.loadingText(1000));
        assertEquals("CARREGANDO...", SlideTiming.loadingText(1500));
        assertEquals("CARREGANDO", SlideTiming.loadingText(2000));
    }
}
