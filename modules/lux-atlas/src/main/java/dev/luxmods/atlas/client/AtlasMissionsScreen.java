package dev.luxmods.atlas.client;

import dev.luxmods.engine.api.LuxQuestPage;
import dev.luxmods.engine.api.LuxQuestTracker;
import dev.luxmods.engine.client.LuxGuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/** Mission list only. No world map, terrain sampling or map key conflicts. */
public final class AtlasMissionsScreen extends Screen {
    private int page;

    public AtlasMissionsScreen() {
        super(Component.literal("Lux Mods - Missões"));
    }

    private List<LuxQuestTracker.Contract> quests() {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) return List.of();
        return LuxQuestTracker.nearest(minecraft.level.dimension().location().toString(),
                minecraft.player.getBlockX(), minecraft.player.getBlockZ());
    }

    private int pageSize() {
        return Math.max(1, Math.min(8, (height - 110) / 45));
    }

    @Override protected void init() {
        int center = width / 2;
        addRenderableWidget(Button.builder(Component.literal("Anterior"), b -> {
            page = Math.max(0, page - 1);
        }).bounds(center - 150, height - 36, 88, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Próxima"), b -> {
            page = Math.min(LuxQuestPage.pages(quests().size(), pageSize()) - 1, page + 1);
        }).bounds(center + 62, height - 36, 88, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
                .bounds(center - 45, height - 36, 90, 20).build());
    }

    private static String material(String objective) {
        return switch (objective) {
            case "minecraft:coal" -> "Carvão";
            case "minecraft:bread" -> "Pão";
            case "minecraft:iron_ingot" -> "Ferro";
            default -> objective;
        };
    }

    private int inventoryCount(String objective) {
        if (minecraft == null || minecraft.player == null) return 0;
        Item required = switch (objective) {
            case "minecraft:coal" -> Items.COAL;
            case "minecraft:bread" -> Items.BREAD;
            case "minecraft:iron_ingot" -> Items.IRON_INGOT;
            default -> null;
        };
        if (required == null) return 0;
        int sum = 0;
        var inventory = minecraft.player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            var stack = inventory.getItem(slot);
            if (stack.is(required)) sum += stack.getCount();
        }
        return sum;
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xEE14111F);
        int left = Math.max(10, width / 2 - 180);
        int right = Math.min(width - 10, width / 2 + 180);
        LuxGuiStyle.panel(g, left, 12, right - left, height - 18);
        g.drawCenteredString(font, "MISSÕES", width / 2, 25, LuxGuiStyle.AMBER);

        List<LuxQuestTracker.Contract> active = quests();
        page = LuxQuestPage.clamp(page, active.size(), pageSize());
        g.drawCenteredString(font, "Página " + (page + 1) + "/"
                + LuxQuestPage.pages(active.size(), pageSize()),
                width / 2, 42, LuxGuiStyle.MUTED_TEXT);

        if (active.isEmpty()) {
            g.drawCenteredString(font, "Nenhum contrato aceito nesta dimensão.",
                    width / 2, height / 2 - 12, LuxGuiStyle.FOREGROUND);
            g.drawCenteredString(font, "Converse com um aldeão para aceitar um contrato.",
                    width / 2, height / 2 + 5, LuxGuiStyle.MUTED_TEXT);
        } else {
            int index = 0;
            for (var quest : LuxQuestPage.slice(active, page, pageSize())) {
                int y = 65 + index++ * 45;
                int count = Math.min(quest.quantity(), inventoryCount(quest.objective()));
                String progress = quest.status() == 4 ? "Entrega em revisão"
                        : count + "/" + quest.quantity();
                g.drawString(font, material(quest.objective()) + "  " + progress,
                        left + 16, y, LuxGuiStyle.FOREGROUND, false);
                g.drawString(font, "NPC: X " + quest.x() + "  Z " + quest.z(),
                        left + 16, y + 15, LuxGuiStyle.MUTED_TEXT, false);
            }
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Entire background is rendered above; do not blur the game twice.
    }

    @Override public boolean isPauseScreen() { return false; }
}
