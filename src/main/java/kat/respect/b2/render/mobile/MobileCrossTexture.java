package kat.respect.b2.render.mobile;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;

public class MobileCrossTexture {
   public int glId = -1;

   public MobileCrossTexture() {
   }

   public MobileCrossTexture(int glId) {
      this.glId = glId;
   }

   public static MobileCrossTexture from(RenderTarget renderTarget) {
      MobileCrossTexture crossTexture = new MobileCrossTexture();

      try {
         if (renderTarget != null && renderTarget.getColorTexture() instanceof GlTexture glTexture) {
            crossTexture.glId = glTexture.glId();
         }
      } catch (LinkageError var4) {
      }

      return crossTexture;
   }

   public static MobileCrossTexture fromMainRenderTarget() {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null) {
         return new MobileCrossTexture();
      } else {
         try {
            return from(mc.getMainRenderTarget());
         } catch (Throwable var2) {
            return new MobileCrossTexture();
         }
      }
   }
}
