package kat.respect.b2.rotation;

import net.minecraft.world.phys.Vec3;

public class AdaptiveRotController extends AdaptiveRotationController {
   public AdaptiveRotController(float yaw, float pitch) {
      super(yaw, pitch);
   }

   public AdaptiveRotController() {
   }

   public AdaptiveRotController(Vec3 target) {
      super(target);
   }
}
