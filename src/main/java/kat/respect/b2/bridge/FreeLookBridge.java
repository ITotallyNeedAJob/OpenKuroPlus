package kat.respect.b2.bridge;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class FreeLookBridge {
   private static final List<FreeLookBridge.Hook> hooks = new CopyOnWriteArrayList<>();
   private static volatile boolean programmatic = false;
   private static volatile float partialTick = 1.0F;
   private static volatile boolean eyeAnchorValid = false;
   private static volatile double eyeAnchorX;
   private static volatile double eyeAnchorY;
   private static volatile double eyeAnchorZ;

   public static void beginCameraFrame(float tickDelta) {
      partialTick = tickDelta;
      eyeAnchorValid = false;
   }

   public static void setEyeAnchor(double x, double y, double z) {
      eyeAnchorX = x;
      eyeAnchorY = y;
      eyeAnchorZ = z;
      eyeAnchorValid = true;
   }

   public static boolean hasEyeAnchor() {
      return eyeAnchorValid;
   }

   public static double getEyeAnchorX() {
      return eyeAnchorX;
   }

   public static double getEyeAnchorY() {
      return eyeAnchorY;
   }

   public static double getEyeAnchorZ() {
      return eyeAnchorZ;
   }

   public static float getPartialTick() {
      return partialTick;
   }

   public static void setProgrammatic(boolean v) {
      programmatic = v;
   }

   public static boolean isProgrammatic() {
      return programmatic;
   }

   public static void register(FreeLookBridge.Hook h) {
      if (h != null && !hooks.contains(h)) {
         hooks.add(h);
      }
   }

   public static void unregister(FreeLookBridge.Hook h) {
      hooks.remove(h);
   }

   private static FreeLookBridge.Hook getActiveHook() {
      for (int i = hooks.size() - 1; i >= 0; i--) {
         FreeLookBridge.Hook h = hooks.get(i);
         if (h != null && h.isCameraActive()) {
            return h;
         }
      }

      return null;
   }

   public static boolean isActive() {
      return getActiveHook() != null;
   }

   public static void applyTurn(double x, double y) {
      FreeLookBridge.Hook active = getActiveHook();
      if (active != null) {
         active.applyTurn(x, y);
      }
   }

   public static boolean capturesTurn() {
      FreeLookBridge.Hook active = getActiveHook();
      return active != null && active.capturesTurn();
   }

   public static void onFrame() {
      for (FreeLookBridge.Hook h : hooks) {
         if (h != null && h.isCameraActive()) {
            h.onFrame();
         }
      }
   }

   public static float getCameraYaw() {
      FreeLookBridge.Hook active = getActiveHook();
      return active == null ? Float.NaN : active.getCameraYaw();
   }

   public static float getCameraPitch() {
      FreeLookBridge.Hook active = getActiveHook();
      return active == null ? Float.NaN : active.getCameraPitch();
   }

   public static void steerCamera(float yaw, float pitch) {
      FreeLookBridge.Hook active = getActiveHook();
      if (active != null) {
         active.steerCamera(yaw, pitch);
      }
   }

   public static boolean overridesPosition() {
      FreeLookBridge.Hook active = getActiveHook();
      return active != null && active.overridesPosition();
   }

   public static double getCameraX() {
      FreeLookBridge.Hook active = getActiveHook();
      return active == null ? Double.NaN : active.getCameraX();
   }

   public static double getCameraY() {
      FreeLookBridge.Hook active = getActiveHook();
      return active == null ? Double.NaN : active.getCameraY();
   }

   public static double getCameraZ() {
      FreeLookBridge.Hook active = getActiveHook();
      return active == null ? Double.NaN : active.getCameraZ();
   }

   private FreeLookBridge() {
   }

   public interface Hook {
      boolean isCameraActive();

      void applyTurn(double var1, double var3);

      void onFrame();

      float getCameraYaw();

      float getCameraPitch();

      default boolean capturesTurn() {
         return true;
      }

      default void steerCamera(float yaw, float pitch) {
      }

      default boolean overridesPosition() {
         return false;
      }

      default double getCameraX() {
         return 0.0;
      }

      default double getCameraY() {
         return 0.0;
      }

      default double getCameraZ() {
         return 0.0;
      }
   }
}
