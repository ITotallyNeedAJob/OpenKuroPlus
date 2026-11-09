package kat.respect.b2.ct;

import kat.respect.b2.event.ClientEventDispatcher;

public final class AgentTickHook {
   private static volatile boolean active;

   private AgentTickHook() {
   }

   public static void activate() {
      active = true;
      System.out.println("[Kuro+][CT] native tick source active (Minecraft.tick hook)");
   }

   public static void deactivate() {
      active = false;
   }

   public static boolean isActive() {
      return active;
   }

   public static void postPre() {
      ClientEventDispatcher.onClientTickStart("agent");
   }

   public static void postPost() {
      ClientEventDispatcher.onClientTickEnd("agent");
   }
}
