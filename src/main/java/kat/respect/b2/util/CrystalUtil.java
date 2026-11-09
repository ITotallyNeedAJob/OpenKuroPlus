package kat.respect.b2.util;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class CrystalUtil {
   private static final Minecraft mc = Minecraft.getInstance();

   private CrystalUtil() {
   }

   public static boolean canPlace(BlockPos block) {
      if (mc.level == null) {
         return false;
      } else {
         BlockState blockState = mc.level.getBlockState(block);
         return !blockState.is(Blocks.OBSIDIAN) && !blockState.is(Blocks.BEDROCK) ? false : canPlaceAssumeObsidian(block);
      }
   }

   public static boolean canPlaceAssumeObsidian(BlockPos block) {
      if (mc.level == null) {
         return false;
      } else {
         BlockPos blockPos2 = block.above();
         if (!mc.level.getBlockState(blockPos2).isAir()) {
            return false;
         } else {
            double d = blockPos2.getX();
            double e = blockPos2.getY();
            double f = blockPos2.getZ();
            List<Entity> list = mc.level.getEntities((Entity)null, new AABB(d, e, f, d + 1.0, e + 2.0, f + 1.0));
            return list.isEmpty();
         }
      }
   }
}
