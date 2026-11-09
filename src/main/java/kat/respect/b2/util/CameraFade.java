package kat.respect.b2.util;

import kat.respect.b2.bridge.FreeLookBridge;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CameraFade {
   private CameraFade() {
   }

   public static float getAlpha(EntityRenderState state) {
      Minecraft mc = Minecraft.getInstance();
      if (state instanceof AvatarRenderState avatarState
         && FreeLookBridge.isActive()
         && mc.gameRenderer != null
         && mc.player != null
         && avatarState.id == mc.player.getId()) {
         Camera camera = CameraCompat.mainCamera(mc.gameRenderer);
         if (camera != null) {
            Vec3 camPos = camera.position();
            AABB box = mc.player.getBoundingBox();
            double cx = Mth.clamp(camPos.x, box.minX, box.maxX);
            double cy = Mth.clamp(camPos.y, box.minY, box.maxY);
            double cz = Mth.clamp(camPos.z, box.minZ, box.maxZ);
            double dist = camPos.distanceTo(new Vec3(cx, cy, cz));
            double maxDist = 1.85;
            double minDist = 0.35;
            if (dist <= minDist) {
               return 0.0F;
            }

            if (dist < maxDist) {
               float progress = (float)Mth.clamp((dist - minDist) / (maxDist - minDist), 0.0, 1.0);
               return progress * progress * (3.0F - 2.0F * progress);
            }
         }
      }

      return 1.0F;
   }
}
