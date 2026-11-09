package kat.respect.b2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ExplosionUtil {
   private static final Minecraft mc = Minecraft.getInstance();

   private ExplosionUtil() {
   }

   public static float getSeenPercent(Vec3 explosionCenter, AABB targetBox, Entity raycastContext) {
      return getSeenPercent(explosionCenter, targetBox, raycastContext, null);
   }

   public static float getSeenPercent(Vec3 explosionCenter, AABB targetBox, Entity raycastContext, BlockPos ignoredBlockPos) {
      if (explosionCenter != null && targetBox != null && raycastContext != null && mc.level != null) {
         double xStep = 1.0 / (targetBox.getXsize() * 2.0 + 1.0);
         double yStep = 1.0 / (targetBox.getYsize() * 2.0 + 1.0);
         double zStep = 1.0 / (targetBox.getZsize() * 2.0 + 1.0);
         if (!(xStep < 0.0) && !(yStep < 0.0) && !(zStep < 0.0)) {
            double xOffset = (1.0 - Math.floor(1.0 / xStep) * xStep) / 2.0;
            double zOffset = (1.0 - Math.floor(1.0 / zStep) * zStep) / 2.0;
            int clearRays = 0;
            int totalRays = 0;

            for (double x = 0.0; x <= 1.0; x += xStep) {
               for (double y = 0.0; y <= 1.0; y += yStep) {
                  for (double z = 0.0; z <= 1.0; z += zStep) {
                     Vec3 sample = new Vec3(
                        Mth.lerp(x, targetBox.minX, targetBox.maxX) + xOffset,
                        Mth.lerp(y, targetBox.minY, targetBox.maxY),
                        Mth.lerp(z, targetBox.minZ, targetBox.maxZ) + zOffset
                     );
                     if (isRayClear(sample, explosionCenter, raycastContext, ignoredBlockPos)) {
                        clearRays++;
                     }

                     totalRays++;
                  }
               }
            }

            return totalRays == 0 ? 0.0F : (float)clearRays / totalRays;
         } else {
            return 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   private static boolean isRayClear(Vec3 start, Vec3 end, Entity raycastContext, BlockPos ignoredBlockPos) {
      if (ignoredBlockPos == null) {
         BlockHitResult hit = mc.level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, raycastContext));
         return hit.getType() == Type.MISS;
      } else {
         Vec3 current = start;

         for (int i = 0; i < 200; i++) {
            BlockHitResult hit = mc.level.clip(new ClipContext(current, end, Block.COLLIDER, Fluid.NONE, raycastContext));
            if (hit.getType() == Type.MISS) {
               return true;
            }

            if (!hit.getBlockPos().equals(ignoredBlockPos)) {
               return false;
            }

            Vec3 hitPos = hit.getLocation();
            Vec3 dir = end.subtract(current);
            if (dir.lengthSqr() < 1.0E-6) {
               return true;
            }

            current = hitPos.add(dir.normalize().scale(0.01));
            if (current.distanceToSqr(start) >= end.distanceToSqr(start)) {
               return true;
            }
         }

         return false;
      }
   }

   public static float calculateExplosionDamage(LivingEntity target, Vec3 explosionCenter, float power) {
      if (target != null && explosionCenter != null && mc.level != null) {
         double dist = target.position().distanceTo(explosionCenter);
         double maxDist = power * 2.0;
         if (dist > maxDist) {
            return 0.0F;
         } else {
            double impact = 1.0 - dist / maxDist;
            float exposure = getSeenPercent(explosionCenter, target.getBoundingBox(), target);
            impact *= exposure;
            float damage = (float)((impact * impact + impact) / 2.0 * 7.0 * maxDist + 1.0);
            return Math.max(0.0F, damage);
         }
      } else {
         return 0.0F;
      }
   }
}
