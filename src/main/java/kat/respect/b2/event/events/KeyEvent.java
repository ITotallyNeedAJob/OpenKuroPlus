package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;

public class KeyEvent extends Event {
   private final int key;
   private final int action;

   public KeyEvent(int key, int action) {
      this.key = key;
      this.action = action;
   }

   public int getKey() {
      return this.key;
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
