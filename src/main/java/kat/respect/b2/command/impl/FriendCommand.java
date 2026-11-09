package kat.respect.b2.command.impl;

import java.util.ArrayList;
import java.util.List;
import kat.respect.b2.command.Command;
import kat.respect.b2.config.ConfigManager;
import kat.respect.b2.friend.FriendManager;
import kat.respect.b2.util.ChatUtils;
import kat.respect.b2.util.NotificationManager;

public final class FriendCommand extends Command {
   public FriendCommand() {
      super("friend", "Manage the friend list", ".friend <add|remove|list|clear> [name]", "friends");
   }

   @Override
   public void execute(String[] args) {
      if (args.length < 2) {
         ChatUtils.warn("Usage: " + this.getSyntax());
      } else {
         String sub = args[1].toLowerCase();
         switch (sub) {
            case "add":
               if (args.length < 3) {
                  ChatUtils.warn("Usage: .friend add <name>");
                  return;
               }

               String name = args[2];
               FriendManager.INSTANCE.addFriend(name);
               ConfigManager.getInstance().saveGui();
               NotificationManager.push("Friends", "Added " + name, NotificationManager.Type.SUCCESS);
               break;
            case "remove":
            case "del":
            case "delete":
               if (args.length < 3) {
                  ChatUtils.warn("Usage: .friend remove <name>");
                  return;
               }

               String nameDel = args[2];
               FriendManager.INSTANCE.removeFriend(nameDel);
               ConfigManager.getInstance().saveGui();
               NotificationManager.push("Friends", "Removed " + nameDel, NotificationManager.Type.INFO);
               break;
            case "list":
               List<String> friends = new ArrayList<>(FriendManager.INSTANCE.getFriends());
               if (friends.isEmpty()) {
                  ChatUtils.info("Friend list is empty.");
               } else {
                  friends.sort(String.CASE_INSENSITIVE_ORDER);
                  ChatUtils.info("Friends (" + friends.size() + "): " + String.join(", ", friends));
               }
               break;
            case "clear":
               FriendManager.INSTANCE.clear();
               ConfigManager.getInstance().saveGui();
               NotificationManager.push("Friends", "Friend list cleared", NotificationManager.Type.WARNING);
               break;
            default:
               ChatUtils.warn("Usage: " + this.getSyntax());
         }
      }
   }
}
