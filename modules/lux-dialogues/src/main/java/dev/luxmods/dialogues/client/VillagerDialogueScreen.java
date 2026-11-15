package dev.luxmods.dialogues.client;

import dev.luxmods.dialogues.DialogueLines;
import dev.luxmods.dialogues.DialogueNpcTypes;
import dev.luxmods.dialogues.network.DialogueNetwork;
import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
    private final boolean companion;
    private DialogueNetwork.CompanionState companionState;
    private boolean companionRequested;
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
        this.companion = DialogueNpcTypes.isCompanion(npc);
        this.day = npc.level().getDayTime() / 24000L;
        this.message = companion ? "Saudações, viajante. Vamos conversar."
                : DialogueLines.greeting(npc.getUUID(), day, guard);
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
        if (result.success()) contractSnapshot = null;
        message = ContractUiState.deliveryMessage(result.success());
        rebuildWidgets();
    }

    /** The server owns all recruitment requirements and companion orders. */
    public void receiveCompanionState(DialogueNetwork.CompanionState state) {
        if (closed || !companion || state.entityId() != npc.getId()) return;
        companionState = state;
        if (!state.recruited()) {
            StringBuilder text = new StringBuilder("Posso acompanhar você, mas preciso de suprimentos: ");
            if (state.materials().isEmpty()) {
                text.append("ainda não consegui consultar minhas necessidades.");
            } else {
                int i = 0;
                for (var material : state.materials()) {
                    if (i++ > 0) text.append("; ");
                    text.append(i).append(") ").append(material.remaining())
                            .append(" ").append(itemLabel(material.item()));
                }
                text.append(". Traga-os e poderemos viajar juntos.");
            }
            message = text.toString();
        } else if (state.owner()) {
            message = "Agora faço parte da sua equipe. Como posso ajudar?";
        } else {
            message = "Já tenho um companheiro de jornada. Boa sorte na estrada!";
        }
        rebuildWidgets();
    }

    private static String itemLabel(String identifier) {
        ResourceLocation id = ResourceLocation.tryParse(identifier);
        if (id == null) return identifier;
        return BuiltInRegistries.ITEM.get(id).getDescription().getString();
    }

    private void companionAction(int action, int materialIndex) {
        PacketDistributor.sendToServer(
                new DialogueNetwork.CompanionAction(npc.getId(), action, materialIndex));
        message = "Confirmando com o companheiro...";
        rebuildWidgets();
    }

    private void companionOptions(int x, int y, int width, int gap) {
        if (!companionRequested) {
            companionRequested = true;
            PacketDistributor.sendToServer(new DialogueNetwork.CompanionRequest(npc.getId()));
        }
        if (companionState == null) {
            addOption(x, y, width, "Sair", this::onClose);
            return;
        }
        int i = 0;
        if (!companionState.recruited()) {
            for (int index = 0; index < Math.min(3, companionState.materials().size()); index++) {
                var material = companionState.materials().get(index);
                if (material.remaining() <= 0) continue;
                final int requiredIndex = index;
                addOption(x + (width + gap) * i++, y, width,
                        "Entregar item " + (index + 1),
                        () -> companionAction(0, requiredIndex));
            }
            addOption(x + (width + gap) * i++, y, width, "Atualizar",
                    () -> PacketDistributor.sendToServer(
                            new DialogueNetwork.CompanionRequest(npc.getId())));
        } else if (companionState.owner()) {
            addOption(x + (width + gap) * i++, y, width, "Acompanhar",
                    () -> companionAction(1, 0));
            addOption(x + (width + gap) * i++, y, width, "Guardar",
                    () -> companionAction(2, 0));
            addOption(x + (width + gap) * i++, y, width, "Patrulhar",
                    () -> companionAction(3, 0));
            addOption(x + (width + gap) * i++, y, width, "Equipamentos",
                    () -> {
                        closed = true;
                        PacketDistributor.sendToServer(
                                new DialogueNetwork.CompanionAction(npc.getId(), 4, 0));
                        Minecraft.getInstance().setScreen(null);
                    });
        }
        addOption(x + (width + gap) * i, y, width, "Sair", this::onClose);
    }

    private boolean canRequestContract() {
        return npc instanceof Villager villager && !villager.isBaby();
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
        if (companion) {
            int actions = 1;
            if (companionState != null) {
                actions += !companionState.recruited() ? 1
                        + (int) companionState.materials().stream()
                                .limit(3).filter(r -> r.remaining() > 0).count()
                        : companionState.owner() ? 4 : 0;
            }
            int count = Math.min(5, actions);
            // Keep ALL buttons inside the same 470px panel as the dialogue,
            // rather than spreading them across the entire screen width.
            int panelLeft = Math.max(8, width / 2 - 235);
            int panelRight = Math.min(width - 8, width / 2 + 235);
            int available = Math.max(1, panelRight - panelLeft - 28);
            int buttonWidth = Math.min(110,
                    Math.max(1, (available - (count - 1) * gap) / count));
            int totalWidth = count * buttonWidth + (count - 1) * gap;
            int x = panelLeft + Math.max(14, (panelRight - panelLeft - totalWidth) / 2);
            companionOptions(x, height - 42, buttonWidth, gap);
            return;
        }
        int availableActions = contractSnapshot == null ? 0 :
                (ContractUiState.canAccept(contractSnapshot.status()) ? 1 : 0)
                + (ContractUiState.canDeliver(contractSnapshot.status(), contractSnapshot.mine()) ? 1 : 0);
        int count = contractMenu ? 2 + availableActions
                : (replied ? 2 : 3 + (canRequestContract() ? 1 : 0) + (hasShop() ? 1 : 0));
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
            if (canRequestContract()) {
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
        g.drawString(font, npc.getDisplayName().getString(),
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
