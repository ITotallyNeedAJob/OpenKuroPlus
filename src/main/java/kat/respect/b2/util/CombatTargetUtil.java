package kat.respect.b2.util;

import net.minecraft.world.entity.Entity;

@Deprecated
public final class CombatTargetUtil {
   private CombatTargetUtil() {
   }

   public static boolean inWeb(Entity entity) {
      return TargetUtil.inWeb(entity);
   }

   public static boolean isObstructed(Entity self, Entity target, CombatTargetUtil.Obstruction mode, double reach) {
      TargetUtil.Obstruction var10000;
      if (mode == null) {
         var10000 = TargetUtil.Obstruction.OFF;
      } else {
         switch (mode) {
            case CRYSTALS:
               var10000 = TargetUtil.Obstruction.CRYSTALS;
               break;
            case ALL:
               var10000 = TargetUtil.Obstruction.ALL;
               break;
            default:
               var10000 = TargetUtil.Obstruction.OFF;
         }
      }

      TargetUtil.Obstruction targetMode = var10000;
      return TargetUtil.isObstructed(self, target, targetMode, reach);
   }

   public static enum Obstruction {
      OFF,
      CRYSTALS,
      ALL;
   }
}
