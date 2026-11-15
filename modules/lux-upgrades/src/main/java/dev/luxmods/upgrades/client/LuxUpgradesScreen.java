package dev.luxmods.upgrades.client;

import dev.luxmods.upgrades.LuxUpgradeNetwork;
import dev.luxmods.engine.api.LuxUpgradeState;
import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

/** Compact two-column upgrade cards, opened from the pause menu. */
public final class LuxUpgradesScreen extends Screen {
    private static final String[] NAMES = {"Vitalidade", "Força", "Agilidade", "Proteção"};
    private final Button[] buttons = new Button[4];
    private final Screen parent;
    private Button promoteButton;
    private int pendingRevision = -1;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int cardWidth;
    private int cardHeight;

    public LuxUpgradesScreen(Screen parent) {
        super(Component.literal("Lux Mods - Melhorias"));
        this.parent = parent;
    }

    @Override protected void init() {
        panelWidth = Math.min(388, width - 20);
        panelLeft = (width - panelWidth) / 2;
        panelTop = Math.max(6, (height - 216) / 2);
        int gap = 10;
        cardWidth = (panelWidth - 30 - gap) / 2;
        cardHeight = 49;
        for (int perk = 0; perk < 4; perk++) {
            int i = perk;
            int x = panelLeft + 15 + (perk % 2) * (cardWidth + gap);
            int y = panelTop + 51 + (perk / 2) * 53;
            buttons[perk] = addRenderableWidget(Button.builder(
                    Component.literal("Evoluir"), b -> buy(i))
                    .bounds(x + 5, y + 30, cardWidth - 10, 16).build());
        }
        promoteButton = addRenderableWidget(Button.builder(
                Component.literal("Evoluir personagem"), b -> buy(4))
                .bounds(panelLeft + 16, panelTop + 161, panelWidth - 32, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Voltar"), b -> onClose())
                .bounds(panelLeft + (panelWidth - 100) / 2, panelTop + 186, 100, 18).build());
        updateButtons();
    }

    private void buy(int perk) {
        if (pendingRevision >= 0 || (perk == 4
                ? !LuxUpgradeState.canPromote() : !LuxUpgradeState.canBuy(perk))) return;
        pendingRevision = LuxUpgradeState.revision();
        PacketDistributor.sendToServer(new LuxUpgradeNetwork.Purchase(perk));
        updateButtons();
    }

    private void updateButtons() {
        if (pendingRevision >= 0 && pendingRevision != LuxUpgradeState.revision()) {
            pendingRevision = -1;
        }
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == null) continue;
            int cost = LuxUpgradeState.price(i);
            buttons[i].setMessage(Component.literal(cost < 0 ? "Bloqueado"
                    : "Evoluir · " + cost + " níveis"));
            buttons[i].active = pendingRevision < 0 && LuxUpgradeState.canBuy(i);
        }
        if (promoteButton != null) {
            int cost = LuxUpgradeState.promotionCost();
            promoteButton.setMessage(Component.literal(cost < 0 ? "Complete as melhorias"
                    : "Subir personagem · " + cost + " níveis"));
            promoteButton.active = pendingRevision < 0 && LuxUpgradeState.canPromote();
        }
    }

    @Override public void tick() { updateButtons(); }

    private String currentValue(int perk) {
        var player = Minecraft.getInstance().player;
        if (player == null) return "--";
        double value = switch (perk) {
            case 0 -> player.getAttributeValue(Attributes.MAX_HEALTH);
            case 1 -> player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            case 2 -> player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            default -> player.getAttributeValue(Attributes.ARMOR);
        };
        return String.format(Locale.ROOT, perk == 2 ? "%.3f" : "%.1f", value);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xCC11101C);
        LuxGuiStyle.panel(g, panelLeft, panelTop, panelWidth, 211);
        g.drawCenteredString(font, "MELHORIAS", width / 2,
                panelTop + 11, LuxGuiStyle.AMBER);
        String status = LuxUpgradeState.received()
                ? "XP: " + LuxUpgradeState.xpLevel() + "  •  Personagem " + LuxUpgradeState.characterLevel() + "/20"
                : "Sincronizando com servidor...";
        g.drawCenteredString(font, status, width / 2,
                panelTop + 27, LuxGuiStyle.FOREGROUND);
        g.drawCenteredString(font, "Melhore atributos gastando níveis de XP",
                width / 2, panelTop + 40, LuxGuiStyle.MUTED_TEXT);
        for (int perk = 0; perk < 4; perk++) {
            int x = panelLeft + 15 + (perk % 2) * (cardWidth + 10);
            int y = panelTop + 59 + (perk / 2) * 70;
            g.fill(x, y, x + cardWidth, y + cardHeight, 0xC0221A33);
            g.fill(x, y, x + cardWidth, y + 1, LuxGuiStyle.TOP_ACCENT);
            g.drawCenteredString(font, NAMES[perk], x + cardWidth / 2,
                    y + 3, LuxGuiStyle.AMBER);
            g.drawCenteredString(font, "Atual: " + currentValue(perk),
                    x + cardWidth / 2, y + 15, LuxGuiStyle.FOREGROUND);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {}
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { Minecraft.getInstance().setScreen(parent); }
}
