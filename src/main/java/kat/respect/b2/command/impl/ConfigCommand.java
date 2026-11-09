package kat.respect.b2.command.impl;

import java.util.List;
import kat.respect.b2.command.Command;
import kat.respect.b2.config.ConfigManager;
import kat.respect.b2.gui.GuiClipboard;
import kat.respect.b2.util.ChatUtil;
import kat.respect.b2.util.NotificationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.CopyToClipboard;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;

public final class ConfigCommand extends Command {
   public ConfigCommand() {
      super(
         "config",
         "Manage and share client configurations",
         ".config <save|load|list|delete|rename|create|export|import|reload|copy|reset|dir> [args]",
         "cfg",
         "c"
      );
   }

   @Override
   public void execute(String[] args) {
      if (args.length < 2) {
         this.showUsage();
      } else {
         ConfigManager manager = ConfigManager.getInstance();
         String sub = args[1].toLowerCase();
         switch (sub) {
            case "save":
               String name = args.length >= 3 ? args[2] : manager.getActiveProfile();
               manager.saveProfile(name);
               manager.saveClient();
               NotificationManager.push("Config", "Saved: " + name, NotificationManager.Type.SUCCESS);
               break;
            case "load":
               if (args.length < 3) {
                  ChatUtil.warn("Usage: .config load <name>");
                  return;
               }

               String nameLoad = args[2];
               if (manager.loadProfile(nameLoad)) {
                  NotificationManager.push("Config", "Loaded: " + nameLoad, NotificationManager.Type.SUCCESS);
               } else {
                  NotificationManager.push("Config", "Not found: " + nameLoad, NotificationManager.Type.ERROR);
               }
               break;
            case "list":
               List<String> configs = manager.listProfiles();
               if (configs.isEmpty()) {
                  ChatUtil.info("No saved profiles yet. Use .config save <name> or .config create <name>.");
                  return;
               }

               ChatUtil.info("Profiles (" + configs.size() + "):");

               for (String config : configs) {
                  boolean active = config.equals(manager.getActiveProfile());
                  ChatUtil.info(Component.literal(" §7- " + config + (active ? " §a(active)" : "")));
               }
               break;
            case "delete":
            case "del":
            case "remove":
               if (args.length < 3) {
                  ChatUtil.warn("Usage: .config delete <name>");
                  return;
               }

               String nameDel = args[2];
               if (manager.deleteProfile(nameDel)) {
                  NotificationManager.push("Config", "Deleted: " + nameDel, NotificationManager.Type.INFO);
               } else {
                  NotificationManager.push("Config", "Not found: " + nameDel, NotificationManager.Type.ERROR);
               }
               break;
            case "rename":
               if (args.length < 4) {
                  ChatUtil.warn("Usage: .config rename <oldName> <newName>");
                  return;
               }

               String oldName = args[2];
               String newName = args[3];
               if (manager.renameProfile(oldName, newName)) {
                  NotificationManager.push("Config", "Renamed: " + oldName + " -> " + newName, NotificationManager.Type.SUCCESS);
               } else {
                  NotificationManager.push("Config", "Rename failed for: " + oldName, NotificationManager.Type.ERROR);
               }
               break;
            case "create":
               if (args.length < 3) {
                  ChatUtil.warn("Usage: .config create <name>");
                  return;
               }

               String nameNew = args[2];
               manager.saveProfile(nameNew);
               manager.setActiveProfile(nameNew);
               manager.saveClient();
               NotificationManager.push("Config", "Created profile: " + nameNew, NotificationManager.Type.SUCCESS);
               break;
            case "share":
            case "export":
               String namex = args.length >= 3 ? args[2] : manager.getActiveProfile();
               String codex = manager.shareSaved(namex);
               if (codex == null) {
                  codex = manager.exportProfile(namex);
               }

               if (codex == null) {
                  NotificationManager.push("Config", "Not found: " + namex, NotificationManager.Type.ERROR);
                  return;
               }

               ChatUtil.info(
                  Component.literal("Export for '" + namex + "': ")
                     .append(this.copyLink("[click to copy share code]", codex, "Click to copy code to clipboard"))
               );
               break;
            case "copy":
               String nameCopy = args.length >= 3 ? args[2] : manager.getActiveProfile();
               String code = manager.shareSaved(nameCopy);
               if (code == null) {
                  code = manager.exportProfile(nameCopy);
               }

               if (code == null) {
                  NotificationManager.push("Config", "Not found: " + nameCopy, NotificationManager.Type.ERROR);
                  return;
               }

               GuiClipboard.set(code);
               NotificationManager.push("Config", "Copied to clipboard (" + code.length() + " chars)", NotificationManager.Type.SUCCESS);
               break;
            case "import":
            case "get":
               if (args.length < 3) {
                  ChatUtil.warn("Usage: .config import <code> [name]");
                  return;
               }

               String payload = args[2];
               String targetName = args.length >= 4 ? args[3] : null;
               String imported = manager.importCode(payload, targetName);
               if (imported == null && manager.importProfile(targetName, payload)) {
                  imported = targetName != null ? targetName : manager.getActiveProfile();
               }

               if (imported == null) {
                  NotificationManager.push("Config", "Invalid profile or code", NotificationManager.Type.ERROR);
                  return;
               }

               String finalImported = imported;
               MutableComponent load = Component.literal("[click to load]")
                  .withStyle(
                     style -> style.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new RunCommand(".config load " + finalImported))
                        .withHoverEvent(new ShowText(Component.literal("Runs .config load " + finalImported)))
                  );
               ChatUtil.info(Component.literal("Imported as '" + finalImported + "' ").append(load));
               break;
            case "reload":
               manager.loadClient();
               manager.loadFriends();
               manager.loadHud();
               if (manager.loadProfile(manager.getActiveProfile())) {
                  NotificationManager.push("Config", "Reloaded active profile: " + manager.getActiveProfile(), NotificationManager.Type.SUCCESS);
               } else {
                  NotificationManager.push("Config", "Reload failed for active profile", NotificationManager.Type.ERROR);
               }
               break;
            case "reset":
               manager.resetAll();
               NotificationManager.push("Config", "All modules reset to defaults", NotificationManager.Type.INFO);
               break;
            case "dir":
            case "folder":
            case "path":
               String path = manager.root().toAbsolutePath().toString();
               ChatUtil.info(Component.literal("Config folder: §7" + path + " ").append(this.copyLink("[copy path]", path, "Click to copy folder path")));
               break;
            case "help":
            case "?":
               this.showUsage();
               break;
            default:
               this.showUsage();
         }
      }
   }

   private void showUsage() {
      ChatUtil.info("Config commands:");
      ChatUtil.info(" §7.config save [name] §f- save current profile");
      ChatUtil.info(" §7.config load <name> §f- load a profile");
      ChatUtil.info(" §7.config list §f- list all profiles");
      ChatUtil.info(" §7.config delete <name> §f- delete a profile");
      ChatUtil.info(" §7.config rename <old> <new> §f- rename a profile");
      ChatUtil.info(" §7.config create <name> §f- create a new profile");
      ChatUtil.info(" §7.config export [name] §f- show export / share code");
      ChatUtil.info(" §7.config copy [name] §f- copy export code to clipboard");
      ChatUtil.info(" §7.config import <code> [name] §f- import a profile code");
      ChatUtil.info(" §7.config reload §f- reload configs from disk");
      ChatUtil.info(" §7.config reset §f- reset all modules to defaults");
      ChatUtil.info(" §7.config dir §f- show config folder path");
   }

   private MutableComponent copyLink(String label, String value, String hover) {
      return Component.literal(label)
         .withStyle(
            style -> style.withColor(ChatFormatting.AQUA).withClickEvent(new CopyToClipboard(value)).withHoverEvent(new ShowText(Component.literal(hover)))
         );
   }
}
