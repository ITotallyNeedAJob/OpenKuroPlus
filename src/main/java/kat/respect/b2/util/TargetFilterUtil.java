package kat.respect.b2.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class TargetFilterUtil {
   private static final Minecraft mc = Minecraft.getInstance();

   private TargetFilterUtil() {
   }

   public static boolean isValidTarget(LivingEntity entity, TargetFilterUtil.TargetQuery query) {
      if (entity != null && mc.player != null) {
         LocalPlayer player = mc.player;
         if (entity != player && entity != mc.getCameraEntity()) {
            if (!entity.isAlive() || entity.getHealth() <= 0.0F) {
               return false;
            } else if (player.distanceTo(entity) > query.range) {
               return false;
            } else if (query.requireLineOfSight && !player.hasLineOfSight(entity)) {
               return false;
            } else if (query.webCheck && TargetUtil.inWeb(entity)) {
               return false;
            } else if (query.obstruction != TargetUtil.Obstruction.OFF && TargetUtil.isObstructed(player, entity, query.obstruction, query.range)) {
               return false;
            } else {
               if (query.fov < 360.0) {
                  double diffX = entity.getX() - player.getX();
                  double diffZ = entity.getZ() - player.getZ();
                  double diffY = entity.getY() + entity.getBbHeight() / 2.0 - player.getEyePosition().y;
                  double distXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
                  float yawToTarget = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
                  float pitchToTarget = (float)(-Math.toDegrees(Math.atan2(diffY, distXZ)));
                  float yawDiff = Math.abs(RotationHandler.getAngleDifference(yawToTarget, player.getYRot()));
                  float pitchDiff = Math.abs(pitchToTarget - player.getXRot());
                  if (Math.hypot(yawDiff, pitchDiff) > query.fov / 2.0) {
                     return false;
                  }
               }

               Module filter = ModuleManager.INSTANCE != null ? ModuleManager.INSTANCE.getModuleById("combat.targetfilter") : null;
               if (filter != null && filter.isEnabled() && filter instanceof TargetFilterProvider provider) {
                  return provider.allowTarget(entity);
               } else if (entity instanceof Player p && p.isSpectator()) {
                  return false;
               } else {
                  return entity instanceof Player && !query.targetPlayers ? false : entity instanceof Player || query.targetMobs;
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static List<LivingEntity> listTargets(TargetFilterUtil.TargetQuery query) {
      List<LivingEntity> result = new ArrayList<>();
      if (mc.level != null && mc.player != null) {
         for (LivingEntity entity : TargetCache.living()) {
            if (isValidTarget(entity, query)) {
               result.add(entity);
            }
         }
         Comparator<LivingEntity> comparator = switch (query.priority) {
            case DISTANCE -> Comparator.comparingDouble(e -> mc.player.distanceToSqr(e));
            case LOWEST_HP -> Comparator.comparingDouble(LivingEntity::getHealth);
            case HIGHEST_HP -> Comparator.comparingDouble(LivingEntity::getHealth).reversed();
         };
         result.sort(comparator);
         return result;
      } else {
         return result;
      }
   }

   public static LivingEntity findOptimalTarget(TargetFilterUtil.TargetQuery query) {
      if (query.sameTarget && TargetSync.get() instanceof LivingEntity living && isValidTarget(living, query)) {
         TargetSync.set(living);
         return living;
      } else {
         List<LivingEntity> targets = listTargets(query);
         if (targets.isEmpty()) {
            return null;
         } else {
            LivingEntity best = targets.get(0);
            TargetSync.set(best);
            return best;
         }
      }
   }

   public static enum Priority {
      DISTANCE,
      LOWEST_HP,
      HIGHEST_HP;
   }

   public static final class TargetQuery {
      public double range = 3.0;
      public double fov = 360.0;
      public boolean requireLineOfSight = true;
      public boolean targetPlayers = true;
      public boolean targetMobs = true;
      public boolean webCheck = true;
      public TargetUtil.Obstruction obstruction = TargetUtil.Obstruction.CRYSTALS;
      public boolean sameTarget = false;
      public TargetFilterUtil.Priority priority = TargetFilterUtil.Priority.DISTANCE;

      public static TargetFilterUtil.TargetQuery create() {
         return new TargetFilterUtil.TargetQuery();
      }

      public TargetFilterUtil.TargetQuery range(double range) {
         this.range = range;
         return this;
      }

      public TargetFilterUtil.TargetQuery fov(double fov) {
         this.fov = fov;
         return this;
      }

      public TargetFilterUtil.TargetQuery requireLineOfSight(boolean requireLineOfSight) {
         this.requireLineOfSight = requireLineOfSight;
         return this;
      }

      public TargetFilterUtil.TargetQuery targetPlayers(boolean targetPlayers) {
         this.targetPlayers = targetPlayers;
         return this;
      }

      public TargetFilterUtil.TargetQuery targetMobs(boolean targetMobs) {
         this.targetMobs = targetMobs;
         return this;
      }

      public TargetFilterUtil.TargetQuery webCheck(boolean webCheck) {
         this.webCheck = webCheck;
         return this;
      }

      public TargetFilterUtil.TargetQuery obstruction(TargetUtil.Obstruction obstruction) {
         this.obstruction = obstruction;
         return this;
      }

      public TargetFilterUtil.TargetQuery sameTarget(boolean sameTarget) {
         this.sameTarget = sameTarget;
         return this;
      }

      public TargetFilterUtil.TargetQuery priority(TargetFilterUtil.Priority priority) {
         this.priority = priority;
         return this;
      }
   }
}
