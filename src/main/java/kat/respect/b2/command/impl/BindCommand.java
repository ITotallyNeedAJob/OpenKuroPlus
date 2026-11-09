package kat.respect.b2.command.impl;

import kat.respect.b2.client.ClickGuiKeybinds;
import kat.respect.b2.command.Command;
import kat.respect.b2.config.ConfigManager;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import kat.respect.b2.util.ChatUtils;
import kat.respect.b2.util.KeyUtils;
import kat.respect.b2.util.NotificationManager;

public final class BindCommand extends Command {
   public BindCommand() {
      super("bind", "Bind a module to a key", ".bind <module> <key|none>", "b");
   }

   @Override
   public void execute(String[] args) {
      if (args.length < 3) {
         ChatUtils.warn("Usage: " + this.getSyntax());
      } else {
         String modName = args[1];
         Module module = ModuleManager.INSTANCE.getModuleByName(modName);
         if (module == null) {
            ChatUtils.error("Module not found: " + modName);
         } else {
            String keyStr = args[2].toUpperCase();
            if (!keyStr.equals("NONE") && !keyStr.equals("0") && !keyStr.equals("-1")) {
               int keyCode = KeyUtils.getKeyCode(keyStr);
               if (keyCode == -1) {
                  ChatUtils.error("Unknown key: " + args[2]);
               } else {
                  module.setKey(keyCode);
                  if ("ClickGUI".equalsIgnoreCase(module.getName())) {
                     ClickGuiKeybinds.syncFromModule();
                  }

                  ConfigManager.getInstance().saveGui();
                  NotificationManager.push("Keybind", "Bound " + module.getName() + " to " + KeyUtils.getKeyName(keyCode), NotificationManager.Type.SUCCESS);
               }
            } else {
               module.setKey(-1);
               if ("ClickGUI".equalsIgnoreCase(module.getName())) {
                  ClickGuiKeybinds.syncFromModule();
               }

               ConfigManager.getInstance().saveGui();
               NotificationManager.push("Keybind", "Unbound " + module.getName(), NotificationManager.Type.INFO);
            }
         }
      }
   }
}
