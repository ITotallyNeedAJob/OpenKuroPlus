package kat.respect.b2.rotation.builder;

import kat.respect.b2.module.Module;
import kat.respect.b2.rotation.RotationManager;
import kat.respect.b2.util.RotationUtils;

public class RestoreBuilder {
   private final Module requester;
   private float speed = 180.0F;
   private boolean silent = true;
   private RotationUtils.AimType aimType = RotationUtils.AimType.REGULAR;
   private boolean scaleSpeed = true;
   private Runnable onComplete;

   public RestoreBuilder(Module requester) {
      this.requester = requester;
   }

   public RestoreBuilder speed(float s) {
      this.speed = s;
      return this;
   }

   public RestoreBuilder instant() {
      this.aimType = RotationUtils.AimType.BLATANT;
      return this;
   }

   public RestoreBuilder silent(boolean s) {
      this.silent = s;
      return this;
   }

   public RestoreBuilder onComplete(Runnable r) {
      this.onComplete = r;
      return this;
   }

   public boolean execute() {
      RotationManager rm = RotationManager.INSTANCE;
      if (this.requester != null) {
         rm.releaseOwned(this.requester);
      } else if (rm.getActiveController() != null) {
         rm.releaseController(rm.getActiveController());
      }

      if (rm.isStillReturning()) {
         return true;
      } else {
         if (this.onComplete != null) {
            this.onComplete.run();
         }

         return false;
      }
   }
}
