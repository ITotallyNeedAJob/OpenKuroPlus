package kat.respect.b2.mixin;

import kat.respect.b2.bridge.HitboxBridge;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ProjectileUtil.class})
public class HitboxMixin {
   @Redirect(
      method = {"getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"
      )
   )
   @CInline
   private static AABB respectB2$expandHitbox(Entity entity) {
      return HitboxBridge.expand(entity);
   }
}
