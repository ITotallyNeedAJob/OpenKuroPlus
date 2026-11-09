package kat.respect.b2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.SwingSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class BlockUtil {
   private static final Minecraft mc = Minecraft.getInstance();
   private static final double PLACE_RAY_EPSILON = 1.0E-4;
   private static final double PLACE_FACE_CORNER_OFFSET = 0.49;
   private static final double[][] PLACE_FACE_SAMPLE_OFFSETS = new double[][]{{0.0, 0.0}, {0.49, 0.49}, {0.49, -0.49}, {-0.49, 0.49}, {-0.49, -0.49}};

   private BlockUtil() {
   }

   public static boolean isBlock(BlockPos pos, Block block) {
      return mc.level != null && pos != null ? mc.level.getBlockState(pos).getBlock() == block : false;
   }

   public static boolean isChargedAnchor(BlockPos pos) {
      return isBlock(pos, Blocks.RESPAWN_ANCHOR) && mc.level.getBlockState(pos).getValue(RespawnAnchorBlock.CHARGE) != 0;
   }

   public static boolean isUnchargedAnchor(BlockPos pos) {
      return isBlock(pos, Blocks.RESPAWN_ANCHOR) && mc.level.getBlockState(pos).getValue(RespawnAnchorBlock.CHARGE) == 0;
   }

   public static InteractionResult interact(BlockHitResult hit, boolean swing) {
      if (mc.player != null && mc.gameMode != null && hit != null) {
         InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
         if (result.consumesAction() && swing && result instanceof Success success && shouldSwing(success)) {
            SwingCompat.swing(mc.player, InteractionHand.MAIN_HAND);
         }

         return result;
      } else {
         return InteractionResult.PASS;
      }
   }

   private static boolean shouldSwing(Success success) {
      try {
         return success.swingSource() != SwingSource.NONE;
      } catch (LinkageError var4) {
         try {
            if (Success.class.getMethod("shouldSwing").invoke(success) instanceof Boolean swing) {
               return swing;
            }
         } catch (Throwable var3) {
         }

         return true;
      }
   }

   public static boolean collidesEntity(BlockPos pos, BlockState state) {
      if (mc.level != null && pos != null && state != null) {
         AABB searchBox = new AABB(pos);

         for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != mc.player)) {
            if (entity.getBoundingBox().intersects(searchBox)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean collidesEntity(BlockPos pos, Block block) {
      return collidesEntity(pos, block.defaultBlockState());
   }

   public static boolean collidesPlayer(BlockPos pos) {
      return mc.player != null && pos != null && mc.player.getBoundingBox().intersects(new AABB(pos));
   }

   public static boolean collidesBlocking(BlockPos pos) {
      return pos != null && mc.level != null ? hasBlocking(new AABB(pos)) : false;
   }

   public static boolean hasBlocking(AABB box) {
      if (box != null && mc.level != null) {
         for (Entity entity : mc.level.getEntities(null, box)) {
            if (isBlocking(entity) && entity.getBoundingBox().intersects(box)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean isBlocking(Entity entity) {
      return entity != null && entity != mc.player ? !entity.isRemoved() && entity.blocksBuilding : false;
   }

   public static BlockPos targetPlacePos() {
      HitResult hitResult = mc.hitResult;
      if (hitResult != null && hitResult.getType() == Type.BLOCK) {
         BlockHitResult blockHitResult = (BlockHitResult)hitResult;
         return blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
      } else {
         return null;
      }
   }

   public static boolean isValid(BlockPos pos) {
      if (pos != null && mc.level != null) {
         BlockState state = mc.level.getBlockState(pos);
         return state.isAir() || state.canBeReplaced();
      } else {
         return false;
      }
   }

   public static boolean hasFace(BlockPos pos) {
      if (pos != null && mc.level != null) {
         for (Direction direction : Direction.values()) {
            BlockPos adjacent = pos.relative(direction);
            if (mc.level.getBlockState(adjacent).isFaceSturdy(mc.level, adjacent, direction.getOpposite())) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean hasCrystal(BlockPos pos) {
      if (mc.level != null && pos != null) {
         BlockPos crystalPos = pos.above();
         if (!mc.level.isEmptyBlock(crystalPos)) {
            return false;
         } else {
            double x = crystalPos.getX();
            double y = crystalPos.getY();
            double z = crystalPos.getZ();
            AABB crystalAABB = new AABB(x, y, z, x + 1.0, y + 2.0, z + 1.0);
            return !mc.level.getEntities(null, crystalAABB).isEmpty();
         }
      } else {
         return false;
      }
   }

   public static boolean canPlace(BlockPos pos) {
      return findHit(pos, true) != null;
   }

   public static BlockUtil.Hit findHit(BlockPos pos, boolean requireRaycast) {
      if (pos != null && mc.level != null && mc.player != null) {
         float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         Vec3 eyePos = mc.player.getEyePosition(pt);
         BlockUtil.Hit bestHit = null;
         double bestDistanceSq = Double.MAX_VALUE;

         for (Direction direction : Direction.values()) {
            BlockPos adjacent = pos.relative(direction);
            Direction face = direction.getOpposite();
            if (mc.level.getBlockState(adjacent).isFaceSturdy(mc.level, adjacent, face)) {
               BlockUtil.Hit hit = findPlacementHitOnFace(eyePos, pos, adjacent, direction, face, requireRaycast);
               if (hit != null) {
                  double distanceSq = eyePos.distanceToSqr(hit.hitResult().getLocation());
                  if (distanceSq < bestDistanceSq) {
                     bestDistanceSq = distanceSq;
                     bestHit = hit;
                  }
               }
            }
         }

         return bestHit;
      } else {
         return null;
      }
   }

   private static BlockUtil.Hit findPlacementHitOnFace(
      Vec3 eyePos, BlockPos placementPos, BlockPos adjacent, Direction direction, Direction face, boolean requireRaycast
   ) {
      for (double[] sampleOffset : PLACE_FACE_SAMPLE_OFFSETS) {
         Vec3 facePoint = getFaceSamplePoint(adjacent, face, sampleOffset[0], sampleOffset[1]);
         BlockHitResult hitResult = new BlockHitResult(facePoint, face, adjacent, false);
         if (!requireRaycast || canRaycastPlacementFace(eyePos, hitResult, direction)) {
            return new BlockUtil.Hit(placementPos, hitResult);
         }
      }

      return null;
   }

   private static boolean canRaycastPlacementFace(Vec3 eyePos, BlockHitResult placementHit, Direction direction) {
      Vec3 facePoint = placementHit.getLocation();
      Vec3 rayEnd = facePoint.add(direction.getStepX() * 1.0E-4, direction.getStepY() * 1.0E-4, direction.getStepZ() * 1.0E-4);
      BlockHitResult hitResult = mc.level.clip(new ClipContext(eyePos, rayEnd, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, mc.player));
      if (hitResult.getType() != Type.BLOCK) {
         return false;
      } else {
         return !hitResult.getBlockPos().equals(placementHit.getBlockPos()) ? false : hitResult.getDirection() == placementHit.getDirection();
      }
   }

   private static Vec3 getFaceSamplePoint(BlockPos adjacent, Direction face, double offsetA, double offsetB) {
      Vec3 faceCenter = Vec3.atCenterOf(adjacent).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);

      return switch (face.getAxis()) {
         case X -> faceCenter.add(0.0, offsetA, offsetB);
         case Y -> faceCenter.add(offsetA, 0.0, offsetB);
         case Z -> faceCenter.add(offsetA, offsetB, 0.0);
      };
   }

   public record Hit(BlockPos placementPos, BlockHitResult hitResult) {
   }
}
