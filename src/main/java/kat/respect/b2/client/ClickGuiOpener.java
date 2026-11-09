package kat.respect.b2.client;

import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.keybind.KeybindManager;
import kat.respect.b2.util.ScreenUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class ClickGuiOpener {
   private static boolean registered = false;

   private ClickGuiOpener() {
   }

   public static synchronized void register() {
      if (!registered) {
         registered = true;
         ClientTickEvents.START_CLIENT_TICK.register(ClickGuiOpener::onTick);
      }
   }

   private static void onTick(Minecraft mc) {
      if (ClickGuiKeybinds.OPEN_GUI != null && mc != null) {
         boolean pressed = false;

         while (ClickGuiKeybinds.OPEN_GUI.consumeClick()) {
            pressed = true;
         }

         if (pressed) {
            if (!(ScreenUtil.getScreen(mc) instanceof ClickGuiScreen)) {
               if (!KeybindManager.INSTANCE.isCapturing()) {
                  ClickGuiKeybinds.syncToModule();
                  ClickGuiScreen gui = ClickGuiScreen.getInstance();
                  gui.resetOpenProgress();
                  ScreenUtil.setScreen(mc, gui);
               }
            }
         }
      }
   }
}
