package dev.luxmods.dialogues.client;

import dev.luxmods.dialogues.DialogueLines;
import dev.luxmods.dialogues.DialogueNpcTypes;
import dev.luxmods.dialogues.network.DialogueNetwork;
import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** Bottom-screen PT-BR RPG conversation; keeps the standard villager shop. */
public final class VillagerDialogueScreen extends Screen {
    private final LivingEntity npc;
    private final boolean guard;
    private final long day;
    private String message;
    private boolean replied;
    private boolean contractMenu;
    private DialogueNetwork.ContractSnapshot contractSnapshot;
    private boolean closed;

    public VillagerDialogueScreen(LivingEntity npc) {
        super(Component.literal("Lux Dialogues"));
        this.npc = npc;
        this.guard = DialogueNpcTypes.isGuard(npc);
        this.day = npc.level().getDayTime() / 24000L;
        this.message = DialogueLines.greeting(npc.getUUID(), day, guard);
    }

    /** Only applies server snapshots belonging to the NPC shown on this screen. */
    public void receiveContractSnapshot(DialogueNetwork.ContractSnapshot snapshot) {
        if (closed || !(npc instanceof Villager) || snapshot.entityId() != npc.getId()) return;
        contractSnapshot = snapshot;
        if (contractMenu) {
            message = ContractUiState.description(snapshot.objective(), snapshot.quantity(),
                    snapshot.expiresDay(), snapshot.status(), snapshot.mine());
            rebuildWidgets();
        } else if (replied) {
            message = "Contrato: " + ContractUiState.description(snapshot.objective(),
                    snapshot.quantity(), snapshot.expiresDay(), snapshot.status(), snapshot.mine());
        }
    }

    /** Delivery result is deliberately independent of the next NPC offer. */
    public void receiveDeliveryResult(DialogueNetwork.ContractDeliveryResult result) {
        if (closed || !contractMenu || result.entityId() != npc.getId()) return;
        if (result.success()) {
            contractSnapshot = null;
            message = "Entrega concluída! A recompensa foi enviada ao inventário. "
                    + "Confira o chat para ver a quantidade recebida.";
        } else {
            message = "Não foi possível concluir a entrega. Confira os materiais "
                    + "e a mensagem do servidor no chat.";
        }
        rebuildWidgets();
    }

    private boolean hasShop() {
        if (!(npc instanceof Villager villager)) return false;
        var profession = villager.getVillagerData().getProfession();
        return !villager.isBaby() && profession != VillagerProfession.NONE
                && profession != VillagerProfession.NITWIT;
    }

