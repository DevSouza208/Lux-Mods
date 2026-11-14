package dev.luxmods.hud.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import dev.luxmods.hud.HudPartyCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.List;

/** No placeholders: only real server-authoritative scoreboard-team snapshots. */
public final class HudPartyClientState {
    private static ClientLevel world;
    private static List<HudPartyCodec.Member> members = List.of();

    private HudPartyClientState() { }

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
        int compactWidth = width - 18;
        for (int i = 0; i < members.size(); i++) {
            int y = startY + i * 33;
            if (y + 32 >= g.guiHeight() - 24) break;
            var member = members.get(i);
            LuxGuiStyle.panel(g, x, y, compactWidth, 29);
            var info = mc.getConnection() == null ? null :
                    mc.getConnection().getPlayerInfo(member.uuid());
            if (info != null) {
                PlayerFaceRenderer.draw(g, info.getSkin(), x + 5, y + 4, 21);
            } else {
                g.fill(x + 5, y + 4, x + 26, y + 25, 0xFF62567A);
            }
            String name = member.name();
            if (name.length() > 13) name = name.substring(0, 12) + "...";
            g.drawString(mc.font, name, x + 33, y + 5, LuxGuiStyle.FOREGROUND, true);
            String level = "Nv." + member.level();
            g.drawString(mc.font, level, x + compactWidth - 9 - mc.font.width(level),
                    y + 5, LuxGuiStyle.MUTED_TEXT, false);
            LuxGuiStyle.icon(g, "stat_health", x + 32, y + 17, 9, 9);
            LuxGuiStyle.bar(g, x + 44, y + 18, compactWidth - 54, 7,
                    member.health(), member.maxHealth(), LuxGuiStyle.HEALTH);
        }
    }
}
