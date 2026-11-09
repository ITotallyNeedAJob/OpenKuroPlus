package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;
import net.minecraft.world.entity.LivingEntity;

public class AttackEvent extends Event {
   private final LivingEntity target;

   public AttackEvent(LivingEntity target) {
      super(true);
      this.target = target;
   }

   public LivingEntity getTarget() {
      return this.target;
   }
}
