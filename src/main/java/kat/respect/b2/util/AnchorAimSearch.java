package kat.respect.b2.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

@Deprecated
public final class AnchorAimSearch {
   private AnchorAimSearch() {
   }

   public static Vec3 findClearAimPoint(BlockPos anchorPos, float partialTick) {
      return AnchorAim.findAim(anchorPos, partialTick);
   }

   public static boolean canHitAnchor(Vec3 eye, Vec3 aimPoint, BlockPos anchorPos) {
      return AnchorAim.canHit(eye, aimPoint, anchorPos);
   }
}
