package kat.respect.b2.mixin;

import kat.respect.b2.bridge.FreeLookBridge;
import kat.respect.b2.bridge.ZoomBridge;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Camera.class})
public abstract class CameraMixin {
   @Shadow
   private boolean detached;

   @Shadow
   protected abstract void setRotation(float var1, float var2);

   @Shadow
   protected abstract void setPosition(double var1, double var3, double var5);

   @Shadow
   public abstract Vec3 position();

   @Shadow
   public abstract boolean isDetached();

   @Inject(
      method = {"alignWithEntity(F)V"},
      at = {@At("HEAD")}
   )
   @CInline
   private void respectB2$beginCameraFrame(float tickDelta, CallbackInfo ci) {
      FreeLookBridge.beginCameraFrame(tickDelta);
   }

   @Inject(
      method = {"alignWithEntity(F)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
         shift = Shift.AFTER
      )}
   )
   @CInline
   private void respectB2$captureEyeAnchor(CallbackInfo ci) {
      this.captureEyeAnchor();
   }

   @Inject(
      method = {"alignWithEntity(F)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Camera;setPosition(Lnet/minecraft/world/phys/Vec3;)V",
         shift = Shift.AFTER
      )}
   )
   @CInline
   private void respectB2$captureEyeAnchorRiding(CallbackInfo ci) {
      this.captureEyeAnchor();
   }

   @Inject(
      method = {"alignWithEntity(F)V"},
      at = {@At("TAIL")}
   )
   @CInline
   private void respectB2$applyFreeCamera(CallbackInfo ci) {
      if (!this.isDetached()) {
         this.captureEyeAnchor();
      }

      FreeLookBridge.onFrame();
      Minecraft mc = Minecraft.getInstance();
      boolean isFirstPerson = mc.options != null && mc.options.getCameraType().isFirstPerson();
      if (FreeLookBridge.isActive()) {
         float yaw = FreeLookBridge.getCameraYaw();
         float pitch = FreeLookBridge.getCameraPitch();
         if (!Float.isNaN(yaw) && !Float.isNaN(pitch)) {
            this.setRotation(yaw, pitch);
         }

         if (FreeLookBridge.overridesPosition()) {
            double x = FreeLookBridge.getCameraX();
            double y = FreeLookBridge.getCameraY();
            double z = FreeLookBridge.getCameraZ();
            if (!Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z)) {
               this.setPosition(x, y, z);
            }
         }
      } else if (isFirstPerson && this.detached) {
         this.detached = false;
         Vec3 eye = null;
         if (FreeLookBridge.hasEyeAnchor()) {
            eye = new Vec3(FreeLookBridge.getEyeAnchorX(), FreeLookBridge.getEyeAnchorY(), FreeLookBridge.getEyeAnchorZ());
         } else if (mc.player != null) {
            eye = mc.player.getEyePosition(FreeLookBridge.getPartialTick());
         }

         if (eye != null) {
            this.setPosition(eye.x, eye.y, eye.z);
         }

         if (mc.player != null) {
            this.setRotation(mc.player.getYRot(FreeLookBridge.getPartialTick()), mc.player.getXRot(FreeLookBridge.getPartialTick()));
         }
      }
   }

   @CInline
   private void captureEyeAnchor() {
      Vec3 pos = this.position();
      if (pos != null) {
         FreeLookBridge.setEyeAnchor(pos.x, pos.y, pos.z);
      }
   }

   @Inject(
      method = {"calculateFov(F)F"},
      at = {@At("RETURN")},
      cancellable = true
   )
   @CInline
   private void respectB2$zoomFov(float tickDelta, CallbackInfoReturnable<Float> cir) {
      if (ZoomBridge.isActive()) {
         cir.setReturnValue((float)ZoomBridge.getFov(((Float)cir.getReturnValue()).floatValue()));
      }
   }

   @Inject(
      method = {"getMaxZoom(F)F"},
      at = {@At("RETURN")},
      cancellable = true
   )
   @CInline
   private void respectB2$zoomMaxDistance(float original, CallbackInfoReturnable<Float> cir) {
      if (ZoomBridge.isThirdPersonActive()) {
         cir.setReturnValue((float)ZoomBridge.getThirdPersonDistance(((Float)cir.getReturnValue()).floatValue()));
      }
   }
}
