package kat.respect.b2.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Command {
   private final String name;
   private final String description;
   private final String syntax;
   private final List<String> aliases;

   protected Command(String name, String description, String syntax, String... aliases) {
      this.name = name;
      this.description = description;
      this.syntax = syntax;
      this.aliases = aliases.length == 0 ? Collections.emptyList() : Arrays.asList(aliases);
   }

   public abstract void execute(String[] var1);

   public List<String> complete(String[] args) {
      return Collections.emptyList();
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getSyntax() {
      return this.syntax;
   }

   public List<String> getAliases() {
      return this.aliases;
   }

   public boolean matches(String label) {
      if (this.name.equalsIgnoreCase(label)) {
         return true;
      } else {
         for (String alias : this.aliases) {
            if (alias.equalsIgnoreCase(label)) {
               return true;
            }
         }

         return false;
      }
   }
}
