package kat.respect.b2.util;

import java.lang.reflect.Field;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

public final class InputUtil {
   private static final Field MOVE_VECTOR_FIELD = findMoveVectorField();

   private InputUtil() {
   }

   public static float forwardImpulse(ClientInput input) {
      return input == null ? 0.0F : input.getMoveVector().y;
   }

   public static float leftImpulse(ClientInput input) {
      return input == null ? 0.0F : input.getMoveVector().x;
   }

   public static void stop(ClientInput input) {
      if (input != null) {
         applyInput(input, Input.EMPTY);
      }
   }

   public static void applyInput(ClientInput input, Input keyPresses) {
      if (input != null) {
         input.keyPresses = keyPresses == null ? Input.EMPTY : keyPresses;
         setMoveVector(input, movementVector(input.keyPresses));
      }
   }

   public static Vec2 movementVector(Input keyPresses) {
      if (keyPresses == null) {
         return Vec2.ZERO;
      } else {
         float forward = calculateImpulse(keyPresses.forward(), keyPresses.backward());
         float left = calculateImpulse(keyPresses.left(), keyPresses.right());
         return new Vec2(left, forward).normalized();
      }
   }

   private static float calculateImpulse(boolean positive, boolean negative) {
      return positive == negative ? 0.0F : (positive ? 1.0F : -1.0F);
   }

   public static void setMoveVector(ClientInput input, Vec2 moveVector) {
      if (input != null && moveVector != null) {
         try {
            if (MOVE_VECTOR_FIELD != null) {
               MOVE_VECTOR_FIELD.set(input, moveVector);
            }
         } catch (IllegalAccessException var3) {
         }
      }
   }

   private static Field findMoveVectorField() {
      try {
         Field field = ClientInput.class.getDeclaredField("moveVector");
         field.setAccessible(true);
         return field;
      } catch (ReflectiveOperationException var6) {
         try {
            for (Field f : ClientInput.class.getDeclaredFields()) {
               if (f.getType() == Vec2.class) {
                  f.setAccessible(true);
                  return f;
               }
            }
         } catch (Exception var5) {
         }

         return null;
      }
   }
}
