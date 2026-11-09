package kat.respect.b2.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kat.respect.b2.bridge.AnimationsBridge;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public class ItemInHandRendererMixin {
   @Inject(
      method = {"renderArmWithItem"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
         shift = Shift.AFTER
      )}
   )
   private void onRenderArmWithItem(
      AbstractClientPlayer player,
      float tickProgress,
      float pitch,
      InteractionHand hand,
      float swingProgress,
      ItemStack item,
      float equipProgress,
      PoseStack matrices,
      SubmitNodeCollector orderedRenderCommandQueue,
      int light,
      CallbackInfo ci
   ) {
      AnimationsBridge.applyTransform(hand, matrices);
   }

   @Inject(
      method = {"swingArm"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSwingArm(float swingProgress, PoseStack matrixStack, int i, HumanoidArm arm, CallbackInfo ci) {
      if (AnimationsBridge.isCustomSwingActive()) {
         ci.cancel();
         AnimationsBridge.applySwing(swingProgress, matrixStack, i, arm);
      }
   }
}
