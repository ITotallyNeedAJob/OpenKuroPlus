package kat.respect.b2.rotation;

import net.minecraft.world.phys.Vec3;

public interface WorldPointTarget {
   Vec3 getTarget();

   void setTarget(Vec3 var1);

   default void setTarget(double x, double y, double z) {
      this.setTarget(new Vec3(x, y, z));
   }
}
