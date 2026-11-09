package kat.respect.b2.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@Deprecated
public final class TrajectoryLaunchUtils {
   private TrajectoryLaunchUtils() {
   }

   public static Vec3 getProjectileSpawnPos(Player player, float partialTick) {
      return LaunchUtil.getProjectileSpawnPos(player, partialTick);
   }

   public static Vec3 getLaunchDirection(Player player, float partialTick) {
      return LaunchUtil.getLaunchDirection(player, partialTick);
   }

   public static Vec3 getLaunchDirection(Player player, float pitchOffset, float partialTick) {
      return LaunchUtil.getLaunchDirection(player, pitchOffset, partialTick);
   }

   public static Vec3 getInheritedVelocity(Player player) {
      return LaunchUtil.getInheritedVelocity(player);
   }
}
