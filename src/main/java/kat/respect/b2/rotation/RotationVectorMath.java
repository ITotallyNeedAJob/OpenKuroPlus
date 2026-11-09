package kat.respect.b2.rotation;

import net.minecraft.world.phys.Vec3;

@Deprecated
public final class RotationVectorMath {
   public static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
   public static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;

   private RotationVectorMath() {
   }

   public static Vec3 getLookVector(RotationAngles rotation) {
      return RotMath.lookVec(rotation);
   }

   public static RotationAngles calculateRotation(Vec3 eye, Vec3 target, float currentYaw, boolean normalizeYaw) {
      RotAngles r = RotMath.calcRot(eye, target, currentYaw, normalizeYaw);
      return new RotationAngles(r.yaw(), r.pitch());
   }

   public static RotationAngles subtract(RotationAngles from, RotationAngles to) {
      RotAngles r = RotMath.sub(from, to);
      return new RotationAngles(r.yaw(), r.pitch());
   }

   public static RotationAngles getFixedRotation(Vec3 eye, Vec3 target, float currentYaw, float currentPitch) {
      RotAngles r = RotMath.fixedRot(eye, target, currentYaw, currentPitch);
      return new RotationAngles(r.yaw(), r.pitch());
   }
}
