package kat.respect.b2.rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class FixedRotationController extends MouseRotationController {
   public static final float UNSET_ROTATION = -999.0F;
   public float targetYaw;
   public float targetPitch;
   private boolean clampStepToRemaining;
   private boolean scaleAxesProportionally;
   private boolean restoreCapturedRotation;
   private boolean cubicAcceleration;
   private boolean linearAcceleration;
   private boolean angleBasedAcceleration;

   public FixedRotationController(float yaw, float pitch) {
      this.targetYaw = yaw;
      this.targetPitch = pitch;
   }

   public FixedRotationController(RotationAngles rotationAngles) {
      this(rotationAngles.getYaw(), rotationAngles.getPitch());
   }

   public float getTargetYaw() {
      return this.targetYaw;
   }

   public float getTargetPitch() {
      return this.targetPitch;
   }

   public void setTargetRotation(float yaw, float pitch) {
      this.targetYaw = yaw;
      this.targetPitch = pitch;
      this.setComplete(false);
   }

   public void setTargetRotation(RotationAngles rotationAngles) {
      this.setTargetRotation(rotationAngles.getYaw(), rotationAngles.getPitch());
   }

   public float getCurrentYaw() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null ? mc.player.getYRot() : 0.0F;
   }

   public float getCurrentPitch() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null ? mc.player.getXRot() : 0.0F;
   }

   public void setClampStepToRemaining(boolean enabled) {
      this.clampStepToRemaining = enabled;
   }

   public boolean isStepClampedToRemaining() {
      return this.clampStepToRemaining;
   }

   public void setScaleAxesProportionally(boolean enabled) {
      this.scaleAxesProportionally = enabled;
   }

   public boolean isAxisScalingEnabled() {
      return this.scaleAxesProportionally;
   }

   public void setRestoreCapturedRotation(boolean restore) {
      this.restoreCapturedRotation = restore;
   }

   public boolean shouldRestoreCapturedRotation() {
      return this.restoreCapturedRotation;
   }

   public void setCubicAcceleration(boolean enabled) {
      this.cubicAcceleration = enabled;
   }

   public boolean isCubicAccelerationEnabled() {
      return this.cubicAcceleration;
   }

   public void setLinearAcceleration(boolean enabled) {
      this.linearAcceleration = enabled;
   }

   public boolean isLinearAccelerationEnabled() {
      return this.linearAcceleration;
   }

   public void setAngleBasedAcceleration(boolean enabled) {
      this.angleBasedAcceleration = enabled;
   }

   public boolean isAngleBasedAccelerationEnabled() {
      return this.angleBasedAcceleration;
   }

   @Override
   public boolean updateYaw() {
      if (this.targetYaw == -999.0F) {
         return true;
      } else {
         float mouseScale = this.getMouseScale();
         float predictedYaw = this.getCurrentYaw() + (int)this.pendingYawDelta * mouseScale * 0.15F;
         float predictedPitch = this.getCurrentPitch() - (int)(-this.pendingPitchDelta) * mouseScale * 0.15F;
         double yawError = Mth.wrapDegrees((this.targetYaw - predictedYaw) % 360.0F);
         double pitchError = Mth.wrapDegrees((this.targetPitch - predictedPitch) % 360.0F);
         double absoluteYawError = Math.abs(yawError);
         if (!this.isOutsideTolerance(absoluteYawError)) {
            return true;
         } else {
            double step = this.getSpeed() * 0.25;
            double axisRatio = absoluteYawError / Math.abs(pitchError);
            if (this.scaleAxesProportionally && axisRatio < 1.0) {
               step *= axisRatio;
            }

            step = this.applyYawAcceleration(step, absoluteYawError);
            this.pendingYawDelta = this.addPendingStep(this.pendingYawDelta, yawError, step);
            return false;
         }
      }
   }

   @Override
   public boolean updatePitch() {
      if (this.targetPitch == -999.0F) {
         return true;
      } else {
         float currentPitch = this.getCurrentPitch() == -90.0F ? -89.99F : this.getCurrentPitch();
         float mouseScale = this.getMouseScale();
         float predictedYaw = this.getCurrentYaw() + (int)this.pendingYawDelta * mouseScale * 0.15F;
         float predictedPitch = currentPitch - (int)(-this.pendingPitchDelta) * mouseScale * 0.15F;
         double yawError = Mth.wrapDegrees((this.targetYaw - predictedYaw) % 360.0F);
         double pitchError = Mth.wrapDegrees((this.targetPitch - predictedPitch) % 360.0F);
         double absolutePitchError = Math.abs(pitchError);
         if (!this.isOutsideTolerance(absolutePitchError)) {
            return true;
         } else {
            double step = this.getSpeed() * 0.25;
            double axisRatio = absolutePitchError / Math.abs(yawError);
            if (this.scaleAxesProportionally && axisRatio < 1.0) {
               step *= axisRatio;
            }

            step = this.applyPitchAcceleration(step, absolutePitchError);
            this.pendingPitchDelta = this.addPendingStep(this.pendingPitchDelta, pitchError, step);
            return false;
         }
      }
   }

   @Override
   public void onPreMouseUpdate() {
      Minecraft mc = Minecraft.getInstance();
      if (this.shouldRestoreCapturedRotation() && mc.player != null) {
         if (mc.player.getYRot() != this.savedYaw) {
            mc.player.setYRot(this.savedYaw);
         }

         if (mc.player.yRotO != this.savedPreviousYaw) {
            mc.player.yRotO = this.savedPreviousYaw;
         }

         if (mc.player.getXRot() != this.savedPitch) {
            mc.player.setXRot(this.savedPitch);
         }

         if (mc.player.xRotO != this.savedPreviousPitch) {
            mc.player.xRotO = this.savedPreviousPitch;
         }
      }
   }

   protected double applyYawAcceleration(double step, double absoluteError) {
      if (this.angleBasedAcceleration) {
         return step * (225.0 + absoluteError) / 180.0;
      } else if (this.linearAcceleration) {
         return step + absoluteError * 0.05;
      } else if (!this.cubicAcceleration) {
         return step;
      } else {
         double normalizedError = absoluteError / 100.0;
         double shiftedError = normalizedError + 0.7;
         double multiplier = 0.4 + 2.0 * Math.pow(shiftedError, 3.0) + Math.pow(shiftedError, 2.0);
         return step * Math.min(Math.max(1.0, multiplier), 4.0);
      }
   }

   protected double applyPitchAcceleration(double step, double absoluteError) {
      if (this.angleBasedAcceleration) {
         return step * (135.0 + absoluteError) / 90.0;
      } else if (this.linearAcceleration) {
         return step + absoluteError * 0.05;
      } else if (!this.cubicAcceleration) {
         return step;
      } else {
         double normalizedError = absoluteError / 75.0;
         double shiftedError = normalizedError + 0.7;
         double multiplier = 0.4 + 2.0 * Math.pow(shiftedError, 3.0) + Math.pow(shiftedError, 2.0);
         return step * Math.max(1.0, multiplier);
      }
   }

   @Override
   protected float addPendingStep(float pendingDelta, double error, double step) {
      if (!this.clampStepToRemaining) {
         return (float)(error > 0.0 ? pendingDelta + step : pendingDelta - step);
      } else {
         double remainingSteps = Math.abs(error / (this.getMouseScale() * 0.15F));
         double appliedStep = Math.min(step, remainingSteps);
         return (float)(error > 0.0 ? pendingDelta + appliedStep : pendingDelta - appliedStep);
      }
   }
}
