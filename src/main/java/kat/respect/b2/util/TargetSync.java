package kat.respect.b2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class TargetSync {
   private static final long EXPIRY_MS = 1500L;
   private static final double MAX_DIST = 10.0;
   private static int syncedId = -1;
   private static long lastSetMs = 0L;

   private TargetSync() {
   }

   public static void set(Entity entity) {
      if (entity == null) {
         clear();
      } else {
         syncedId = entity.getId();
         lastSetMs = System.currentTimeMillis();
      }
   }

   public static Entity get() {
      if (syncedId == -1) {
         return null;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.player != null) {
            if (System.currentTimeMillis() - lastSetMs > 1500L) {
               clear();
               return null;
            } else {
               Entity entity = mc.level.getEntity(syncedId);
               if (entity != null && !entity.isRemoved() && entity.isAlive() && entity != mc.player && !(mc.player.distanceTo(entity) > 10.0)) {
                  return entity;
               } else {
                  clear();
                  return null;
               }
            }
         } else {
            clear();
            return null;
         }
      }
   }

   public static boolean isCurrent(Entity entity) {
      return entity != null && entity.getId() == syncedId;
   }

   public static void clear() {
      syncedId = -1;
      lastSetMs = 0L;
   }
}
