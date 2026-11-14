import java.util.HashMap;
import java.util.Map;
import dev.luxmods.atlas.client.AtlasRegionMerge;

/** Standalone regression: repeated writes may not rewrite unchanged regions. */
public final class LuxAtlasRegionMergeSmoke {
    public static void main(String[] args) {
        Map<Long, Integer> region = new HashMap<>();
        if (!AtlasRegionMerge.merge(region, Map.of(1L, 0xff234567)))
            throw new AssertionError("New sample must change region");
        if (AtlasRegionMerge.merge(region, Map.of(1L, 0xff234567)))
            throw new AssertionError("Identical data must not trigger recompression");
        if (!AtlasRegionMerge.merge(region, Map.of(1L, 0xff987654)))
            throw new AssertionError("Changed terrain sample must be stored");
        if (!AtlasRegionMerge.merge(region, Map.of(2L, 0xff000000)))
            throw new AssertionError("Different coordinate must be added");
        if (region.size() != 2 || region.get(1L) != 0xff987654)
            throw new AssertionError("Updates must preserve unrelated coordinates");
        System.out.println("LUX ATLAS REGION MERGE: OK");
    }
}
