package kat.respect.b2.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ServerRot {
   private static float yaw = 0.0F;
   private static float pitch = 0.0F;
   private static float prevYaw = 0.0F;
   private static float prevPitch = 0.0F;
   private static float deltaYaw = 0.0F;
   private static boolean silent = false;

   private ServerRot() {
   }

   public static float yaw() {
      return yaw;
   }

   public static void setYaw(float y) {
      yaw = y;
   }

   public static float pitch() {
      return pitch;
   }

   public static void setPitch(float p) {
      pitch = p;
   }

   public static float prevYaw() {
      return prevYaw;
   }

   public static float prevPitch() {
      return prevPitch;
   }

   public static float deltaYaw() {
      return deltaYaw;
   }

   public static void setDeltaYaw(float d) {
      deltaYaw = d;
   }

   public static boolean hasSilent() {
      return silent;
   }

   public static void setSilent(boolean s) {
      silent = s;
   }

   public static void update(float y, float p) {
      prevYaw = yaw;
      prevPitch = pitch;
      yaw = yaw + Mth.wrapDegrees(y - yaw);
      pitch = Mth.clamp(p, -90.0F, 90.0F);
      deltaYaw = 0.0F;
   }

   public static void setWithDelta(float y, float p) {
      prevYaw = yaw;
      prevPitch = pitch;
      deltaYaw = Mth.wrapDegrees(y - yaw);
      yaw = y;
      pitch = Mth.clamp(p, -90.0F, 90.0F);
   }

   public static void reset() {
      deltaYaw = 0.0F;
      silent = false;
   }

   public static void sync(float playerYaw, float playerPitch) {
      prevYaw = yaw;
      prevPitch = pitch;
      yaw = yaw + Mth.wrapDegrees(playerYaw - yaw);
      pitch = Mth.clamp(playerPitch, -90.0F, 90.0F);
      deltaYaw = 0.0F;
      silent = false;
   }

   public static Vec3 rotVec(float p, float y) {
      float pitchRad = p * (float) (Math.PI / 180.0);
      float yawRad = -y * (float) (Math.PI / 180.0);
      float cosPitch = Mth.cos(pitchRad);
      float sinPitch = Mth.sin(pitchRad);
      float cosYaw = Mth.cos(yawRad);
      float sinYaw = Mth.sin(yawRad);
      return new Vec3(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
   }
}
