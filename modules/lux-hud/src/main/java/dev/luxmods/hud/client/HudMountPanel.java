package dev.luxmods.hud.client;

import dev.luxmods.engine.client.LuxGuiStyle;
import dev.luxmods.hud.HudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/** Compact horse status card displayed directly below the player's panel. */
public final class HudMountPanel {
    public static final int HEIGHT = 48;
    private HudMountPanel() {}

    public static boolean hasMount(Minecraft mc) {
        return mc.player != null && mc.player.getVehicle() instanceof AbstractHorse;
    }

    public static void render(GuiGraphics g, Minecraft mc, int x, int y, int width) {
        if (!HudConfig.SHOW_MOUNT.get() || !hasMount(mc)) return;
        AbstractHorse horse = (AbstractHorse) mc.player.getVehicle();
        LuxGuiStyle.panel(g,x,y,width,HEIGHT);
        int px=x+8, py=y+6;
        g.fill(px,py,px+35,py+35,0xFF282234);
        g.enableScissor(px,py,px+35,py+35);
        try {
            // Render the actual horse entity so variants and modded appearances are preserved.
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                g,px,py,px+35,py+35,18,0f,0f,0f,horse);
        } finally {
            g.disableScissor();
        }
        LuxGuiStyle.icon(g,"portrait_frame",px-1,py-1,37,37);
        String name=horse.getDisplayName().getString();
        int nameWidth=width-65;
        while (name.length()>1 && mc.font.width(name)>nameWidth) {
            name=name.substring(0,name.length()-1);
        }
        g.drawString(mc.font,name,x+52,y+8,LuxGuiStyle.FOREGROUND,true);
        float health=Math.max(0,horse.getHealth()), max=Math.max(1,horse.getMaxHealth());
        LuxGuiStyle.bar(g,x+52,y+25,width-65,11,health,max,LuxGuiStyle.HEALTH);
        String value=Math.round(health)+"/"+Math.round(max);
        g.drawString(mc.font,value,x+58,y+26,0xFFFFFFFF,true);
    }
}
