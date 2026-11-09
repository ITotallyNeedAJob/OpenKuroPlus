package kat.respect.b2.util;

import kat.respect.b2.mixin.IMinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ItemUseUtil {
   private static final Minecraft mc = Minecraft.getInstance();

   private ItemUseUtil() {
   }

   public static void useItemInAir() {
      if (mc.player != null && mc.gameMode != null) {
         HitResult original = mc.hitResult;
         if (original == null || original.getType() != Type.MISS) {
            Vec3 eye = mc.player.getEyePosition();
            mc.hitResult = BlockHitResult.miss(eye, mc.player.getDirection(), BlockPos.containing(eye));
         }

         try {
            ((IMinecraftAccessor)mc).invokeStartUseItem();
         } finally {
            if (original != null) {
               mc.hitResult = original;
            }
         }
      }
   }
}
