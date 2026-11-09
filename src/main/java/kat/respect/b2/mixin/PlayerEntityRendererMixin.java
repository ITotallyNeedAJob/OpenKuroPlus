package kat.respect.b2.mixin;

import kat.respect.b2.util.RotationUtil;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AvatarRenderer.class})
public abstract class PlayerEntityRendererMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V"},
      at = {@At("RETURN")}
   )
   private void respectB2$applyThirdPersonRotations(Avatar avatar, AvatarRenderState state, float tickDelta, CallbackInfo info) {
      if (avatar instanceof AbstractClientPlayer player) {
         if (player.isLocalPlayer()) {
            if (RotationUtil.isUsingSilentRotation()) {
               float silentYaw = RotationUtil.getSilentYaw();
               float silentPitch = RotationUtil.getSilentPitch();
               float bodyYaw = Mth.rotLerp(tickDelta, player.yBodyRotO, player.yBodyRot);
               state.yRot = Mth.wrapDegrees(silentYaw - bodyYaw);
               state.bodyRot = bodyYaw;
               state.xRot = silentPitch;
            }
         }
      }
   }
}
