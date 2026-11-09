package kat.respect.b2.bridge;

public final class ReachBridge {
   private static ReachBridge.Hook hook;

   public static void register(ReachBridge.Hook h) {
      hook = h;
   }

   public static double getActiveReachDistance() {
      return hook == null ? 0.0 : hook.getActiveReachDistance();
   }

   public static boolean isVerticalCheckEnabled() {
      return hook != null && hook.isVerticalCheckEnabled();
   }

   private ReachBridge() {
   }

   public interface Hook {
      double getActiveReachDistance();

      boolean isVerticalCheckEnabled();
   }
}
