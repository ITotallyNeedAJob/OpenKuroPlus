package kat.respect.b2.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kat.respect.b2.bridge.HurtCamBridge;
import kat.respect.b2.event.ClientEventDispatcher;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"bobHurt"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void onBobHurt(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
      if (HurtCamBridge.isNoHurtCamEnabled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"renderLevel"},
      at = {@At("TAIL")}
   )
   @CInline
   private void respectB2$onRender3D(CallbackInfo ci) {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.gameRenderer != null) {
            float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            ClientEventDispatcher.onRenderLevelTail(partialTick);
         }
      } catch (Throwable var4) {
      }
   }
}
