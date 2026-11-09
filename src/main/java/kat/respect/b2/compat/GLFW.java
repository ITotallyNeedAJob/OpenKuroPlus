package kat.respect.b2.compat;

import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Method;
import kat.respect.b2.util.ScreenUtil;
import net.minecraft.client.Minecraft;

public final class GLFW {
   public static final int GLFW_PRESS = 1;
   public static final int GLFW_RELEASE = 0;
   public static final int GLFW_REPEAT = 2;
   public static final int GLFW_MOUSE_BUTTON_1 = 0;
   public static final int GLFW_MOUSE_BUTTON_2 = 1;
   public static final int GLFW_MOUSE_BUTTON_3 = 2;
   public static final int GLFW_MOUSE_BUTTON_4 = 3;
   public static final int GLFW_MOUSE_BUTTON_5 = 4;
   public static final int GLFW_MOUSE_BUTTON_6 = 5;
   public static final int GLFW_MOUSE_BUTTON_7 = 6;
   public static final int GLFW_MOUSE_BUTTON_8 = 7;
   public static final int GLFW_MOUSE_BUTTON_LEFT = 0;
   public static final int GLFW_MOUSE_BUTTON_RIGHT = 1;
   public static final int GLFW_MOUSE_BUTTON_MIDDLE = 2;
   public static final int GLFW_KEY_UNKNOWN = -1;
   public static final int GLFW_KEY_SPACE = 32;
   public static final int GLFW_KEY_APOSTROPHE = 39;
   public static final int GLFW_KEY_COMMA = 44;
   public static final int GLFW_KEY_MINUS = 45;
   public static final int GLFW_KEY_PERIOD = 46;
   public static final int GLFW_KEY_SLASH = 47;
   public static final int GLFW_KEY_0 = 48;
   public static final int GLFW_KEY_1 = 49;
   public static final int GLFW_KEY_2 = 50;
   public static final int GLFW_KEY_3 = 51;
   public static final int GLFW_KEY_4 = 52;
   public static final int GLFW_KEY_5 = 53;
   public static final int GLFW_KEY_6 = 54;
   public static final int GLFW_KEY_7 = 55;
   public static final int GLFW_KEY_8 = 56;
   public static final int GLFW_KEY_9 = 57;
   public static final int GLFW_KEY_SEMICOLON = 59;
   public static final int GLFW_KEY_EQUAL = 61;
   public static final int GLFW_KEY_A = 65;
   public static final int GLFW_KEY_B = 66;
   public static final int GLFW_KEY_C = 67;
   public static final int GLFW_KEY_D = 68;
   public static final int GLFW_KEY_E = 69;
   public static final int GLFW_KEY_F = 70;
   public static final int GLFW_KEY_G = 71;
   public static final int GLFW_KEY_H = 72;
   public static final int GLFW_KEY_I = 73;
   public static final int GLFW_KEY_J = 74;
   public static final int GLFW_KEY_K = 75;
   public static final int GLFW_KEY_L = 76;
   public static final int GLFW_KEY_M = 77;
   public static final int GLFW_KEY_N = 78;
   public static final int GLFW_KEY_O = 79;
   public static final int GLFW_KEY_P = 80;
   public static final int GLFW_KEY_Q = 81;
   public static final int GLFW_KEY_R = 82;
   public static final int GLFW_KEY_S = 83;
   public static final int GLFW_KEY_T = 84;
   public static final int GLFW_KEY_U = 85;
   public static final int GLFW_KEY_V = 86;
   public static final int GLFW_KEY_W = 87;
   public static final int GLFW_KEY_X = 88;
   public static final int GLFW_KEY_Y = 89;
   public static final int GLFW_KEY_Z = 90;
   public static final int GLFW_KEY_LEFT_BRACKET = 91;
   public static final int GLFW_KEY_BACKSLASH = 92;
   public static final int GLFW_KEY_RIGHT_BRACKET = 93;
   public static final int GLFW_KEY_GRAVE_ACCENT = 96;
   public static final int GLFW_KEY_WORLD_1 = 161;
   public static final int GLFW_KEY_WORLD_2 = 162;
   public static final int GLFW_KEY_ESCAPE = 256;
   public static final int GLFW_KEY_ENTER = 257;
   public static final int GLFW_KEY_TAB = 258;
   public static final int GLFW_KEY_BACKSPACE = 259;
   public static final int GLFW_KEY_INSERT = 260;
   public static final int GLFW_KEY_DELETE = 261;
   public static final int GLFW_KEY_RIGHT = 262;
   public static final int GLFW_KEY_LEFT = 263;
   public static final int GLFW_KEY_DOWN = 264;
   public static final int GLFW_KEY_UP = 265;
   public static final int GLFW_KEY_PAGE_UP = 266;
   public static final int GLFW_KEY_PAGE_DOWN = 267;
   public static final int GLFW_KEY_HOME = 268;
   public static final int GLFW_KEY_END = 269;
   public static final int GLFW_KEY_CAPS_LOCK = 280;
   public static final int GLFW_KEY_SCROLL_LOCK = 281;
   public static final int GLFW_KEY_NUM_LOCK = 282;
   public static final int GLFW_KEY_PRINT_SCREEN = 283;
   public static final int GLFW_KEY_PAUSE = 284;
   public static final int GLFW_KEY_F1 = 290;
   public static final int GLFW_KEY_F2 = 291;
   public static final int GLFW_KEY_F3 = 292;
   public static final int GLFW_KEY_F4 = 293;
   public static final int GLFW_KEY_F5 = 294;
   public static final int GLFW_KEY_F6 = 295;
   public static final int GLFW_KEY_F7 = 296;
   public static final int GLFW_KEY_F8 = 297;
   public static final int GLFW_KEY_F9 = 298;
   public static final int GLFW_KEY_F10 = 299;
   public static final int GLFW_KEY_F11 = 300;
   public static final int GLFW_KEY_F12 = 301;
   public static final int GLFW_KEY_F13 = 302;
   public static final int GLFW_KEY_F14 = 303;
   public static final int GLFW_KEY_F15 = 304;
   public static final int GLFW_KEY_F16 = 305;
   public static final int GLFW_KEY_F17 = 306;
   public static final int GLFW_KEY_F18 = 307;
   public static final int GLFW_KEY_F19 = 308;
   public static final int GLFW_KEY_F20 = 309;
   public static final int GLFW_KEY_F21 = 310;
   public static final int GLFW_KEY_F22 = 311;
   public static final int GLFW_KEY_F23 = 312;
   public static final int GLFW_KEY_F24 = 313;
   public static final int GLFW_KEY_F25 = 314;
   public static final int GLFW_KEY_KP_0 = 320;
   public static final int GLFW_KEY_KP_1 = 321;
   public static final int GLFW_KEY_KP_2 = 322;
   public static final int GLFW_KEY_KP_3 = 323;
   public static final int GLFW_KEY_KP_4 = 324;
   public static final int GLFW_KEY_KP_5 = 325;
   public static final int GLFW_KEY_KP_6 = 326;
   public static final int GLFW_KEY_KP_7 = 327;
   public static final int GLFW_KEY_KP_8 = 328;
   public static final int GLFW_KEY_KP_9 = 329;
   public static final int GLFW_KEY_KP_DECIMAL = 330;
   public static final int GLFW_KEY_KP_DIVIDE = 331;
   public static final int GLFW_KEY_KP_MULTIPLY = 332;
   public static final int GLFW_KEY_KP_SUBTRACT = 333;
   public static final int GLFW_KEY_KP_ADD = 334;
   public static final int GLFW_KEY_KP_ENTER = 335;
   public static final int GLFW_KEY_KP_EQUAL = 336;
   public static final int GLFW_KEY_LEFT_SHIFT = 340;
   public static final int GLFW_KEY_LEFT_CONTROL = 341;
   public static final int GLFW_KEY_LEFT_ALT = 342;
   public static final int GLFW_KEY_LEFT_SUPER = 343;
   public static final int GLFW_KEY_RIGHT_SHIFT = 344;
   public static final int GLFW_KEY_RIGHT_CONTROL = 345;
   public static final int GLFW_KEY_RIGHT_ALT = 346;
   public static final int GLFW_KEY_RIGHT_SUPER = 347;
   public static final int GLFW_KEY_MENU = 348;
   private static volatile boolean probed = false;
   private static volatile Boolean realGlfw = null;
   private static volatile Method realGetKey = null;
   private static volatile Method realGetMouse = null;
   private static volatile Method keyDown1 = null;
   private static volatile Method keyDownW = null;

