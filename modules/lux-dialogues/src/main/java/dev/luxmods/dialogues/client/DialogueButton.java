package dev.luxmods.dialogues.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Purple-and-gold Lux action: uses Minecraft's normal focus and click handling. */
public final class DialogueButton extends Button {
    public DialogueButton(int x, int y, int w, int h, Component text, OnPress callback) {
        super(x, y, w, h, text, callback, DEFAULT_NARRATION);
    }

    @Override protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean hover = isHoveredOrFocused();
        int edge = !active ? 0xFF585264 : hover ? 0xFFE0B6FF : LuxGuiStyle.TOP_ACCENT;
        int bg = hover && active ? 0xF042315B : 0xF0221A32;
        g.fill(x + 2, y + 3, x + w + 2, y + h + 3, 0x88090012);
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, edge);
        g.fill(x, y + h - 1, x + w, y + h, edge);
        g.fill(x, y, x + 1, y + h, edge);
        g.fill(x + w - 1, y, x + w, y + h, edge);
        if (hover && active) {
            LuxGuiStyle.icon(g, "ornament_diamond", x + 4, y + h / 2 - 4, 8, 8);
            LuxGuiStyle.icon(g, "ornament_diamond", x + w - 12, y + h / 2 - 4, 8, 8);
        }
        g.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                x + w / 2, y + (h - 8) / 2, !active ? 0xFF9C93AB : 0xFFF8ECFF);
    }
}
