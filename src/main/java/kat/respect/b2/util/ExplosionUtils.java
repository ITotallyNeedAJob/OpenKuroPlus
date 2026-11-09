package kat.respect.b2.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Deprecated
public final class ExplosionUtils {
   private ExplosionUtils() {
   }

   public static float getSeenPercent(Vec3 explosionCenter, AABB targetBox, Entity raycastContext) {
      return ExplosionUtil.getSeenPercent(explosionCenter, targetBox, raycastContext);
   }
}
