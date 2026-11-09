package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;
import net.minecraft.world.entity.player.Input;

public class MovementInputEvent extends Event {
   private Input input;

   public MovementInputEvent(Input input) {
      this.input = input;
   }

   public Input getInput() {
      return this.input;
   }

   public void setInput(Input input) {
      this.input = input;
   }
}
