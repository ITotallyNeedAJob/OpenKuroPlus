package kat.respect.b2.util;

import com.mojang.blaze3d.platform.InputConstants.Key;
import java.lang.reflect.Field;
import kat.respect.b2.compat.GLFW;
import kat.respect.b2.mixin.KeyMappingAccessor;
import kat.respect.b2.module.setting.KeySetting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class KeyUtil {
   private static final Minecraft mc = Minecraft.getInstance();

   private KeyUtil() {
   }

   public static Key keyOf(KeyMapping mapping) {
      if (mapping == null) {
         return null;
      } else {
         try {
            return ((KeyMappingAccessor)mapping).getKey();
         } catch (Throwable var4) {
            try {
               Field f = KeyMapping.class.getDeclaredField("key");
               f.setAccessible(true);
               return (Key)f.get(mapping);
            } catch (Exception var3) {
               return null;
            }
         }
      }
   }

   public static void clearClick(KeyMapping mapping) {
      if (mapping != null) {
         try {
            ((KeyMappingAccessor)mapping).setClickCount(0);
         } catch (Throwable var4) {
            try {
               Field f = KeyMapping.class.getDeclaredField("clickCount");
               f.setAccessible(true);
               f.setInt(mapping, 0);
            } catch (Exception var3) {
            }
         }
      }
   }

   public static void click(KeyMapping mapping) {
      if (mapping != null) {
         try {
            KeyMappingAccessor accessor = (KeyMappingAccessor)mapping;
            accessor.setClickCount(accessor.getClickCount() + 1);
            mapping.setDown(true);
            mapping.setDown(false);
         } catch (Throwable var4) {
            try {
               Field f = KeyMapping.class.getDeclaredField("clickCount");
               f.setAccessible(true);
               f.setInt(mapping, f.getInt(mapping) + 1);
               mapping.setDown(true);
               mapping.setDown(false);
            } catch (Exception var3) {
            }
         }
      }
   }

   public static void queueClick(KeyMapping mapping) {
      if (mapping != null) {
         try {
            KeyMappingAccessor accessor = (KeyMappingAccessor)mapping;
            accessor.setClickCount(accessor.getClickCount() + 1);
         } catch (Throwable var4) {
            try {
               Field f = KeyMapping.class.getDeclaredField("clickCount");
               f.setAccessible(true);
               f.setInt(mapping, f.getInt(mapping) + 1);
            } catch (Exception var3) {
            }
         }
      }
   }

   public static boolean isDown(int key) {
      if (key == -1 || key == -1) {
         return false;
      } else if (mc.getWindow() == null) {
         return false;
      } else {
         long handle = mc.getWindow().handle();
         if (handle == 0L) {
            return false;
         } else if (key >= 1000 && key < 1008) {
            return GLFW.glfwGetMouseButton(handle, key - 1000) == 1;
         } else {
            return key < 8 ? GLFW.glfwGetMouseButton(handle, key) == 1 : GLFW.glfwGetKey(handle, key) == 1;
         }
      }
   }

   public static boolean isPhysicallyDown(int key) {
      if (key == -1 || key == -1) {
         return false;
      } else if (mc.getWindow() == null) {
         return false;
      } else {
         long handle = mc.getWindow().handle();
         return handle == 0L ? false : GLFW.isPhysicalDown(handle, key);
      }
   }

   public static String name(int key) {
      return KeySetting.keyName(key);
   }

   public static int code(String keyName) {
      // Reconstructed from bytecode: vineflower hit a StackOverflowError here.
      // Maps a key name to the client's abstract code table; "KEY-<n>" parses n.
      if (keyName == null || keyName.trim().isEmpty()) {
         return -1;
      } else {
         String s = keyName.trim().toUpperCase();
         if (s.equals("NONE") || s.equals("UNKNOWN")) {
            return -1;
         } else if (s.length() == 1) {
            char c = s.charAt(0);
            if (c >= 'A' && c <= 'Z') {
               return c;
            }

            if (c >= '0' && c <= '9') {
               return c;
            }
         }

         int v = switch (s) {
            case "ALT" -> 14;
            case "BACKSPACE" -> 22;
            case "CAPS" -> 37;
            case "CAPSLOCK" -> 38;
            case "CONTROL" -> 9;
            case "CTRL" -> 8;
            case "DEL" -> 23;
            case "DELETE" -> 24;
            case "DOWN" -> 34;
            case "END" -> 28;
            case "ENTER" -> 18;
            case "ESC" -> 20;
            case "ESCAPE" -> 21;
            case "F1" -> 39;
            case "F10" -> 48;
            case "F11" -> 49;
            case "F12" -> 50;
            case "F2" -> 40;
            case "F3" -> 41;
            case "F4" -> 42;
            case "F5" -> 43;
            case "F6" -> 44;
            case "F7" -> 45;
            case "F8" -> 46;
            case "F9" -> 47;
            case "HOME" -> 27;
            case "INS" -> 25;
            case "INSERT" -> 26;
            case "L-ALT" -> 13;
            case "L-CTRL" -> 7;
            case "L-SHIFT" -> 2;
            case "LALT" -> 12;
            case "LCLICK" -> 51;
            case "LCTRL" -> 6;
            case "LEFT" -> 35;
            case "LSHIFT" -> 1;
            case "M4" -> 57;
            case "M5" -> 59;
            case "MCLICK" -> 55;
            case "MOUSE1" -> 52;
            case "MOUSE2" -> 54;
            case "MOUSE3" -> 56;
            case "MOUSE4" -> 58;
            case "MOUSE5" -> 60;
            case "PAGE_DOWN" -> 32;
            case "PAGE_UP" -> 30;
            case "PGDN" -> 31;
            case "PGUP" -> 29;
            case "R-ALT" -> 16;
            case "R-CTRL" -> 11;
            case "R-SHIFT" -> 5;
            case "RALT" -> 15;
            case "RCLICK" -> 53;
            case "RCTRL" -> 10;
            case "RETURN" -> 19;
            case "RIGHT" -> 36;
            case "RSHIFT" -> 4;
            case "SHIFT" -> 3;
            case "SPACE" -> 0;
            case "TAB" -> 17;
            case "UP" -> 33;
            default -> {
               if (s.startsWith("KEY-")) {
                  try {
                     yield Integer.parseInt(s.substring(4));
                  } catch (NumberFormatException var5) {
                     yield -1;
                  }
               } else {
                  yield -1;
               }
            }
         };
         return v;
      }
   }
}
