package kat.respect.b2.bridge;

public final class HurtCamBridge {
   private static volatile boolean noHurtCamEnabled = false;

   private HurtCamBridge() {
   }

   public static boolean isNoHurtCamEnabled() {
      return noHurtCamEnabled;
   }

   public static void setNoHurtCamEnabled(boolean enabled) {
      noHurtCamEnabled = enabled;
   }
}