   private GLFW() {
   }

   public static int toScancode(int glfwKey) {
      if (glfwKey >= 65 && glfwKey <= 90) {
         return 65 + (glfwKey - 65);
      } else if (glfwKey >= 49 && glfwKey <= 57) {
         return 49 + (glfwKey - 49);
      } else if (glfwKey >= 290 && glfwKey <= 301) {
         return 290 + (glfwKey - 290);
      } else if (glfwKey >= 302 && glfwKey <= 313) {
         return 302 + (glfwKey - 302);
      } else if (glfwKey >= 321 && glfwKey <= 329) {
         return 321 + (glfwKey - 321);
      } else {
         return switch (glfwKey) {
            case 32 -> 32;
            case 39 -> 39;
            case 44 -> 44;
            case 45 -> 45;
            case 46 -> 46;
            case 47 -> 47;
            case 48 -> 48;
            case 59 -> 59;
            case 61 -> 61;
            case 91 -> 91;
            case 92 -> 92;
            case 93 -> 93;
            case 96 -> 96;
            case 256 -> 256;
            case 257 -> 257;
            case 258 -> 258;
            case 259 -> 259;
            case 260 -> 260;
            case 261 -> 261;
            case 262 -> 262;
            case 263 -> 263;
            case 264 -> 264;
            case 265 -> 265;
            case 266 -> 266;
            case 267 -> 267;
            case 268 -> 268;
            case 269 -> 269;
            case 280 -> 280;
            case 281 -> 281;
            case 282 -> 282;
            case 283 -> 283;
            case 284 -> 284;
            case 320 -> 320;
            case 330 -> 330;
            case 332 -> 332;
            case 334 -> 334;
            case 335 -> 335;
            case 336 -> 336;
            case 340 -> 340;
            case 341 -> 341;
            case 342 -> 342;
            case 343 -> 343;
            case 344 -> 344;
            case 345 -> 345;
            case 346 -> 346;
            case 347 -> 347;
            default -> glfwKey;
         };
      }
   }

