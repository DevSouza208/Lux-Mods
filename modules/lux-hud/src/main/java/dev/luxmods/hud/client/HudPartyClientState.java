package dev.luxmods.hud.client;

import dev.luxmods.hud.HudBarMath;
import dev.luxmods.hud.HudPartyCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.List;

/**
 * Client-side snapshot only, cleared on world change; no fake teammate values.
 */
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
        for (int i = 0; i < members.size(); i++) {
            int y = startY + i * 38;
            if (y + 35 >= g.guiHeight() - 24) break;
            var member = members.get(i);
            g.fill(x - 2, y - 2, x + width + 2, y + 33, 0xA00B0B14);
            g.fill(x, y, x + 2, y + 31, 0xFF8061D8);

            var info = mc.getConnection() == null ? null :
                    mc.getConnection().getPlayerInfo(member.uuid());
            if (info != null) {
                PlayerFaceRenderer.draw(g, info.getSkin(), x + 8, y + 5, 22);
            } else {
                g.fill(x + 8, y + 5, x + 30, y + 27, 0xFF62567A);
            }

            g.drawString(mc.font, member.name(), x + 38, y + 4, 0xFFFFFFFF, true);
            g.drawString(mc.font, "Nv. " + member.level(), x + width - 46, y + 4, 0xFFD9D3EC, false);
            g.fill(x + 38, y + 20, x + width - 8, y + 26, 0xFF292733);
            int barWidth = width - 46;
            int filled = HudBarMath.filledWidth(member.health(), member.maxHealth(), barWidth);
            if (filled > 0) g.fill(x + 38, y + 20, x + 38 + filled, y + 26, 0xFFDB4E62);
        }
    }
}
