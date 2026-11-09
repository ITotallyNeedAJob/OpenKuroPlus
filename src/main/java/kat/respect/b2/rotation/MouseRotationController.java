package kat.respect.b2.rotation;

import java.util.Random;
import net.minecraft.client.player.LocalPlayer;

public abstract class MouseRotationController {
   public float tolerance = 3.0F;
   public float speed = 1.0F;
   public float savedYaw;
   public float savedPitch;
   public float savedPreviousYaw;
   public float savedPreviousPitch;
   public float renderedYaw;
   public float renderedPitch;
   public float pendingYawDelta;
   public float pendingPitchDelta;
   public float yawDistance;
   public float pitchDistance;
   private boolean complete;
   private boolean retainAfterCompletion;
   private boolean randomizeMovement;
   private int jitterTicks;
   private int yawJitterDirection;
   private int pitchJitterDirection;
   private int yawJitter;
   private int pitchJitter;
   private long lastJitterTime = -1L;
   private final Random random = new Random();

   public boolean isComplete() {
      return this.complete;
   }

   public void setComplete(boolean complete) {
      this.complete = complete;
   }

   public boolean shouldRetainAfterCompletion() {
      return this.retainAfterCompletion;
   }

   public void setRetainAfterCompletion(boolean retainAfterCompletion) {
      this.retainAfterCompletion = retainAfterCompletion;
   }

   public float getSpeed() {
      return this.speed;
   }

   public MouseRotationController setSpeed(float speed) {
      this.speed = speed;
      return this;
   }

   public MouseRotationController setTolerance(float tolerance) {
      this.tolerance = tolerance;
      return this;
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

   public boolean isMovementRandomized() {
      return this.randomizeMovement;
   }

   public void setRandomizeMovement(boolean randomizeMovement) {
      this.randomizeMovement = randomizeMovement;
   }

   public abstract boolean updateYaw();

   public abstract boolean updatePitch();

   public void capturePlayerRotation(LocalPlayer player) {
      this.savedYaw = player.getYRot();
      this.savedPitch = player.getXRot();
      this.savedPreviousYaw = player.yRotO;
      this.savedPreviousPitch = player.xRotO;
   }

   public void onPostRenderTick() {
   }

   public void onPreMouseUpdate() {
   }

   public void update(LocalPlayer player, boolean screenOpen) {
      if (player != null && !screenOpen) {
         boolean yawComplete = this.updateYaw();
         boolean pitchComplete = this.updatePitch();
         if (yawComplete && pitchComplete && Math.abs(this.pendingYawDelta) < 1.0F && Math.abs(this.pendingPitchDelta) < 1.0F) {
            this.complete = true;
         }
      }
   }

   public boolean waitUntilComplete(long timeoutMillis) {
      int attempts = 0;

      while (!this.isComplete()) {
         try {
            Thread.sleep(10L);
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
            return true;
         }

         if (++attempts > timeoutMillis / 10L) {
            return true;
         }
      }

      return false;
   }

   protected float getMouseScale() {
      float sensitivityBase = RotationManager.INSTANCE.getMouseSensitivity() * 0.6F + 0.2F;
      return sensitivityBase * sensitivityBase * sensitivityBase * 8.0F;
   }

   protected boolean isOutsideTolerance(double absoluteError) {
      float rotationPerStep = this.getMouseScale() * 0.15F;
      return Math.round(absoluteError / rotationPerStep) > Math.max((long)Math.round(this.tolerance / rotationPerStep), 0L);
   }

   protected float addPendingStep(float pendingDelta, double error, double step) {
      return (float)(error > 0.0 ? pendingDelta + step : pendingDelta - step);
   }

   private void updateJitter() {
      this.jitterTicks++;
      if (this.jitterTicks >= 250 + this.random.nextInt(50)) {
         this.jitterTicks = this.random.nextInt(50) - 100;
         this.yawJitterDirection = this.random.nextInt(3) - 1;
         this.pitchJitterDirection = -(this.random.nextInt(3) - 1);
      }

      int yawStep = this.yawJitterDirection;
      int pitchStep = this.pitchJitterDirection;
      if (this.random.nextInt(10) < 2) {
         yawStep = 0;
      }

      if (this.random.nextInt(10) < 2) {
         pitchStep = 0;
      }

      if (this.jitterTicks < 0) {
         yawStep = 0;
         pitchStep = 0;
      }

      if (this.random.nextInt(20) == 1) {
         this.yawJitter += yawStep;
         this.pitchJitter += pitchStep;
      }

      if (this.pendingYawDelta > 0.0F && this.yawJitter < 0 || this.pendingYawDelta < 0.0F && this.yawJitter > 0) {
         this.yawJitter = 0;
      }
   }

   public void applyPendingMovement(boolean screenOpen) {
      if (this.randomizeMovement) {
         if (this.lastJitterTime == -1L) {
            this.lastJitterTime = System.currentTimeMillis();
         }

         long elapsedMillis = Math.min(System.currentTimeMillis() - this.lastJitterTime, 50L);
         this.lastJitterTime = System.currentTimeMillis();

         while (elapsedMillis-- > 0L) {
            this.updateJitter();
         }

         this.pendingYawDelta = this.pendingYawDelta + this.yawJitter;
         this.pendingPitchDelta = this.pendingPitchDelta + this.pitchJitter;
      }

      int yawSteps = (int)this.pendingYawDelta;
      int pitchSteps = (int)this.pendingPitchDelta;
      float remainingYaw = this.pendingYawDelta - yawSteps;
      float remainingPitch = this.pendingPitchDelta - pitchSteps;
      if (Math.abs(yawSteps) == 0) {
         yawSteps = 0;
      }

      if (Math.abs(pitchSteps) == 0) {
         pitchSteps = 0;
      }

      float mouseScale = this.getMouseScale();
      float yawDelta = yawSteps * mouseScale;
      float pitchDelta = pitchSteps * mouseScale;
      if (!screenOpen) {
         this.applyMouseDelta(yawDelta, -pitchDelta);
      }

      this.yawDistance = this.yawDistance + Math.abs(yawDelta) * 0.15F;
      this.pitchDistance = this.pitchDistance + Math.abs(pitchDelta) * 0.15F;
      this.pendingYawDelta = remainingYaw;
      this.pendingPitchDelta = remainingPitch;
      this.yawJitter = 0;
      this.pitchJitter = 0;
   }

   protected void applyMouseDelta(float yawDelta, float pitchDelta) {
      this.renderedYaw += yawDelta * 0.15F;
      this.renderedPitch = Math.max(-90.0F, Math.min(90.0F, this.renderedPitch - pitchDelta * 0.15F));
   }
}
