package kat.respect.b2.util;

import kat.respect.b2.bridge.FreeLookBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SmoothRotation {
   private static final Minecraft mc = Minecraft.getInstance();
   private float currentYaw;
   private float currentPitch;
   private float targetYaw;
   private float targetPitch;
   private float startYaw;
   private float startPitch;
   private float rotationProgress;
   private float totalRotationDistance;
   private boolean needsRotation;
   private double worldTargetX = Double.NaN;
   private double worldTargetY;
   private double worldTargetZ;
   private float speed = 180.0F;
   private float easingStrength = 3.0F;
   private float tolerance = 1.5F;
   private long lastFrameTime = -1L;

   public SmoothRotation() {
      this.reset();
   }

   public SmoothRotation setSpeed(float degPerSecond) {
      this.speed = Math.max(1.0F, degPerSecond);
      return this;
   }

   public SmoothRotation setEasingStrength(float strength) {
      this.easingStrength = Math.max(0.1F, strength);
      return this;
   }

   public SmoothRotation setTolerance(float tolerance) {
      this.tolerance = Math.max(0.05F, tolerance);
      return this;
   }

   public void reset() {
      if (mc.player != null) {
         this.currentYaw = mc.player.getYRot();
         this.currentPitch = mc.player.getXRot();
      }

      this.targetYaw = this.currentYaw;
      this.targetPitch = this.currentPitch;
      this.startYaw = this.currentYaw;
      this.startPitch = this.currentPitch;
      this.rotationProgress = 1.0F;
      this.totalRotationDistance = 0.1F;
      this.needsRotation = false;
      this.worldTargetX = Double.NaN;
      this.lastFrameTime = -1L;
   }

   public void setTarget(Vec3 worldPos) {
      this.worldTargetX = worldPos.x;
      this.worldTargetY = worldPos.y;
      this.worldTargetZ = worldPos.z;
      this.recalculateTarget();
   }

   public void setTargetAngles(float yaw, float pitch) {
      this.worldTargetX = Double.NaN;
      this.targetYaw = yaw;
      this.targetPitch = Mth.clamp(pitch, -90.0F, 90.0F);
      this.beginRotation();
   }

   private void recalculateTarget() {
      if (!Double.isNaN(this.worldTargetX) && mc.player != null) {
         float[] rots = RotationUtils.calculate(mc.player.getEyePosition(), new Vec3(this.worldTargetX, this.worldTargetY, this.worldTargetZ));
         this.targetYaw = rots[0];
         this.targetPitch = Mth.clamp(rots[1], -90.0F, 90.0F);
         this.beginRotation();
      }
   }

   private void beginRotation() {
      this.startYaw = this.currentYaw;
      this.startPitch = this.currentPitch;
      float yawDiff = Math.abs(Mth.wrapDegrees(this.targetYaw - this.startYaw));
      float pitchDiff = Math.abs(this.targetPitch - this.startPitch);
      this.totalRotationDistance = (float)Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
      if (this.totalRotationDistance < 0.1F) {
         this.totalRotationDistance = 0.1F;
      }

      this.rotationProgress = 0.0F;
      this.needsRotation = true;
   }

   public void update() {
      long now = System.nanoTime();
      float deltaTime = this.lastFrameTime == -1L ? 0.016666668F : (float)(now - this.lastFrameTime) / 1.0E9F;
      this.lastFrameTime = now;
      this.update(deltaTime);
   }

   public void update(float deltaSeconds) {
      if (this.needsRotation) {
         float deltaTime = Math.min(deltaSeconds, 0.05F);
         this.recalculateTarget();
         float progressIncrement = this.speed * deltaTime / this.totalRotationDistance;
         this.rotationProgress = Math.min(this.rotationProgress + progressIncrement, 1.0F);
         float eased = 1.0F - (float)Math.pow(1.0F - this.rotationProgress, this.easingStrength);
         float yawDiff = Mth.wrapDegrees(this.targetYaw - this.startYaw);
         this.currentYaw = Mth.wrapDegrees(this.startYaw + yawDiff * eased);
         this.currentPitch = Mth.clamp(this.startPitch + (this.targetPitch - this.startPitch) * eased, -90.0F, 90.0F);
      }
   }

   public boolean isActive() {
      return this.needsRotation;
   }

   public boolean isDone() {
      return !this.needsRotation
         || Math.abs(Mth.wrapDegrees(this.targetYaw - this.currentYaw)) < this.tolerance && Math.abs(this.targetPitch - this.currentPitch) < this.tolerance;
   }

   public float getYaw() {
      return this.currentYaw;
   }

   public float getPitch() {
      return this.currentPitch;
   }

   public void apply() {
      if (mc.player != null) {
         mc.player.setYRot(this.currentYaw);
         mc.player.yRotO = this.currentYaw;
         mc.player.setXRot(this.currentPitch);
         mc.player.xRotO = this.currentPitch;
         FreeLookBridge.steerCamera(this.currentYaw, this.currentPitch);
      }
   }
}
