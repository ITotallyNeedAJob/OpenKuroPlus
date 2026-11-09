package kat.respect.b2.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import kat.respect.b2.bridge.ReachBridge;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LocalPlayer.class})
public class ReachMixin {
   @ModifyExpressionValue(
      method = {"raycastHitResult"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/LocalPlayer;entityInteractionRange()D"
      )}
   )
   @CInline
   private double respectB2$extendReach(double original) {
      return Math.max(ReachBridge.getActiveReachDistance(), original);
   }

   @Inject(
      method = {"raycastHitResult"},
      at = {@At("RETURN")},
      cancellable = true
   )
   @CInline
   private void respectB2$verticalCheck(float partialTick, Entity camera, CallbackInfoReturnable<HitResult> cir) {
      if (ReachBridge.isVerticalCheckEnabled()) {
         LocalPlayer player = (LocalPlayer)(Object)this;
         if (cir.getReturnValue() instanceof EntityHitResult ehr) {
            Entity target = ehr.getEntity();
            if (Math.abs(target.getY() - player.getY()) > 0.2) {
               cir.setReturnValue(BlockHitResult.miss(player.getEyePosition(), Direction.UP, BlockPos.ZERO));
            }
         }
      }
   }
}
