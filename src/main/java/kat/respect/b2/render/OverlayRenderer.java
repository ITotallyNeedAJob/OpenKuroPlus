package kat.respect.b2.render;

import java.lang.reflect.Field;
import kat.respect.b2.debug.OverlayStats;
import kat.respect.b2.event.ClientEventDispatcher;
import kat.respect.b2.event.EventBus;
import kat.respect.b2.event.events.Render2DEvent;
import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.render.mobile.MobileDraw2D;
import kat.respect.b2.util.ScreenUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class OverlayRenderer {
   private static final Field MC_SCREEN_FIELD;

   private OverlayRenderer() {
   }

   private static Screen getFallbackScreen(Minecraft mc) {
      if (mc != null && MC_SCREEN_FIELD != null) {
         try {
            return (Screen)MC_SCREEN_FIELD.get(mc);
         } catch (Throwable var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void draw(Minecraft mc, int frames, int source) {
      OverlayStats.overlayCalls++;
      OverlayStats.lastSource = source;
      if (!OverlayStats.hookLogged) {
         OverlayStats.hookLogged = true;
         System.out.println("[Kuro+] overlay hook live (src=" + OverlayStats.sourceName(source) + " frame=" + frames + ")");
      }

      OverlayStats.lastFrame = frames;

      try {
         long now = System.nanoTime();
         if (now - OverlayStats.lastSummaryNanos > 5000000000L) {
            OverlayStats.lastSummaryNanos = now;
            Screen scr = ScreenUtil.getScreen(mc);
            if (scr == null) {
               scr = getFallbackScreen(mc);
            }

            String screen = scr == null ? "<none>" : scr.getClass().getSimpleName();
            System.out
               .println(
                  "[Kuro+] overlay alive: calls="
                     + OverlayStats.overlayCalls
                     + " src="
                     + OverlayStats.sourceName(source)
                     + " guiCalls="
                     + OverlayStats.guiCalls
                     + " screen="
                     + screen
                     + (OverlayStats.guiError == null ? "" : " guiError=" + OverlayStats.guiError)
               );
         }
      } catch (Throwable var10) {
      }

      try {
         if (Renderer2D.isForceVulkan()) {
            return;
         }

         if (mc.getWindow() == null) {
            return;
         }

         OverlayStats.guiCalls++;
         float screenW = mc.getWindow().getGuiScaledWidth();
         float screenH = mc.getWindow().getGuiScaledHeight();
         Screen currentScreen = ScreenUtil.getScreen(mc);
         if (currentScreen == null) {
            currentScreen = getFallbackScreen(mc);
         }

         if (currentScreen instanceof ClickGuiScreen gui) {
            gui.renderMobile();
         }

         if (EventBus.INSTANCE.hasListeners(Render2DEvent.class)) {
            boolean isMobile = Renderer2D.isMobileActive();
            if (isMobile && !(currentScreen instanceof ClickGuiScreen)) {
               MobileDraw2D draw2D = MobileDraw2D.getInstance();
               if (draw2D.needsBlur()) {
                  try {
                     draw2D.computeBlur();
                  } catch (Throwable var9) {
                  }
               }
            }

            Renderer2D.getMobile().begin(screenW, screenH, 1.0F, 1.0F);
            ClientEventDispatcher.onRender2D(null, mc.getDeltaTracker());
            Renderer2D.getMobile().end();
         }
      } catch (Throwable var11) {
         if (!OverlayStats.errorLogged) {
            OverlayStats.errorLogged = true;
            String msg = String.valueOf(var11);
            OverlayStats.guiError = msg.length() > 300 ? msg.substring(0, 300) : msg;
            System.err.println("[Kuro+] overlay draw FAILED (recorded, overlay off for this frame): " + var11);
            var11.printStackTrace();
         }
      }
   }

   static {
      Field f = null;

      try {
         f = Minecraft.class.getField("screen");
      } catch (Throwable var4) {
         try {
            f = Minecraft.class.getDeclaredField("screen");
            f.setAccessible(true);
         } catch (Throwable var3) {
         }
      }

      MC_SCREEN_FIELD = f;
   }
}
