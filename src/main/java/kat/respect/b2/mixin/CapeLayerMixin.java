package kat.respect.b2.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kat.respect.b2.util.CameraFadeUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CapeLayer.class})
public abstract class CapeLayerMixin {
   @Shadow
   private HumanoidModel<AvatarRenderState> model;

   @Shadow
   private native boolean hasLayer(ItemStack var1, LayerType var2);

   @Inject(
      method = {"submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void respectB2$submitCapeDissolve(
      PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot, CallbackInfo ci
   ) {
      float alpha = CameraFadeUtil.calculateProximityAlpha(state);
      if (alpha <= 0.005F) {
         ci.cancel();
      } else {
         if (alpha < 0.999F) {
            if (state.isInvisible || !state.showCape) {
               return;
            }

            if (state.skin == null || state.skin.cape() == null) {
               return;
            }

            if (this.hasLayer(state.chestEquipment, LayerType.WINGS)) {
               return;
            }

            poseStack.pushPose();
            if (this.hasLayer(state.chestEquipment, LayerType.HUMANOID)) {
               poseStack.translate(0.0F, -0.053125F, 0.06875F);
            }

            RenderType renderType = RenderTypes.entityTranslucentCullItemTarget(state.skin.cape().texturePath());
            int a = (int)(alpha * 255.0F);
            int color = a << 24 | 16777215;
            collector.submitModel(this.model, state, poseStack, renderType, light, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor, null);
            poseStack.popPose();
            ci.cancel();
         }
      }
   }
}
