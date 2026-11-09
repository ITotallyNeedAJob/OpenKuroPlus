package kat.respect.b2.mixin;

import kat.respect.b2.bridge.MovementBridge;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientInput.class})
public abstract class ClientInputMixin {
   @Inject(
      method = {"hasForwardImpulse"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void respectB2$keepSprintWhileCorrecting(CallbackInfoReturnable<Boolean> cir) {
      if (MovementBridge.shouldKeepSprint() && MovementBridge.rawHasForwardImpulse()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && (Object)this == mc.player.input) {
            cir.setReturnValue(true);
         }
      }
   }
}
