package kat.respect.b2.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

public final class PotionCompat {
   private static final Map<String, Holder<Potion>> CACHE = new ConcurrentHashMap<>();

   private PotionCompat() {
   }

   public static Holder<Potion> holder(String name) {
      if (name == null) {
         return null;
      } else {
         Holder<Potion> cached = CACHE.get(name);
         if (cached != null) {
            return cached;
         } else {
            try {
               if (Potions.class.getField(name).get(null) instanceof Holder<?> holder) {
                  CACHE.put(name, (Holder<Potion>)holder);
                  return (Holder<Potion>)holder;
               }
            } catch (Throwable var4) {
            }

            return null;
         }
      }
   }

   public static Holder<Potion> healing() {
      return holder("HEALING");
   }

   public static Holder<Potion> strongHealing() {
      return holder("STRONG_HEALING");
   }

   public static Holder<Potion> swiftness() {
      return holder("SWIFTNESS");
   }

   public static Holder<Potion> strongSwiftness() {
      return holder("STRONG_SWIFTNESS");
   }

   public static Holder<Potion> strength() {
      return holder("STRENGTH");
   }

   public static Holder<Potion> strongStrength() {
      return holder("STRONG_STRENGTH");
   }
}
