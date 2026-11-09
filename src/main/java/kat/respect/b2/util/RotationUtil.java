package kat.respect.b2.util;

import kat.respect.b2.rotation.RotationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RotationUtil {
   private static final Minecraft mc = Minecraft.getInstance();
   private static boolean usingSilentRotation = false;
   private static float silentYaw = 0.0F;
   private static float silentPitch = 0.0F;
   private static float previousYaw = 0.0F;
   private static float previousPitch = 0.0F;

   public static float[] getRotationsTo(Vec3 from, Vec3 to) {
      double diffX = to.x - from.x;
      double diffY = to.y - from.y;
      double diffZ = to.z - from.z;
      double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0F;
      float pitch = (float)(-(Math.atan2(diffY, distance) * 180.0 / Math.PI));
      return new float[]{yaw, pitch};
   }

   public static float[] getRotationsToEntity(Entity entity) {
      return mc.player == null
         ? new float[]{0.0F, 0.0F}
         : getRotationsTo(mc.player.getEyePosition(), entity.position().add(0.0, entity.getBbHeight() / 2.0F, 0.0));
   }

   public static float[] getRotationsTo(Vec3 to) {
      return mc.player == null ? new float[]{0.0F, 0.0F} : getRotationsTo(mc.player.getEyePosition(), to);
   }

   public static void setSilentRotation(float yaw, float pitch) {
      if (mc.player != null) {
         silentYaw = normalizeYaw(yaw);
         silentPitch = Mth.clamp(pitch, -90.0F, 90.0F);
         usingSilentRotation = true;
         RotationHandler.setSilent(silentYaw, silentPitch, 10.0F);
         ServerRotationHandler.setServerRotationsWithDelta(silentYaw, silentPitch);
      }
   }

   public static void resetSilentRotation() {
      usingSilentRotation = false;
      silentYaw = 0.0F;
      silentPitch = 0.0F;
      RotationHandler.clearSilent();
      ServerRotationHandler.reset();
   }

   public static float getSilentYaw() {
      if (ServerRotationHandler.isHasSilentRotation()) {
         return ServerRotationHandler.getServerYaw();
      } else if (RotationHandler.isSilentActive()) {
         return RotationHandler.getSilentYaw();
      } else {
         try {
            if (RotationManager.INSTANCE.isControllerUpdating()) {
               return RotationManager.INSTANCE.getManagedYaw();
            }
         } catch (Throwable var1) {
         }

         if (usingSilentRotation) {
            return silentYaw;
         } else {
            return mc.player != null ? mc.player.getYRot() : 0.0F;
         }
      }
   }

   public static float getSilentPitch() {
      if (ServerRotationHandler.isHasSilentRotation()) {
         return ServerRotationHandler.getServerPitch();
      } else if (RotationHandler.isSilentActive()) {
         return RotationHandler.getSilentPitch();
      } else {
         try {
            if (RotationManager.INSTANCE.isControllerUpdating()) {
               return RotationManager.INSTANCE.getManagedPitch();
            }
         } catch (Throwable var1) {
         }

         if (usingSilentRotation) {
            return silentPitch;
         } else {
            return mc.player != null ? mc.player.getXRot() : 0.0F;
         }
      }
   }

   public static boolean isUsingSilentRotation() {
      if (!usingSilentRotation && !RotationHandler.isSilentActive() && !ServerRotationHandler.isHasSilentRotation()) {
         try {
            return RotationManager.INSTANCE.isControllerUpdating();
         } catch (Throwable var1) {
            return false;
         }
      } else {
         return true;
      }
   }

   public static float normalizeYaw(float yaw) {
      yaw %= 360.0F;
      if (yaw >= 180.0F) {
         yaw -= 360.0F;
      }

      if (yaw < -180.0F) {
         yaw += 360.0F;
      }

      return yaw;
   }

   public static float getAngleDifference(float a, float b) {
      float diff = Math.abs(a - b) % 360.0F;
      if (diff > 180.0F) {
         diff = 360.0F - diff;
      }

      return diff;
   }

   public static float smoothRotate(float current, float target, float speed) {
      float diff = normalizeYaw(target - current);
      if (Math.abs(diff) <= speed) {
         return target;
      } else {
         return diff > 0.0F ? current + speed : current - speed;
      }
   }

   public static float[] smoothRotate(float currentYaw, float currentPitch, float targetYaw, float targetPitch, float speed) {
      float newYaw = smoothRotate(currentYaw, targetYaw, speed);
      float newPitch = smoothRotate(currentPitch, targetPitch, speed);
      return new float[]{newYaw, newPitch};
   }

   public static boolean isLookingAt(Vec3 pos, float threshold) {
      if (mc.player == null) {
         return false;
      } else {
         float[] targetRotation = getRotationsTo(pos);
         float yawDiff = getAngleDifference(mc.player.getYRot(), targetRotation[0]);
         float pitchDiff = getAngleDifference(mc.player.getXRot(), targetRotation[1]);
         return yawDiff <= threshold && pitchDiff <= threshold;
      }
   }

   public static boolean isLookingAtEntity(Entity entity, float threshold) {
      if (mc.player == null) {
         return false;
      } else {
         float[] targetRotation = getRotationsToEntity(entity);
         float yawDiff = getAngleDifference(mc.player.getYRot(), targetRotation[0]);
         float pitchDiff = getAngleDifference(mc.player.getXRot(), targetRotation[1]);
         return yawDiff <= threshold && pitchDiff <= threshold;
      }
   }

   public static float getRotationDistance(Vec3 pos) {
      if (mc.player == null) {
         return 0.0F;
      } else {
         float[] targetRotation = getRotationsTo(pos);
         float yawDiff = getAngleDifference(mc.player.getYRot(), targetRotation[0]);
         float pitchDiff = getAngleDifference(mc.player.getXRot(), targetRotation[1]);
         return (float)Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
      }
   }
}