   public static int glfwGetKey(long window, int key) {
      if (key < 0) {
         return 0;
      } else {
         probe();
         if (Boolean.TRUE.equals(realGlfw) && realGetKey != null) {
            try {
               if (realGetKey.invoke(null, window, key) instanceof Integer i) {
                  return i == 1 ? 1 : 0;
               }
            } catch (Throwable var5) {
            }
         }

         int scancode = toScancode(key);
         if (keyDownFallback(window, scancode)) {
            return 1;
         } else {
            return scancode != key && keyDownFallback(window, key) ? 1 : 0;
         }
      }
   }

   public static int glfwGetMouseButton(long window, int button) {
      if (button < 0) {
         return 0;
      } else {
         try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.mouseHandler != null) {
               if (button == 0 && mc.mouseHandler.isLeftPressed()) {
                  return 1;
               }

               if (button == 1 && mc.mouseHandler.isRightPressed()) {
                  return 1;
               }

               if (button == 2 && mc.mouseHandler.isMiddlePressed()) {
                  return 1;
               }

               if (button <= 2) {
                  boolean inGame = true;

                  try {
                     inGame = !ScreenUtil.hasScreen(mc);
                  } catch (Throwable var9) {
                  }

                  if (inGame) {
                     return 0;
                  }
               }
            }

            if (mc != null && mc.options != null) {
               try {
                  if (button == 0 && mc.options.keyAttack != null && mc.options.keyAttack.isDown()) {
                     return 1;
                  }

                  if (button == 1 && mc.options.keyUse != null && mc.options.keyUse.isDown()) {
                     return 1;
                  }
               } catch (Throwable var8) {
               }
            }
         } catch (Throwable var10) {
         }

