package kat.respect.b2.util;

import java.lang.reflect.Method;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public final class SwingCompat {
   private static volatile Method swing3 = null;
   private static volatile boolean swing3Probed = false;
   private static volatile Method isSwingingMethod = null;
   private static volatile boolean isSwingingProbed = false;

   private SwingCompat() {
   }

   public static void swing(LivingEntity entity, InteractionHand hand) {
      if (entity != null && hand != null) {
         try {
            Method m = swing3;
            if (!swing3Probed) {
               swing3Probed = true;

               try {
                  Class<?> animClass = Class.forName("net.minecraft.world.item.component.SwingAnimation");
                  Object def = animClass.getField("DEFAULT").get(null);
                  swing3 = LivingEntity.class.getMethod("swing", InteractionHand.class, animClass, boolean.class);
                  swing3.invoke(entity, hand, def, false);
                  return;
               } catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException var6) {
                  swing3 = null;
               }
            } else if (m != null) {
               m.invoke(entity, hand, m.getParameterTypes()[1].getField("DEFAULT").get(null), false);
               return;
            }
         } catch (Throwable var7) {
         }

         try {
            entity.swing(hand);
         } catch (Throwable var5) {
         }
      }
   }

   public static boolean isSwinging(LivingEntity entity) {
      if (entity == null) {
         return false;
      } else {
         try {
            return entity.swinging;
         } catch (Throwable var5) {
            try {
               Method m = isSwingingMethod;
               if (!isSwingingProbed) {
                  isSwingingProbed = true;
                  m = LivingEntity.class.getMethod("isSwinging");
                  isSwingingMethod = m;
               }

               if (m != null && m.invoke(entity) instanceof Boolean swinging) {
                  return swinging;
               }
            } catch (Throwable var4) {
            }

            return false;
         }
      }
   }
}
