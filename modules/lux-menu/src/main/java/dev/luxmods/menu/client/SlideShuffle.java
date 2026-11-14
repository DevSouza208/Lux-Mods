package dev.luxmods.menu.client;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Pure Java playlist shuffle. A round visits each image once.
 * Tries not to repeat a boundary image on the next round.
 */
public final class SlideShuffle {
    private SlideShuffle() { }

    public static <T> void shuffleInPlace(List<T> images, Random random, T previousLast) {
        if (images == null || random == null) throw new IllegalArgumentException();
        if (images.isEmpty()) return;
        Collections.shuffle(images, random);
        if (images.size() > 1 && previousLast != null
                && previousLast.equals(images.get(0))) {
            Collections.swap(images, 0, 1);
        }
    }
}
