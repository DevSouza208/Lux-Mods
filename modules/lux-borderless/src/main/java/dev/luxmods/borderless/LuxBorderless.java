package dev.luxmods.borderless;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import java.nio.IntBuffer;

@Mod(LuxBorderless.MOD_ID)
public final class LuxBorderless {
    public static final String MOD_ID = "lux_borderless";
    public LuxBorderless(IEventBus bus, ModContainer container) {
        if (FMLEnvironment.dist == Dist.CLIENT) NeoForge.EVENT_BUS.addListener(Client::onTick);
    }
    private static final class Client {
        private static boolean wasDown, active;
        private static int oldX, oldY, oldW, oldH;
        private static void onTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getWindow() == null) return;
            long win = mc.getWindow().getWindow();
            if (win == 0) return;
            boolean down = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_F10) == GLFW.GLFW_PRESS;
            if (mc.getWindow().isFullscreen()) active = false;
            if (down && !wasDown) {
                if (mc.getWindow().isFullscreen()) {
                    notice(mc, "Use F11 para sair do fullscreen antes de ativar o Lux Borderless.");
                } else {
                    try {
                        if (active) restore(win); else enable(win);
                        notice(mc, active ? "Lux Borderless ativado (F10)" : "Janela normal restaurada (F10)");
                    } catch (RuntimeException e) {
                        notice(mc, "Lux Borderless falhou: consulte latest.log");
                    }
                }
            }
            wasDown = down;
        }
        private static void enable(long win) {
            try (MemoryStack st = MemoryStack.stackPush()) {
                IntBuffer x=st.mallocInt(1), y=st.mallocInt(1), w=st.mallocInt(1), h=st.mallocInt(1);
                GLFW.glfwGetWindowPos(win,x,y); GLFW.glfwGetWindowSize(win,w,h);
                oldX=x.get(0); oldY=y.get(0); oldW=w.get(0); oldH=h.get(0);
            }
            long monitor = findMonitor(win);
            GLFWVidMode vm = GLFW.glfwGetVideoMode(monitor);
            if (vm == null) throw new IllegalStateException("Monitor sem modo de video");
            try (MemoryStack st = MemoryStack.stackPush()) {
                IntBuffer x=st.mallocInt(1), y=st.mallocInt(1);
                GLFW.glfwGetMonitorPos(monitor,x,y);
                GLFW.glfwSetWindowAttrib(win,GLFW.GLFW_DECORATED,GLFW.GLFW_FALSE);
                GLFW.glfwSetWindowPos(win,x.get(0),y.get(0));
                GLFW.glfwSetWindowSize(win,vm.width(),vm.height());
            }
            active=true;
        }
        private static void restore(long win) {
            GLFW.glfwSetWindowAttrib(win,GLFW.GLFW_DECORATED,GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowSize(win,Math.max(400,oldW),Math.max(300,oldH));
            GLFW.glfwSetWindowPos(win,oldX,oldY);
            active=false;
        }
        private static long findMonitor(long win) {
            PointerBuffer monitors=GLFW.glfwGetMonitors();
            long best=GLFW.glfwGetPrimaryMonitor(), bestArea=-1;
            if (monitors==null) return best;
            int wx,wy,ww,wh;
            try (MemoryStack st=MemoryStack.stackPush()) {
                IntBuffer x=st.mallocInt(1),y=st.mallocInt(1),w=st.mallocInt(1),h=st.mallocInt(1);
                GLFW.glfwGetWindowPos(win,x,y); GLFW.glfwGetWindowSize(win,w,h);
                wx=x.get(0);wy=y.get(0);ww=w.get(0);wh=h.get(0);
            }
            for (int i=0;i<monitors.limit();i++) {
                long m=monitors.get(i); GLFWVidMode vm=GLFW.glfwGetVideoMode(m);
                if(vm==null)continue;
                try (MemoryStack st=MemoryStack.stackPush()) {
                    IntBuffer x=st.mallocInt(1), y=st.mallocInt(1);
                    GLFW.glfwGetMonitorPos(m,x,y);
                    int mx=x.get(0),my=y.get(0);
                    long a=(long)Math.max(0,Math.min(wx+ww,mx+vm.width())-Math.max(wx,mx))
                           *Math.max(0,Math.min(wy+wh,my+vm.height())-Math.max(wy,my));
                    if(a>bestArea){bestArea=a;best=m;}
                }
            }
            return best;
        }
        private static void notice(Minecraft mc,String s) {
            if(mc.player!=null) mc.player.displayClientMessage(Component.literal(s),true);
        }
    }
}
