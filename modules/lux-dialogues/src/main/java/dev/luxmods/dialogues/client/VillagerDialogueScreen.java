package dev.luxmods.dialogues.client;

import dev.luxmods.dialogues.network.DialogueNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.network.PacketDistributor;

/** Bottom-screen RPG conversation. Never replaces the vanilla trade menu. */
public final class VillagerDialogueScreen extends Screen {
    private final Villager villager;
    private boolean replied;
    private int closeTicks = -1;
    private boolean requestedShop;

    public VillagerDialogueScreen(Villager villager) {
        super(Component.literal("Lux Dialogues"));
        this.villager = villager;
    }

    private boolean hasShop() {
        var profession = villager.getVillagerData().getProfession();
        return !villager.isBaby() && profession != VillagerProfession.NONE
            && profession != VillagerProfession.NITWIT;
    }

    @Override protected void init() {
        clearWidgets();
        int buttonY = height - 49;
        int buttonWidth = 94;
        int count = hasShop() && !replied ? 3 : (replied ? 1 : 2);
        int start = width / 2 - (count * buttonWidth + (count - 1) * 7) / 2;
        int index = 0;
        if (!replied) {
            addRenderableWidget(Button.builder(Component.literal("Como vai?"), b -> {
                replied = true;
                closeTicks = 65;
                rebuildWidgets();
            }).bounds(start + (buttonWidth + 7) * index++, buttonY, buttonWidth, 20).build());
            if (hasShop()) {
                addRenderableWidget(Button.builder(Component.literal("Abrir loja"), b -> {
                    requestedShop = true;
                    PacketDistributor.sendToServer(new DialogueNetwork.OpenShop(villager.getId()));
                    onClose();
                }).bounds(start + (buttonWidth + 7) * index++, buttonY, buttonWidth, 20).build());
            }
        }
        addRenderableWidget(Button.builder(Component.literal(replied ? "Continuar" : "Sair"), b -> onClose())
            .bounds(start + (buttonWidth + 7) * index, buttonY, buttonWidth, 20).build());
    }

    @Override public void tick() {
        if (villager.isRemoved() || Minecraft.getInstance().player == null ||
                Minecraft.getInstance().player.distanceToSqr(villager) > 64D) {
            onClose();
            return;
        }
        if (closeTicks > 0 && --closeTicks == 0) onClose();
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        int left = Math.max(8, width / 2 - 220);
        int right = Math.min(width - 8, width / 2 + 220);
        int top = height - 116;
        g.fill(left, top, right, height - 15, 0xE71A1729);
        g.fill(left, top, right, top + 2, 0xFFB17DD8);
        g.fill(left, height - 17, right, height - 15, 0xFF76518B);
        String npc = villager.getDisplayName().getString();
        g.drawString(font, npc, left + 13, top + 11, 0xFFEADAF8, true);
        String message = replied ? "Tudo bem! Que bom ver voce por aqui." : "Ola, viajante!";
        g.drawString(font, message, left + 13, top + 31, 0xFFFFFFFF, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }
}
