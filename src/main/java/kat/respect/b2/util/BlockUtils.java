package kat.respect.b2.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

@Deprecated
public final class BlockUtils {
   private BlockUtils() {
   }

   public static boolean isBlock(BlockPos pos, Block block) {
      return BlockUtil.isBlock(pos, block);
   }

   public static boolean isBlockAtPosition(BlockPos blockPos, Block block) {
      return BlockUtil.isBlock(blockPos, block);
   }

   public static boolean isRespawnAnchorCharged(BlockPos blockPos) {
      return BlockUtil.isChargedAnchor(blockPos);
   }

   public static boolean isRespawnAnchorUncharged(BlockPos blockPos) {
      return BlockUtil.isUnchargedAnchor(blockPos);
   }

   public static InteractionResult interactWithBlock(BlockHitResult blockHitResult, boolean shouldSwingHand) {
      return BlockUtil.interact(blockHitResult, shouldSwingHand);
   }

   public static boolean willPlacedBlockPlacementCollideWithEntity(BlockPos blockPos, BlockState blockState) {
      return BlockUtil.collidesEntity(blockPos, blockState);
   }

   public static boolean isBlockPlacingCollideWithEntity(BlockPos blockPos, Block block) {
      return BlockUtil.collidesEntity(blockPos, block);
   }

   public static boolean collidesWithPlayer(BlockPos pos) {
      return BlockUtil.collidesPlayer(pos);
   }

   public static boolean collidesWithBlockingEntity(BlockPos pos) {
      return BlockUtil.collidesBlocking(pos);
   }

   public static boolean hasBlockingEntity(AABB box) {
      return BlockUtil.hasBlocking(box);
   }

   public static BlockPos getTargetPlacePos() {
      return BlockUtil.targetPlacePos();
   }

   public static boolean isValidPlacement(BlockPos pos) {
      return BlockUtil.isValid(pos);
   }

   public static boolean hasSupportingFace(BlockPos pos) {
      return BlockUtil.hasFace(pos);
   }

   public static boolean hasCrystalOnBlock(BlockPos pos) {
      return BlockUtil.hasCrystal(pos);
   }

   public static boolean canPlaceLegit(BlockPos pos) {
      return BlockUtil.canPlace(pos);
   }

   public static BlockUtils.PlacementHit findPlacementHit(BlockPos pos, boolean requireRaycast) {
      BlockUtil.Hit hit = BlockUtil.findHit(pos, requireRaycast);
      return hit != null ? new BlockUtils.PlacementHit(hit.placementPos(), hit.hitResult()) : null;
   }

   public record PlacementHit(BlockPos placementPos, BlockHitResult hitResult) {
   }
}
