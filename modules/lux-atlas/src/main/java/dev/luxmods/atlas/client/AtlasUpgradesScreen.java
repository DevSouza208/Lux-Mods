package dev.luxmods.atlas.client;

import dev.luxmods.engine.LuxUpgradeNetwork;
import dev.luxmods.engine.api.LuxUpgradeState;
import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Four modest, tiered upgrades; costs and purchases are validated server-side. */
public final class AtlasUpgradesScreen extends Screen {
    private static final String[] NAMES = {"Vitalidade", "Força", "Agilidade", "Proteção"};
    private static final String[] BONUSES = {
            "+2 HP por nível", "+0,5 dano por nível",
            "+0,004 velocidade por nível", "+1 armadura por nível"
    };
    private final Button[] buttons = new Button[4];
    private Button promoteButton;
    private int pendingRevision = -1;

    public AtlasUpgradesScreen() {
        super(Component.literal("Lux Mods - Upgrades"));
    }

    @Override protected void init() {
        int center = width / 2;
        for (int perk = 0; perk < 4; perk++) {
            final int selected = perk;
            int x = center + (perk % 2 == 0 ? -165 : 5);
            int y = height / 2 - 20 + (perk / 2) * 58;
            buttons[perk] = addRenderableWidget(Button.builder(
                    Component.literal("Comprar " + NAMES[perk]), b -> buy(selected))
                    .bounds(x, y, 160, 20).build());
        }
        promoteButton = addRenderableWidget(Button.builder(
                Component.literal("Subir nível do personagem"), b -> buy(4))
                .bounds(center - 114, height - 59, 228, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
                .bounds(center - 45, height - 32, 90, 20).build());
        updateButtons();
    }

    private void buy(int perk) {
        if ((perk == 4 ? !LuxUpgradeState.canPromote() : !LuxUpgradeState.canBuy(perk))
                || pendingRevision >= 0) return;
        pendingRevision = LuxUpgradeState.revision();
        PacketDistributor.sendToServer(new LuxUpgradeNetwork.Purchase(perk));
        updateButtons();
    }

    private void updateButtons() {
        // A server response increments revision, including a rejected purchase.
        if (pendingRevision >= 0 && pendingRevision != LuxUpgradeState.revision())
            pendingRevision = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null)
                buttons[i].active = pendingRevision < 0 && LuxUpgradeState.canBuy(i);
        }
        if (promoteButton != null)
            promoteButton.active = pendingRevision < 0 && LuxUpgradeState.canPromote();
    }

    @Override public void tick() { updateButtons(); }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xEE14111F);
        int left = Math.max(6, width / 2 - 183);
        int right = Math.min(width - 6, width / 2 + 183);
        LuxGuiStyle.panel(g, left, 8, right - left, height - 12);
        g.drawCenteredString(font, "UPGRADES", width / 2, 18, LuxGuiStyle.AMBER);
        String level = LuxUpgradeState.received()
                ? "Personagem " + LuxUpgradeState.characterLevel() + "/20  |  XP Minecraft: " + LuxUpgradeState.xpLevel()
                : "Sincronizando XP com servidor...";
        g.drawCenteredString(font, level, width / 2, 36, LuxGuiStyle.FOREGROUND);
        g.drawCenteredString(font, "Complete 4 melhorias para liberar a promoção.",
                width / 2, 49, LuxGuiStyle.MUTED_TEXT);
        for (int i = 0; i < 4; i++) {
            int x = width / 2 + (i % 2 == 0 ? -164 : 6);
            int y = height / 2 - 47 + (i / 2) * 58;
            String cost = LuxUpgradeState.rank(i) >= 20 ? "Máximo"
                    : LuxUpgradeState.price(i) < 0 ? "Próxima etapa"
                    : "Custo " + LuxUpgradeState.price(i) + " XP";
            g.drawString(font, NAMES[i] + " " + LuxUpgradeState.rank(i) + "/20  ·  " + cost,
                    x, y, LuxGuiStyle.AMBER, false);
            g.drawString(font, BONUSES[i], x, y + 12, LuxGuiStyle.FOREGROUND, false);
        }
        if (LuxUpgradeState.received()) {
            int cost = LuxUpgradeState.promotionCost();
            String status = LuxUpgradeState.characterLevel() >= 20
                    ? "Nível máximo de personagem"
                    : cost < 0 ? "Complete as 4 melhorias desta etapa"
                    : "Promoção custa " + cost + " níveis de XP";
            g.drawCenteredString(font, status, width / 2, height - 71,
                    LuxGuiStyle.AMBER);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // The menu already paints its own backdrop.
    }

    @Override public boolean isPauseScreen() { return false; }
}
