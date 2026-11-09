package kat.respect.b2.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class LaunchUtil {
   private static final double PROJECTILE_SPAWN_Y_OFFSET = 0.1;

   private LaunchUtil() {
   }

   public static Vec3 getProjectileSpawnPos(Player player, float partialTick) {
      double x = Mth.lerp((double)partialTick, player.xo, player.getX());
      double y = Mth.lerp((double)partialTick, player.yo, player.getY()) + player.getEyeHeight() - 0.1;
      double z = Mth.lerp((double)partialTick, player.zo, player.getZ());
      return new Vec3(x, y, z);
   }

   public static Vec3 getLaunchDirection(Player player, float partialTick) {
      return Vec3.directionFromRotation(player.getViewXRot(partialTick), player.getViewYRot(partialTick)).normalize();
   }

   public static Vec3 getLaunchDirection(Player player, float pitchOffset, float partialTick) {
      return Vec3.directionFromRotation(player.getViewXRot(partialTick) + pitchOffset, player.getViewYRot(partialTick)).normalize();
   }

   public static Vec3 getInheritedVelocity(Player player) {
      Vec3 movement = player.getDeltaMovement();
      return player.onGround() ? new Vec3(movement.x, 0.0, movement.z) : movement;
   }
}
