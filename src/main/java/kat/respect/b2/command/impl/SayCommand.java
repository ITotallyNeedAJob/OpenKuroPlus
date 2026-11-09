package kat.respect.b2.command.impl;

import java.util.Arrays;
import kat.respect.b2.command.Command;
import kat.respect.b2.command.CommandManager;
import kat.respect.b2.util.ChatUtils;
import net.minecraft.client.Minecraft;

public final class SayCommand extends Command {
   private static final Minecraft mc = Minecraft.getInstance();

   public SayCommand() {
      super("say", "Send a chat message or server command bypassing client prefix", ".say <message>");
   }

   @Override
   public void execute(String[] args) {
      if (args.length < 2) {
         ChatUtils.warn("Usage: " + this.getSyntax());
      } else if (mc.player != null && mc.player.connection != null) {
         String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
         CommandManager.INSTANCE.setSuppressCommands(true);

         try {
            if (message.startsWith("/")) {
               mc.player.connection.sendCommand(message.substring(1));
            } else {
               mc.player.connection.sendChat(message);
            }
         } finally {
            CommandManager.INSTANCE.setSuppressCommands(false);
         }
      }
   }
}
