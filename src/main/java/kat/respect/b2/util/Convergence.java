package kat.respect.b2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class Convergence {
   private static final Minecraft mc = Minecraft.getInstance();
   private float prevPlayerYaw;
   private float prevPlayerPitch;
   private float prevServerYaw;
   private float prevServerPitch;
   private float lastDeltaPlayerYaw;
   private float lastDeltaPlayerPitch;
   private float lastDeltaServerYaw;
   private float lastDeltaServerPitch;
   private float lastInteractionPitchDelta;
   private boolean primed;
   private boolean hasInteractionPitchDelta;

   public void reset() {
      this.primed = false;
      this.hasInteractionPitchDelta = false;
   }

   public void update() {
      if (mc.player == null) {
         this.primed = false;
      } else {
         float curPlayerYaw = mc.player.getYRot();
         float curPlayerPitch = mc.player.getXRot();
         float curServerYaw = RotationHandler.getSilentYaw();
         float curServerPitch = RotationHandler.getSilentPitch();
         if (this.primed) {
            this.lastDeltaPlayerYaw = Math.abs(Mth.wrapDegrees(curPlayerYaw - this.prevPlayerYaw));
            this.lastDeltaPlayerPitch = Math.abs(curPlayerPitch - this.prevPlayerPitch);
            this.lastDeltaServerYaw = Math.abs(Mth.wrapDegrees(curServerYaw - this.prevServerYaw));
            this.lastDeltaServerPitch = Math.abs(curServerPitch - this.prevServerPitch);
         } else {
            this.lastDeltaPlayerYaw = this.lastDeltaPlayerPitch = Float.MAX_VALUE;
            this.lastDeltaServerYaw = this.lastDeltaServerPitch = Float.MAX_VALUE;
         }

         this.prevPlayerYaw = curPlayerYaw;
         this.prevPlayerPitch = curPlayerPitch;
         this.prevServerYaw = curServerYaw;
         this.prevServerPitch = curServerPitch;
         this.primed = true;
      }
   }

   public boolean isSettled(float threshold) {
      if (!this.primed) {
         return false;
      } else {
         boolean silent = RotationHandler.isSilentActive();
         float deltaYaw = silent ? this.lastDeltaServerYaw : this.lastDeltaPlayerYaw;
         float deltaPitch = silent ? this.lastDeltaServerPitch : this.lastDeltaPlayerPitch;
         return deltaYaw < threshold && deltaPitch < threshold;
      }
   }

   public boolean isPlaceSafe(float threshold) {
      if (!this.primed) {
         return false;
      } else {
         float deltaPitch = this.pitchDelta();
         return deltaPitch < threshold ? true : !this.hasInteractionPitchDelta || Math.abs(deltaPitch - this.lastInteractionPitchDelta) >= 0.001F;
      }
   }

   public boolean isDuplicateRotPlaceSafe(float threshold) {
      return this.isPlaceSafe(threshold);
   }

   public void markInteract() {
      if (this.primed) {
         this.lastInteractionPitchDelta = this.pitchDelta();
         this.hasInteractionPitchDelta = true;
      }
   }

   public void markInteraction() {
      this.markInteract();
   }

   public float pitchDelta() {
      return RotationHandler.isSilentActive() ? this.lastDeltaServerPitch : this.lastDeltaPlayerPitch;
   }
}
