package kat.respect.b2.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public final class EntityTypeCompat {
   private EntityTypeCompat() {
   }

   public static EntityType<?> enderPearl() {
      return byId("minecraft:ender_pearl");
   }

   public static EntityType<?> endCrystal() {
      return byId("minecraft:end_crystal");
   }

   private static EntityType<?> byId(String id) {
      try {
         Identifier ident = Identifier.tryParse(id);
         return ident == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(ident);
      } catch (Throwable var2) {
         return null;
      }
   }
}
