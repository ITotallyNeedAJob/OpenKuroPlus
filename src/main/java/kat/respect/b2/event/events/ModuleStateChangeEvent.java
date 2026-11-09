package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;
import kat.respect.b2.module.Module;

public class ModuleStateChangeEvent extends Event {
   private final Module module;
   private final boolean enabled;

   public ModuleStateChangeEvent(Module module, boolean enabled) {
      this.module = module;
      this.enabled = enabled;
   }

   public Module getModule() {
      return this.module;
   }

   public boolean isEnabled() {
      return this.enabled;
   }
}
