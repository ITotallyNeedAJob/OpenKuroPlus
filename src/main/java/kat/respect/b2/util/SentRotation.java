package kat.respect.b2.util;

import net.minecraft.util.Mth;

public final class SentRotation {
   private static volatile float yaw = Float.NaN;
   private static volatile float pitch = Float.NaN;
   private static volatile boolean spoofed = false;

   private SentRotation() {
   }

   public static void update(float newYaw, float newPitch, boolean wasSpoof) {
      yaw = newYaw;
      pitch = Mth.clamp(newPitch, -90.0F, 90.0F);
      spoofed = wasSpoof;
   }

   public static boolean isValid() {
      return !Float.isNaN(yaw);
   }

   public static boolean isSpoofed() {
      return spoofed && isValid();
   }

   public static float yaw() {
      return yaw;
   }

   public static float pitch() {
      return pitch;
   }

   public static boolean matches(float wantYaw, float wantPitch, float toleranceDeg) {
      return !isValid() ? false : Math.abs(Mth.wrapDegrees(wantYaw - yaw)) <= toleranceDeg && Math.abs(wantPitch - pitch) <= toleranceDeg;
   }

   public static void reset() {
      yaw = Float.NaN;
      pitch = Float.NaN;
      spoofed = false;
   }
}
