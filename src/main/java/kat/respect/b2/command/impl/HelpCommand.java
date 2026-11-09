package kat.respect.b2.command.impl;

import kat.respect.b2.command.Command;
import kat.respect.b2.command.CommandManager;
import kat.respect.b2.util.ChatUtils;

public final class HelpCommand extends Command {
   public HelpCommand() {
      super("help", "Displays available client commands", ".help", "?");
   }

   @Override
   public void execute(String[] args) {
      ChatUtils.info("Commands:");

      for (Command cmd : CommandManager.INSTANCE.getCommands()) {
         ChatUtils.info(" " + cmd.getSyntax() + " - " + cmd.getDescription());
      }
   }
}
