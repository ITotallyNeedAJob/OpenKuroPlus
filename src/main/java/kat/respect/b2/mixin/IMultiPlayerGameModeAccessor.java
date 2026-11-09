package kat.respect.b2.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({MultiPlayerGameMode.class})
public interface IMultiPlayerGameModeAccessor {
   @Accessor("carriedIndex")
   int getCarriedIndex();

   @Accessor("carriedIndex")
   void setCarriedIndex(int var1);

   @Invoker("ensureHasSentCarriedItem")
   void invokeEnsureHasSentCarriedItem();
}
