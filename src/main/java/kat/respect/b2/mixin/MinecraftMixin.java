package kat.respect.b2.mixin;

import kat.respect.b2.event.ClientEventDispatcher;
import kat.respect.b2.event.events.ClickAttackEvent;
import kat.respect.b2.event.events.ItemUseEvent;
import kat.respect.b2.rotation.RotationManager;
import kat.respect.b2.util.ScreenUtil;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Minecraft.class})
public abstract class MinecraftMixin {
   @Inject(
      method = {"startUseItem"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   @CInline
   private void respectB2$onStartUseItem(CallbackInfo ci) {
      Minecraft mc = (Minecraft)(Object)this;
      if (mc.player != null && mc.level != null && !ScreenUtil.hasScreen(mc)) {
         ItemUseEvent event = ClientEventDispatcher.onItemUse();
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Minecraft;handleKeybinds()V",
         shift = Shift.BEFORE
      )},
      require = 0
   )
   @CInline
   private void respectB2$onHandleKeybindsEarly(CallbackInfo ci) {
      ClientEventDispatcher.onHandleKeybindsEarly();
   }

   @Inject(
      method = {"handleKeybinds"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Minecraft;startUseItem()V",
         shift = Shift.BEFORE
      )},
      cancellable = true
   )
   @CInline
   private void respectB2$blockManualPlacement(CallbackInfo ci) {
      Minecraft mc = (Minecraft)(Object)this;
      if (mc.player != null && mc.level != null && !ScreenUtil.hasScreen(mc)) {
         if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK) {
            if (RotationManager.getPacketRotation() != null) {
               ci.cancel();
            }
         }
      }
   }

   @Inject(
      method = {"startAttack"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   @CInline
   private void respectB2$onStartAttack(CallbackInfoReturnable<Boolean> cir) {
      Minecraft mc = (Minecraft)(Object)this;
      if (mc.player != null && mc.level != null && !ScreenUtil.hasScreen(mc)) {
         ClickAttackEvent clickEvent = ClientEventDispatcher.onClickAttack();
         if (clickEvent.isCancelled()) {
            cir.setReturnValue(false);
         }
      }
   }
}
