package kat.respect.b2.mixin;

import kat.respect.b2.bridge.FreeLookBridge;
import kat.respect.b2.bridge.ZoomBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public abstract class FreeLookMixin {
   @ModifyVariable(
      method = {"turn(DD)V"},
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private double respectB2$zoomScaleTurnX(double xDelta) {
      return (Object)this == Minecraft.getInstance().player && ZoomBridge.shouldScaleSensitivity() ? xDelta * ZoomBridge.getSensitivityScale() : xDelta;
   }

   @ModifyVariable(
      method = {"turn(DD)V"},
      at = @At("HEAD"),
      ordinal = 1,
      argsOnly = true
   )
   private double respectB2$zoomScaleTurnY(double yDelta) {
      return (Object)this == Minecraft.getInstance().player && ZoomBridge.shouldScaleSensitivity() ? yDelta * ZoomBridge.getSensitivityScale() : yDelta;
   }

   @Inject(
      method = {"turn(DD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void respectB2$freeLookTurn(double xDelta, double yDelta, CallbackInfo ci) {
      if ((Object)this == Minecraft.getInstance().player) {
         if (FreeLookBridge.capturesTurn() && !FreeLookBridge.isProgrammatic()) {
            FreeLookBridge.applyTurn(xDelta, yDelta);
            ci.cancel();
         }
      }
   }
}
