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
        // Regional zoom carries a truthful coverage fraction.
        equal(2, lod.cell(0, 0, 16, samples).known(), "two exact blocks discovered");
        equal(256, lod.cell(0, 0, 16, samples).total(), "16x16 cell area");
        equal(4, lod.cell(16, 16, 16, samples).known(), "legacy 2x2 footprint");
        equal(null, lod.cell(80, 80, 16, samples), "unexplored cells stay absent");

        // New detailed samples replace only the matching old footprint block,
        // not all four blocks of an already explored 2x2 legacy square.
        samples.put(AtlasViewport.sampleKey(22, 18, 1), 0xff112233);
        lod.rebuild(samples);
        equal(4, lod.cell(16, 16, 16, samples).known(),
                "no double counting of legacy coverage");
        int blended = 0xff000000 | ((0xaa * 3 + 0x11) / 4 << 16)
                | ((0x88 * 3 + 0x22) / 4 << 8) | ((0x44 * 3 + 0x33) / 4);
        equal(blended, lod.color(16, 16, 16, samples),
                "new block replaces only its own prior approximate color");

        // A fully mapped 2x2 cell is 100% explored, not merely nonempty.
        var complete = new HashMap<Long, Integer>();
        for (int z = 0; z < 2; z++) for (int x = 0; x < 2; x++)
            complete.put(AtlasViewport.sampleKey(x, z, 1), 0xff557799);
        lod.rebuild(complete);
        equal(4, lod.cell(0, 0, 2, complete).known(), "fully explored block count");
        equal(4, lod.cell(0, 0, 2, complete).total(), "fully explored cell size");
        System.out.println("LUX ATLAS LOD: sampling, legacy and fog OK");
    }
}
