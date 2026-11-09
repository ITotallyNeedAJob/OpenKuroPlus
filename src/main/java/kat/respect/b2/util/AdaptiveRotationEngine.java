package kat.respect.b2.util;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class AdaptiveRotationEngine {
   private static final Minecraft mc = Minecraft.getInstance();
   private float pendingYawDelta;
   private float pendingPitchDelta;
   private float targetYaw = 0.0F;
   private float targetPitch = 0.0F;
   private static final float UNSET_ROTATION = -999.0F;
   private double worldTargetX = Double.NaN;
   private double worldTargetY = Double.NaN;
   private double worldTargetZ = Double.NaN;
   private float speed = 1.0F;
   private float tolerance = 3.0F;
   private boolean clampStepToRemaining = true;
   private boolean scaleAxesProportionally = true;
   private AdaptiveRotationEngine.AccelerationMode accelerationMode = AdaptiveRotationEngine.AccelerationMode.CUBIC;
   private boolean complete = true;
   private float currentYaw = 0.0F;
   private float currentPitch = 0.0F;
   private final Random random = new Random();
   private boolean jitterEnabled = true;
   private float yawJitterPhase = 0.0F;
   private float pitchJitterPhase = 0.0F;
   private float yawJitterSpeed = 0.0F;
   private float pitchJitterSpeed = 0.0F;
   private float yawJitterAmplitude = 0.0F;
   private float pitchJitterAmplitude = 0.0F;
   private long lastJitterUpdateTime = 0L;
   private float yawDistance = 0.0F;
   private float pitchDistance = 0.0F;
   private float previousYaw = 0.0F;
   private float previousPitch = 0.0F;

   public AdaptiveRotationEngine() {
      if (mc.player != null) {
         this.currentYaw = mc.player.getYRot();
         this.currentPitch = mc.player.getXRot();
      }
   }

   public void reset() {
      this.complete = true;
      this.pendingYawDelta = 0.0F;
      this.pendingPitchDelta = 0.0F;
      this.targetYaw = -999.0F;
      this.targetPitch = -999.0F;
      this.yawDistance = 0.0F;
      this.pitchDistance = 0.0F;
      this.lastJitterUpdateTime = 0L;
      if (mc.player != null) {
         this.currentYaw = mc.player.getYRot();
         this.currentPitch = mc.player.getXRot();
      }
   }

   public AdaptiveRotationEngine setSpeed(float speed) {
      this.speed = speed;
      return this;
   }

   public AdaptiveRotationEngine speed(float speed) {
      return this.setSpeed(speed);
   }

   public AdaptiveRotationEngine setTolerance(float tolerance) {
      this.tolerance = tolerance;
      return this;
   }

   public AdaptiveRotationEngine tol(float tolerance) {
      return this.setTolerance(tolerance);
   }

   public AdaptiveRotationEngine setClampStepToRemaining(boolean v) {
      this.clampStepToRemaining = v;
      return this;
   }

   public AdaptiveRotationEngine setScaleAxesProportionally(boolean v) {
      this.scaleAxesProportionally = v;
      return this;
   }

   public AdaptiveRotationEngine setAccelerationMode(AdaptiveRotationEngine.AccelerationMode mode) {
      this.accelerationMode = mode;
      return this;
   }

   public AdaptiveRotationEngine setJitterEnabled(boolean v) {
      this.jitterEnabled = v;
      return this;
   }

   public AdaptiveRotationEngine setCurrentYaw(float yaw) {
      this.currentYaw = yaw;
      return this;
   }

   public AdaptiveRotationEngine yaw(float yaw) {
      return this.setCurrentYaw(yaw);
   }

   public AdaptiveRotationEngine setCurrentPitch(float pitch) {
      this.currentPitch = pitch;
      return this;
   }

   public AdaptiveRotationEngine pitch(float pitch) {
      return this.setCurrentPitch(pitch);
   }

   public boolean isComplete() {
      return this.complete;
   }

   public boolean complete() {
      return this.complete;
   }

   public float getCurrentYaw() {
      return this.currentYaw;
   }

   public float yaw() {
      return this.currentYaw;
   }

   public float getCurrentPitch() {
      return this.currentPitch;
   }

   public float pitch() {
      return this.currentPitch;
   }

   public float getPreviousYaw() {
      return this.previousYaw;
   }

   public float getPreviousPitch() {
      return this.previousPitch;
   }

   public float getTargetYaw() {
      return this.targetYaw;
   }

   public float getTargetPitch() {
      return this.targetPitch;
   }

   public float getYawDistance() {
      return this.yawDistance;
   }

   public float getPitchDistance() {
      return this.pitchDistance;
   }

   public float getPendingYawDelta() {
      return this.pendingYawDelta;
   }

   public float getPendingPitchDelta() {
      return this.pendingPitchDelta;
   }

   public void setTarget(float yaw, float pitch) {
      this.targetYaw = yaw;
      this.targetPitch = Mth.clamp(pitch, -90.0F, 90.0F);
      this.worldTargetX = Double.NaN;
      this.complete = false;
   }

   public void setWorldTarget(double x, double y, double z) {
      this.worldTargetX = x;
      this.worldTargetY = y;
      this.worldTargetZ = z;
      this.complete = false;
      this.recalcWorldTarget();
   }

   private void recalcWorldTarget() {
      if (!Double.isNaN(this.worldTargetX) && mc.player != null) {
         Vec3 eyes = mc.player.getEyePosition();
         float[] rots = getRotations(eyes.x, eyes.y, eyes.z, this.worldTargetX, this.worldTargetY, this.worldTargetZ);
         this.targetYaw = rots[0];
         this.targetPitch = Mth.clamp(rots[1], -90.0F, 90.0F);
      }
   }

   public boolean hasWorldTarget() {
      return !Double.isNaN(this.worldTargetX);
   }

   public void clearTarget() {
      this.targetYaw = -999.0F;
      this.targetPitch = -999.0F;
      this.worldTargetX = Double.NaN;
      this.complete = true;
   }

   public boolean update() {
      return this.update(1);
   }

   public boolean update(int iterations) {
      if (mc.player != null && !ScreenUtil.hasScreen(mc)) {
         this.recalcWorldTarget();
         this.previousYaw = this.currentYaw;
         this.previousPitch = this.currentPitch;
         if (this.jitterEnabled) {
            this.updateJitter();
         }

         int steps = Math.min(iterations, 200);

         for (int i = 0; i < steps; i++) {
            boolean yawComplete = this.updateYaw();
            boolean pitchComplete = this.updatePitch();
            if (yawComplete && pitchComplete && Math.abs(this.pendingYawDelta) < 1.0F && Math.abs(this.pendingPitchDelta) < 1.0F) {
               this.complete = true;
               this.flushPendingMovement();
               break;
            }

            this.flushPendingMovement();
            if (this.complete) {
               break;
            }
         }

         return this.complete;
      } else {
         return this.complete;
      }
   }

   private boolean updateYaw() {
      if (this.targetYaw == -999.0F) {
         return true;
      } else {
         float mouseScale = getMouseScale();
         float predictedYaw = this.currentYaw + (int)this.pendingYawDelta * mouseScale * 0.15F;
         float predictedPitch = this.currentPitch + (int)this.pendingPitchDelta * mouseScale * 0.15F;
         double yawError = Mth.wrapDegrees(this.targetYaw - predictedYaw);
         double pitchError = this.targetPitch - predictedPitch;
         double absoluteYawError = Math.abs(yawError);
         if (!this.isOutsideTolerance(absoluteYawError)) {
            return true;
         } else {
            double step = this.speed * 0.25;
            double axisRatio = absoluteYawError / Math.max(Math.abs(pitchError), 0.001);
            if (this.scaleAxesProportionally && axisRatio < 1.0) {
               step *= axisRatio;
            }

            step = this.applyYawAcceleration(step, absoluteYawError);
            this.pendingYawDelta = this.addPendingStep(this.pendingYawDelta, yawError, step);
            return false;
         }
      }
   }

   private boolean updatePitch() {
      if (this.targetPitch == -999.0F) {
         return true;
      } else {
         float effectivePitch = this.currentPitch == -90.0F ? -89.99F : (this.currentPitch == 90.0F ? 89.99F : this.currentPitch);
         float mouseScale = getMouseScale();
         float predictedYaw = this.currentYaw + (int)this.pendingYawDelta * mouseScale * 0.15F;
         float predictedPitch = effectivePitch + (int)this.pendingPitchDelta * mouseScale * 0.15F;
         double yawError = Mth.wrapDegrees(this.targetYaw - predictedYaw);
         double pitchError = this.targetPitch - predictedPitch;
         double absolutePitchError = Math.abs(pitchError);
         if (!this.isOutsideTolerance(absolutePitchError)) {
            return true;
         } else {
            double step = this.speed * 0.25;
            double axisRatio = absolutePitchError / Math.max(Math.abs(yawError), 0.001);
            if (this.scaleAxesProportionally && axisRatio < 1.0) {
               step *= axisRatio;
            }

            step = this.applyPitchAcceleration(step, absolutePitchError);
            this.pendingPitchDelta = this.addPendingStep(this.pendingPitchDelta, pitchError, step);
            return false;
         }
      }
   }

   private void flushPendingMovement() {
      if (this.jitterEnabled) {
         float jitterYaw = this.calculateYawJitter();
         float jitterPitch = this.calculatePitchJitter();
         this.pendingYawDelta += jitterYaw;
         this.pendingPitchDelta += jitterPitch;
      }

      int yawSteps = (int)this.pendingYawDelta;
      int pitchSteps = (int)this.pendingPitchDelta;
      float remainingYaw = this.pendingYawDelta - yawSteps;
      float remainingPitch = this.pendingPitchDelta - pitchSteps;
      float mouseScale = getMouseScale();
      float yawDelta = yawSteps * mouseScale;
      float pitchDelta = pitchSteps * mouseScale;
      this.currentYaw += yawDelta * 0.15F;
      this.currentPitch += pitchDelta * 0.15F;
      this.currentPitch = Mth.clamp(this.currentPitch, -90.0F, 90.0F);
      this.yawDistance = this.yawDistance + Math.abs(yawDelta * 0.15F);
      this.pitchDistance = this.pitchDistance + Math.abs(pitchDelta * 0.15F);
      this.pendingYawDelta = remainingYaw;
      this.pendingPitchDelta = remainingPitch;
   }

   public static float getMouseScale() {
      if (mc.options != null && mc.options.sensitivity() != null) {
         float sensitivityBase = mc.options.sensitivity().get().floatValue() * 0.6F + 0.2F;
         return sensitivityBase * sensitivityBase * sensitivityBase * 8.0F;
      } else {
         return 0.43307877F;
      }
   }

   public static float getSensitivityTimeScale() {
      float sensitivityBase = mc.options != null && mc.options.sensitivity() != null ? mc.options.sensitivity().get().floatValue() * 0.6F + 0.2F : 0.5F;
      float currentScale = sensitivityBase * sensitivityBase * sensitivityBase * 8.0F;
      float referenceBase = 0.5F;
      float referenceScale = referenceBase * referenceBase * referenceBase * 8.0F;
      return referenceScale / currentScale;
   }

   private boolean isOutsideTolerance(double absoluteError) {
      float rotationPerStep = getMouseScale() * 0.15F;
      long stepsToTarget = Math.round(absoluteError / rotationPerStep);
      long toleranceSteps = Math.max((long)Math.round(this.tolerance / rotationPerStep), 0L);
      return stepsToTarget > toleranceSteps;
   }

   private double applyYawAcceleration(double step, double absoluteError) {
      return switch (this.accelerationMode) {
         case ANGLE_BASED -> step * (225.0 + absoluteError) / 180.0;
         case LINEAR -> step + absoluteError * 0.05;
         case CUBIC -> {
            double normalizedError = absoluteError / 100.0;
            double shiftedError = normalizedError + 0.7;
            double multiplier = 0.4 + 2.0 * Math.pow(shiftedError, 3.0) + Math.pow(shiftedError, 2.0);
            yield step * Mth.clamp(multiplier, 1.0, 4.0);
         }
         default -> step;
      };
   }

   private double applyPitchAcceleration(double step, double absoluteError) {
      return switch (this.accelerationMode) {
         case ANGLE_BASED -> step * (135.0 + absoluteError) / 90.0;
         case LINEAR -> step + absoluteError * 0.05;
         case CUBIC -> {
            double normalizedError = absoluteError / 75.0;
            double shiftedError = normalizedError + 0.7;
            double multiplier = 0.4 + 2.0 * Math.pow(shiftedError, 3.0) + Math.pow(shiftedError, 2.0);
            yield step * Math.max(1.0, multiplier);
         }
         default -> step;
      };
   }

   private float addPendingStep(float pendingDelta, double error, double step) {
      if (!this.clampStepToRemaining) {
         return error > 0.0 ? pendingDelta + (float)step : pendingDelta - (float)step;
      } else {
         double remainingSteps = Math.abs(error / (getMouseScale() * 0.15F));
         double appliedStep = Math.min(step, remainingSteps);
         return error > 0.0 ? pendingDelta + (float)appliedStep : pendingDelta - (float)appliedStep;
      }
   }

   private void updateJitter() {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastJitterUpdateTime > 200 + this.random.nextInt(300)) {
         this.lastJitterUpdateTime = currentTime;
         this.yawJitterSpeed = 0.05F + this.random.nextFloat() * 0.15F;
         this.pitchJitterSpeed = 0.04F + this.random.nextFloat() * 0.12F;
         this.yawJitterAmplitude = 0.15F + this.random.nextFloat() * 0.25F;
         this.pitchJitterAmplitude = 0.1F + this.random.nextFloat() * 0.2F;
      }

      this.yawJitterPhase = this.yawJitterPhase + this.yawJitterSpeed;
      this.pitchJitterPhase = this.pitchJitterPhase + this.pitchJitterSpeed;
      if (this.yawJitterPhase > Math.PI * 2) {
         this.yawJitterPhase -= (float) (Math.PI * 2);
      }

      if (this.pitchJitterPhase > Math.PI * 2) {
         this.pitchJitterPhase -= (float) (Math.PI * 2);
      }
   }

   private float calculateYawJitter() {
      float primaryWave = (float)(Math.sin(this.yawJitterPhase) * this.yawJitterAmplitude);
      float secondaryWave = (float)(Math.sin(this.yawJitterPhase * 2.7F + 1.3F) * this.yawJitterAmplitude * 0.3);
      return primaryWave + secondaryWave;
   }

   private float calculatePitchJitter() {
      float primaryWave = (float)(Math.sin(this.pitchJitterPhase) * this.pitchJitterAmplitude);
      float secondaryWave = (float)(Math.sin(this.pitchJitterPhase * 3.1F + 0.7F) * this.pitchJitterAmplitude * 0.25);
      return primaryWave + secondaryWave;
   }

   public static float[] getRotations(double eyeX, double eyeY, double eyeZ, double targetX, double targetY, double targetZ) {
      double dx = targetX - eyeX;
      double dy = targetY - eyeY;
      double dz = targetZ - eyeZ;
      double distXZ = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distXZ)));
      return new float[]{yaw, pitch};
   }

   public static float angleDiff(float a, float b) {
      return ((a - b) % 360.0F + 540.0F) % 360.0F - 180.0F;
   }

   public static enum AccelerationMode {
      NONE,
      ANGLE_BASED,
      LINEAR,
      CUBIC;
   }
}
