package kat.respect.b2.util;

import kat.respect.b2.rotation.RotationManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public final class MovementUtil {
   private MovementUtil() {
   }

   public static double horizontalSpeed(Entity entity) {
      Vec3 motion = entity.getDeltaMovement();
      return Math.sqrt(motion.x * motion.x + motion.z * motion.z);
   }

   public static boolean isMoving(Input input) {
      return input != null && (input.forward() || input.backward() || input.left() || input.right());
   }

   public static boolean isMoving(LocalPlayer player) {
      return player != null && player.input != null && isMoving(player.input.keyPresses);
   }

   public static float getMovementYaw(LocalPlayer player) {
      if (player == null) {
         return 0.0F;
      } else {
         float baseYaw = player.getYRot();
         Input keys = player.input != null ? player.input.keyPresses : Input.EMPTY;
         return RotationManager.INSTANCE.adjustMovementYaw(baseYaw, keys.forward(), keys.left(), keys.right(), keys.backward());
      }
   }

   public static Vec3 withStrafe(Vec3 motion, double speed, double strength, float yaw) {
      double oneMinusStrength = 1.0 - strength;
      double prevX = motion.x * oneMinusStrength;
      double prevZ = motion.z * oneMinusStrength;
      double usedSpeed = speed * strength;
      double angle = Math.toRadians(yaw);
      return new Vec3(prevX - Math.sin(angle) * usedSpeed, motion.y, prevZ + Math.cos(angle) * usedSpeed);
   }
}
