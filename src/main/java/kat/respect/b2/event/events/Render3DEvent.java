package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;

public class Render3DEvent extends Event {
   private final float tickDelta;

   public Render3DEvent(float tickDelta) {
      this.tickDelta = tickDelta;
   }

   public float getTickDelta() {
      return this.tickDelta;
   }
}
