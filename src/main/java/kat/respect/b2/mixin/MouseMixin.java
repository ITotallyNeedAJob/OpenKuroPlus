package kat.respect.b2.mixin;

import kat.respect.b2.bridge.ZoomBridge;
import kat.respect.b2.event.ClientEventDispatcher;
import kat.respect.b2.util.ScreenUtil;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MouseHandler.class})
public class MouseMixin {
   @Inject(
      method = {"onButton"},
      at = {@At("HEAD")}
   )
   @CInline
   private void onMouseClick(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
      ClientEventDispatcher.onMouseButton(buttonInfo.button(), action);
   }

   @Inject(
      method = {"onScroll"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void respectB2$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
      if (!ScreenUtil.hasScreen() && ZoomBridge.onMouseScroll(vertical)) {
         ci.cancel();
      }
   }
}
