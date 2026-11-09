package kat.respect.b2.gui;

import net.minecraft.client.Minecraft;

public final class GuiClipboard {
   private GuiClipboard() {
   }

   public static String get() {
      String value = Minecraft.getInstance().keyboardHandler.getClipboard();
      return value != null ? value : "";
   }

   public static String getSanitized(int maxLength) {
      String value = get();
      StringBuilder builder = new StringBuilder(value.length());

      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         if (c == '\r' || c == '\n' || c == '\t') {
            builder.append(' ');
         } else if (c >= ' ' && c != 127) {
            builder.append(c);
         }
      }

      return maxLength > 0 && builder.length() > maxLength ? builder.substring(0, maxLength) : builder.toString();
   }

   public static void set(String value) {
      Minecraft.getInstance().keyboardHandler.setClipboard(value != null ? value : "");
   }
}
