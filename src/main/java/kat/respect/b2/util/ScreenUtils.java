package kat.respect.b2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenUtils {
   private ScreenUtils() {
   }

   public static Screen getScreen() {
      return ScreenUtil.getScreen();
   }

   public static Screen getScreen(Minecraft mc) {
      return ScreenUtil.getScreen(mc);
   }

   public static boolean hasScreen() {
      return ScreenUtil.hasScreen();
   }

   public static boolean hasScreen(Minecraft mc) {
      return ScreenUtil.hasScreen(mc);
   }

   public static void setScreen(Screen screen) {
      ScreenUtil.setScreen(screen);
   }
}
