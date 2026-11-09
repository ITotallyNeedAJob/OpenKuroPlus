package kat.respect.b2.util;

import java.lang.reflect.Method;
import net.minecraft.world.effect.MobEffect;

public final class MobEffectCompat {
   private static volatile Method method = null;

   private MobEffectCompat() {
   }

   public static boolean isInstantaneous(MobEffect effect) {
      if (effect == null) {
         return false;
      } else {
         if (method == null) {
            try {
               method = MobEffect.class.getMethod("isInstantaneous");
            } catch (NoSuchMethodException var4) {
               try {
                  method = MobEffect.class.getMethod("isInstantenous");
               } catch (Throwable var3) {
               }
            } catch (Throwable var5) {
            }
         }

         if (method != null) {
            try {
               return (Boolean)method.invoke(effect);
            } catch (Throwable var6) {
            }
         }

         return false;
      }
   }
}
