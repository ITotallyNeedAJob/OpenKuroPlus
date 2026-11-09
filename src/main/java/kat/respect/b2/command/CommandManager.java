package kat.respect.b2.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kat.respect.b2.command.impl.BindCommand;
import kat.respect.b2.command.impl.ConfigCommand;
import kat.respect.b2.command.impl.FriendCommand;
import kat.respect.b2.command.impl.HelpCommand;
import kat.respect.b2.command.impl.PanicCommand;
import kat.respect.b2.command.impl.SayCommand;
import kat.respect.b2.command.impl.ToggleCommand;
import kat.respect.b2.util.ChatUtils;

public final class CommandManager {
   public static final CommandManager INSTANCE = new CommandManager();
   private final List<Command> commands = new ArrayList<>();
   private final Map<String, Command> commandMap = new HashMap<>();
   private String prefix = ".";
   private boolean suppressCommands = false;
   private boolean initialized = false;

   private CommandManager() {
   }

   public synchronized void init() {
      if (!this.initialized) {
         this.register(new HelpCommand());
         this.register(new ConfigCommand());
         this.register(new FriendCommand());
         this.register(new PanicCommand());
         this.register(new SayCommand());
         this.register(new BindCommand());
         this.register(new ToggleCommand());
         this.initialized = true;
      }
   }

   public void register(Command command) {
      if (command != null) {
         this.commands.add(command);
         this.commandMap.put(command.getName().toLowerCase(), command);

         for (String alias : command.getAliases()) {
            this.commandMap.put(alias.toLowerCase(), command);
         }
      }
   }

   public List<Command> getCommands() {
      return Collections.unmodifiableList(this.commands);
   }

   public Command getCommand(String name) {
      return name == null ? null : this.commandMap.get(name.toLowerCase());
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   public boolean isSuppressCommands() {
      return this.suppressCommands;
   }

   public void setSuppressCommands(boolean suppressCommands) {
      this.suppressCommands = suppressCommands;
   }

   public boolean isCommand(String message) {
      if (message != null && !this.suppressCommands) {
         String trimmed = message.trim();
         return this.prefix != null && !this.prefix.isEmpty() && trimmed.startsWith(this.prefix);
      } else {
         return false;
      }
   }

   public void handleCommand(String input) {
      if (input != null && !input.isEmpty()) {
         String clean = input.trim();
         if (this.prefix != null && !this.prefix.isEmpty() && clean.startsWith(this.prefix)) {
            clean = clean.substring(this.prefix.length()).trim();
         }

         if (!clean.isEmpty()) {
            String[] parts = clean.split("\\s+");
            String label = parts[0].toLowerCase();
            Command command = this.commandMap.get(label);
            if (command != null) {
               try {
                  command.execute(parts);
               } catch (Throwable var7) {
                  ChatUtils.error("Error executing command '" + label + "': " + var7.getMessage());
               }
            } else {
               ChatUtils.error("Unknown command '" + label + "'. Type " + this.prefix + "help for a list of commands.");
            }
         }
      }
   }
}
