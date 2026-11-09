package kat.respect.b2.mixin;

import kat.respect.b2.render.OverlayRenderer;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public class WindowBlitMixin {
   @Shadow
   private int frames;

   @Inject(
      method = {"renderFrame"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen()V",
         shift = Shift.AFTER
      )},
      require = 0
   )
   @CInline
   private void respectB2$renderOverlayPostBlit(boolean advanceGameTime, CallbackInfo ci) {
      OverlayRenderer.draw((Minecraft)(Object)this, this.frames, 1);
   }
}
