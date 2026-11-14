package dev.luxmods.atlas.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * Functional terrain map: reads only client-loaded chunks and never fabricates
 * unexplored geography. Left/right panels share the Lux purple/amber style.
 */
public final class AtlasMapScreen extends Screen {
    private static final int BG = 0xF0171425;
    private static final int DARK = 0xED242032;
    private static final int EDGE = 0xFFB88BDD;
    private final Map<Long, Integer> tiles = new LinkedHashMap<>(4096, 0.75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, Integer> eldest) {
            return size() > 400_000;
        }
    };
    // LRU is just a RAM optimization; every discovered cell persists on disk.
    private final Set<Long> loadedRegions = new HashSet<>();
    private final Map<Long, Integer> unsavedTiles = new LinkedHashMap<>();
    private final List<AtlasMarkers.Marker> markers = new ArrayList<>();

    private int centerX, centerZ;
    private int step = 2;
    private int mapX, mapY, mapW, mapH;
    private int selectionX, selectionZ;
    private int zoomIndex = 3;
    private boolean dragging;
    private double panRemainderX, panRemainderZ;
    private boolean firstOpen = true;
    private boolean terrainLoaded;
    private int lastMergedTick = -1;
    private boolean showMarkers = true;
    private boolean showTerrain = true;
    private boolean showPlayer = true;
    private boolean showGrid;
    private int lastStartX = Integer.MIN_VALUE, lastStartZ = Integer.MIN_VALUE;
    private String status = "Exploracao salva por regioes";

    public AtlasMapScreen() {
        super(Component.literal("Lux Atlas"));
    }

    @Override protected void init() {
        if (minecraft == null || minecraft.player == null) return;
        if (markers.isEmpty()) markers.addAll(AtlasMarkers.load(minecraft));
        if (!terrainLoaded && minecraft.level != null) {
            terrainLoaded = true;
            AtlasWalkDiscovery.flush();
            AtlasTerrainCache.migrate(minecraft, minecraft.level);
            AtlasWalkDiscovery.mergeInto(tiles);
        }
        if (firstOpen) {
            firstOpen = false;
            centerX = minecraft.player.getBlockX();
            centerZ = minecraft.player.getBlockZ();
            selectionX = centerX;
            selectionZ = centerZ;
        }
        layout();
        addControls();
    }

    private void layout() {
        int leftWidth = width >= 790 ? 143 : 92;
        int rightWidth = width >= 790 ? 168 : 0;
        mapX = leftWidth + 13;
        mapY = 55;
        mapW = Math.max(60, width - mapX - rightWidth - 21);
        mapH = Math.max(60, height - 88);
    }

    private void addControls() {
        clearWidgets();
        addButton(width / 2 - 174, 12, 105, "Missões", b -> status = "Missões: em desenvolvimento");
        addButton(width / 2 - 52, 12, 105, "Mapa", b -> status = "Mapa ativo");
        addButton(width / 2 + 70, 12, 105, "Upgrades", b -> status = "Upgrades: em desenvolvimento");
        addButton(Math.max(mapX + 7, mapX + mapW - 184), mapY + 7, 88, "+ Marcador", b -> addMarker());
        addButton(Math.max(mapX + 98, mapX + mapW - 93), mapY + 7, 84, "- Marcador", b -> removeMarker());
        addButton(12, height - 28, 34, "-", b -> setZoom(zoomIndex - 1));
        addButton(50, height - 28, 34, "+", b -> setZoom(zoomIndex + 1));
        addButton(90, height - 28, 88, "Centralizar", b -> centerPlayer());
        addButton(width - 74, height - 28, 62, "Fechar", b -> onClose());
    }

    private void addButton(int x, int y, int w, String text, Button.OnPress onPress) {
        addRenderableWidget(Button.builder(Component.literal(text), onPress)
                .bounds(x, y, w, 20).build());
    }

    private void centerPlayer() {
        if (minecraft == null || minecraft.player == null) return;
        centerX = minecraft.player.getBlockX();
        centerZ = minecraft.player.getBlockZ();
        selectionX = centerX;
        selectionZ = centerZ;
        status = "Centralizado no jogador";
    }

    private void setZoom(int value) {
        zoomIndex = Math.max(0, Math.min(5, value));
        step = new int[]{16, 8, 4, 2, 1, 1}[zoomIndex];
        // All zoom levels reuse the same world-aligned 2x2 terrain cache.
    }

    private boolean inMap(double x, double y) {
        return x >= mapX && x < mapX + mapW && y >= mapY && y < mapY + mapH;
    }

    // Each 2-pixel terrain cell corresponds to 'step' world blocks.
    private int worldX(double px) { return centerX + (int) Math.floor((px - mapX - mapW / 2.0) * step / 2.0); }
    private int worldZ(double py) { return centerZ + (int) Math.floor((py - mapY - mapH / 2.0) * step / 2.0); }

    private void addMarker() {
        if (minecraft == null || minecraft.level == null) return;
        markers.add(new AtlasMarkers.Marker("Marcador " + (markers.size() + 1), selectionX, selectionZ,
                AtlasMarkers.dimension(minecraft.level)));
        AtlasMarkers.save(minecraft, markers);
        status = "Marcador fixado: " + selectionX + ", " + selectionZ;
    }

    private void removeMarker() {
        if (minecraft == null || minecraft.level == null) return;
        String dimension = AtlasMarkers.dimension(minecraft.level);
        AtlasMarkers.Marker closest = null;
        double distance = Double.MAX_VALUE;
        for (var marker : markers) {
            if (!marker.dimension().equals(dimension)) continue;
            double d = Math.pow(marker.x() - selectionX, 2) + Math.pow(marker.z() - selectionZ, 2);
            if (d < distance) { distance = d; closest = marker; }
        }
        if (closest == null || distance > Math.pow(step * 14.0, 2)) {
            status = "Selecione um marcador para remover";
            return;
        }
        markers.remove(closest);
        AtlasMarkers.save(minecraft, markers);
        status = "Marcador removido";
    }

    @Override public boolean mouseClicked(double x, double y, int button) {
        if (super.mouseClicked(x, y, button)) return true;
        if (inMap(x, y)) {
            selectionX = worldX(x);
            selectionZ = worldZ(y);
            // Left click selects a coordinate; only right click pans the map.
            dragging = button == 1;
            panRemainderX = 0;
            panRemainderZ = 0;
            return true;
        }
        if (button == 0 && x < mapX && y > 90 && y < 175) {
            int row = ((int) y - 99) / 22;
            switch (row) {
                case 0 -> showPlayer = !showPlayer;
                case 1 -> showMarkers = !showMarkers;
                case 2 -> showTerrain = !showTerrain;
                case 3 -> showGrid = !showGrid;
                default -> { return false; }
            }
            return true;
        }
        return false;
    }

    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (dragging && button == 1) {
            panRemainderX += dx * step / 2.0;
            panRemainderZ += dy * step / 2.0;
            int moveX = (int) panRemainderX;
            int moveZ = (int) panRemainderZ;
            centerX -= moveX;
            centerZ -= moveZ;
            panRemainderX -= moveX;
            panRemainderZ -= moveZ;
            return true;
        }
        return super.mouseDragged(x, y, button, dx, dy);
    }

    @Override public boolean mouseReleased(double x, double y, int button) {
        dragging = false;
        panRemainderX = 0;
        panRemainderZ = 0;
        return super.mouseReleased(x, y, button);
    }

    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (inMap(x, y) && vertical != 0) {
            // Zoom around the cursor, not the middle of the screen.
            int wx = worldX(x), wz = worldZ(y);
            setZoom(zoomIndex + (vertical > 0 ? 1 : -1));
            centerX += wx - worldX(x);
            centerZ += wz - worldZ(y);
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    static long key(int x, int z, int sampleStep) {
        long a = ((long) x & 0xFFFFFFFL);
        long b = ((long) z & 0xFFFFFFFL);
        return (a << 36) ^ (b << 8) ^ sampleStep;
    }

    private int sample(int wx, int wz) {
        if (minecraft == null || minecraft.level == null) return 0xFF141421;
        var level = minecraft.level;
        if (!level.getChunkSource().hasChunk(wx >> 4, wz >> 4)) return 0xFF181827;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz);
        if (y <= level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) return 0xFF181827;
        BlockPos pos = new BlockPos(wx, y - 1, wz);
        var state = level.getBlockState(pos);
        int color = state.getMapColor(level, pos).col;
        if (color == 0) color = 0x576C52;
        // Directional relief shading gives mountain ridges clearer contours
        // without adding any textures, world generation or new GUI overlays.
        int northY = level.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz - 1);
        int westY = level.getHeight(Heightmap.Types.WORLD_SURFACE, wx - 1, wz);
        int relief = (y - northY) * 5 + (y - westY) * 3;
        int shade = Math.max(-37, Math.min(42, (y - 72) / 4 + relief));
        if (state.is(Blocks.WATER)) shade = -12;
        int red = Math.max(0, Math.min(255, ((color >> 16) & 255) + shade));
        int green = Math.max(0, Math.min(255, ((color >> 8) & 255) + shade));
        int blue = Math.max(0, Math.min(255, (color & 255) + shade));
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    /** Read only the persisted regions currently visible in the map window.
     *  At most six region files per rendered frame; no full-world RAM preload.
     */
    private void loadVisibleRegions(int startX, int startZ) {
        if (minecraft == null || minecraft.level == null) return;
        int endX = startX + Math.max(1, mapW / 2) * step;
        int endZ = startZ + Math.max(1, mapH / 2) * step;
        int minRx = AtlasTerrainCache.region(startX);
        int minRz = AtlasTerrainCache.region(startZ);
        int maxRx = AtlasTerrainCache.region(endX);
        int maxRz = AtlasTerrainCache.region(endZ);
        int budget = 6;
        // Load from the player's camera center outward to avoid a visible
        // upper-left sweep when opening a large world map.
        int middleRx = AtlasTerrainCache.region(centerX);
        int middleRz = AtlasTerrainCache.region(centerZ);
        int maximumDistance = Math.max(Math.max(middleRx - minRx, maxRx - middleRx),
                Math.max(middleRz - minRz, maxRz - middleRz));
        for (int radius = 0; radius <= maximumDistance && budget > 0; radius++) {
            for (int rz = Math.max(minRz, middleRz - radius);
                    rz <= Math.min(maxRz, middleRz + radius) && budget > 0; rz++) {
                for (int rx = Math.max(minRx, middleRx - radius);
                        rx <= Math.min(maxRx, middleRx + radius) && budget > 0; rx++) {
                    if (Math.max(Math.abs(rx - middleRx), Math.abs(rz - middleRz)) != radius) continue;
                    long regionId = AtlasTerrainCache.regionKey(rx, rz);
                    if (loadedRegions.add(regionId)) {
                        AtlasTerrainCache.loadRegion(minecraft, minecraft.level, rx, rz, tiles);
                        // A user's just-discovered samples take precedence.
                        tiles.putAll(unsavedTiles);
                        budget--;
                    }
                }
            }
        }
    }

    private void drawMap(GuiGraphics g) {
        g.fill(mapX - 4, mapY - 4, mapX + mapW + 4, mapY + mapH + 4, 0xFF705286);
        g.fill(mapX - 2, mapY - 2, mapX + mapW + 2, mapY + mapH + 2, 0xFFC7A372);
        g.fill(mapX, mapY, mapX + mapW, mapY + mapH, 0xFF181827);
        int cellsX = Math.max(1, mapW / 2);
        int cellsY = Math.max(1, mapH / 2);
        int drawSize = 2;
        int startX = centerX - cellsX * step / 2;
        int startZ = centerZ - cellsY * step / 2;
        // Keep the open map current when the player explores without closing it.
        if (minecraft != null && minecraft.player != null
                && minecraft.player.tickCount != lastMergedTick
                && minecraft.player.tickCount % 40 == 0) {
            lastMergedTick = minecraft.player.tickCount;
            AtlasWalkDiscovery.mergeInto(tiles);
        }
        // Disk-backed explored regions are loaded lazily, even when unloaded
        // by Minecraft itself. The player can pan to previously visited land.
        loadVisibleRegions(startX, startZ);
        lastStartX = startX;
        lastStartZ = startZ;
        g.enableScissor(mapX, mapY, mapX + mapW, mapY + mapH);
        try {
            if (showTerrain) {
                // Limit initial frame work; map fills progressively as the user explores.
                int budget = 1600;
                for (int py = 0; py < cellsY; py++) {
                    for (int px = 0; px < cellsX; px++) {
                        int wx = startX + px * step;
                        int wz = startZ + py * step;
                        // Canonical 2-block cells are independent of viewport
                        // offset and zoom; revisiting never redraws old terrain.
                        int sampleX = Math.floorDiv(wx, 2) * 2;
                        int sampleZ = Math.floorDiv(wz, 2) * 2;
                        long id = key(sampleX, sampleZ, 2);
                        Integer color = tiles.get(id);
                        if (color == null && budget > 0 && minecraft != null
                                && minecraft.level != null
                                && minecraft.level.getChunkSource().hasChunk(sampleX >> 4, sampleZ >> 4)) {
                            // Do not cache unloaded regions as empty terrain:
                            // when the player explores them later they can be sampled.
                            budget--;
                            color = sample(sampleX, sampleZ);
                            tiles.put(id, color);
                            unsavedTiles.put(id, color);
                        }
                        if (color != null) {
                            int sx = mapX + px * drawSize;
                            int sy = mapY + py * drawSize;
                            g.fill(sx, sy, sx + drawSize, sy + drawSize, color);
                        }
                    }
                }
            }
            if (showGrid) {
                for (int x = mapX; x < mapX + mapW; x += 32) g.fill(x, mapY, x + 1, mapY + mapH, 0x305D486B);
                for (int y = mapY; y < mapY + mapH; y += 32) g.fill(mapX, y, mapX + mapW, y + 1, 0x305D486B);
            }
            if (showMarkers && minecraft.level != null) {
                String dim = AtlasMarkers.dimension(minecraft.level);
                for (var marker : markers) {
                    if (!marker.dimension().equals(dim)) continue;
                    int mx = (int) Math.round(mapX + mapW / 2.0 + (marker.x() - centerX) * 2.0 / step);
                    int my = (int) Math.round(mapY + mapH / 2.0 + (marker.z() - centerZ) * 2.0 / step);
                    if (mx < mapX || mx >= mapX + mapW || my < mapY || my >= mapY + mapH) continue;
                    // Pinned diamond: coordinates remain fixed while camera pans.
                    g.fill(mx - 4, my, mx + 5, my + 1, 0xFF33243F);
                    g.fill(mx, my - 4, mx + 1, my + 5, 0xFF33243F);
                    g.fill(mx - 2, my, mx + 3, my + 1, 0xFFEBC17A);
                    g.fill(mx, my - 2, mx + 1, my + 3, 0xFFEBC17A);
                }
            }
            if (showPlayer && minecraft.player != null) {
                int px = (int) Math.round(mapX + mapW / 2.0 + (minecraft.player.getBlockX() - centerX) * 2.0 / step);
                int py = (int) Math.round(mapY + mapH / 2.0 + (minecraft.player.getBlockZ() - centerZ) * 2.0 / step);
                g.fill(px - 4, py - 4, px + 5, py + 5, 0xFF492A60);
                g.fill(px - 2, py - 2, px + 3, py + 3, 0xFFFFE6A2);
            }
            int sx = (int) Math.round(mapX + mapW / 2.0 + (selectionX - centerX) * 2.0 / step);
            int sy = (int) Math.round(mapY + mapH / 2.0 + (selectionZ - centerZ) * 2.0 / step);
            g.fill(sx - 5, sy, sx + 6, sy + 1, 0xFFDEACF8);
            g.fill(sx, sy - 5, sx + 1, sy + 6, 0xFFDEACF8);
        } finally {
            g.disableScissor();
        }
    }

    private void label(GuiGraphics g, String text, int x, int y, int color) {
        g.drawString(font, text, x, y, color, false);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, BG);
        g.fill(0, 0, width, 44, 0xFF201A30);
        g.fill(0, 43, width, 45, EDGE);
        label(g, "LUX ATLAS", 17, 19, LuxGuiStyle.AMBER);
        g.fill(7, 54, mapX - 9, height - 42, DARK);
        label(g, "CAMADAS", 15, 69, LuxGuiStyle.FOREGROUND);
        String[] options = {"Jogador", "Marcadores", "Terreno", "Grade"};
        boolean[] enabled = {showPlayer, showMarkers, showTerrain, showGrid};
        for (int i = 0; i < 4; i++) {
            label(g, (enabled[i] ? "[x] " : "[ ] ") + options[i], 14, 99 + i * 22, LuxGuiStyle.MUTED_TEXT);
        }
        label(g, "Escala: " + step + " blocos", 14, height - 59, LuxGuiStyle.AMBER);
        if (minecraft != null && minecraft.player != null && width >= 790) {
            int rx = mapX + mapW + 12;
            g.fill(rx - 5, 54, width - 9, height - 42, DARK);
            label(g, "REGIAO", rx + 3, 69, LuxGuiStyle.AMBER);
            label(g, "X: " + minecraft.player.getBlockX(), rx + 3, 92, LuxGuiStyle.FOREGROUND);
            label(g, "Z: " + minecraft.player.getBlockZ(), rx + 3, 106, LuxGuiStyle.FOREGROUND);
            label(g, "MARCADORES", rx + 3, 134, LuxGuiStyle.AMBER);
            int line = 0;
            for (var marker : markers) {
                if (line >= 9) break;
                if (minecraft.level != null && marker.dimension().equals(AtlasMarkers.dimension(minecraft.level))) {
                    label(g, marker.label(), rx + 3, 150 + line * 13, LuxGuiStyle.MUTED_TEXT);
                    line++;
                }
            }
        }
        drawMap(g);
        label(g, "N", mapX + mapW / 2, mapY + 34, LuxGuiStyle.AMBER);
        label(g, "X " + selectionX + "  Z " + selectionZ, mapX + 12, mapY + mapH - 16, 0xFFF2E9FF);
        label(g, status, Math.max(186, width / 2 - 170), height - 23, LuxGuiStyle.MUTED_TEXT);
        super.render(g, mouseX, mouseY, partialTick);
    }

    // Atlas paints a full opaque background itself. Prevent vanilla screen blur
    // from applying AFTER the map has been rendered but before the buttons.
    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // No blur: our render() draws the atlas background, terrain, and side panels.
    }

    @Override public void onClose() {
        if (minecraft != null && minecraft.level != null) {
            AtlasWalkDiscovery.flush();
            AtlasTerrainCache.save(minecraft, minecraft.level, unsavedTiles);
            unsavedTiles.clear();
        }
        super.onClose();
    }

    @Override public boolean isPauseScreen() { return false; }
}
