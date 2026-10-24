package dev.luxmods.menu.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

/**
 * Decorates a vanilla button and delegates presses to the ORIGINAL object.
 * Minecraft's action handlers (including mods/options/quit/resume) stay intact.
 */
final class LuxMenuButton extends Button {
    private final Button original;

    LuxMenuButton(Button original) {
        super(original.getX(), original.getY(), original.getWidth(), original.getHeight(),
                original.getMessage(), ignored -> original.onPress(), DEFAULT_NARRATION);
        this.original = original;
        this.active = original.active;
        this.visible = original.visible;
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Keep the original button's state in sync when vanilla changes it later.
        this.active = original.active;
        this.visible = original.visible;
        this.setMessage(original.getMessage());
        if (!visible) return;

        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean selected = active && isHoveredOrFocused();
        int outer = !active ? 0xFF484351 : selected ? 0xFFCE9DF8 : LuxGuiStyle.EDGE;
        int top = selected ? 0xFFCF9CFF : LuxGuiStyle.TOP_ACCENT;
        int bg = selected ? 0xEF2B2140 : 0xED191621;
        int text = !active ? 0xFF9D96A8 : selected ? 0xFFFFFFFF : LuxGuiStyle.FOREGROUND;

        g.fill(x + 1, y + 2, x + w + 2, y + h + 2, 0x6004000C);
        g.fill(x, y, x + w, y + h, bg);
        g.fill(x, y, x + w, y + 1, top);
        g.fill(x, y + h - 1, x + w, y + h, outer);
        g.fill(x, y, x + 1, y + h, outer);
        g.fill(x + w - 1, y, x + w, y + h, outer);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, 0x667E668E);

        // The jewel is decorative only; missing images never obscure the action.
        if (selected) {
            LuxGuiStyle.icon(g, "ornament_diamond", x + 4, y + h / 2 - 4, 8, 8);
            LuxGuiStyle.icon(g, "ornament_diamond", x + w - 12, y + h / 2 - 4, 8, 8);
        }
        g.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                x + w / 2, y + (h - 8) / 2, text);
    }
}
