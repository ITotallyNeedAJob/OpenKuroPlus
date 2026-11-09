package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;

public class MouseButtonEvent extends Event {
   private final int button;
   private final int action;

   public MouseButtonEvent(int button, int action) {
      this.button = button;
      this.action = action;
   }

   public int getButton() {
      return this.button;
   }

   public int getAction() {
      return this.action;
   }

   public boolean isPress() {
      return this.action == 1;
   }

   public boolean isRelease() {
      return this.action == 0;
   }
}