         probe();
         if (Boolean.TRUE.equals(realGlfw) && realGetMouse != null) {
            try {
               if (realGetMouse.invoke(null, window, button) instanceof Integer i) {
                  return i == 1 ? 1 : 0;
               }
            } catch (Throwable var7) {
            }
         }

         try {
            if (Class.forName("org.lwjgl.glfw.GLFW").getMethod("glfwGetMouseButton", long.class, int.class).invoke(null, window, button) instanceof Integer i) {
               return i == 1 ? 1 : 0;
            }
         } catch (Throwable var6) {
         }

         return 0;
      }
   }

   public static boolean isPhysicalDown(long window, int key) {
      if (key >= 0 && window != 0L) {
         probe();

         try {
            if (key < 8) {
               if (realGetMouse != null && realGetMouse.invoke(null, window, key) instanceof Integer i) {
                  return i == 1;
               }
            } else if (realGetKey != null && realGetKey.invoke(null, window, key) instanceof Integer i) {
               return i == 1;
            }
         } catch (Throwable var6) {
         }

         if (key >= 8) {
            int scancode = toScancode(key);
            if (keyDownFallback(window, scancode)) {
               return true;
            }

            if (scancode != key && keyDownFallback(window, key)) {
               return true;
            }
         } else {
            try {
               if (Class.forName("org.lwjgl.glfw.GLFW").getMethod("glfwGetMouseButton", long.class, int.class).invoke(null, window, key) instanceof Integer i) {
                  return i == 1;
               }
            } catch (Throwable var5) {
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static void probe() {
      if (!probed) {
         synchronized (GLFW.class) {
            if (!probed) {
               try {
                  Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");

                  try {
                     realGetKey = glfw.getMethod("glfwGetKey", long.class, int.class);
                  } catch (Throwable var7) {
                  }

                  try {
                     realGetMouse = glfw.getMethod("glfwGetMouseButton", long.class, int.class);
                  } catch (Throwable var6) {
                  }

                  realGlfw = realGetKey != null;
               } catch (Throwable var8) {
                  realGlfw = false;
               }

               try {
                  keyDown1 = InputConstants.class.getMethod("isKeyDown", int.class);
               } catch (Throwable var5) {
               }

               if (keyDown1 == null) {
                  try {
                     Object win = windowInstance();
                     if (win != null) {
                        keyDownW = InputConstants.class.getMethod("isKeyDown", win.getClass(), int.class);
                     }
                  } catch (Throwable var4) {
                  }
               }

               probed = true;
            }
         }
      }
   }

   private static Object windowInstance() {
      try {
         Minecraft mc = Minecraft.getInstance();
         return mc == null ? null : mc.getWindow();
      } catch (Throwable var1) {
         return null;
      }
   }

   private static boolean keyDownFallback(long window, int code) {
      try {
         if (keyDown1 != null && keyDown1.invoke(null, code) instanceof Boolean b) {
            return b;
         }

         if (keyDownW != null) {
            Object win = windowInstance();
            if (win != null && keyDownW.invoke(null, win, code) instanceof Boolean b) {
               return b;
            }
         }
      } catch (Throwable var6) {
      }

      return false;
   }
}
