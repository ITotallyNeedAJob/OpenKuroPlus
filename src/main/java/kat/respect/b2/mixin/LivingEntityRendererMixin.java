package kat.respect.b2.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kat.respect.b2.bridge.EspOutlineBridge;
import kat.respect.b2.util.CameraFadeUtil;
import kat.respect.b2.util.RenderCompat;
import kat.respect.b2.util.RotationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntityRenderer.class})
public class LivingEntityRendererMixin {
   @Unique
   private float respectB2$originalYaw;
   @Unique
   private float respectB2$originalYawO;
   @Unique
   private float respectB2$originalPitch;
   @Unique
   private float respectB2$originalPitchO;
   @Unique
   private float respectB2$originalHeadYaw;
   @Unique
   private float respectB2$originalHeadYawO;
   @Unique
   private float respectB2$originalBodyYaw;
   @Unique
   private float respectB2$originalBodyYawO;

   @Inject(
      method = {"extractRenderState"},
      at = {@At("HEAD")}
   )
   private void onExtractRenderStateHead(LivingEntity entity, LivingEntityRenderState state, float f, CallbackInfo ci) {
      if (entity == Minecraft.getInstance().player && RotationHandler.isSilentHeld()) {
         float rYaw = RotationHandler.getRenderYaw();
         float prevRYaw = RotationHandler.getPrevRenderYaw();
         float rPitch = RotationHandler.getRenderPitch();
         float prevRPitch = RotationHandler.getPrevRenderPitch();
         this.respectB2$originalYaw = entity.getYRot();
         this.respectB2$originalYawO = entity.yRotO;
         this.respectB2$originalPitch = entity.getXRot();
         this.respectB2$originalPitchO = entity.xRotO;
         this.respectB2$originalHeadYaw = entity.yHeadRot;
         this.respectB2$originalHeadYawO = entity.yHeadRotO;
         this.respectB2$originalBodyYaw = entity.yBodyRot;
         this.respectB2$originalBodyYawO = entity.yBodyRotO;
         entity.setYRot(rYaw);
         entity.yRotO = prevRYaw;
         entity.setXRot(rPitch);
         entity.xRotO = prevRPitch;
         entity.yHeadRot = rYaw;
         entity.yHeadRotO = prevRYaw;
         entity.yBodyRot = RotationHandler.getRenderBodyYaw();
         entity.yBodyRotO = RotationHandler.getPrevRenderBodyYaw();
      }
   }

   @Inject(
      method = {"extractRenderState"},
      at = {@At("RETURN")}
   )
   private void onExtractRenderStateReturn(LivingEntity entity, LivingEntityRenderState state, float f, CallbackInfo ci) {
      if (entity == Minecraft.getInstance().player && RotationHandler.isSilentHeld()) {
         entity.setYRot(this.respectB2$originalYaw);
         entity.yRotO = this.respectB2$originalYawO;
         entity.setXRot(this.respectB2$originalPitch);
         entity.xRotO = this.respectB2$originalPitchO;
         entity.yHeadRot = this.respectB2$originalHeadYaw;
         entity.yHeadRotO = this.respectB2$originalHeadYawO;
         entity.yBodyRot = this.respectB2$originalBodyYaw;
         entity.yBodyRotO = this.respectB2$originalBodyYawO;
      }
   }

   @Redirect(
      method = {"submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V"
      )
   )
   private void respectB2$silhouetteLayers(
      RenderLayer layer, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, EntityRenderState state, float yRot, float xRot
   ) {
      int saved = state.outlineColor;
      if (saved != 0 && EspOutlineBridge.isBodyOnlySilhouette()) {
         state.outlineColor = 0;
      }

      layer.submit(poseStack, collector, lightCoords, state, yRot, xRot);
      state.outlineColor = saved;
   }

   @Inject(
      method = {"getRenderType"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void respectB2$translucentRenderType(
      LivingEntityRenderState state, boolean bodyVisible, boolean isTranslucent, boolean isOutline, CallbackInfoReturnable<RenderType> cir
   ) {
      float alpha = CameraFadeUtil.calculateProximityAlpha(state);
      if (alpha < 0.999F && alpha > 0.005F) {
         Identifier texture = ((LivingEntityRenderer)(Object)this).getTextureLocation(state);
         RenderType translucent = RenderCompat.entityTranslucentCull(texture);
         if (translucent != null) {
            cir.setReturnValue(translucent);
         }
      }
   }

   @Inject(
      method = {"submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void respectB2$cullIfTooClose(
      LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci
   ) {
      float alpha = CameraFadeUtil.calculateProximityAlpha(state);
      if (alpha <= 0.005F) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"getModelTint"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void respectB2$cameraProximityFade(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
      float alpha = CameraFadeUtil.calculateProximityAlpha(state);
      if (alpha < 0.999F) {
         int a = (int)(alpha * 255.0F);
         int orig = (Integer)cir.getReturnValue();
         cir.setReturnValue(a << 24 | orig & 16777215);
      }
   }
}
