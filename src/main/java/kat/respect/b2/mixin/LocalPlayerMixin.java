package kat.respect.b2.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import kat.respect.b2.bridge.MovementBridge;
import kat.respect.b2.event.ClientEventDispatcher;
import kat.respect.b2.event.events.MotionEvent;
import kat.respect.b2.rotation.RotationManager;
import kat.respect.b2.util.RotationHandler;
import kat.respect.b2.util.ServerRotationHandler;
import kat.respect.b2.util.SilentRaycastHelper;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LocalPlayer.class})
public class LocalPlayerMixin {
   @Inject(
      method = {"raycastHitResult"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void respectB2$silentMouseOver(float partialTicks, Entity camera, CallbackInfoReturnable<HitResult> cir) {
      if (!SilentRaycastHelper.isInSilentRaycast()) {
         if (camera == (Object)this) {
            float[] rotation = RotationManager.getPacketRotation();
            if (rotation != null) {
               LocalPlayer player = (LocalPlayer)(Object)this;
               float savedYaw = player.getYRot();
               float savedPitch = player.getXRot();
               player.setYRot(rotation[0]);
               player.yHeadRot = rotation[0];
               player.setXRot(rotation[1]);
               SilentRaycastHelper.setInSilentRaycast(true);

               try {
                  cir.setReturnValue(((LocalPlayer)(Object)this).raycastHitResult(partialTicks, camera));
               } finally {
                  SilentRaycastHelper.setInSilentRaycast(false);
                  player.setYRot(savedYaw);
                  player.yHeadRot = savedYaw;
                  player.setXRot(savedPitch);
               }
            }
         }
      }
   }

   @Inject(
      method = {"sendPosition"},
      at = {@At("RETURN")}
   )
   @CInline
   private void onSendPositionPost(CallbackInfo ci) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      RotationManager.INSTANCE.onSendPositionPost(player);
      if (ServerRotationHandler.isHasSilentRotation()) {
         RotationHandler.updateRenderBodyYaw(player, ServerRotationHandler.getServerYaw());
         player.yHeadRot = ServerRotationHandler.getServerYaw();
         player.yBodyRot = RotationHandler.getRenderBodyYaw();
      } else if (RotationHandler.isSilentActive()) {
         RotationHandler.updateRenderBodyYaw(player, RotationHandler.getSilentYaw());
         player.yHeadRot = RotationHandler.getRenderYaw();
         player.yBodyRot = RotationHandler.getRenderBodyYaw();
      }

      RotationHandler.endTick();
      ClientEventDispatcher.onPlayerAiStepPost(player);
   }

   @ModifyExpressionValue(
      method = {"sendPosition", "tick"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"
      )},
      require = 0
   )
   @CInline
   private float hookSilentRotationYaw(float original) {
      if (ServerRotationHandler.isHasSilentRotation()) {
         return ServerRotationHandler.getServerYaw();
      } else {
         return RotationHandler.isSilentActive() ? RotationHandler.getSilentYaw() : original;
      }
   }

   @ModifyExpressionValue(
      method = {"sendPosition", "tick"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"
      )},
      require = 0
   )
   @CInline
   private float hookSilentRotationPitch(float original) {
      if (ServerRotationHandler.isHasSilentRotation()) {
         return ServerRotationHandler.getServerPitch();
      } else {
         return RotationHandler.isSilentActive() ? RotationHandler.getSilentPitch() : original;
      }
   }

   @Inject(
      method = {"aiStep"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/ClientInput;tick()V",
         shift = Shift.AFTER
      )}
   )
   @CInline
   private void onInputTick(CallbackInfo ci) {
   }

   @Inject(
      method = {"aiStep"},
      at = {@At("RETURN")}
   )
   @CInline
   private void onAiStepReturn(CallbackInfo ci) {
      MovementBridge.restoreTickRotation((LocalPlayer)(Object)this);
   }

   @Inject(
      method = {"aiStep"},
      at = {@At("HEAD")}
   )
   @CInline
   private void onAiStepPre(CallbackInfo ci) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      RotationManager.INSTANCE.onPlayerTickHead(player);
      RotationHandler.startTick();
      RotationHandler.setOriginal(player.getYRot(), player.getXRot());
      if (!ServerRotationHandler.isHasSilentRotation()) {
         ServerRotationHandler.updateServerRotations(player.getYRot(), Mth.clamp(player.getXRot(), -90.0F, 90.0F));
      }

      MotionEvent event = ClientEventDispatcher.onPlayerAiStepHead(player);
      if (event != null) {
         if (!ServerRotationHandler.isHasSilentRotation()
            && (event.getYaw() != RotationHandler.getOriginalYaw() || event.getPitch() != RotationHandler.getOriginalPitch())) {
            RotationHandler.setSilent(event.getYaw(), event.getPitch(), 10.0F);
         }

         if (RotationHandler.isSilentActive() && player.input != null) {
            float forward = (player.input.keyPresses.forward() ? 1.0F : 0.0F) - (player.input.keyPresses.backward() ? 1.0F : 0.0F);
            float strafe = (player.input.keyPresses.left() ? 1.0F : 0.0F) - (player.input.keyPresses.right() ? 1.0F : 0.0F);
            if (forward != 0.0F || strafe != 0.0F) {
               double moveAngleRad = Math.atan2(-strafe, forward);
               float moveAngleDeg = (float)Math.toDegrees(moveAngleRad);
               if (Math.abs(moveAngleDeg) > 35.0F && player.isSprinting() && !MovementBridge.isSprintFixActive()) {
                  player.setSprinting(false);
               }
            }
         }

         MovementBridge.prepareMovementCorrection(player);
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At("RETURN")}
   )
   @CInline
   private void onPlayerTickReturn(CallbackInfo ci) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      if (ServerRotationHandler.isHasSilentRotation()) {
         player.yHeadRot = ServerRotationHandler.getServerYaw();
         player.yBodyRot = RotationHandler.getRenderBodyYaw();
      } else if (RotationHandler.isSilentHeld()) {
         player.yHeadRot = RotationHandler.getRenderYaw();
         player.yBodyRot = RotationHandler.getRenderBodyYaw();
      } else {
         RotationHandler.clearRenderBodyYaw();
      }

      RotationManager.INSTANCE.onPlayerTickReturn(player);
   }
}
