package kat.respect.b2.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kat.respect.b2.bridge.EspOutlineBridge;
import kat.respect.b2.bridge.NameTagsBridge;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public class EntityRendererMixin {
   @Inject(
      method = {"submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void respectB2$customNameTags(
      EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, int offset, CallbackInfo ci
   ) {
      NameTagsBridge.Hook hook = NameTagsBridge.get();
      if (hook != null && hook.isEnabled()) {
         if (state instanceof AvatarRenderState avatar) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
               if (mc.level.getEntity(avatar.id) instanceof Player player) {
                  if (hook.shouldHide(player, player == mc.player)) {
                     ci.cancel();
                  }
               }
            }
         }
      }
   }

   @Inject(
      method = {"extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V"},
      at = {@At("TAIL")}
   )
   @CInline
   private void respectB2$espOutline(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
      int color = EspOutlineBridge.outlineColor(entity);
      if (color != 0) {
         state.outlineColor = color;
      }
   }
}
