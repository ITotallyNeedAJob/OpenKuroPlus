package kat.respect.b2.client;

import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;

public final class KeybindCategories {
   private static Category category;

   private KeybindCategories() {
   }

   public static synchronized Category kuroplus() {
      if (category == null) {
         category = Category.register(Identifier.fromNamespaceAndPath("kuroplus", "kuroplus"));
      }

      return category;
   }

   public static synchronized Category respect() {
      return kuroplus();
   }

   public static synchronized Category kami() {
      return kuroplus();
   }
}
