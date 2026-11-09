package kat.respect.b2.util;

import net.minecraft.ChatFormatting;

public final class TextUtil {
   private TextUtil() {
   }

   public static String sanitize(String text) {
      if (text != null && !text.isEmpty()) {
         String cleaned = ChatFormatting.stripFormatting(text);
         StringBuilder builder = new StringBuilder(cleaned.length());

         for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c >= ' ' && c != 127) {
               builder.append(c);
            }
         }

         return builder.toString().trim();
      } else {
         return "";
      }
   }
}
