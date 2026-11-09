package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;

public class TickEvent extends Event {
   private final TickEvent.Phase phase;

   public TickEvent(TickEvent.Phase phase) {
      this.phase = phase;
   }

   public TickEvent.Phase getPhase() {
      return this.phase;
   }

   public boolean isPre() {
      return this.phase == TickEvent.Phase.PRE;
   }

   public boolean isPost() {
      return this.phase == TickEvent.Phase.POST;
   }

   public static enum Phase {
      PRE,
      POST;
   }
}
