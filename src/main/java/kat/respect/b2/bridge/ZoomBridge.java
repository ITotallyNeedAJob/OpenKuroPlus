package kat.respect.b2.bridge;

public final class ZoomBridge {
   private static volatile ZoomBridge.Hook hook;

   public static void register(ZoomBridge.Hook h) {
      hook = h;
   }

   public static void unregister(ZoomBridge.Hook h) {
      if (hook == h) {
         hook = null;
      }
   }

   public static boolean isActive() {
      ZoomBridge.Hook h = hook;
      return h != null && h.isZoomActive();
   }

   public static double getFov(double original) {
      ZoomBridge.Hook h = hook;
      return h == null ? original : h.getFov(original);
   }

   public static boolean shouldScaleSensitivity() {
      ZoomBridge.Hook h = hook;
      return h != null && h.shouldScaleSensitivity();
   }

   public static double getSensitivityScale() {
      ZoomBridge.Hook h = hook;
      return h == null ? 1.0 : h.getSensitivityScale();
   }

   public static boolean onMouseScroll(double vertical) {
      ZoomBridge.Hook h = hook;
      return h != null && h.onMouseScroll(vertical);
   }

   public static boolean isThirdPersonActive() {
      ZoomBridge.Hook h = hook;
      return h != null && h.isThirdPersonActive();
   }

   public static double getThirdPersonDistance(double original) {
      ZoomBridge.Hook h = hook;
      return h == null ? original : h.getThirdPersonDistance(original);
   }

   private ZoomBridge() {
   }

   public interface Hook {
      boolean isZoomActive();

      double getFov(double var1);

      boolean shouldScaleSensitivity();

      double getSensitivityScale();

      boolean onMouseScroll(double var1);

      boolean isThirdPersonActive();

      double getThirdPersonDistance(double var1);
   }
}
