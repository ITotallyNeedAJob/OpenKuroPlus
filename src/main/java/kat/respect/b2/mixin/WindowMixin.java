package kat.respect.b2.mixin;

import kat.respect.b2.event.ClientEventDispatcher;
import kat.respect.b2.event.EventBus;
import kat.respect.b2.event.events.Render2DEvent;
import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.render.Renderer2D;
import kat.respect.b2.render.mobile.MobileDraw2D;
import kat.respect.b2.util.ScreenUtil;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public class WindowMixin {
   @Inject(
      method = {"renderFrame"},
      at = {@At("HEAD")}
   )
   @CInline
   private void kuro$beginFrame(boolean advanceGameTime, CallbackInfo ci) {
      ClientEventDispatcher.beginFrame();
   }

   @Inject(
      method = {"renderFrame"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen()V",
         shift = Shift.AFTER
      )},
      require = 1
   )
   @CInline
   private void respectB2$renderOverlay(boolean advanceGameTime, CallbackInfo ci) {
      if (!Renderer2D.isForceVulkan()) {
         Minecraft mc = (Minecraft)(Object)this;
         if (mc.getWindow() != null) {
            float screenW = mc.getWindow().getGuiScaledWidth();
            float screenH = mc.getWindow().getGuiScaledHeight();
            Screen currentScreen = ScreenUtil.getScreen(mc);
            if (currentScreen instanceof ClickGuiScreen gui) {
               gui.renderMobile();
            }

            if (EventBus.INSTANCE.hasListeners(Render2DEvent.class)) {
               boolean isMobile = Renderer2D.isMobileActive();
               if (isMobile && !(currentScreen instanceof ClickGuiScreen)) {
                  MobileDraw2D draw2D = MobileDraw2D.getInstance();
                  if (draw2D.needsBlur()) {
                     try {
                        draw2D.computeBlur();
                     } catch (Throwable var10) {
                     }
                  }
               }

               Renderer2D.getMobile().begin(screenW, screenH, 1.0F, 1.0F);
               ClientEventDispatcher.onRender2D(null, mc.getDeltaTracker());
               Renderer2D.getMobile().end();
            }
         }
      }
   }
}
