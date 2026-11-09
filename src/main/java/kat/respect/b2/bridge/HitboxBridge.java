package kat.respect.b2.bridge;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class HitboxBridge {
   private static final double PLAYER_STANDING_HITBOX_HEIGHT = 1.8;
   private static HitboxBridge.Hook hook;

   public static void register(HitboxBridge.Hook h) {
      hook = h;
   }

   public static AABB expand(Entity entity) {
      AABB vanilla = entity.getBoundingBox().inflate(entity.getPickRadius());
      if (hook != null && hook.isEnabled()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == null) {
            return vanilla;
         } else if (hook.isPlayersOnly() && !(entity instanceof Player)) {
            return vanilla;
         } else if (!hook.isTargetEligible(entity)) {
            return vanilla;
         } else if (mc.player.distanceToSqr(entity) > Math.pow(hook.getRange(), 2.0)) {
            return vanilla;
         } else {
            AABB baseBox = entity.getBoundingBox();
            if (hook.isStaticHitbox() && entity instanceof Player player && player.isFallFlying()) {
               baseBox = new AABB(baseBox.minX, baseBox.minY, baseBox.minZ, baseBox.maxX, baseBox.minY + 1.8, baseBox.maxZ);
            }

            AABB originalBox = baseBox.inflate(entity.getPickRadius());
            if (hook.isUniform()) {
               double expandSize = hook.getExpandSize();
               return originalBox.inflate(expandSize, expandSize, expandSize);
            } else {
               return originalBox.inflate(hook.getExpandX(), hook.getExpandY(), hook.getExpandZ());
            }
         }
      } else {
         return vanilla;
      }
   }

   private HitboxBridge() {
   }

   public interface Hook {
      boolean isEnabled();

      boolean isPlayersOnly();

      boolean isTargetEligible(Entity var1);

      double getRange();

      boolean isStaticHitbox();

      boolean isUniform();

      double getExpandSize();

      double getExpandX();

      double getExpandY();

      double getExpandZ();
   }
}
