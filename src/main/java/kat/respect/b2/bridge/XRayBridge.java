package kat.respect.b2.bridge;

import net.minecraft.world.level.block.state.BlockState;

public final class XRayBridge {
   private static volatile XRayBridge.Hook hook;

   public static void register(XRayBridge.Hook h) {
      if (h != null) {
         hook = h;
      }
   }

   public static void unregister(XRayBridge.Hook h) {
      if (hook == h) {
         hook = null;
      }
   }

   public static int decide(BlockState state) {
      XRayBridge.Hook h = hook;
      if (h != null && h.isEnabled()) {
         return h.isVisible(state) ? 1 : -1;
      } else {
         return 0;
      }
   }

   private XRayBridge() {
   }

   public interface Hook {
      boolean isEnabled();

      boolean isVisible(BlockState var1);
   }
}
