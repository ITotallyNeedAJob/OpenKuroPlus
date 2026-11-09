package kat.respect.b2.util;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenUtil {
   private static final Field MC_SCREEN_FIELD;

   private ScreenUtil() {
   }

   public static Screen getScreen() {
      return getScreen(Minecraft.getInstance());
   }

   public static Screen getScreen(Minecraft mc) {
      if (mc == null) {
         return null;
      } else {
         if (MC_SCREEN_FIELD != null) {
            try {
               Screen s = (Screen)MC_SCREEN_FIELD.get(mc);
               if (s != null) {
                  return s;
               }
            } catch (Throwable var4) {
            }
         }

         try {
            if (mc.gui != null && mc.gui.getClass().getMethod("screen").invoke(mc.gui) instanceof Screen s && s != null) {
               return s;
            }
         } catch (Throwable var3) {
         }

         return null;
      }
   }

   public static boolean hasScreen() {
      return getScreen() != null;
   }

   public static boolean hasScreen(Minecraft mc) {
      return getScreen(mc) != null;
   }

   public static void setScreen(Screen screen) {
      setScreen(Minecraft.getInstance(), screen);
   }

   public static void setScreen(Minecraft mc, Screen screen) {
      if (mc != null) {
         mc.setScreenAndShow(screen);
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
