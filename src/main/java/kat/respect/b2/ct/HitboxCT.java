package kat.respect.b2.ct;

import kat.respect.b2.bridge.HitboxBridge;
import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CRedirect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;

@CTransformer({ProjectileUtil.class})
public class HitboxCT {
   @CInline
   @CRedirect(
      method = {"getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;"},
      target = @CTarget(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"
      )
   )
   public static AABB respectB2$expandHitbox(Entity entity) {
      return HitboxBridge.expand(entity);
   }
}
