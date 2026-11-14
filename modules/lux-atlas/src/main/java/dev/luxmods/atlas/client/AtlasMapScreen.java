package dev.luxmods.atlas.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import dev.luxmods.engine.api.LuxQuestTracker;
import dev.luxmods.engine.api.LuxQuestPage;
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
import java.nio.file.Path;

/**
 * Functional terrain map: reads only client-loaded chunks and never fabricates
 * unexplored geography. Left/right panels share the Lux purple/amber style.
 */
public final class AtlasMapScreen extends Screen {
    private static final int BG = 0xF0171425;
    private static final int DARK = 0xED242032;
    private static final int EDGE = 0xFFB88BDD;
    // Explicitly pruned by visible region. LRU eviction while loading a
    // region caused the same region to be reloaded every frame on large maps.
    private final Map<Long, Integer> tiles = new LinkedHashMap<>();
    // LRU is just a RAM optimization; every discovered cell persists on disk.
    private final Set<Long> loadedRegions = new HashSet<>();
    private final AtlasLevelOfDetail lod = new AtlasLevelOfDetail();
    private boolean lodDirty = true;
    private final Map<Long, Integer> failedRegionRetry = new LinkedHashMap<>();
    private int screenTicks;
    private final Map<Long, Integer> unsavedTiles = new LinkedHashMap<>();
    // Visual overlay outlives autosave. A successful journal enqueue clears
    // dirty tiles but must not revert the still-open map to older colors.
    private final Map<Long, Integer> screenSamples = new LinkedHashMap<>();
    private Path openedScope;
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
    private int autosaveTicks;
    private int terrainScanCursor;
    private int cachedMinRx = Integer.MIN_VALUE, cachedMinRz = Integer.MIN_VALUE;
    private int cachedMaxRx = Integer.MIN_VALUE, cachedMaxRz = Integer.MIN_VALUE;
    private boolean showMarkers = true;
    private boolean showTerrain = true;
    private boolean showPlayer = true;
    private boolean showGrid;
    private boolean questsTab;
    private boolean showQuests = true;
    private int questPage;
    private int lastStartX = Integer.MIN_VALUE, lastStartZ = Integer.MIN_VALUE;
    private String status = "Exploracao salva por regioes";

    public AtlasMapScreen() {
        super(Component.literal("Lux Atlas"));
    }

