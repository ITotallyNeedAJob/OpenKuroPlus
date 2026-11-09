package kat.respect.b2.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({Minecraft.class})
public interface IMinecraftAccessor {
   @Accessor("missTime")
   int getMissTime();

   @Accessor("missTime")
   void setMissTime(int var1);

   @Accessor("rightClickDelay")
   int getRightClickDelay();

   @Accessor("rightClickDelay")
   void setRightClickDelay(int var1);

   @Invoker("startUseItem")
   void invokeStartUseItem();

   @Invoker("startAttack")
   boolean invokeStartAttack();
}
