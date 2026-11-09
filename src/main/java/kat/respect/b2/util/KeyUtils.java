package kat.respect.b2.util;

import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;

@Deprecated
public final class KeyUtils {
   private KeyUtils() {
   }

   public static Key keyOf(KeyMapping keyMapping) {
      return KeyUtil.keyOf(keyMapping);
   }

   public static void clearClickCount(KeyMapping keyMapping) {
      KeyUtil.clearClick(keyMapping);
   }

   public static void simulateClick(KeyMapping keyMapping) {
      KeyUtil.click(keyMapping);
   }

   public static boolean isKeyPressed(int key) {
      return KeyUtil.isDown(key);
   }

   public static String getKeyName(int key) {
      return KeyUtil.name(key);
   }

   public static int getKeyCode(String keyName) {
      return KeyUtil.code(keyName);
   }
}