    @Override protected void init() {
        if (minecraft == null || minecraft.player == null) return;
        if (markers.isEmpty()) markers.addAll(AtlasMarkers.load(minecraft));
        if (!terrainLoaded && minecraft.level != null) {
            openedScope = AtlasTerrainCache.scope(minecraft, minecraft.level);
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

    /** Keep the mission pager below visible rows even at reduced GUI heights. */
    private int questPageSize() {
        return Math.max(1, Math.min(5, (height - 170) / 28));
    }

    private int questPagerY() {
        return 105 + questPageSize() * 28;
    }

    private void addControls() {
        clearWidgets();
        addButton(width / 2 - 174, 12, 105, "Missões", b -> {
            questsTab = true;
            status = "Missões conhecidas nesta sessão";
            rebuildWidgets();
        });
        addButton(width / 2 - 52, 12, 105, "Mapa", b -> {
            questsTab = false;
            status = "Mapa ativo";
            rebuildWidgets();
        });
        addButton(width / 2 + 70, 12, 105, "Upgrades", b -> status = "Upgrades: em desenvolvimento");
        if (questsTab) {
            addButton(mapX + 7, mapY + 60, 112,
                    showQuests ? "Ocultar missões" : "Mostrar missões", b -> {
                showQuests = !showQuests;
                rebuildWidgets();
            });
            addButton(12, questPagerY(), 38, "◀", b -> {
                questPage = Math.max(0, questPage - 1);
            });
            addButton(56, questPagerY(), 38, "▶", b -> {
                if (minecraft == null || minecraft.level == null || minecraft.player == null) return;
                int count = LuxQuestTracker.inDimension(
                        minecraft.level.dimension().location().toString()).size();
                questPage = Math.min(LuxQuestPage.pages(count, questPageSize()) - 1, questPage + 1);
            });
        }
        addButton(Math.max(mapX + 7, mapX + mapW - 184), mapY + 7, 88, "+ Marcador", b -> addMarker());
        addButton(Math.max(mapX + 98, mapX + mapW - 93), mapY + 7, 84, "- Marcador", b -> removeMarker());
        addButton(Math.max(mapX + 7, mapX + mapW - 184), mapY + 33, 88, "Editar", btn -> editMarker());
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
        // Zoom changes only display resolution; archive keeps one-block detail.
    }

    private boolean inMap(double x, double y) {
        return x >= mapX && x < mapX + mapW && y >= mapY && y < mapY + mapH;
    }

    // Each 2-pixel terrain cell corresponds to 'step' world blocks.
    private int startX() { return AtlasViewport.start(centerX, mapW, step); }
    private int startZ() { return AtlasViewport.start(centerZ, mapH, step); }
    private int worldX(double px) { return AtlasViewport.pixelToWorld(px, mapX, startX(), step); }
    private int worldZ(double py) { return AtlasViewport.pixelToWorld(py, mapY, startZ(), step); }
    private int screenX(int wx) { return AtlasViewport.worldToPixel(wx, mapX, startX(), step); }
    private int screenZ(int wz) { return AtlasViewport.worldToPixel(wz, mapY, startZ(), step); }

    private void addMarker() {
        if (minecraft == null || minecraft.level == null) return;
        AtlasMarkers.Marker fresh = new AtlasMarkers.Marker("Marcador " + (markers.size() + 1),
                selectionX, selectionZ, AtlasMarkers.dimension(minecraft.level));
        minecraft.setScreen(new AtlasMarkerEditScreen(this, fresh, marker -> {
            markers.add(marker);
            AtlasMarkers.save(minecraft, markers);
            status = "Marcador salvo: " + marker.label();
        }));
    }

    private void editMarker() {
        AtlasMarkers.Marker closest = findSelectedMarker();
        if (closest == null) {
            status = "Selecione um marcador proximo";
            return;
        }
        minecraft.setScreen(new AtlasMarkerEditScreen(this, closest, updated -> {
            markers.set(markers.indexOf(closest), updated);
            AtlasMarkers.save(minecraft, markers);
            status = "Marcador atualizado: " + updated.label();
        }));
    }

    private AtlasMarkers.Marker findSelectedMarker() {
        if (minecraft == null || minecraft.level == null) return null;
        String dim = AtlasMarkers.dimension(minecraft.level);
        AtlasMarkers.Marker closest = null;
        double best = Double.MAX_VALUE;
        for (var marker : markers) {
            if (!marker.dimension().equals(dim)) continue;
            double dx = (double) marker.x() - selectionX;
            double dz = (double) marker.z() - selectionZ;
            double distance = dx * dx + dz * dz;
            if (distance < best) {
                best = distance;
                closest = marker;
            }
        }
        return closest != null && best <= Math.pow(step * 14.0, 2) ? closest : null;
    }

    private void removeMarker() {
        if (minecraft == null || minecraft.level == null) return;
        AtlasMarkers.Marker closest = findSelectedMarker();
        if (closest == null) {
            status = "Selecione um marcador para remover";
            return;
        }
        minecraft.setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(confirmed -> {
            if (confirmed) {
                markers.remove(closest);
                AtlasMarkers.save(minecraft, markers);
                status = "Marcador removido";
            }
            minecraft.setScreen(this);
        }, Component.literal("Remover marcador?"), Component.literal(closest.label())));
    }

    @Override public boolean mouseClicked(double x, double y, int button) {
        if (super.mouseClicked(x, y, button)) return true;
        if (inMap(x, y)) {
            if (button == 0 && showQuests && minecraft != null && minecraft.level != null) {
                String dimension = minecraft.level.dimension().location().toString();
                for (var quest : LuxQuestTracker.inDimension(dimension)) {
                    int markerX = screenX(quest.x()), markerY = screenZ(quest.z());
                    if (Math.abs(x - markerX) <= 8 && Math.abs(y - markerY) <= 8) {
                        selectionX = quest.x();
                        selectionZ = quest.z();
                        status = "NPC: " + quest.quantity() + "x " + quest.objective();
                        return true;
                    }
                }
            }
            selectionX = worldX(x);
            selectionZ = worldZ(y);
            // Left click selects a coordinate; only right click pans the map.
            dragging = button == 1;
            panRemainderX = 0;
            panRemainderZ = 0;
            return true;
        }
        if (button == 0 && questsTab && x < mapX && y >= 95) {
            if (minecraft == null || minecraft.level == null || minecraft.player == null) return false;
            var active = LuxQuestTracker.nearest(minecraft.level.dimension().location().toString(),
                            minecraft.player.getBlockX(), minecraft.player.getBlockZ());
            int index = ((int) y - 100) / 28;
            var page = LuxQuestPage.slice(active, questPage, questPageSize());
            if (index >= 0 && index < page.size() && index < questPageSize()) {
                var quest = page.get(index);
                centerX = quest.x();
                centerZ = quest.z();
                selectionX = centerX;
                selectionZ = centerZ;
                status = "Centralizado no NPC do contrato";
                return true;
            }
        }
        if (button == 0 && !questsTab && x < mapX && y > 90 && y < 175) {
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
        return AtlasViewport.sampleKey(x, z, sampleStep);
    }

    /** Never record samples from a level different from the one that opened this screen. */
    private boolean isOriginalWorld() {
        return minecraft != null && minecraft.level != null && openedScope != null
                && openedScope.equals(AtlasTerrainCache.scope(minecraft, minecraft.level));
    }

    private int sample(int wx, int wz) {
        if (!isOriginalWorld()) return 0xFF141421;
        var level = minecraft.level;
        if (!level.getChunkSource().hasChunk(wx >> 4, wz >> 4)) return 0xFF181827;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz);
        if (y <= level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) return 0xFF181827;
        BlockPos pos = new BlockPos(wx, y - 1, wz);
        var state = level.getBlockState(pos);
        // Shade only from loaded neighbors; do not force chunk loads.
        int northY = level.getChunkSource().hasChunk(wx >> 4, (wz - 1) >> 4)
                ? level.getHeight(Heightmap.Types.WORLD_SURFACE, wx, wz - 1) : y;
        int westY = level.getChunkSource().hasChunk((wx - 1) >> 4, wz >> 4)
                ? level.getHeight(Heightmap.Types.WORLD_SURFACE, wx - 1, wz) : y;
        return AtlasTerrainColor.shade(state.getMapColor(level, pos).col,
                y, northY, westY, state.is(Blocks.WATER));
    }

    /** Read only the persisted regions currently visible in the map window.
     *  At most six region files per rendered frame; no full-world RAM preload.
     */
    private void loadVisibleRegions(int startX, int startZ) {
        if (!isOriginalWorld()) return;
        int endX = startX + Math.max(1, mapW / 2) * step;
        int endZ = startZ + Math.max(1, mapH / 2) * step;
        int minRx = AtlasTerrainCache.region(startX);
        int minRz = AtlasTerrainCache.region(startZ);
        int maxRx = AtlasTerrainCache.region(endX);
        int maxRz = AtlasTerrainCache.region(endZ);
        // Drop only out-of-view RAM copies, never explored files on disk.
        // Returning to an older area forces its regions to be reloaded.
        if (minRx != cachedMinRx || minRz != cachedMinRz
                || maxRx != cachedMaxRx || maxRz != cachedMaxRz) {
            cachedMinRx = minRx;
            cachedMinRz = minRz;
            cachedMaxRx = maxRx;
            cachedMaxRz = maxRz;
            final int left = minRx - 1, right = maxRx + 1;
            final int top = minRz - 1, bottom = maxRz + 1;
            loadedRegions.removeIf(id -> {
                int rx = (int) (id >> 32), rz = (int) (long) id;
                return rx < left || rx > right || rz < top || rz > bottom;
            });
            failedRegionRetry.keySet().removeIf(id -> {
                int rx = (int) (id >> 32), rz = (int) (long) id;
                return rx < left || rx > right || rz < top || rz > bottom;
            });
            tiles.keySet().removeIf(id -> {
                int bx = (int) ((id >>> 36) & 0x0fffffffL);
                int bz = (int) ((id >>> 8) & 0x0fffffffL);
                bx = (bx << 4) >> 4;
                bz = (bz << 4) >> 4;
                int rx = AtlasTerrainCache.region(bx), rz = AtlasTerrainCache.region(bz);
                return rx < left || rx > right || rz < top || rz > bottom;
            });
            tiles.putAll(screenSamples);
            lodDirty = true;
        }
        int budget = 1; // one compressed region per client tick
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
                    if (!loadedRegions.contains(regionId)
                            && screenTicks >= failedRegionRetry.getOrDefault(regionId, 0)) {
                        boolean loaded = AtlasTerrainCache.loadRegion(
                                minecraft, minecraft.level, rx, rz, tiles);
                        if (loaded) {
                            loadedRegions.add(regionId);
                            failedRegionRetry.remove(regionId);
                            // The region on disk may lag behind the active
                            // walking collector and this screen's edits.
                            // Precedence: persisted region < live walk < local edits.
                            AtlasWalkDiscovery.mergeInto(tiles, screenSamples);
                            lodDirty = true;
                        } else {
                            failedRegionRetry.put(regionId, screenTicks + 100);
                        }
                        budget--;
                    }
                }
            }
        }
    }

    private static String markerGlyph(String kind) {
        return switch (kind.toLowerCase(java.util.Locale.ROOT)) {
            case "casa" -> "C";
            case "vila" -> "V";
            case "portal" -> "P";
            case "torre" -> "T";
            case "mina" -> "M";
            case "cavalo" -> "H";
            case "estrutura" -> "◆";
            default -> "✦";
        };
    }

    /**
     * At regional zoom a filled 2x2 square means all underlying blocks are
     * documented. One to three colored pixels indicate partial exploration.
     * Unexplored remains dark; never paints a whole cell from one sample.
     */
    private static void drawCoverageCell(GuiGraphics g, int x, int y,
                                         AtlasLevelOfDetail.Cell cell) {
        if (cell == null) return;
        if (cell.known() >= cell.total()) {
            g.fill(x, y, x + 2, y + 2, cell.color());
            return;
        }
        int pixels = Math.max(1, (int) Math.ceil(4.0 * cell.fraction()));
        int color = cell.color();
        // A trace amount of exploration should not look like 25% coverage.
        if (cell.fraction() < 0.125) {
            int r = (((color >>> 16) & 255) + 24) / 2;
            int green = (((color >>> 8) & 255) + 24) / 2;
            int b = ((color & 255) + 39) / 2;
            color = 0xff000000 | r << 16 | green << 8 | b;
        }
        if (pixels >= 1) g.fill(x, y, x + 1, y + 1, color);
        if (pixels >= 2) g.fill(x + 1, y + 1, x + 2, y + 2, color);
        if (pixels >= 3) g.fill(x + 1, y, x + 2, y + 1, color);
        if (pixels >= 4) g.fill(x, y + 1, x + 1, y + 2, color);
    }

    private void drawMap(GuiGraphics g) {
        g.fill(mapX - 4, mapY - 4, mapX + mapW + 4, mapY + mapH + 4, 0xFF705286);
        g.fill(mapX - 2, mapY - 2, mapX + mapW + 2, mapY + mapH + 2, 0xFFC7A372);
        g.fill(mapX, mapY, mapX + mapW, mapY + mapH, 0xFF181827);
        int cellsX = Math.max(1, mapW / 2);
        int cellsY = Math.max(1, mapH / 2);
        int drawSize = 2;
        int startX = startX();
        int startZ = startZ();
        // Keep the open map current when the player explores without closing it.
        if (isOriginalWorld() && minecraft.player != null
                && minecraft.player.tickCount != lastMergedTick
                && minecraft.player.tickCount % 40 == 0) {
            lastMergedTick = minecraft.player.tickCount;
            // A successful journal enqueue clears unsavedTiles, but these
            // visual overlays remain until the screen is closed.
            AtlasWalkDiscovery.mergeInto(tiles, screenSamples);
            lodDirty = true;
        }
        // Region IO occurs in tick(); rendering only reads the RAM snapshot.
        lastStartX = startX;
        lastStartZ = startZ;
        g.enableScissor(mapX, mapY, mapX + mapW, mapY + mapH);
        try {
            if (showTerrain) {
                // Limit initial frame work; map fills progressively as the user explores.
                for (int py = 0; py < cellsY; py++) {
                    for (int px = 0; px < cellsX; px++) {
                        int wx = startX + px * step;
                        int wz = startZ + py * step;
                        // Rendering must be read-only. Terrain sampling is budgeted
                        // in tick(), otherwise fast monitors trigger repeated
                        // heightmap/block lookups and severe frame-time spikes.
                        AtlasLevelOfDetail.Cell cell = lod.cell(wx, wz, step, tiles);
                        if (cell != null) {
                            int sx = mapX + px * drawSize;
                            int sy = mapY + py * drawSize;
                            drawCoverageCell(g, sx, sy, cell);
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
                    int mx = screenX(marker.x());
                    int my = screenZ(marker.z());
                    if (mx < mapX || mx >= mapX + mapW || my < mapY || my >= mapY + mapH) continue;
                    // Typed waypoint with persistent user color.
                    String glyph = markerGlyph(marker.icon());
                    g.fill(mx - 6, my - 6, mx + 7, my + 6, 0xCC21162F);
                    g.fill(mx - 6, my - 6, mx + 7, my - 5, marker.color());
                    g.drawString(font, glyph, mx - font.width(glyph) / 2,
                            my - 4, marker.color(), true);
                }
            }
            // Contracts received from the authoritative NPC server snapshot.
            // Only known positions are marked, never invented coordinates.
            if (showQuests && minecraft != null && minecraft.level != null) {
                String dimension = minecraft.level.dimension().location().toString();
                for (var quest : LuxQuestTracker.inDimension(dimension)) {
                    int qx = screenX(quest.x()), qz = screenZ(quest.z());
                    if (qx < mapX || qx >= mapX + mapW || qz < mapY || qz >= mapY + mapH) continue;
                    g.fill(qx - 5, qz - 5, qx + 6, qz + 6, 0xFF3B1D4C);
                    g.drawString(font, "!", qx - 2, qz - 4, 0xFFFFD68A, true);
                }
            }
            if (showPlayer && minecraft.player != null) {
                int px = screenX(minecraft.player.getBlockX());
                int py = screenZ(minecraft.player.getBlockZ());
                g.fill(px - 4, py - 4, px + 5, py + 5, 0xFF492A60);
                g.fill(px - 2, py - 2, px + 3, py + 3, 0xFFFFE6A2);
            }
            int sx = screenX(selectionX);
            int sy = screenZ(selectionZ);
            g.fill(sx - 5, sy, sx + 6, sy + 1, 0xFFDEACF8);
            g.fill(sx, sy - 5, sx + 1, sy + 6, 0xFFDEACF8);
        } finally {
            g.disableScissor();
        }
    }

    /** Read-only progress from the local inventory; NPC remains authoritative. */
    private int itemsCarried(String objective) {
        if (minecraft == null || minecraft.player == null) return 0;
        net.minecraft.world.item.Item required = switch (objective) {
            case "minecraft:coal" -> net.minecraft.world.item.Items.COAL;
            case "minecraft:bread" -> net.minecraft.world.item.Items.BREAD;
            case "minecraft:iron_ingot" -> net.minecraft.world.item.Items.IRON_INGOT;
            default -> null;
        };
        if (required == null) return 0;
        int total = 0;
        var inventory = minecraft.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (stack.is(required)) total += stack.getCount();
        }
        return total;
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
        if (questsTab) {
            label(g, "MISSÕES", 15, 69, LuxGuiStyle.AMBER);
            var quests = minecraft == null || minecraft.level == null || minecraft.player == null
                    ? List.<LuxQuestTracker.Contract>of()
                    : LuxQuestTracker.nearest(minecraft.level.dimension().location().toString(),
                            minecraft.player.getBlockX(), minecraft.player.getBlockZ());
            if (quests.isEmpty()) label(g, "Nenhuma conhecida", 14, 99, LuxGuiStyle.MUTED_TEXT);
            questPage = LuxQuestPage.clamp(questPage, quests.size(), questPageSize());
            int totalPages = LuxQuestPage.pages(quests.size(), questPageSize());
            label(g, "Página " + (questPage + 1) + "/" + totalPages, 14, 83, LuxGuiStyle.AMBER);
            int line = 0;
            for (var quest : LuxQuestPage.slice(quests, questPage, questPageSize())) {
                String item = switch (quest.objective()) {
                    case "minecraft:coal" -> "Carvão";
                    case "minecraft:bread" -> "Pão";
                    case "minecraft:iron_ingot" -> "Ferro";
                    default -> "Material";
                };
                int owned = Math.min(quest.quantity(), itemsCarried(quest.objective()));
                String progress = quest.status() == 4 ? "Em revisão"
                        : owned + "/" + quest.quantity();
                label(g, progress + " " + item, 14, 100 + 28 * line, LuxGuiStyle.FOREGROUND);
                label(g, quest.status() == 4 ? "Em revisão" : "Ver NPC no mapa",
                        14, 112 + 28 * line, LuxGuiStyle.MUTED_TEXT);
                line++;
            }
        } else {
            label(g, "CAMADAS", 15, 69, LuxGuiStyle.FOREGROUND);
            String[] options = {"Jogador", "Marcadores", "Terreno", "Grade"};
            boolean[] enabled = {showPlayer, showMarkers, showTerrain, showGrid};
            for (int i = 0; i < 4; i++) {
                label(g, (enabled[i] ? "[x] " : "[ ] ") + options[i],
                        14, 99 + i * 22, LuxGuiStyle.MUTED_TEXT);
            }
        }
        // Explain partial pixels so a single explored point is never mistaken
        // for an entirely surveyed regional cell.
        if (height >= 255 && mapX >= 120) {
            label(g, "EXPLORACAO", 14, height - 101, LuxGuiStyle.FOREGROUND);
            g.fill(15, height - 87, 23, height - 79, 0xFF78B891);
            label(g, "Completa", 28, height - 86, LuxGuiStyle.MUTED_TEXT);
            g.fill(15, height - 70, 19, height - 66, 0xFF78B891);
            label(g, "Parcial", 28, height - 71, LuxGuiStyle.MUTED_TEXT);
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
        AtlasWriteQueue.Status disk = AtlasWriteQueue.status();
        String currentStatus = disk.error() ? "ERRO NO MAPA: " + disk.message()
                : disk.pendingSamples() > 0 || disk.writing() ? "Salvando exploracao..."
                : status;
        label(g, currentStatus, Math.max(186, width / 2 - 170), height - 23,
                disk.error() ? 0xFFFF8787 : LuxGuiStyle.MUTED_TEXT);
        super.render(g, mouseX, mouseY, partialTick);
    }

    // Atlas paints a full opaque background itself. Prevent vanilla screen blur
    // from applying AFTER the map has been rendered but before the buttons.
    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // No blur: our render() draws the atlas background, terrain, and side panels.
    }

    private void submitDirtyTiles() {
        if (openedScope == null || unsavedTiles.isEmpty()) return;
        if (AtlasWriteQueue.enqueue(openedScope, unsavedTiles))
            unsavedTiles.clear();
    }

    @Override public void onClose() {
        AtlasWalkDiscovery.flush();
        submitDirtyTiles();
        if (!unsavedTiles.isEmpty() && openedScope != null) {
            // The screen will be destroyed. Preserve data under the ORIGINAL
            // world path even if Minecraft changed level before closing it.
            AtlasWalkDiscovery.keepUnsubmitted(openedScope, unsavedTiles);
            unsavedTiles.clear();
        }
        super.onClose();
    }

    /**
     * Read a small number of already loaded columns per game tick.
     * No terrain queries happen during rendering and no distant chunks
     * are generated. The incremental cursor continues across ticks.
     */
    private void sampleVisibleTerrain() {
        if (!showTerrain || !isOriginalWorld()) return;
        int cellsX = Math.max(1, mapW / 2);
        int cellsZ = Math.max(1, mapH / 2);
        int count = cellsX * cellsZ;
        int firstX = startX(), firstZ = startZ();
        int budget = Math.min(96, count);
        for (int i = 0; i < budget; i++) {
            int index = Math.floorMod(terrainScanCursor++, count);
            int wx = firstX + (index % cellsX) * step;
            int wz = firstZ + (index / cellsX) * step;
            if (!minecraft.level.getChunkSource().hasChunk(wx >> 4, wz >> 4)) continue;
            long id = key(wx, wz, 1);
            if (tiles.containsKey(id)) continue;
            int color = sample(wx, wz);
            tiles.put(id, color);
            screenSamples.put(id, color);
            unsavedTiles.put(id, color);
            lodDirty = true;
        }
    }

    @Override public void tick() {
        screenTicks++;
        sampleVisibleTerrain();
        // Region files are loaded during screen ticks, not once per rendered
        // frame, so uncached disk IO does not repeat at the monitor refresh rate.
        loadVisibleRegions(startX(), startZ());
        // Build only from loaded and discovered samples. Avoid rebuilding
        // hundreds of thousands of aggregates at display refresh speed.
        if (lodDirty && screenTicks % 20 == 0) {
            lod.rebuild(tiles);
            lodDirty = false;
        }
        if (++autosaveTicks >= 200) {
            autosaveTicks = 0;
            submitDirtyTiles();
            AtlasWriteQueue.retry();
        }
    }

    @Override public boolean isPauseScreen() { return false; }
}
