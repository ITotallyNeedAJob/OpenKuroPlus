package kat.respect.b2.mixin;

import kat.respect.b2.bridge.XRayBridge;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ModelBlockRenderer.class})
public class ModelBlockRendererMixin {
   @Inject(
      method = {"shouldRenderFace"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void respectB2$xray(BlockAndTintGetter level, BlockState state, Direction direction, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      int decision = XRayBridge.decide(state);
      if (decision == 1) {
         cir.setReturnValue(true);
      } else if (decision == -1) {
         cir.setReturnValue(false);
      }
   }
}
