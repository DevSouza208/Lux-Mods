package dev.luxmods.atlas.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Dedicated upgrades menu shell. Do not fabricate unimplemented RPG points,
 * purchasable perks, or client-side attribute changes.
 */
public final class AtlasUpgradesScreen extends Screen {
    public AtlasUpgradesScreen() {
        super(Component.literal("Lux Mods - Upgrades"));
    }

    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
                .bounds(width / 2 - 45, height - 36, 90, 20).build());
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xEE14111F);
        int left = Math.max(10, width / 2 - 180);
        int right = Math.min(width - 10, width / 2 + 180);
        LuxGuiStyle.panel(g, left, 12, right - left, height - 18);
        g.drawCenteredString(font, "UPGRADES", width / 2, 25, LuxGuiStyle.AMBER);
        if (minecraft != null && minecraft.player != null) {
            g.drawCenteredString(font, "Nível atual: " + minecraft.player.experienceLevel,
                    width / 2, 64, LuxGuiStyle.FOREGROUND);
        }
        g.drawCenteredString(font, "Melhorias ainda não disponíveis.",
                width / 2, height / 2 - 10, LuxGuiStyle.MUTED_TEXT);
        g.drawCenteredString(font, "Nenhum ponto será gasto.",
                width / 2, height / 2 + 8, LuxGuiStyle.MUTED_TEXT);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Opaque screen; avoid vanilla blur.
    }

    @Override public boolean isPauseScreen() { return false; }
}
