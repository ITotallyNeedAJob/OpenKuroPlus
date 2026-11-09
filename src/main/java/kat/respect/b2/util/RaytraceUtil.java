package kat.respect.b2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class RaytraceUtil {
   public static final RaytraceUtil INSTANCE = new RaytraceUtil();
   private static final Minecraft mc = Minecraft.getInstance();

   public static Vec3 getPlayerLookVec(Rotation rotation) {
      float f = rotation.getPitch() * (float) (Math.PI / 180.0);
      float g = -rotation.getYaw() * (float) (Math.PI / 180.0);
      float h = (float)Math.cos(g);
      float i = (float)Math.sin(g);
      float j = (float)Math.cos(f);
      float k = (float)Math.sin(f);
      return new Vec3(i * j, -k, h * j);
   }

   public static BlockHitResult raycastBlock(Rotation rotation, double reach) {
      if (mc.player != null && mc.level != null) {
         Vec3 eyePos = mc.player.getEyePosition();
         Vec3 lookVec = getPlayerLookVec(rotation);
         Vec3 endPos = eyePos.add(lookVec.scale(reach));
         return mc.level.clip(new ClipContext(eyePos, endPos, Block.OUTLINE, Fluid.NONE, mc.player));
      } else {
         return null;
      }
   }

   public static EntityHitResult raycastEntity(Rotation rotation, double reach) {
      if (mc.player != null && mc.level != null) {
         Vec3 eyePos = mc.player.getEyePosition();
         Vec3 lookVec = getPlayerLookVec(rotation);
         Vec3 endPos = eyePos.add(lookVec.scale(reach));
         AABB box = mc.player.getBoundingBox().expandTowards(lookVec.scale(reach)).inflate(1.0);
         return ProjectileUtil.getEntityHitResult(mc.player, eyePos, endPos, box, e -> !e.isSpectator() && e.isPickable(), reach * reach);
      } else {
         return null;
      }
   }
}
