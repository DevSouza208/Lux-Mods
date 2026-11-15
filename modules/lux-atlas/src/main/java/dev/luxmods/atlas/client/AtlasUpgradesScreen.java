package dev.luxmods.atlas.client;

import dev.luxmods.engine.LuxUpgradeNetwork;
import dev.luxmods.engine.api.LuxUpgradeState;
import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Two server-validated upgrades. No local attribute or point mutation. */
public final class AtlasUpgradesScreen extends Screen {
    private Button healthButton;
    private Button speedButton;

    public AtlasUpgradesScreen() {
        super(Component.literal("Lux Mods - Upgrades"));
    }

    @Override protected void init() {
        int center = width / 2;
        healthButton = addRenderableWidget(Button.builder(
                Component.literal("Melhorar vida"), b -> buy(0))
                .bounds(center - 150, height / 2 - 20, 140, 22).build());
        speedButton = addRenderableWidget(Button.builder(
                Component.literal("Melhorar velocidade"), b -> buy(1))
                .bounds(center + 10, height / 2 - 20, 140, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
                .bounds(center - 45, height - 36, 90, 20).build());
        updateButtons();
    }

    private void buy(int perk) {
        if (!LuxUpgradeState.received() || LuxUpgradeState.available() < 1) return;
        PacketDistributor.sendToServer(new LuxUpgradeNetwork.Purchase(perk));
        // The server must acknowledge the new balance; buttons remain disabled
        // until the next authoritative snapshot to prevent repeat requests.
        healthButton.active = false;
        speedButton.active = false;
    }

    private void updateButtons() {
        if (healthButton == null || speedButton == null) return;
        healthButton.active = LuxUpgradeState.received()
                && LuxUpgradeState.available() > 0 && !LuxUpgradeState.health();
        speedButton.active = LuxUpgradeState.received()
                && LuxUpgradeState.available() > 0 && !LuxUpgradeState.speed();
    }

    @Override public void tick() {
        updateButtons();
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xEE14111F);
        int left = Math.max(10, width / 2 - 185);
        int right = Math.min(width - 10, width / 2 + 185);
        LuxGuiStyle.panel(g, left, 12, right - left, height - 18);
        g.drawCenteredString(font, "UPGRADES", width / 2, 25, LuxGuiStyle.AMBER);
        if (!LuxUpgradeState.received()) {
            g.drawCenteredString(font, "Aguardando dados do servidor...",
                    width / 2, 65, LuxGuiStyle.MUTED_TEXT);
        } else {
            g.drawCenteredString(font, "Maior nível: " + LuxUpgradeState.highestLevel()
                    + "    Pontos disponíveis: " + LuxUpgradeState.available(),
                    width / 2, 55, LuxGuiStyle.FOREGROUND);
            g.drawCenteredString(font, "1 ponto a cada 5 níveis (até 2 pontos)",
                    width / 2, 72, LuxGuiStyle.MUTED_TEXT);
            int y = height / 2 - 46;
            g.drawCenteredString(font, LuxUpgradeState.health()
                    ? "Vida +2 HP — adquirido" : "Vida +2 HP — custa 1 ponto",
                    width / 2 - 80, y, LuxGuiStyle.FOREGROUND);
            g.drawCenteredString(font, LuxUpgradeState.speed()
                    ? "Velocidade +0,005 — adquirido" : "Velocidade +0,005 — custa 1 ponto",
                    width / 2 + 80, y, LuxGuiStyle.FOREGROUND);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Drawn above with a full-screen background; no vanilla blur.
    }

    @Override public boolean isPauseScreen() { return false; }
}
