package kat.respect.b2.module.mode;

import kat.respect.b2.event.events.MotionEvent;
import kat.respect.b2.event.events.PacketEvent;
import kat.respect.b2.event.events.TickEvent;

public interface ModuleMode {
   String getName();

   default void onEnable(Object module) {
   }

   default void onDisable(Object module) {
   }

   default void onTick(TickEvent event, Object module) {
   }

   default void onMotion(MotionEvent event, Object module) {
   }

   default void onPacket(PacketEvent event, Object module) {
   }
}
