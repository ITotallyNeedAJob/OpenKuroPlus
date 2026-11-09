package kat.respect.b2.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import kat.respect.b2.bridge.MovementBridge;
import kat.respect.b2.event.ClientEventDispatcher;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin {
   @ModifyExpressionValue(
      method = {"jumpFromGround"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"
      )}
   )
   @CInline
   private float respectB2$jumpBoostYaw(float original) {
      return (Object)this != Minecraft.getInstance().player ? original : MovementBridge.getMovementYaw(original);
   }

   @Inject(
      method = {"handleEntityEvent"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/LivingEntity;onKineticHit()V"
      )}
   )
   @CInline
   private void respectB2$onKineticHit(CallbackInfo ci) {
      if ((Object)this == Minecraft.getInstance().player) {
         ClientEventDispatcher.onKineticHit();
      }
   }
}
