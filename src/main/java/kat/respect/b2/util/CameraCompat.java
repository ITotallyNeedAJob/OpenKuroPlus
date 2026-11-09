package kat.respect.b2.util;

import java.lang.reflect.Method;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

public final class CameraCompat {
   private static volatile Boolean direct = null;
   private static volatile Method fallback = null;

   private CameraCompat() {
   }

   public static Camera mainCamera(GameRenderer renderer) {
      if (renderer == null) {
         return null;
      } else {
         try {
            return renderer.getMainCamera();
         } catch (LinkageError var5) {
            try {
               Method m = fallback;
               if (m == null) {
                  m = GameRenderer.class.getMethod("getMainCamera");
                  fallback = m;
               }

               if (m.invoke(renderer) instanceof Camera camera) {
                  return camera;
               }
            } catch (Throwable var4) {
            }

            return null;
         }
      }
   }
}
