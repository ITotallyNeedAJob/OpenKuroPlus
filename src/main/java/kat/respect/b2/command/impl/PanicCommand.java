package kat.respect.b2.command.impl;

import java.util.ArrayList;
import kat.respect.b2.command.Command;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import kat.respect.b2.util.NotificationManager;

public final class PanicCommand extends Command {
   public PanicCommand() {
      super("panic", "Disable every enabled module", ".panic");
   }

   @Override
   public void execute(String[] args) {
      int disabled = 0;

      for (Module module : new ArrayList<>(ModuleManager.INSTANCE.getEnabledModules())) {
         if (!"ClickGUI".equalsIgnoreCase(module.getName())) {
            module.setEnabled(false);
            disabled++;
         }
      }

      NotificationManager.push("Panic", "Disabled " + disabled + " module" + (disabled == 1 ? "" : "s"), NotificationManager.Type.WARNING);
   }
}
