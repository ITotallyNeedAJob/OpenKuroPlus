package kat.respect.b2.debug;

public final class OverlayStats {
   public static final int SRC_POST_BLIT = 1;
   public static final int SRC_PRE_PRESENT = 2;
   public static final int SRC_POST_RENDER = 3;
   public static volatile long overlayCalls;
   public static volatile int lastSource;
   public static volatile int lastFrame = -1;
   public static volatile long guiCalls;
   public static volatile String guiError;
   public static volatile String glInfo;
   public static volatile boolean hookLogged;
   public static volatile boolean errorLogged;
   public static volatile long lastSummaryNanos;

   private OverlayStats() {
   }

   public static String sourceName(int source) {
      switch (source) {
         case 1:
            return "post-blit";
         case 2:
            return "pre-present";
         case 3:
            return "post-render";
         default:
            return "none";
      }
   }
}
