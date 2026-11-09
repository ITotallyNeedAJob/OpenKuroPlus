package kat.respect.b2.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.lang.reflect.Field;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;

public final class ClickGuiKeybinds {
   public static final int DEFAULT_KEY = 344;
   public static KeyMapping OPEN_GUI;
   private static boolean registered = false;

   private ClickGuiKeybinds() {
   }

   private static Type getKeyType() {
      try {
         return Enum.valueOf(Type.class, "KEYSYM");
      } catch (Throwable var3) {
         try {
            return Enum.valueOf(Type.class, "KEYBOARD");
         } catch (Throwable var2) {
            return Type.values()[0];
         }
      }
   }

   public static synchronized void register() {
      if (!registered) {
         registered = true;

         try {
            Category category = KeybindCategories.kuroplus();
            KeyMapping mapping = new KeyMapping("key.kuroplus.clickgui", getKeyType(), 344, category);

            try {
               OPEN_GUI = KeyMappingHelper.registerKeyMapping(mapping);
            } catch (Throwable var3) {
               OPEN_GUI = mapping;
               System.out.println("[Kuro+] keybind registered directly (late attach): " + var3.getMessage());
            }
         } catch (Throwable var4) {
            System.err.println("[Kuro+] failed to register ClickGUI key mapping: " + var4.getMessage());
         }
      }
   }

   public static int getBoundKey() {
      if (OPEN_GUI == null) {
         return 344;
      } else {
         try {
            Key key = KeyMappingHelper.getBoundKeyOf(OPEN_GUI);
            if (key != null && key.getValue() != InputConstants.UNKNOWN.getValue()) {
               return key.getValue();
            }
         } catch (Throwable var3) {
         }

         try {
            Field keyField = KeyMapping.class.getDeclaredField("key");
            keyField.setAccessible(true);
            Key key = (Key)keyField.get(OPEN_GUI);
            if (key != null && key.getValue() != InputConstants.UNKNOWN.getValue()) {
               return key.getValue();
            }
         } catch (Throwable var2) {
         }

         return 344;
      }
   }

   public static void setBoundKey(int code) {
      if (OPEN_GUI != null) {
         if (code != -1 && code >= 0) {
            OPEN_GUI.setKey(getKeyType().getOrCreate(code));
         } else {
            OPEN_GUI.setKey(InputConstants.UNKNOWN);
         }
      }
   }

   private static Module clickGuiModule() {
      try {
         return ModuleManager.INSTANCE.getModuleByName("ClickGUI");
      } catch (Throwable var1) {
         return null;
      }
   }

   public static void syncFromModule() {
      Module module = clickGuiModule();
      if (module != null && OPEN_GUI != null) {
         int bind = module.getKey();
         if (bind != -1 && bind >= 0) {
            setBoundKey(bind);
         } else {
            setBoundKey(344);
         }
      }
   }

   public static void syncToModule() {
      Module module = clickGuiModule();
      if (module != null) {
         int bound = getBoundKey();
         if (module.getKey() != bound) {
            module.setKey(bound);
         }
      }
   }
}
