package dev.luxmods.atlas.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** Simple marker editor: no external UI assets or server dependency. */
public final class AtlasMarkerEditScreen extends Screen {
    private static final String[] TYPES = {"Casa", "Vila", "Portal", "Torre", "Mina", "Cavalo", "Outro"};
    private static final int[] COLORS = {
            0xFFE8BE78, 0xFFB792E9, 0xFF69C1E8,
            0xFF84C78E, 0xFFF08080, 0xFFEAEAEA
    };

    private final Screen previous;
    private final AtlasMarkers.Marker original;
    private final Consumer<AtlasMarkers.Marker> onSave;
    private int typeIndex;
    private int colorIndex;
    private EditBox name;

    public AtlasMarkerEditScreen(Screen previous, AtlasMarkers.Marker original,
                                 Consumer<AtlasMarkers.Marker> onSave) {
        super(Component.literal("Editar marcador"));
        this.previous = previous;
        this.original = original;
        this.onSave = onSave;
        for (int i = 0; i < TYPES.length; i++) {
            if (TYPES[i].equalsIgnoreCase(original.icon())) typeIndex = i;
        }
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == original.color()) colorIndex = i;
        }
    }

    @Override protected void init() {
        int left = width / 2 - 128, top = height / 2 - 70;
        name = new EditBox(font, left + 10, top + 30, 236, 20, Component.literal("Nome"));
        name.setMaxLength(48);
        name.setValue(original.label());
        addRenderableWidget(name);
        addRenderableWidget(Button.builder(Component.literal("Tipo: " + TYPES[typeIndex]), b -> {
            typeIndex = (typeIndex + 1) % TYPES.length;
            b.setMessage(Component.literal("Tipo: " + TYPES[typeIndex]));
        }).bounds(left + 10, top + 58, 145, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cor"), b -> {
            colorIndex = (colorIndex + 1) % COLORS.length;
        }).bounds(left + 163, top + 58, 83, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Salvar"), b -> save())
                .bounds(left + 10, top + 91, 113, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancelar"), b -> onClose())
                .bounds(left + 133, top + 91, 113, 20).build());
        setInitialFocus(name);
    }

    private void save() {
        String label = name.getValue().trim();
        if (label.isEmpty()) return;
        onSave.accept(new AtlasMarkers.Marker(label, original.x(), original.z(),
                original.dimension(), TYPES[typeIndex], COLORS[colorIndex]));
        onClose();
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xCC171323);
        int left = width / 2 - 128, top = height / 2 - 70;
        LuxGuiStyle.panel(g, left, top, 256, 125);
        g.drawString(font, "EDITAR MARCADOR", left + 12, top + 11, 0xFFF0CC87, true);
        g.fill(left + 238, top + 64, left + 244, top + 73, COLORS[colorIndex]);
        g.drawString(font, "X " + original.x() + "  Z " + original.z(),
                left + 12, top + 115, 0xFFE5D6F5, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {}

    @Override public void onClose() {
        if (minecraft != null) minecraft.setScreen(previous);
    }

    @Override public boolean isPauseScreen() { return false; }
}
