package kat.respect.b2.mixin;

import kat.respect.b2.event.ClientEventDispatcher;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardHandler.class})
public class KeyboardMixin {
   @Inject(
      method = {"keyPress"},
      at = {@At("HEAD")}
   )
   @CInline
   private void onKey(long window, int action, KeyEvent event, CallbackInfo ci) {
      if (event.key() != -1) {
         ClientEventDispatcher.onKey(event.key(), action);
      }
   }
}
