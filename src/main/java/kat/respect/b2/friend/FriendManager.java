package kat.respect.b2.friend;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import kat.respect.b2.config.ConfigManager;

public class FriendManager {
   public static final FriendManager INSTANCE = new FriendManager();
   private final Set<String> friends = ConcurrentHashMap.newKeySet();

   private FriendManager() {
   }

   public void addFriend(String name) {
      if (name != null && !name.trim().isEmpty()) {
         boolean changed = this.friends.add(name.toLowerCase().trim());
         if (changed) {
            this.notifyDirty();
         }
      }
   }

   public void removeFriend(String name) {
      if (name != null) {
         boolean changed = this.friends.remove(name.toLowerCase().trim());
         if (changed) {
            this.notifyDirty();
         }
      }
   }

   public boolean isFriend(String name) {
      return name == null ? false : this.friends.contains(name.toLowerCase().trim());
   }

   public boolean toggleFriend(String name) {
      if (this.isFriend(name)) {
         this.removeFriend(name);
         return false;
      } else {
         this.addFriend(name);
         return true;
      }
   }

   public void clear() {
      if (!this.friends.isEmpty()) {
         this.friends.clear();
         this.notifyDirty();
      }
   }

   public Set<String> getFriends() {
      return this.friends;
   }

   private void notifyDirty() {
      try {
         ConfigManager.getInstance().markDirty();
      } catch (Throwable var2) {
      }
   }
}
