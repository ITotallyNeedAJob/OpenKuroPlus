package kat.respect.b2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public class EntityUtil {
   public static final EntityUtil INSTANCE = new EntityUtil();
   private final Minecraft mc = Minecraft.getInstance();

   public Player getTargetPlayer() {
      if (this.mc.level != null && this.mc.player != null) {
         Player target = null;
         double closestDist = Double.MAX_VALUE;

         for (Player p : this.mc.level.players()) {
            if (p.isAlive() && !p.equals(this.mc.player)) {
               double dist = this.mc.player.distanceToSqr(p);
               if (dist < closestDist) {
                  closestDist = dist;
                  target = p;
               }
            }
         }

         return target;
      } else {
         return null;
      }
   }

   public boolean isUsingShield(Player player) {
      return player == null
         ? false
         : (player.getMainHandItem().getItem() == Items.SHIELD || player.getOffhandItem().getItem() == Items.SHIELD) && player.isUsingItem();
   }

   public boolean isCollidesEntity(BlockPos pos) {
      if (this.mc.level == null) {
         return false;
      } else {
         AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

         for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity) && box.intersects(entity.getBoundingBox())) {
               return true;
            }
         }

         return false;
      }
   }
}
