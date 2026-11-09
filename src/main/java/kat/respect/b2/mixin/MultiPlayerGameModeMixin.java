package kat.respect.b2.mixin;

import kat.respect.b2.event.ClientEventDispatcher;
import kat.respect.b2.event.events.AttackEvent;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MultiPlayerGameMode.class})
public class MultiPlayerGameModeMixin {
   @Inject(
      method = {"attack"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void respectB2$onAttackEntity(Player player, Entity target, CallbackInfo ci) {
      if (target instanceof LivingEntity living) {
         AttackEvent event = ClientEventDispatcher.onAttack(living);
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }
}
