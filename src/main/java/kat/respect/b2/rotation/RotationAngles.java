package kat.respect.b2.rotation;

import net.minecraft.util.Mth;

@Deprecated
public class RotationAngles extends RotAngles {
   public RotationAngles(float yaw, float pitch) {
      super(yaw, pitch);
   }

   public RotationAngles(double yaw, double pitch) {
      super(yaw, pitch);
   }

   public RotationAngles(RotAngles other) {
      super(other.yaw(), other.pitch());
   }

   public RotationAngles add(RotationAngles other) {
      return new RotationAngles(this.yaw + other.yaw, this.pitch + other.pitch);
   }

   public RotationAngles subtract(RotationAngles other) {
      return new RotationAngles(this.yaw - other.yaw, this.pitch - other.pitch);
   }

   public RotationAngles norm() {
      return new RotationAngles(Mth.wrapDegrees(this.yaw), this.pitch);
   }

   public RotationAngles normalizeYaw() {
      return this.norm();
   }
}
