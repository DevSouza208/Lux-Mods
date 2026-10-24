package dev.luxmods.hud.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import dev.luxmods.hud.HudPartyCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.List;

/** Render only genuinely server-synchronized members of the player's team. */
public final class HudPartyClientState {
    private static ClientLevel world;
    private static List<HudPartyCodec.Member> members = List.of();

    private HudPartyClientState() {
    }

    public static void update(String encoded) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (world != mc.level) {
            world = mc.level;
            members = List.of();
        }
        members = HudPartyCodec.decode(encoded);
    }

    public static void render(GuiGraphics g, Minecraft mc, int x, int startY, int width) {
        if (world != mc.level) {
            world = mc.level;
            members = List.of();
        }
        int teammateWidth = width - 16;
        for (int i = 0; i < members.size(); i++) {
            int y = startY + i * 34;
            if (y + 31 >= g.guiHeight() - 24) break;
            var member = members.get(i);
            LuxGuiStyle.panel(g, x, y, teammateWidth, 30);
            var info = mc.getConnection() == null ? null :
                    mc.getConnection().getPlayerInfo(member.uuid());
            if (info != null) {
                PlayerFaceRenderer.draw(g, info.getSkin(), x + 6, y + 4, 22);
            } else {
                g.fill(x + 6, y + 4, x + 28, y + 26, 0xFF62567A);
            }
            g.drawString(mc.font, member.name(), x + 35, y + 5,
                    LuxGuiStyle.FOREGROUND, true);
            g.drawString(mc.font, "Nv." + member.level(), x + teammateWidth - 49,
                    y + 5, LuxGuiStyle.MUTED_TEXT, false);
            LuxGuiStyle.bar(g, x + 35, y + 19, teammateWidth - 45, 5,
                    member.health(), member.maxHealth(), LuxGuiStyle.HEALTH);
        }
    }
}
