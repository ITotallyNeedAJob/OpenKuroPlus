package kat.respect.b2.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Deprecated
public final class CustomGetVelocity {
   private CustomGetVelocity() {
   }

   public static void updateAll() {
      VelocityUtil.updateAll();
   }

   public static void updateFor(Entity e) {
      VelocityUtil.updateFor(e);
   }

   public static Vec3 get(Entity e) {
      return VelocityUtil.get(e);
   }

   public static Vec3 getPerSecond(Entity e) {
      return VelocityUtil.getPerSecond(e);
   }

   public static double getHorizontalSpeed(Entity e) {
      return VelocityUtil.getHorizontalSpeed(e);
   }

   public static double getHorizontalSpeedPerSecond(Entity e) {
      return VelocityUtil.getHorizontalSpeedPerSecond(e);
   }

   public static void reset(Entity e) {
      VelocityUtil.reset(e);
   }

   public static void resetAll() {
      VelocityUtil.resetAll();
   }

   public static boolean hasSample(Entity e) {
      return VelocityUtil.hasSample(e);
   }
}
