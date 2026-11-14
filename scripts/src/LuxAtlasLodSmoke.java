import dev.luxmods.atlas.client.AtlasLevelOfDetail;
import dev.luxmods.atlas.client.AtlasViewport;

import java.util.HashMap;
import java.util.Map;

/** JVM-only regression for known terrain/unknown fog at every map zoom. */
public final class LuxAtlasLodSmoke {
    private static void equal(Integer expected, Integer actual, String context) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(context + ": " + expected + " versus " + actual);
        }
    }

    public static void main(String[] args) {
        var samples = new HashMap<Long, Integer>();
        // Samples are deliberately not at the top-left of their 16x16 cells.
        samples.put(AtlasViewport.sampleKey(7, 9, 1), 0xffff0000);
        samples.put(AtlasViewport.sampleKey(8, 9, 1), 0xff0000ff);
        samples.put(AtlasViewport.sampleKey(-7, -9, 1), 0xff00ff00);
        samples.put(AtlasViewport.sampleKey(22, 18, 2), 0xffaa8844); // old snapshot
        AtlasLevelOfDetail lod = new AtlasLevelOfDetail();
        lod.rebuild(samples);

        equal(0xff7f007f, lod.color(0, 0, 16, samples), "average real samples at 16x");
        equal(0xff7f007f, lod.color(7, 9, 16, samples), "same cell same color");
        equal(0xff00ff00, lod.color(-16, -16, 16, samples), "negative world coordinates");
        equal(null, lod.color(80, 80, 16, samples), "unvisited region stays undiscovered");
        equal(0xffff0000, lod.color(7, 9, 1, samples), "1:1 original color unchanged");
        equal(0xffaa8844, lod.color(23, 18, 1, samples), "old 2x2 snapshot fallback");
        equal(0xffaa8844, lod.color(16, 16, 16, samples), "old snapshot regional color");
        System.out.println("LUX ATLAS LOD: sampling, legacy and fog OK");
    }
}
