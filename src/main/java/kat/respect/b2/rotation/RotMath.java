package kat.respect.b2.rotation;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class RotMath {
   public static final double DEG_TO_RAD = Math.PI / 180.0;
   public static final double RAD_TO_DEG = 180.0 / Math.PI;

   private RotMath() {
   }

   public static Vec3 lookVec(RotAngles rot) {
      float f = Mth.cos(-rot.yaw() * (float) (Math.PI / 180.0) - (float) Math.PI);
      float f2 = Mth.sin(-rot.yaw() * (float) (Math.PI / 180.0) - (float) Math.PI);
      float f3 = -Mth.cos(-rot.pitch() * (float) (Math.PI / 180.0));
      float f4 = Mth.sin(-rot.pitch() * (float) (Math.PI / 180.0));
      return new Vec3(f2 * f3, f4, f * f3);
   }

   public static RotAngles calcRot(Vec3 eye, Vec3 target, float currentYaw, boolean normalizeYaw) {
      double d0 = eye.x - target.x;
      double d1 = eye.y - target.y;
      double d2 = eye.z - target.z;
      double yaw = Math.atan2(d0, -d2) * (180.0 / Math.PI);
      double horizontalDistance = Math.sqrt(d0 * d0 + d2 * d2);
      double pitch = Math.atan2(d1, horizontalDistance) * (180.0 / Math.PI);
      double yawDifference = (int)((yaw - currentYaw) % 360.0);
      yawDifference = Math.abs(Mth.wrapDegrees((float)yawDifference));
      if (normalizeYaw && yawDifference > 90.0) {
         pitch = 90.0;
      }

      return new RotAngles((float)yaw, (float)pitch);
   }

   public static RotAngles sub(RotAngles from, RotAngles to) {
      float yawDelta = Mth.wrapDegrees(to.yaw()) - Mth.wrapDegrees(from.yaw());
      if (yawDelta <= -180.0F) {
         yawDelta += 360.0F;
      } else if (yawDelta > 180.0F) {
         yawDelta -= 360.0F;
      }

      return new RotAngles(yawDelta, to.pitch() - from.pitch());
   }

   public static RotAngles fixedRot(Vec3 eye, Vec3 target, float currentYaw, float currentPitch) {
      double dx = eye.x - target.x;
      double dy = eye.y - target.y;
      double dz = eye.z - target.z;
      double rawYaw = Math.atan2(dx, -dz) * (180.0 / Math.PI);
      double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
      double rawPitch = Math.atan2(dy, horizontalDistance) * (180.0 / Math.PI);
      float sensitivity = RotationManager.INSTANCE.getMouseSensitivity();
      float sensitivityBase = sensitivity * 0.6F + 0.2F;
      float degreesPerPixel = sensitivityBase * sensitivityBase * sensitivityBase * 8.0F * 0.15F;
      float yawError = Mth.wrapDegrees((float)(rawYaw - currentYaw));
      int yawPixels = Math.round(yawError / degreesPerPixel);
      float pitchError = Mth.wrapDegrees((float)(rawPitch - currentPitch));
      int pitchPixels = Math.round(pitchError / degreesPerPixel);
      return new RotAngles(currentYaw + degreesPerPixel * yawPixels, Mth.clamp(currentPitch + degreesPerPixel * pitchPixels, -90.0F, 90.0F));
   }
}
