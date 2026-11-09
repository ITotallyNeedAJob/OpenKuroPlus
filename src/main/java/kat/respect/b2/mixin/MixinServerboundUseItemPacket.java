package kat.respect.b2.mixin;

import kat.respect.b2.util.SentRotation;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerboundUseItemPacket.class})
public abstract class MixinServerboundUseItemPacket {
   @Mutable
   @Shadow
   @Final
   private float yRot;
   @Mutable
   @Shadow
   @Final
   private float xRot;

   @Inject(
      method = {"<init>(Lnet/minecraft/world/InteractionHand;IFF)V"},
      at = {@At("RETURN")}
   )
   private void respectB2$modifyRotation(InteractionHand hand, int sequence, float yRot, float xRot, CallbackInfo ci) {
      if (SentRotation.isSpoofed()) {
         this.yRot = SentRotation.yaw();
         this.xRot = SentRotation.pitch();
      }
   }
}
