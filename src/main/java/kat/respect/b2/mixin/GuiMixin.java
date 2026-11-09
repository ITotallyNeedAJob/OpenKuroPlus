package kat.respect.b2.mixin;

import kat.respect.b2.event.ClientEventDispatcher;
import kat.respect.b2.render.Renderer2D;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Gui.class})
public class GuiMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"},
      at = {@At("RETURN")}
   )
   private void respectB2$postRender2D(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      if (Renderer2D.isForceVulkan()) {
         Renderer2D.getBlaze3D().setGraphics(graphics);
         ClientEventDispatcher.beginFrame();
         ClientEventDispatcher.onRender2D(graphics, deltaTracker);
      }
   }

   @Inject(
      method = {"extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"},
      at = {@At("RETURN")}
   )
   private void respectB2$postRenderGui2D(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      ClientEventDispatcher.onRenderGui2D(graphics);
   }
}
