package kat.respect.b2.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import kat.respect.b2.bridge.MovementBridge;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Entity.class})
public abstract class EntityMixin {
   @ModifyExpressionValue(
      method = {"moveRelative"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/Entity;getYRot()F"
      )}
   )
   @CInline
   private float respectB2$movementBasisYaw(float original) {
      return (Object)this != Minecraft.getInstance().player ? original : MovementBridge.getMovementYaw(original);
   }
}
