package kat.respect.b2.injector;

import kat.respect.b2.KuroPlusClient;
import kat.respect.b2.event.EventBus;
import kat.respect.b2.event.events.MotionEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;

public final class RespectHookBridge {
   private RespectHookBridge() {
   }

   public static void onClientTick() {
      try {
         KuroPlusClient.getInstance().onUpdate();
      } catch (Throwable var1) {
      }
   }

   public static void onSendMovementPacketsHead(Object playerObj) {
      try {
         if (playerObj instanceof LocalPlayer player) {
            EventBus.INSTANCE
               .post(new MotionEvent(MotionEvent.State.PRE, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.onGround()));
         }
      } catch (Throwable var2) {
      }
   }

   public static void onSendMovementPacketsTail(Object playerObj) {
      try {
         if (playerObj instanceof LocalPlayer player) {
            EventBus.INSTANCE
               .post(
                  new MotionEvent(MotionEvent.State.POST, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.onGround())
               );
         }
      } catch (Throwable var2) {
      }
   }

   public static void onSetScreen(Object screenObj) {
      try {
         if (screenObj instanceof Screen var1) {
            ;
         }
      } catch (Throwable var2) {
      }
   }
}