    @Override protected void init() {
        clearWidgets();
        int gap = 7;
        int availableActions = contractSnapshot == null ? 0 :
                (ContractUiState.canAccept(contractSnapshot.status()) ? 1 : 0)
                + (ContractUiState.canDeliver(contractSnapshot.status(), contractSnapshot.mine()) ? 1 : 0);
        int count = contractMenu ? 2 + availableActions : (replied ? 2 : (hasShop() ? 5 : (guard ? 3 : 4)));
        int buttonWidth = Math.min(116, Math.max(48, (width - 20 - (count - 1) * gap) / count));
        int total = count * buttonWidth + (count - 1) * gap;
        int x = Math.max(8, (width - total) / 2);
        int y = height - 42;
        int i = 0;
        if (contractMenu) {
            if (contractSnapshot != null && ContractUiState.canAccept(contractSnapshot.status())) {
                addOption(x + (buttonWidth + gap) * i++, y, buttonWidth, "Aceitar", () -> {
                    // Refresh before accepting again; server independently checks all state.
                    contractSnapshot = null;
                    PacketDistributor.sendToServer(new DialogueNetwork.AcceptContract(npc.getId()));
                    message = "Registrando pedido...";
                    rebuildWidgets();
                });
            }
            if (contractSnapshot != null
                    && ContractUiState.canDeliver(contractSnapshot.status(), contractSnapshot.mine())) {
                addOption(x + (buttonWidth + gap) * i++, y, buttonWidth, "Entregar", () -> {
                    contractSnapshot = null;
                    PacketDistributor.sendToServer(new DialogueNetwork.DeliverContract(npc.getId()));
                    message = "Verificando materiais...";
                    rebuildWidgets();
                });
            }
            addOption(x + (buttonWidth + gap) * i++, y, buttonWidth, "Voltar", () -> {
                contractMenu = false;
                replied = false;
                message = DialogueLines.greeting(npc.getUUID(), day, guard);
                rebuildWidgets();
            });
            addOption(x + (buttonWidth + gap) * i, y, buttonWidth, "Sair", this::onClose);
            return;
        }
        if (!replied) {
            addOption(x + (buttonWidth + gap) * i++, y, buttonWidth, "Como vai?",
                    () -> respond(DialogueLines.howAreYou(npc.getUUID(), day, guard)));
            addOption(x + (buttonWidth + gap) * i++, y, buttonWidth, "Qual é a boa?",
                    () -> respond(DialogueLines.rumor(npc.getUUID(), day, guard)));
            if (!guard && npc instanceof Villager) {
                addOption(x + (buttonWidth + gap) * i++, y, buttonWidth, "Contrato", () -> {
                    contractMenu = true;
                    contractSnapshot = null;
                    message = "Consultando a oferta deste aldeão...";
                    rebuildWidgets();
                    PacketDistributor.sendToServer(new DialogueNetwork.RequestContract(npc.getId()));
                });
            }
            if (hasShop()) {
                addOption(x + (buttonWidth + gap) * i++, y, buttonWidth, "Abrir loja", () -> {
                    closed = true;
                    PacketDistributor.sendToServer(new DialogueNetwork.OpenShop(npc.getId()));
                    Minecraft.getInstance().setScreen(null);
                });
            }
        } else {
            addOption(x + (buttonWidth + gap) * i++, y, buttonWidth, "Voltar", () -> {
                replied = false;
                message = DialogueLines.greeting(npc.getUUID(), day, guard);
                rebuildWidgets();
            });
        }
        addOption(x + (buttonWidth + gap) * i, y, buttonWidth, "Sair", this::onClose);
    }

    private void addOption(int x, int y, int w, String label, Runnable run) {
        addRenderableWidget(new DialogueButton(x, y, w, 22, Component.literal(label), b -> run.run()));
    }

    private void respond(String answer) {
        replied = true;
        message = answer;
        rebuildWidgets();
    }

    @Override public void tick() {
        if (npc.isRemoved() || Minecraft.getInstance().player == null
                || Minecraft.getInstance().player.distanceToSqr(npc) > 64D
                || DialogueNpcTypes.guardInCombat(npc)) {
            onClose();
        }
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int left = Math.max(8, width / 2 - 235);
        int right = Math.min(width - 8, width / 2 + 235);
        int top = height - 133;
        LuxGuiStyle.panel(g, left, top, right - left, 118);
        g.fill(left + 12, top + 26, right - 12, top + 27, 0x887E619B);
        g.drawString(font, guard ? "Guarda da vila" : npc.getDisplayName().getString(),
                left + 13, top + 12, 0xFFEADAF8, true);
        List<FormattedCharSequence> lines = font.split(Component.literal(message), right - left - 29);
        for (int i = 0; i < Math.min(4, lines.size()); i++) {
            g.drawString(font, lines.get(i), left + 14, top + 36 + i * 11, 0xFFFFFFFF, false);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Keep the NPC and the world visible behind the dialogue.
    }

    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() {
        if (closed) return;
        closed = true;
        PacketDistributor.sendToServer(new DialogueNetwork.EndDialogue(npc.getId()));
        Minecraft.getInstance().setScreen(null);
    }
}
