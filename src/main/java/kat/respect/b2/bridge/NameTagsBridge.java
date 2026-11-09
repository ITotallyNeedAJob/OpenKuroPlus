package kat.respect.b2.bridge;

import net.minecraft.world.entity.player.Player;

public final class NameTagsBridge {
   private static volatile NameTagsBridge.Hook hook;

   public static void register(NameTagsBridge.Hook h) {
      hook = h;
   }

   public static void unregister(NameTagsBridge.Hook h) {
      if (hook == h) {
         hook = null;
      }
   }

   public static NameTagsBridge.Hook get() {
      return hook;
   }

   private NameTagsBridge() {
   }

   public interface Hook {
      boolean isEnabled();

      boolean shouldHide(Player var1, boolean var2);
   }
}
