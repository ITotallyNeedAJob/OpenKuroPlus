package kat.respect.b2.util;

import net.minecraft.core.BlockPos;

@Deprecated
public final class CrystalUtils {
   private CrystalUtils() {
   }

   public static boolean canPlaceCrystalClient(BlockPos block) {
      return CrystalUtil.canPlace(block);
   }

   public static boolean canPlaceCrystalClientAssumeObsidian(BlockPos block) {
      return CrystalUtil.canPlaceAssumeObsidian(block);
   }
}
