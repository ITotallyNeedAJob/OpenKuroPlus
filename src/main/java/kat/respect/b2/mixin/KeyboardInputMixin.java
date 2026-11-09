package kat.respect.b2.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import kat.respect.b2.bridge.MovementBridge;
import kat.respect.b2.event.ClientEventDispatcher;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({KeyboardInput.class})
public abstract class KeyboardInputMixin extends ClientInput {
   @ModifyExpressionValue(
      method = {"tick"},
      at = {@At(
         value = "NEW",
         target = "(ZZZZZZZ)Lnet/minecraft/world/entity/player/Input;"
      )}
   )
   @CInline
   private Input respectB2$modifyMovementInput(Input original) {
      Input input = MovementBridge.correctInput(original);
      return ClientEventDispatcher.onModifyInput(input);
   }
}
