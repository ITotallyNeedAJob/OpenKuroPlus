package kat.respect.b2.rotation;

import net.minecraft.util.Mth;

public class RotAngles {
   protected final float yaw;
   protected final float pitch;

   public RotAngles(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public RotAngles(double yaw, double pitch) {
      this((float)yaw, (float)pitch);
   }

   public float yaw() {
      return this.yaw;
   }

   public float pitch() {
      return this.pitch;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public RotAngles add(RotAngles other) {
      return new RotAngles(this.yaw + other.yaw, this.pitch + other.pitch);
   }

   public RotAngles sub(RotAngles other) {
      return new RotAngles(this.yaw - other.yaw, this.pitch - other.pitch);
   }

   public RotAngles subtract(RotAngles other) {
      return this.sub(other);
   }

   public RotAngles norm() {
      return new RotAngles(Mth.wrapDegrees(this.yaw), this.pitch);
   }

   public RotAngles normalizeYaw() {
      return this.norm();
   }

   public double mag() {
      return Math.abs(this.yaw) + Math.abs(this.pitch);
   }

   public double magnitude() {
      return this.mag();
   }

   public boolean sameYaw(RotAngles other) {
      float diff = Math.abs(Mth.wrapDegrees(this.yaw) - Mth.wrapDegrees(other.yaw));
      return diff < 0.1F || diff > 359.9F;
   }

   public boolean hasSameYaw(RotAngles other) {
      return this.sameYaw(other);
   }

   public boolean approx(RotAngles other) {
      return this.sameYaw(other) && Math.abs(this.pitch - other.pitch) < 0.1F;
   }

   public boolean approximatelyEquals(RotAngles other) {
      return this.approx(other);
   }

   public boolean within(RotAngles other, float tol) {
      float yawDiff = Math.abs(Mth.wrapDegrees(this.yaw) - Mth.wrapDegrees(other.yaw));
      float pitchDiff = Math.abs(this.pitch - other.pitch);
      return (yawDiff < tol || yawDiff > 360.0F - tol) && pitchDiff < tol;
   }

   public boolean isWithin(RotAngles other, float tolerance) {
      return this.within(other, tolerance);
   }
}
