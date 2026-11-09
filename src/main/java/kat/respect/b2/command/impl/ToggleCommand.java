package kat.respect.b2.command.impl;

import kat.respect.b2.command.Command;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import kat.respect.b2.util.ChatUtils;

public final class ToggleCommand extends Command {
   public ToggleCommand() {
      super("toggle", "Toggle a module on or off", ".toggle <module>", "t");
   }

   @Override
   public void execute(String[] args) {
      if (args.length < 2) {
         ChatUtils.warn("Usage: " + this.getSyntax());
      } else {
         String modName = args[1];
         Module module = ModuleManager.INSTANCE.getModuleByName(modName);
         if (module == null) {
            ChatUtils.error("Module not found: " + modName);
         } else {
            module.toggle();
         }
      }
   }
}
