package kat.respect.b2.util;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class AnchorAim {
   private static final Minecraft mc = Minecraft.getInstance();

   private AnchorAim() {
   }

   public static Vec3 findAim(BlockPos anchorPos, float partialTick) {
      if (anchorPos == null) {
         return null;
      } else if (mc.player != null && mc.level != null) {
         Vec3 eye = mc.player.getEyePosition(partialTick);
         Vec3 anchorCenter = Vec3.atCenterOf(anchorPos);
         Vec3 toEye = eye.subtract(anchorCenter);
         Direction[] sorted = (Direction[])Direction.values().clone();
         Arrays.sort(sorted, (a, b) -> Double.compare(dotEye(b, toEye), dotEye(a, toEye)));

         for (Direction faceDir : sorted) {
            if (!(dotEye(faceDir, toEye) <= 0.0)) {
               Vec3 candidate = searchFace(eye, anchorPos, anchorCenter, faceDir);
               if (candidate != null) {
                  return candidate;
               }
            }
         }

         return anchorCenter;
      } else {
         return Vec3.atCenterOf(anchorPos);
      }
   }

   public static boolean canHit(Vec3 eye, Vec3 aimPoint, BlockPos anchorPos) {
      if (mc.level != null && mc.player != null) {
         Vec3 dir = aimPoint.subtract(eye);
         if (dir.lengthSqr() < 1.0E-8) {
            return true;
         } else {
            Vec3 end = eye.add(dir.normalize().scale(mc.player.blockInteractionRange()));
            BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, mc.player));
            return hit.getType() == Type.BLOCK && hit.getBlockPos().equals(anchorPos);
         }
      } else {
         return false;
      }
   }

   private static double dotEye(Direction dir, Vec3 toEye) {
      return dir.getStepX() * toEye.x + dir.getStepY() * toEye.y + dir.getStepZ() * toEye.z;
   }

   private static Vec3 searchFace(Vec3 eye, BlockPos anchorPos, Vec3 anchorCenter, Direction faceDir) {
      double fcx = anchorCenter.x + faceDir.getStepX() * 0.5;
      double fcy = anchorCenter.y + faceDir.getStepY() * 0.5;
      double fcz = anchorCenter.z + faceDir.getStepZ() * 0.5;
      Axis axis = faceDir.getAxis();
      double signA;
      double signB;
      switch (axis) {
         case X:
            signA = eye.y >= anchorCenter.y ? 1.0 : -1.0;
            signB = eye.z >= anchorCenter.z ? 1.0 : -1.0;
            break;
         case Y:
            signA = eye.x >= anchorCenter.x ? 1.0 : -1.0;
            signB = eye.z >= anchorCenter.z ? 1.0 : -1.0;
            break;
         default:
            signA = eye.x >= anchorCenter.x ? 1.0 : -1.0;
            signB = eye.y >= anchorCenter.y ? 1.0 : -1.0;
      }

      double startOff = 0.3 + ThreadLocalRandom.current().nextDouble() * 0.1;
      double[][] sampleOffsets = new double[][]{
         {signA * startOff, signB * startOff},
         {signA * 0.25, signB * 0.25},
         {signA * 0.2, signB * 0.2},
         {signA * 0.15, signB * 0.15},
         {signA * 0.1, signB * 0.1},
         {signA * 0.3, 0.0},
         {0.0, signB * 0.3},
         {0.0, 0.0},
         {-signA * 0.3, signB * 0.3},
         {signA * 0.3, -signB * 0.3},
         {-signA * 0.3, -signB * 0.3},
         {-signA * 0.3, 0.0},
         {0.0, -signB * 0.3}
      };

      for (double[] off : sampleOffsets) {
         Vec3 candidate = faceSamplePoint(axis, fcx, fcy, fcz, off[0], off[1]);
         if (canHit(eye, candidate, anchorPos)) {
            return candidate;
         }
      }

      return null;
   }

   private static Vec3 faceSamplePoint(Axis axis, double fcx, double fcy, double fcz, double offA, double offB) {
      return switch (axis) {
         case X -> new Vec3(fcx, fcy + offA, fcz + offB);
         case Y -> new Vec3(fcx + offA, fcy, fcz + offB);
         case Z -> new Vec3(fcx + offA, fcy + offB, fcz);
      };
   }
}
