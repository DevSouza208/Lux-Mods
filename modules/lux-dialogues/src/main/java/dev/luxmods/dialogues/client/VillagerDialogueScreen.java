package dev.luxmods.dialogues.client;

import dev.luxmods.dialogues.network.DialogueNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import dev.luxmods.engine.client.LuxGuiStyle;
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
        int buttonY = height - 46;
        int buttonWidth = 104;
        int count = hasShop() && !replied ? 3 : (replied ? 1 : 2);
        int start = width / 2 - (count * buttonWidth + (count - 1) * 9) / 2;
        int index = 0;
        if (!replied) {
            addRenderableWidget(new DialogueButton(start + (buttonWidth + 9) * index++, buttonY,
                    buttonWidth, 22, Component.literal("Como vai?"), b -> {
                replied = true;
                closeTicks = 65;
                rebuildWidgets();
            }));
            if (hasShop()) {
                addRenderableWidget(new DialogueButton(start + (buttonWidth + 9) * index++,
                        buttonY, buttonWidth, 22, Component.literal("Abrir loja"), b -> {
                    PacketDistributor.sendToServer(new DialogueNetwork.OpenShop(villager.getId()));
                    // The server takes over the screen with the normal trade inventory.
                    Minecraft.getInstance().setScreen(null);
                }));
            }
        }
        addRenderableWidget(new DialogueButton(start + (buttonWidth + 9) * index,
                buttonY, buttonWidth, 22,
                Component.literal(replied ? "Continuar" : "Sair"), b -> onClose()));
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
        int left = Math.max(8, width / 2 - 220);
        int right = Math.min(width - 8, width / 2 + 220);
        int top = height - 116;
        LuxGuiStyle.panel(g, left, top, right - left, 101);
        g.fill(left + 12, top + 24, right - 12, top + 25, 0x887E619B);
        String npc = villager.getDisplayName().getString();
        g.drawString(font, npc, left + 13, top + 11, 0xFFEADAF8, true);
        String message = replied ? "Tudo bem! Que bom ver voce por aqui." : "Ola, viajante!";
        g.drawString(font, message, left + 13, top + 31, 0xFFFFFFFF, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Keep the villager and the world sharp behind this conversation.
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() {
        PacketDistributor.sendToServer(new DialogueNetwork.EndDialogue(villager.getId()));
        Minecraft.getInstance().setScreen(null);
    }
}
