package kat.respect.b2.util;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import kat.respect.b2.module.Module;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class RotationUtils {
   private static final Minecraft mc = Minecraft.getInstance();
   private static float[] lastRotations = new float[]{0.0F, 0.0F};
   private static float[] lastSentRotations = new float[]{0.0F, 0.0F};
   private static double windYawVel = 0.0;
   private static double windPitchVel = 0.0;
   private static double windYaw = 0.0;
   private static double windPitch = 0.0;
   private static float gcdYawRemainder = 0.0F;
   private static float gcdPitchRemainder = 0.0F;
   private static long lastNanoTime = 0L;
   private static boolean isTracking = false;
   private static LivingEntity currentTarget = null;
   private static Module currentController = null;
   private static RotationUtils.Priority currentPriority = RotationUtils.Priority.LOWEST;
   private static LivingEntity stickyAltTarget = null;
   private static Vec3 stickyAltOffset = null;

   private RotationUtils() {
   }

   private static float getDeltaSeconds() {
      long now = System.nanoTime();
      float delta;
      if (lastNanoTime == 0L) {
         delta = 0.016666668F;
      } else {
         delta = (float)(now - lastNanoTime) / 1.0E9F;
      }

      lastNanoTime = now;
      return Math.min(delta, 0.1F);
   }

   public static void resetState() {
      if (mc.player != null) {
         lastRotations[0] = mc.player.getYRot();
         lastRotations[1] = mc.player.getXRot();
         lastSentRotations[0] = mc.player.getYRot();
         lastSentRotations[1] = mc.player.getXRot();
         ServerRotationHandler.syncToPlayer(mc.player.getYRot(), mc.player.getXRot());
      } else {
         lastRotations = new float[]{0.0F, 0.0F};
         lastSentRotations = new float[]{0.0F, 0.0F};
         ServerRotationHandler.reset();
      }

      windYawVel = 0.0;
      windPitchVel = 0.0;
      windYaw = 0.0;
      windPitch = 0.0;
      gcdYawRemainder = 0.0F;
      gcdPitchRemainder = 0.0F;
      lastNanoTime = 0L;
      isTracking = false;
      currentTarget = null;
      currentController = null;
      currentPriority = RotationUtils.Priority.LOWEST;
      clearStickyAlt();
   }

   public static void initFromPlayer() {
      if (mc.player != null) {
         lastRotations[0] = mc.player.getYRot();
         lastRotations[1] = mc.player.getXRot();
         lastSentRotations[0] = mc.player.getYRot();
         lastSentRotations[1] = mc.player.getXRot();
         gcdYawRemainder = 0.0F;
         gcdPitchRemainder = 0.0F;
         ServerRotationHandler.updateServerRotations(mc.player.getYRot(), mc.player.getXRot());
      }
   }

   public static void stopTracking() {
      if (mc.player != null) {
         ServerRotationHandler.syncToPlayer(mc.player.getYRot(), mc.player.getXRot());
         lastSentRotations[0] = mc.player.getYRot();
         lastSentRotations[1] = mc.player.getXRot();
      } else {
         ServerRotationHandler.reset();
      }

      lastNanoTime = 0L;
      isTracking = false;
      currentTarget = null;
      currentController = null;
      currentPriority = RotationUtils.Priority.LOWEST;
      clearStickyAlt();
   }

   public static boolean isTracking() {
      return isTracking;
   }

   public static LivingEntity getCurrentTarget() {
      return currentTarget;
   }

   public static float[] getLastRotations() {
      return (float[])lastRotations.clone();
   }

   public static boolean rotateTo(
      LivingEntity target,
      float yawSpeed,
      float pitchSpeed,
      boolean silent,
      RotationUtils.AimType aimType,
      RotationUtils.EntityPoints pointsMode,
      float multipoint
   ) {
      return rotateTo(target, yawSpeed, pitchSpeed, silent, aimType, pointsMode, multipoint, false, false, null, RotationUtils.Priority.NORMAL);
   }

   public static boolean rotateTo(
      LivingEntity target,
      float yawSpeed,
      float pitchSpeed,
      boolean silent,
      RotationUtils.AimType aimType,
      RotationUtils.EntityPoints pointsMode,
      float multipoint,
      boolean useRandom,
      Module requester,
      RotationUtils.Priority priority
   ) {
      return rotateTo(target, yawSpeed, pitchSpeed, silent, aimType, pointsMode, multipoint, useRandom, false, requester, priority);
   }

   public static boolean rotateTo(
      LivingEntity target,
      float yawSpeed,
      float pitchSpeed,
      boolean silent,
      RotationUtils.AimType aimType,
      RotationUtils.EntityPoints pointsMode,
      float multipoint,
      boolean useRandom,
      boolean throughWalls,
      Module requester,
      RotationUtils.Priority priority
   ) {
      if (target == null || mc.player == null || mc.level == null) {
         return false;
      } else if (requester != null && !tryTakeControl(requester, priority)) {
         return false;
      } else {
         float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         float shrink = multipoint * 0.01F;
         Optional<Vec3> opt = getPossiblePoints(target, shrink, pointsMode, useRandom, throughWalls, pt);
         if (opt.isEmpty()) {
            return false;
         } else {
            if (currentTarget != target) {
               currentTarget = target;
               windYawVel = 0.0;
               windPitchVel = 0.0;
               windYaw = 0.0;
               windPitch = 0.0;
               if (!isTracking) {
                  initFromPlayer();
               }
            } else if (!isTracking) {
               initFromPlayer();
            }

            isTracking = true;
            float[] targetRot = calculate(mc.player.getEyePosition(pt), opt.get());
            float[] baseRotation = silent ? lastRotations : new float[]{mc.player.getYRot(), mc.player.getXRot()};
            targetRot = ensureContinuity(baseRotation, targetRot);
            lastRotations = getFixedRotations(lastRotations, getTargetRotations(lastRotations, targetRot, yawSpeed, pitchSpeed, silent, aimType));
            applyRotations(lastRotations, silent);
            return true;
         }
      }
   }

   public static boolean rotateTo(Vec3 position, float yawSpeed, float pitchSpeed, boolean silent, RotationUtils.AimType aimType) {
      return rotateTo(position, yawSpeed, pitchSpeed, silent, aimType, null, RotationUtils.Priority.NORMAL);
   }

   public static boolean rotateTo(
      Vec3 position, float yawSpeed, float pitchSpeed, boolean silent, RotationUtils.AimType aimType, Module requester, RotationUtils.Priority priority
   ) {
      if (position == null || mc.player == null) {
         return false;
      } else if (requester != null && !tryTakeControl(requester, priority)) {
         return false;
      } else {
         currentTarget = null;
         windYawVel = 0.0;
         windPitchVel = 0.0;
         windYaw = 0.0;
         windPitch = 0.0;
         if (!isTracking) {
            initFromPlayer();
         }

         isTracking = true;
         float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         float[] targetRot = calculate(mc.player.getEyePosition(pt), position);
         float[] baseRotation = silent ? lastRotations : new float[]{mc.player.getYRot(), mc.player.getXRot()};
         targetRot = ensureContinuity(baseRotation, targetRot);
         lastRotations = getFixedRotations(lastRotations, getTargetRotations(lastRotations, targetRot, yawSpeed, pitchSpeed, silent, aimType));
         applyRotations(lastRotations, silent);
         return true;
      }
   }

   public static boolean rotateBack(boolean silent) {
      return rotateBack(silent, 150.0F, 150.0F, RotationUtils.AimType.REGULAR);
   }

   public static boolean rotateBack(boolean silent, float yawSpeed, float pitchSpeed, RotationUtils.AimType aimType) {
      return rotateBack(silent, yawSpeed, pitchSpeed, aimType, true);
   }

   public static boolean rotateBack(boolean silent, float yawSpeed, float pitchSpeed, RotationUtils.AimType aimType, boolean scaleSpeed) {
      if (mc.player == null) {
         isTracking = false;
         currentTarget = null;
         currentController = null;
         currentPriority = RotationUtils.Priority.LOWEST;
         return false;
      } else if (!isTracking) {
         ServerRotationHandler.syncToPlayer(mc.player.getYRot(), mc.player.getXRot());
         lastSentRotations[0] = mc.player.getYRot();
         lastSentRotations[1] = mc.player.getXRot();
         currentTarget = null;
         currentController = null;
         currentPriority = RotationUtils.Priority.LOWEST;
         return false;
      } else {
         float[] clientRot = new float[]{mc.player.getYRot(), mc.player.getXRot()};
         if (aimType == RotationUtils.AimType.BLATANT) {
            ServerRotationHandler.syncToPlayer(mc.player.getYRot(), mc.player.getXRot());
            lastSentRotations[0] = mc.player.getYRot();
            lastSentRotations[1] = mc.player.getXRot();
            lastRotations[0] = mc.player.getYRot();
            lastRotations[1] = mc.player.getXRot();
            isTracking = false;
            currentTarget = null;
            currentController = null;
            currentPriority = RotationUtils.Priority.LOWEST;
            return false;
         } else {
            float delta = getDeltaSeconds();
            float speedMultiplier = scaleSpeed ? (silent ? 2.5F : 0.5F) : 1.0F;
            lastRotations[0] = lerpAngle(lastRotations[0], clientRot[0], delta, yawSpeed * speedMultiplier);
            lastRotations[1] = lerpAngle(lastRotations[1], clientRot[1], delta, pitchSpeed * speedMultiplier);
            float yawDiff = Math.abs(Mth.wrapDegrees(lastRotations[0] - clientRot[0]));
            float pitchDiff = Math.abs(lastRotations[1] - clientRot[1]);
            if (yawDiff < 1.0F && pitchDiff < 1.0F) {
               ServerRotationHandler.syncToPlayer(mc.player.getYRot(), mc.player.getXRot());
               lastSentRotations[0] = mc.player.getYRot();
               lastSentRotations[1] = mc.player.getXRot();
               isTracking = false;
               currentTarget = null;
               currentController = null;
               currentPriority = RotationUtils.Priority.LOWEST;
               return false;
            } else {
               if (silent) {
                  applySilentRotations(lastRotations);
               }

               return true;
            }
         }
      }
   }

   public static void syncToClientAndRun(
      Module requester, RotationUtils.Priority priority, RotationUtils.AimType aimType, float yawSpeed, float pitchSpeed, Runnable action
   ) {
      if (mc.player == null || mc.level == null) {
         action.run();
      } else if (!ServerRotationHandler.isHasSilentRotation()) {
         action.run();
      } else {
         boolean tookControl = requester == null || tryTakeControl(requester, priority);
         if (!tookControl) {
            action.run();
         } else {
            if (!isTracking) {
               initFromPlayer();
            }

            isTracking = true;
            currentTarget = null;
            float[] clientRot = new float[]{mc.player.getYRot(), mc.player.getXRot()};
            if (aimType == RotationUtils.AimType.BLATANT) {
               lastRotations[0] = clientRot[0];
               lastRotations[1] = clientRot[1];
               applySilentRotations(lastRotations);
               action.run();
            } else {
               float delta = 1.0F / Math.max(1.0F, (float)mc.getFps());

               float yawDiff;
               float pitchDiff;
               do {
                  lastRotations[0] = lerpAngle(lastRotations[0], clientRot[0], delta, yawSpeed);
                  lastRotations[1] = lerpAngle(lastRotations[1], clientRot[1], delta, pitchSpeed);
                  applySilentRotations(lastRotations);
                  yawDiff = Math.abs(Mth.wrapDegrees(lastRotations[0] - clientRot[0]));
                  pitchDiff = Math.abs(lastRotations[1] - clientRot[1]);
               } while (!(yawDiff < 1.0F) || !(pitchDiff < 1.0F));

               action.run();
            }

            releaseControl(requester);
            if (currentController == null && mc.player != null) {
               ServerRotationHandler.syncToPlayer(mc.player.getYRot(), mc.player.getXRot());
               lastSentRotations[0] = mc.player.getYRot();
               lastSentRotations[1] = mc.player.getXRot();
            }
         }
      }
   }

   public static void applySilentRotations(float[] rotations) {
      float yawDiff = Math.abs(Mth.wrapDegrees(rotations[0] - lastSentRotations[0]));
      float enforcedPitch = Mth.clamp(rotations[1], -90.0F, 90.0F);
      float pitchDiff = Math.abs(enforcedPitch - lastSentRotations[1]);
      if (!(yawDiff <= 0.01F) || !(pitchDiff <= 0.01F)) {
         float normalizedYaw = lastSentRotations[0] + Mth.wrapDegrees(rotations[0] - lastSentRotations[0]);
         float normalizedPitch = Mth.clamp(enforcedPitch, -90.0F, 90.0F);
         ServerRotationHandler.setServerRotationsWithDelta(normalizedYaw, normalizedPitch);
         ServerRotationHandler.setHasSilentRotation(true);
         lastSentRotations[0] = normalizedYaw;
         lastSentRotations[1] = normalizedPitch;
      }
   }

   public static void forceSilentRotations(float yaw, float pitch) {
      if (mc.player != null) {
         float baseYaw = ServerRotationHandler.isHasSilentRotation() ? ServerRotationHandler.getServerYaw() : mc.player.getYRot();
         float basePitch = ServerRotationHandler.isHasSilentRotation() ? ServerRotationHandler.getServerPitch() : mc.player.getXRot();
         lastRotations[0] = baseYaw;
         lastRotations[1] = basePitch;
         lastSentRotations[0] = baseYaw;
         lastSentRotations[1] = basePitch;
         float[] targetRotations = ensureContinuity(new float[]{baseYaw, basePitch}, new float[]{yaw, pitch});
         applySilentRotations(targetRotations);
         lastRotations[0] = targetRotations[0];
         lastRotations[1] = targetRotations[1];
      }
   }

   public static void applyClientRotations(float[] rotations) {
      if (mc.player != null) {
         PlayerMouseRotationApplier.setLocalPlayerRotation(mc.player, rotations[0], rotations[1]);
         ServerRotationHandler.reset();
      }
   }

   private static void applyRotations(float[] rotations, boolean silent) {
      if (silent) {
         applySilentRotations(rotations);
      } else {
         applyClientRotations(rotations);
      }
   }

   public static float[] calculate(Vec3 from, Vec3 to) {
      Vec3 diff = to.subtract(from);
      double distance = Math.hypot(diff.x, diff.z);
      float yaw = Mth.wrapDegrees((float)Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F);
      float pitch = Mth.wrapDegrees((float)(-Math.toDegrees(Math.atan2(diff.y, distance))));
      return new float[]{yaw, pitch};
   }

   public static Vec3 predictAimPoint(LivingEntity entity, int ticks, RotationUtils.EntityPoints mode) {
      if (entity != null && ticks > 0) {
         Vec3 offset = getEntityPoint(entity, mode, 1.0F).subtract(entity.position());
         return entity.position().add(CustomGetVelocity.get(entity).scale(ticks)).add(offset);
      } else {
         return getEntityPoint(entity, mode, 1.0F);
      }
   }

   public static AABB getInterpolatedAABB(Entity entity, float partialTick) {
      Vec3 pos = entity.getPosition(partialTick);
      return entity.getDimensions(entity.getPose()).makeBoundingBox(pos);
   }

   public static Vec3 closestPointToBox(AABB box, float partialTick) {
      if (mc.player == null) {
         return box.getCenter();
      } else {
         Vec3 eye = mc.player.getEyePosition(partialTick);
         return clampToBox(eye, box);
      }
   }

   private static Vec3 clampToBox(Vec3 point, AABB box) {
      double cx = Math.min(Math.max(point.x, box.minX), box.maxX);
      double cy = Math.min(Math.max(point.y, box.minY), box.maxY);
      double cz = Math.min(Math.max(point.z, box.minZ), box.maxZ);
      return new Vec3(cx, cy, cz);
   }

   public static Vec3 closestPointToBoxFromRay(AABB box, Vec3 rayOrigin, Vec3 rayDirection) {
      if (box != null && rayOrigin != null && rayDirection != null) {
         Vec3 toBox = box.getCenter().subtract(rayOrigin);
         double t = Math.max(0.0, toBox.dot(rayDirection));
         Vec3 rayPoint = rayOrigin.add(rayDirection.scale(t));
         double cx = Math.min(Math.max(rayPoint.x, box.minX), box.maxX);
         double cy = Math.min(Math.max(rayPoint.y, box.minY), box.maxY);
         double cz = Math.min(Math.max(rayPoint.z, box.minZ), box.maxZ);
         double minY = box.minY + 0.51;
         if (cy < minY) {
            cy = minY;
         }

         return new Vec3(cx, cy, cz);
      } else {
         return box != null ? box.getCenter() : Vec3.ZERO;
      }
   }

   public static Vec3 randomPointInBox(AABB box) {
      double rx = box.minX + ThreadLocalRandom.current().nextDouble() * (box.maxX - box.minX);
      double ry = box.minY + ThreadLocalRandom.current().nextDouble() * (box.maxY - box.minY);
      double rz = box.minZ + ThreadLocalRandom.current().nextDouble() * (box.maxZ - box.minZ);
      return new Vec3(rx, ry, rz);
   }

   public static Vec3 getEntityPoint(LivingEntity entity, RotationUtils.EntityPoints mode, float partialTick) {
      if (entity == null) {
         return Vec3.ZERO;
      } else {
         AABB box = getInterpolatedAABB(entity, partialTick);
         Vec3 center = box.getCenter();

         return switch (mode) {
            case STRAIGHT -> new Vec3(center.x, box.maxY - 0.1F, center.z);
            case CLOSEST -> closestPointToBox(box, partialTick);
            case RANDOM -> randomPointInBox(box);
            case CENTER -> center;
            case FEET -> new Vec3(center.x, box.minY + 0.1, center.z);
         };
      }
   }

   public static Optional<Vec3> getPossiblePoints(
      LivingEntity entity, float shrink, RotationUtils.EntityPoints entityPoints, boolean useRandom, boolean throughWalls, float partialTick
   ) {
      if (entity != null && mc.player != null && mc.level != null) {
         AABB bBox = getInterpolatedAABB(entity, partialTick);
         Vec3 center = bBox.getCenter();
         Vec3 candidate = getEntityPoint(entity, entityPoints, partialTick);
         if (useRandom) {
            candidate = blendWithRandom(candidate, bBox, entityPoints);
         }

         Vec3 finalCandidate = center.lerp(candidate, shrink);
         if (isPointVisible(finalCandidate, partialTick)) {
            clearStickyAlt();
            return Optional.of(finalCandidate);
         } else {
            if (stickyAltTarget == entity && stickyAltOffset != null) {
               Vec3 sticky = new Vec3(bBox.minX + stickyAltOffset.x, bBox.minY + stickyAltOffset.y, bBox.minZ + stickyAltOffset.z);
               if (isInsideBox(sticky, bBox) && isPointVisible(sticky, partialTick)) {
                  return Optional.of(sticky);
               }
            }

            Vec3 visibleAlt = findVisiblePointInBox(bBox, finalCandidate, partialTick);
            if (visibleAlt != null) {
               stickyAltTarget = entity;
               stickyAltOffset = new Vec3(visibleAlt.x - bBox.minX, visibleAlt.y - bBox.minY, visibleAlt.z - bBox.minZ);
               return Optional.of(visibleAlt);
            } else {
               clearStickyAlt();
               return throughWalls ? Optional.of(finalCandidate) : Optional.empty();
            }
         }
      } else {
         return Optional.empty();
      }
   }

   private static void clearStickyAlt() {
      stickyAltTarget = null;
      stickyAltOffset = null;
   }

   private static boolean isInsideBox(Vec3 p, AABB box) {
      return p.x >= box.minX && p.x <= box.maxX && p.y >= box.minY && p.y <= box.maxY && p.z >= box.minZ && p.z <= box.maxZ;
   }

   private static boolean isPointVisible(Vec3 point, float partialTick) {
      HitResult r = rayTraceTo(point, partialTick);
      return r == null || r.getType() == Type.MISS || r.getLocation().distanceToSqr(point) < 1.0E-6;
   }

   private static Vec3 findVisiblePointInBox(AABB box, Vec3 preferred, float partialTick) {
      if (mc.player != null && mc.level != null) {
         int xSteps = 3;
         int ySteps = 5;
         int zSteps = 3;
         double width = box.maxX - box.minX;
         double height = box.maxY - box.minY;
         double depth = box.maxZ - box.minZ;
         Vec3 best = null;
         double bestDistSq = Double.MAX_VALUE;

         for (int xi = 0; xi < 3; xi++) {
            double x = box.minX + xi / 2.0 * width;

            for (int yi = 0; yi < 5; yi++) {
               double y = box.minY + yi / 4.0 * height;

               for (int zi = 0; zi < 3; zi++) {
                  double z = box.minZ + zi / 2.0 * depth;
                  Vec3 sample = new Vec3(x, y, z);
                  if (isPointVisible(sample, partialTick)) {
                     double distSq = sample.distanceToSqr(preferred);
                     if (distSq < bestDistSq) {
                        bestDistSq = distSq;
                        best = sample;
                     }
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private static Vec3 blendWithRandom(Vec3 candidate, AABB bBox, RotationUtils.EntityPoints entityPoints) {
      Vec3 randomPoint;
      if (entityPoints == RotationUtils.EntityPoints.CLOSEST) {
         double regionWidth = (bBox.maxX - bBox.minX) * 0.9;
         double regionHeight = (bBox.maxY - bBox.minY) * 0.9;
         double regionDepth = (bBox.maxZ - bBox.minZ) * 0.9;
         double rx = candidate.x + (ThreadLocalRandom.current().nextDouble() - 0.5) * regionWidth;
         double ry = candidate.y + (ThreadLocalRandom.current().nextDouble() - 0.5) * regionHeight;
         double rz = candidate.z + (ThreadLocalRandom.current().nextDouble() - 0.5) * regionDepth;
         rx = Math.min(Math.max(rx, bBox.minX), bBox.maxX);
         ry = Math.min(Math.max(ry, bBox.minY), bBox.maxY);
         rz = Math.min(Math.max(rz, bBox.minZ), bBox.maxZ);
         randomPoint = new Vec3(rx, ry, rz);
      } else {
         randomPoint = randomPointInBox(bBox);
      }

      return candidate.lerp(randomPoint, 0.5);
   }

   public static HitResult rayTraceTo(Vec3 end, float partialTick) {
      if (mc.player != null && mc.level != null) {
         Vec3 start = mc.player.getEyePosition(partialTick);
         return mc.level.clip(new ClipContext(start, end, Block.OUTLINE, Fluid.NONE, mc.player));
      } else {
         return null;
      }
   }

   public static float lerpAngle(float current, float target, float deltaSeconds, float speed) {
      float diff = Mth.wrapDegrees(target - current);
      float step = speed * deltaSeconds;
      return Math.abs(diff) <= step ? current + diff : current + Math.signum(diff) * step;
   }

   public static float smoothLerpAngle(float current, float target, float deltaSeconds, float speed) {
      float diff = Mth.wrapDegrees(target - current);
      float factor = 1.0F - (float)Math.exp(-Math.max(0.0F, speed) * deltaSeconds * 0.01F);
      return current + diff * factor;
   }

   public static float[] getTargetRotations(float[] lastRot, float[] targetRot, float yawSpeed, float pitchSpeed, boolean silent, RotationUtils.AimType aimType) {
      if (mc.player != null && mc.level != null) {
         float yaw = silent ? lastRot[0] : mc.player.getYRot();
         float pitch = silent ? lastRot[1] : mc.player.getXRot();
         float delta = getDeltaSeconds();
         float speedMultiplier = silent ? 2.5F : 0.5F;

         return switch (aimType) {
            case REGULAR -> new float[]{
               lerpAngle(yaw, targetRot[0], delta, yawSpeed * speedMultiplier), lerpAngle(pitch, targetRot[1], delta, pitchSpeed * speedMultiplier)
            };
            case BLATANT -> new float[]{targetRot[0], targetRot[1]};
            case WINDMOUSE -> getWindMouseRotations(new float[]{yaw, pitch}, targetRot, yawSpeed, pitchSpeed, delta, silent);
         };
      } else {
         return lastRot;
      }
   }

   private static float[] getWindMouseRotations(float[] currentRot, float[] targetRot, float yawSpeed, float pitchSpeed, float delta, boolean silent) {
      double currentYaw = currentRot[0];
      double currentPitch = currentRot[1];
      double targetYaw = targetRot[0];
      double targetPitch = targetRot[1];
      boolean yawEnabled = yawSpeed > 0.0F;
      boolean pitchEnabled = pitchSpeed > 0.0F;
      double yawScale = yawSpeed * (silent ? 2.5 : 0.5);
      double pitchScale = pitchSpeed * (silent ? 2.5 : 0.5);
      double M0Yaw = yawScale * delta;
      double M0Pitch = pitchScale * delta;
      double baseM0 = Math.max(M0Yaw, M0Pitch);
      double G_0 = baseM0 * 0.6;
      double W_0 = baseM0 * 0.2;
      double D_0 = 8.0;
      double diffYaw = Mth.wrapDegrees(targetYaw - currentYaw);
      double diffPitch = targetPitch - currentPitch;
      double dist = Math.hypot(diffYaw, diffPitch);
      double W_mag = Math.min(W_0, dist);
      if (dist >= D_0) {
         windYaw = yawEnabled ? windYaw / Math.sqrt(3.0) + (ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * W_mag / Math.sqrt(5.0) : 0.0;
         windPitch = pitchEnabled ? windPitch / Math.sqrt(3.0) + (ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * W_mag / Math.sqrt(5.0) : 0.0;
      } else {
         windYaw = yawEnabled ? windYaw / Math.sqrt(3.0) : 0.0;
         windPitch = pitchEnabled ? windPitch / Math.sqrt(3.0) : 0.0;
      }

      double currM0Yaw = M0Yaw;
      double currM0Pitch = M0Pitch;
      if (dist < D_0) {
         currM0Yaw = M0Yaw / Math.sqrt(5.0);
         currM0Pitch = M0Pitch / Math.sqrt(5.0);
      }

      double gravity = Math.min(G_0, dist);
      if (yawEnabled) {
         windYawVel = windYawVel + (windYaw + (dist == 0.0 ? 0.0 : gravity * diffYaw / dist));
      } else {
         windYawVel = 0.0;
      }

      if (pitchEnabled) {
         windPitchVel = windPitchVel + (windPitch + (dist == 0.0 ? 0.0 : gravity * diffPitch / dist));
      } else {
         windPitchVel = 0.0;
      }

      if (dist < 0.5) {
         windYawVel *= 0.7;
         windPitchVel *= 0.7;
      }

      if (dist < 0.1) {
         windYawVel = 0.0;
         windPitchVel = 0.0;
      }

      if (yawEnabled && currM0Yaw > 0.0) {
         double yawLimit = currM0Yaw * (0.5 + ThreadLocalRandom.current().nextDouble() * 0.5);
         windYawVel = Mth.clamp(windYawVel, -yawLimit, yawLimit);
      }

      if (pitchEnabled && currM0Pitch > 0.0) {
         double pitchLimit = currM0Pitch * (0.5 + ThreadLocalRandom.current().nextDouble() * 0.5);
         windPitchVel = Mth.clamp(windPitchVel, -pitchLimit, pitchLimit);
      }

      return new float[]{(float)(currentYaw + windYawVel), (float)(currentPitch + windPitchVel)};
   }

   public static float[] getFixedRotations(float[] prev, float[] current) {
      float yawDiff = Mth.wrapDegrees(current[0] - prev[0]);
      float pitchDiff = Mth.wrapDegrees(current[1] - prev[1]);
      float[] capped = new float[]{prev[0] + yawDiff, prev[1] + pitchDiff};
      return applyGCD(capped, prev);
   }

   public static float[] applyGCD(float[] rotations, float[] lastRotations) {
      if (mc.options != null && mc.options.sensitivity() != null) {
         float f = (float)(mc.options.sensitivity().get() * 0.6F + 0.2F);
         float gcd = f * f * f * 1.2F;
         float accumulatedYaw = gcdYawRemainder + Mth.wrapDegrees(rotations[0] - lastRotations[0]);
         float accumulatedPitch = gcdPitchRemainder + Mth.wrapDegrees(rotations[1] - lastRotations[1]);
         float snappedYaw = gcd <= 0.0F ? accumulatedYaw : (int)(accumulatedYaw / gcd) * gcd;
         float snappedPitch = gcd <= 0.0F ? accumulatedPitch : (int)(accumulatedPitch / gcd) * gcd;
         gcdYawRemainder = Mth.wrapDegrees(accumulatedYaw - snappedYaw);
         gcdPitchRemainder = accumulatedPitch - snappedPitch;
         return new float[]{lastRotations[0] + snappedYaw, lastRotations[1] + snappedPitch};
      } else {
         return rotations;
      }
   }

   public static float[] ensureContinuity(float[] lastRot, float[] targetRot) {
      float lastYaw = Mth.wrapDegrees(lastRot[0]);
      float diff = Mth.wrapDegrees(targetRot[0] - lastYaw);
      return new float[]{lastYaw + diff, Mth.clamp(targetRot[1], -90.0F, 90.0F)};
   }

   public static float getSilentYaw() {
      return lastSentRotations[0];
   }

   public static float getSilentPitch() {
      return lastSentRotations[1];
   }

   public static boolean hasSilentRotation() {
      return ServerRotationHandler.isHasSilentRotation();
   }

   public static boolean tryTakeControl(Module requester, RotationUtils.Priority priority) {
      if (currentController != null && currentController != requester) {
         if (priority.getValue() > currentPriority.getValue()) {
            currentController = requester;
            currentPriority = priority;
            return true;
         } else {
            return false;
         }
      } else {
         currentController = requester;
         currentPriority = priority;
         return true;
      }
   }

   public static boolean isControlledBy(Module module) {
      return currentController == module;
   }

   public static boolean canTakeControl(Module requester, RotationUtils.Priority priority) {
      return currentController != null && currentController != requester ? priority.getValue() > currentPriority.getValue() : true;
   }

   public static void releaseControl(Module requester) {
      if (currentController == requester) {
         currentController = null;
         currentPriority = RotationUtils.Priority.LOWEST;
      }
   }

   public static boolean cleanup(Module requester, boolean silent) {
      return cleanup(requester, silent, 150.0F, 150.0F, RotationUtils.AimType.REGULAR, true);
   }

   public static boolean cleanup(Module requester, boolean silent, float yawSpeed, float pitchSpeed, RotationUtils.AimType aimType, boolean scaleSpeed) {
      if (!isControlledBy(requester)) {
         return false;
      } else {
         tryTakeControl(requester, RotationUtils.Priority.LOWEST);
         if (rotateBack(silent, yawSpeed, pitchSpeed, aimType, scaleSpeed)) {
            return true;
         } else {
            releaseControl(requester);
            return false;
         }
      }
   }

   public static RotationUtils.RotationBuilder aim(Module requester) {
      return new RotationUtils.RotationBuilder(requester);
   }

   public static BlockHitResult raycastFromRotation(float yaw, float pitch, float partialTick) {
      if (mc.player != null && mc.level != null) {
         Vec3 eye = mc.player.getEyePosition(partialTick);
         Vec3 look = ServerRotationHandler.getRotationVector(pitch, yaw);
         Vec3 end = eye.add(look.scale(mc.player.blockInteractionRange()));
         return mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, mc.player));
      } else {
         return null;
      }
   }

   public static boolean rayIntersectsBlock(float yaw, float pitch, BlockPos clickedPos, float partialTick) {
      if (mc.player != null && clickedPos != null) {
         Vec3 eye = mc.player.getEyePosition(partialTick);
         Vec3 look = ServerRotationHandler.getRotationVector(pitch, yaw);
         Vec3 end = eye.add(look.scale(mc.player.blockInteractionRange()));
         return new AABB(clickedPos).inflate(1.0E-4).clip(eye, end).isPresent();
      } else {
         return false;
      }
   }

   public static enum AimType {
      REGULAR,
      BLATANT,
      WINDMOUSE;
   }

   public static enum EntityPoints {
      STRAIGHT,
      CLOSEST,
      RANDOM,
      CENTER,
      FEET;
   }

   public static enum Priority {
      HIGHEST(5),
      HIGH(4),
      NORMAL(3),
      LOW(2),
      LOWEST(1);

      private final int value;

      private Priority(int value) {
         this.value = value;
      }

      public int getValue() {
         return this.value;
      }
   }

   public static class RotationBuilder {
      private final Module requester;
      private RotationUtils.Priority priority = RotationUtils.Priority.NORMAL;
      private boolean silent = false;
      private RotationUtils.AimType aimType = RotationUtils.AimType.REGULAR;
      private float yawSpeed = 100.0F;
      private float pitchSpeed = 100.0F;
      private RotationUtils.EntityPoints points = RotationUtils.EntityPoints.STRAIGHT;
      private float multipoint = 50.0F;
      private boolean random = false;
      private boolean throughWalls = false;
      private int predictionTicks = 0;

      RotationBuilder(Module requester) {
         this.requester = requester;
      }

      public RotationUtils.RotationBuilder priority(RotationUtils.Priority p) {
         this.priority = p;
         return this;
      }

      public RotationUtils.RotationBuilder silent() {
         this.silent = true;
         return this;
      }

      public RotationUtils.RotationBuilder silent(boolean s) {
         this.silent = s;
         return this;
      }

      public RotationUtils.RotationBuilder windMouse() {
         this.aimType = RotationUtils.AimType.WINDMOUSE;
         return this;
      }

      public RotationUtils.RotationBuilder aimType(RotationUtils.AimType t) {
         this.aimType = t;
         return this;
      }

      public RotationUtils.RotationBuilder speed(float s) {
         this.yawSpeed = s;
         this.pitchSpeed = s;
         return this;
      }

      public RotationUtils.RotationBuilder speed(float yaw, float pitch) {
         this.yawSpeed = yaw;
         this.pitchSpeed = pitch;
         return this;
      }

      public RotationUtils.RotationBuilder points(RotationUtils.EntityPoints p) {
         this.points = p;
         return this;
      }

      public RotationUtils.RotationBuilder multipoint(float m) {
         this.multipoint = m;
         return this;
      }

      public RotationUtils.RotationBuilder random(boolean r) {
         this.random = r;
         return this;
      }

      public RotationUtils.RotationBuilder throughWalls(boolean t) {
         this.throughWalls = t;
         return this;
      }

      public RotationUtils.RotationBuilder predict(int ticks) {
         this.predictionTicks = ticks;
         return this;
      }

      public boolean to(Vec3 pos) {
         return RotationUtils.rotateTo(pos, this.yawSpeed, this.pitchSpeed, this.silent, this.aimType, this.requester, this.priority);
      }

      public boolean to(LivingEntity target) {
         if (this.predictionTicks > 0) {
            Vec3 predicted = RotationUtils.predictAimPoint(target, this.predictionTicks, this.points);
            return RotationUtils.rotateTo(predicted, this.yawSpeed, this.pitchSpeed, this.silent, this.aimType, this.requester, this.priority);
         } else {
            return RotationUtils.rotateTo(
               target,
               this.yawSpeed,
               this.pitchSpeed,
               this.silent,
               this.aimType,
               this.points,
               this.multipoint,
               this.random,
               this.throughWalls,
               this.requester,
               this.priority
            );
         }
      }
   }
}
