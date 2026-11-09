package kat.respect.b2.rotation;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class AdaptiveRotationController extends FixedRotationController implements WorldPointRotationTarget {
   private static final RotationAngles ZERO_ROTATION = new RotationAngles(0.0F, 0.0F);
   private Vec3 target;
   private RotationAngles fixedTargetRotation;
   private float yawOffset;
   private boolean relativeMode;
   private boolean normalizeTargetYaw = true;
   private float currentYaw;
   private float currentPitch;
   private LocalPlayer referenceEntity;
   private Float referenceYawOverride;
   private float yawJitterPhase;
   private float pitchJitterPhase;
   private float yawJitterSpeed;
   private float pitchJitterSpeed;
   private float yawJitterAmplitude;
   private float pitchJitterAmplitude;
   private long lastJitterUpdateTime;
   private final Random random = new Random();

   public AdaptiveRotationController(float yaw, float pitch) {
      super(yaw, pitch);
      LocalPlayer player = Minecraft.getInstance().player;
      this.currentYaw = player != null ? player.getYRot() : yaw;
      this.currentPitch = player != null ? player.getXRot() : pitch;
      this.referenceEntity = player;
   }

   public AdaptiveRotationController() {
      this(RotationManager.INSTANCE.getManagedYaw(), RotationManager.INSTANCE.getManagedPitch());
   }

   public AdaptiveRotationController(Vec3 target) {
      this(0.0F, 0.0F);
      this.setTarget(target);
   }

   public float getReferenceYaw() {
      LocalPlayer reference = this.referenceEntity != null ? this.referenceEntity : Minecraft.getInstance().player;
      return this.referenceYawOverride != null ? this.referenceYawOverride : (reference != null ? reference.getYRot() : 0.0F);
   }

   public float getYawOffset() {
      return this.yawOffset;
   }

   public void setYawOffset(float yawOffset) {
      this.yawOffset = yawOffset;
   }

   public boolean isRelativeMode() {
      return this.relativeMode;
   }

   public boolean isNormalizeTargetYaw() {
      return this.normalizeTargetYaw;
   }

   public void setNormalizeTargetYaw(boolean normalizeTargetYaw) {
      this.normalizeTargetYaw = normalizeTargetYaw;
   }

   public void setReferenceEntity(LocalPlayer referenceEntity) {
      this.referenceEntity = referenceEntity;
   }

   public void setReferenceYawOverride(Float yawOverride) {
      this.referenceYawOverride = yawOverride;
   }

   @Override
   public float getCurrentYaw() {
      return this.currentYaw;
   }

   @Override
   public float getCurrentPitch() {
      return this.currentPitch;
   }

   public void setCurrentYaw(float yaw) {
      this.currentYaw = yaw;
   }

   public void setCurrentPitch(float pitch) {
      this.currentPitch = pitch;
   }

   @Override
   public Vec3 getTarget() {
      return this.target;
   }

   @Override
   public void setTarget(Vec3 target) {
      this.fixedTargetRotation = null;
      this.target = target;
      this.setComplete(false);
   }

   @Override
   public void setTargetRotation(float yaw, float pitch) {
      this.target = null;
      super.setTargetRotation(yaw, pitch);
   }

   public RotationAngles calculateRotation(Vec3 target) {
      LocalPlayer reference = this.referenceEntity != null ? this.referenceEntity : Minecraft.getInstance().player;
      return reference == null
         ? new RotationAngles(0.0F, 0.0F)
         : RotationVectorMath.calculateRotation(reference.getEyePosition(), target, this.getCurrentYaw(), false);
   }

   public void refreshTargetRotation() {
      if (this.relativeMode) {
         this.setTargetRotation(ZERO_ROTATION);
      } else {
         RotationAngles targetRotation = null;
         if (this.target != null) {
            targetRotation = this.calculateRotation(this.target);
         } else if (this.fixedTargetRotation != null) {
            targetRotation = this.fixedTargetRotation;
         }

         if (targetRotation != null) {
            if (this.yawOffset != 0.0F) {
               this.setTargetRotation(targetRotation.getYaw() + this.yawOffset, targetRotation.getPitch());
            } else {
               this.setTargetRotation(targetRotation);
            }
         }
      }
   }

   @Override
   public void update(LocalPlayer player, boolean screenOpen) {
      if (player != null && !screenOpen) {
         this.refreshTargetRotation();
         boolean yawComplete = this.updateYaw();
         boolean pitchComplete = this.updatePitch();
         boolean rotationComplete = yawComplete && pitchComplete && Math.abs(this.pendingYawDelta) < 1.0F && Math.abs(this.pendingPitchDelta) < 1.0F;
         if (!this.relativeMode && rotationComplete && !this.shouldRetainAfterCompletion()) {
            this.setRelativeMode(true);
         } else {
            this.setComplete(rotationComplete);
         }
      }
   }

   @Override
   protected void applyMouseDelta(float yawDelta, float pitchDelta) {
      this.currentYaw += yawDelta * 0.15F;
      this.currentPitch = Mth.clamp(this.currentPitch - pitchDelta * 0.15F, -90.0F, 90.0F);
      this.renderedYaw = this.currentYaw;
      this.renderedPitch = this.currentPitch;
   }

   public float getRenderedYaw() {
      LocalPlayer reference = this.referenceEntity != null ? this.referenceEntity : Minecraft.getInstance().player;
      return this.relativeMode ? (reference != null ? reference.getYRot() : 0.0F) + this.currentYaw : this.currentYaw;
   }

   public float getRenderedPitch() {
      LocalPlayer reference = this.referenceEntity != null ? this.referenceEntity : Minecraft.getInstance().player;
      float renderedPitch = this.relativeMode ? (reference != null ? reference.getXRot() : 0.0F) + this.currentPitch : this.currentPitch;
      return Mth.clamp(renderedPitch, -90.0F, 90.0F);
   }

   @Override
   public boolean shouldRetainAfterCompletion() {
      return !this.relativeMode;
   }

   public void setRelativeMode(boolean relativeMode) {
      if (this.relativeMode != relativeMode) {
         LocalPlayer reference = this.referenceEntity != null ? this.referenceEntity : Minecraft.getInstance().player;
         if (reference != null) {
            if (!relativeMode) {
               this.currentYaw = this.currentYaw + reference.getYRot();
               this.currentPitch = Mth.clamp(this.currentPitch + reference.getXRot(), -90.0F, 90.0F);
            } else {
               this.referenceYawOverride = null;
               float yawDifference = reference.getYRot() - this.currentYaw;
               float pitchDifference = reference.getXRot() - this.currentPitch;
               float yawNormalizationOffset = 0.0F;

               while (yawDifference + yawNormalizationOffset > 180.0F) {
                  yawNormalizationOffset -= 360.0F;
               }

               while (yawDifference + yawNormalizationOffset < -180.0F) {
                  yawNormalizationOffset += 360.0F;
               }

               if (yawNormalizationOffset != 0.0F) {
                  reference.setYRot(reference.getYRot() + yawNormalizationOffset);
                  reference.yHeadRot += yawNormalizationOffset;
                  reference.yRotO += yawNormalizationOffset;
                  reference.yBodyRot += yawNormalizationOffset;
               }

               this.currentYaw = Mth.wrapDegrees(-yawDifference);
               this.currentPitch = Mth.clamp(-pitchDifference, -90.0F, 90.0F);
            }
         }

         this.setComplete(false);
      }

      this.relativeMode = relativeMode;
   }

   @Override
   public void onPostRenderTick() {
      if (this.relativeMode && this.referenceEntity == null) {
         LocalPlayer player = Minecraft.getInstance().player;
         if (player == null) {
            return;
         }

         float playerYawDelta = player.getYRot() - this.savedYaw;
         float playerPitchDelta = player.getXRot() - this.savedPitch;
         float remainingYaw = Math.abs(this.currentYaw - playerYawDelta);
         if (remainingYaw < Math.abs(this.currentYaw)) {
            this.currentYaw -= playerYawDelta;
         }

         float remainingPitch = Math.abs(this.currentPitch - playerPitchDelta);
         if (remainingPitch < Math.abs(this.currentPitch)) {
            this.currentPitch -= playerPitchDelta;
         }
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
      if (this.yawJitterPhase > (float) (Math.PI * 2)) {
         this.yawJitterPhase -= (float) (Math.PI * 2);
      }

      if (this.pitchJitterPhase > (float) (Math.PI * 2)) {
         this.pitchJitterPhase -= (float) (Math.PI * 2);
      }
   }

   public void updateJitter(float partialTicks) {
      this.updateJitter();
      this.currentYaw = this.currentYaw + this.calculateYawJitter() * partialTicks;
      this.currentPitch = Mth.clamp(this.currentPitch + this.calculatePitchJitter() * partialTicks, -90.0F, 90.0F);
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

   public void copyFrom(AdaptiveRotationController source) {
      this.currentYaw = source.currentYaw;
      this.currentPitch = source.currentPitch;
      this.targetYaw = source.targetYaw;
      this.targetPitch = source.targetPitch;
      this.pendingYawDelta = source.pendingYawDelta;
      this.pendingPitchDelta = source.pendingPitchDelta;
      this.yawDistance = source.yawDistance;
      this.pitchDistance = source.pitchDistance;
      this.setRestoreCapturedRotation(source.shouldRestoreCapturedRotation());
      this.setClampStepToRemaining(source.isStepClampedToRemaining());
      this.setScaleAxesProportionally(source.isAxisScalingEnabled());
      this.setAngleBasedAcceleration(source.isAngleBasedAccelerationEnabled());
      this.setLinearAcceleration(source.isLinearAccelerationEnabled());
      this.setCubicAcceleration(source.isCubicAccelerationEnabled());
      this.setRetainAfterCompletion(source.shouldRetainAfterCompletion());
      this.setComplete(source.isComplete());
      this.speed = source.speed;
      this.tolerance = source.tolerance;
      this.relativeMode = source.relativeMode;
      this.referenceYawOverride = source.referenceYawOverride;
   }
}
