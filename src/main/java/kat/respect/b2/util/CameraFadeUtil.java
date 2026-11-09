package kat.respect.b2.util;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Deprecated
public final class CameraFadeUtil {
   private CameraFadeUtil() {
   }

   public static float calculateProximityAlpha(EntityRenderState state) {
      return CameraFade.getAlpha(state);
   }
}
