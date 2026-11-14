package dev.luxmods.menu.client;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class SlideShuffleTest {
    @Test void visitsAllImagesEachRoundWithoutRepetition() {
        var images = new ArrayList<Integer>();
        for (int i = 0; i < 125; i++) images.add(i);
        SlideShuffle.shuffleInPlace(images, new Random(1234), null);
        assertEquals(125, new HashSet<>(images).size());
        int prevLast = images.get(images.size() - 1);
        SlideShuffle.shuffleInPlace(images, new Random(1), prevLast);
        assertEquals(125, new HashSet<>(images).size());
        assertNotEquals(prevLast, images.get(0));
    }

    @Test void supportsEmptyAndSingleImage() {
        var empty = new ArrayList<String>();
        SlideShuffle.shuffleInPlace(empty, new Random(1), null);
        assertTrue(empty.isEmpty());
        var single = new ArrayList<>(List.of("one"));
        SlideShuffle.shuffleInPlace(single, new Random(2), "one");
        assertEquals(List.of("one"), single);
    }
}
