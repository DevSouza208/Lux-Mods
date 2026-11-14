import dev.luxmods.atlas.client.AtlasTileLayers;
import java.util.HashMap;
import java.util.Map;

public final class LuxAtlasTileLayersSmoke {
    public static void main(String[] args) {
        long shared = 101L, walkOnly = 102L, diskOnly = 103L;
        Map<Long, Integer> tiles = new HashMap<>(Map.of(
                shared, 0xff111111, diskOnly, 0xff222222));
        Map<Long, Integer> walking = Map.of(
                shared, 0xff333333, walkOnly, 0xff444444);
        Map<Long, Integer> screen = Map.of(shared, 0xff555555);

        AtlasTileLayers.overlay(tiles, walking, screen);
        if (!tiles.equals(Map.of(
                shared, 0xff555555, walkOnly, 0xff444444, diskOnly, 0xff222222)))
            throw new AssertionError("Expected disk < walking < screen precedence");

        // Autosave clears the dirty journal candidates, not visual screen
        // edits. Overlaying another disk snapshot cannot revert the color.
        tiles.put(shared, 0xff111111);
        AtlasTileLayers.overlay(tiles, walking, screen);
        if (tiles.get(shared) != 0xff555555)
            throw new AssertionError("Disk reload reverted screen edit");

        // If no screen edit exists, walking observations win over stale disk.
        screen = Map.of();
        tiles.put(shared, 0xff111111);
        AtlasTileLayers.overlay(tiles, walking, screen);
        if (tiles.get(shared) != 0xff333333)
            throw new AssertionError("Live walk color was overwritten");

        System.out.println("LUX ATLAS TILE LAYERS: OK");
    }
}
