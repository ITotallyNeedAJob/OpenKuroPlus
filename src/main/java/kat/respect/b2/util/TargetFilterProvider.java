package kat.respect.b2.util;

import net.minecraft.world.entity.LivingEntity;

public interface TargetFilterProvider {
   boolean allowTarget(LivingEntity var1);
}
