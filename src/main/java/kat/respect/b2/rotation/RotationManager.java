package kat.respect.b2.rotation;

import kat.respect.b2.bridge.MovementBridge;
import kat.respect.b2.event.EventBus;
import kat.respect.b2.event.EventHandler;
import kat.respect.b2.event.EventPriority;
import kat.respect.b2.event.events.PacketEvent;
import kat.respect.b2.module.Module;
import kat.respect.b2.util.RotationHandler;
import kat.respect.b2.util.ScreenUtil;
import kat.respect.b2.util.SentRotation;
import kat.respect.b2.util.ServerRotationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class RotationManager {
   public static final RotationManager INSTANCE = new RotationManager();
   private static final Minecraft mc = Minecraft.getInstance();
   private static final float MOVEMENT_THRESHOLD = 0.075F;
   private static final float MOVEMENT_YAW_EPSILON = 0.05F;
   private MouseRotationController activeController;
   private float managedYaw;
   private float managedPitch;
   private float mouseSensitivity = 0.5F;
   public final RotationControlClaim rotationControlClaim = new RotationControlClaim();
   private boolean playerYawRestorePending;
   private float savedPlayerYaw;
   private float savedPlayerPitch;
   private boolean movementCorrection = true;
   private boolean strafeInvert;
   private boolean cameraMode;
   private RotationManager.OverrideState forwardOverride = RotationManager.OverrideState.NONE;
   private double controllerUpdateAccumulator;
   private int controllerUpdatesThisTick;

   private RotationManager() {
   }

   public void setMovementCorrection(boolean enabled) {
      this.movementCorrection = enabled;
   }

   public boolean isMovementCorrectionEnabled() {
      return this.movementCorrection;
   }

   public float getMouseSensitivity() {
      return this.mouseSensitivity;
   }

   public MouseRotationController getActiveController() {
      return this.activeController;
   }

   public boolean isControllerUpdating() {
      return this.activeController != null && !this.activeController.isComplete();
   }

   public boolean hasAdaptiveController() {
      return this.activeController instanceof AdaptiveRotationController;
   }

   public float getManagedYaw() {
      return this.hasAdaptiveController() ? this.managedYaw : (mc.player != null ? mc.player.getYRot() : 0.0F);
   }

   public float getManagedPitch() {
      return this.hasAdaptiveController() ? this.managedPitch : (mc.player != null ? mc.player.getXRot() : 0.0F);
   }

   public static float[] getPacketRotation() {
      if (ServerRotationHandler.isHasSilentRotation()) {
         return new float[]{ServerRotationHandler.getServerYaw(), ServerRotationHandler.getServerPitch()};
      } else if (RotationHandler.isSilentActive()) {
         return new float[]{RotationHandler.getSilentYaw(), RotationHandler.getSilentPitch()};
      } else {
         return INSTANCE.isControllerUpdating() ? new float[]{INSTANCE.getManagedYaw(), Mth.clamp(INSTANCE.getManagedPitch(), -90.0F, 90.0F)} : null;
      }
   }

   public float getSensitivityTimeScale() {
      float sensitivityBase = this.mouseSensitivity * 0.6F + 0.2F;
      float currentScale = sensitivityBase * sensitivityBase * sensitivityBase * 8.0F;
      return 1.0F / currentScale;
   }

   public void setController(MouseRotationController controller) {
      if (this.activeController != controller) {
         if (this.activeController instanceof AdaptiveRotationController previous && controller instanceof AdaptiveRotationController next) {
            previous.setRelativeMode(true);
            previous.setComplete(true);
            next.setRelativeMode(false);
            next.setCurrentYaw(previous.getRenderedYaw());
            next.setCurrentPitch(previous.getRenderedPitch());
         }

         if (this.activeController == null && controller instanceof AdaptiveRotationController adaptive) {
            this.managedYaw = adaptive.getRenderedYaw();
            this.managedPitch = adaptive.getRenderedPitch();
         }

         if (this.activeController == null && !(controller instanceof AdaptiveRotationController) && mc.player != null) {
            controller.renderedYaw = mc.player.getYRot();
            controller.renderedPitch = mc.player.getXRot();
         }

         this.activeController = controller;
      }
   }

   public void releaseController(MouseRotationController controller) {
      if (this.activeController != null && this.activeController == controller) {
         if (this.activeController instanceof AdaptiveRotationController adaptive) {
            adaptive.setRelativeMode(true);
         } else {
            this.activeController.setRetainAfterCompletion(false);
            this.activeController.setComplete(true);
         }
      }
   }

   public void setForwardMovementOverride(boolean pressed) {
      this.forwardOverride = pressed ? RotationManager.OverrideState.PRESS : RotationManager.OverrideState.NONE;
   }

   public void suppressForwardMovement() {
      this.forwardOverride = RotationManager.OverrideState.RELEASE;
   }

   public void setStrafeInvert(boolean enabled) {
      this.strafeInvert = enabled;
   }

   public boolean isStrafeInvertEnabled() {
      return this.strafeInvert;
   }

   public void setCameraMode(boolean enabled) {
      this.cameraMode = enabled;
   }

   public boolean isCameraMode() {
      return this.cameraMode;
   }

   public void releaseOwned(Module requester) {
      if (this.rotationControlClaim.getCurrentOwner() == requester && this.activeController != null) {
         this.releaseController(this.activeController);
      }

      this.rotationControlClaim.release(requester);
   }

   public boolean isStillReturning() {
      return this.activeController instanceof AdaptiveRotationController controller && controller.isRelativeMode() && !controller.isComplete();
   }

   public void onPlayerTickHead(LocalPlayer player) {
      if (mc.options != null) {
         this.mouseSensitivity = mc.options.sensitivity().get().floatValue();
      }

      if (this.activeController == null || player == null) {
         this.activeController = null;
         this.controllerUpdateAccumulator = 0.0;
         this.controllerUpdatesThisTick = 0;
      } else if (this.activeController.isComplete() && !this.activeController.shouldRetainAfterCompletion()) {
         this.activeController = null;
         this.controllerUpdateAccumulator = 0.0;
         this.controllerUpdatesThisTick = 0;
      } else {
         double targetUpdates = Math.round(50.0F * this.getSensitivityTimeScale());
         double remainingUpdates = Math.max(targetUpdates - this.controllerUpdatesThisTick, 0.0);
         this.advanceControllerUpdates(remainingUpdates, player);
         this.controllerUpdatesThisTick = 0;
         if (this.hasAdaptiveController()) {
            AdaptiveRotationController controller = (AdaptiveRotationController)this.activeController;
            this.managedYaw = controller.getRenderedYaw();
            this.managedPitch = Mth.clamp(controller.getRenderedPitch(), -90.0F, 90.0F);
         } else {
            this.managedYaw = this.activeController.renderedYaw;
            this.managedPitch = Mth.clamp(this.activeController.renderedPitch, -90.0F, 90.0F);
         }

         this.applyManagedRotation(true);
      }
   }

   public void onSendPositionPost(LocalPlayer player) {
      if (!this.cameraMode && !MovementBridge.isChangeLook()) {
         this.applyManagedRotation(false);
      } else {
         this.playerYawRestorePending = false;
      }
   }

   public void onPlayerTickReturn(LocalPlayer player) {
      if (!this.cameraMode && !MovementBridge.isChangeLook()) {
         this.applyManagedRotation(false);
      } else {
         this.playerYawRestorePending = false;
      }
   }

   private void refreshManagedRotation() {
      if (this.hasAdaptiveController()) {
         AdaptiveRotationController controller = (AdaptiveRotationController)this.activeController;
         this.managedYaw = controller.getRenderedYaw();
         this.managedPitch = Mth.clamp(controller.getRenderedPitch(), -90.0F, 90.0F);
      } else if (this.activeController != null) {
         this.managedYaw = this.activeController.renderedYaw;
         this.managedPitch = Mth.clamp(this.activeController.renderedPitch, -90.0F, 90.0F);
      }
   }

   public void advanceAndApplyNow(int iterations) {
      if (this.activeController != null && mc.player != null) {
         boolean screenOpenNow = ScreenUtil.hasScreen(mc);

         for (int i = 0; i < iterations; i++) {
            this.activeController.update(mc.player, screenOpenNow);
            this.activeController.applyPendingMovement(screenOpenNow);
         }

         this.refreshManagedRotation();
         this.applyManagedRotation(true);
      }
   }

   public void snapControllerToTarget(float yaw, float pitch) {
      if (this.activeController instanceof AdaptiveRotationController controller) {
         controller.setCurrentYaw(yaw);
         controller.setCurrentPitch(Mth.clamp(pitch, -90.0F, 90.0F));
         this.refreshManagedRotation();
         this.applyManagedRotation(true);
      }
   }

   private void advanceControllerUpdates(double updateCount, LocalPlayer player) {
      this.controllerUpdateAccumulator += updateCount;
      int wholeUpdates = (int)Math.round(this.controllerUpdateAccumulator);
      boolean screenOpen = ScreenUtil.hasScreen(mc);

      for (int i = 0; i < wholeUpdates; i++) {
         try {
            this.activeController.update(player, screenOpen);
            this.activeController.applyPendingMovement(screenOpen);
         } catch (Exception var8) {
         }

         this.controllerUpdatesThisTick++;
      }

      this.controllerUpdateAccumulator -= wholeUpdates;
   }

   public void applyManagedRotation(boolean apply) {
      LocalPlayer player = mc.player;
      if (player == null) {
         this.playerYawRestorePending = false;
      } else {
         if (apply) {
            this.savedPlayerYaw = player.getYRot();
            this.savedPlayerPitch = player.getXRot();
            player.setYRot(this.managedYaw);
            player.yHeadRot = this.managedYaw;
            player.setXRot(this.managedPitch);
            this.playerYawRestorePending = true;
         } else if (this.playerYawRestorePending && !MovementBridge.isChangeLook()) {
            player.setYRot(this.savedPlayerYaw);
            player.yHeadRot = this.savedPlayerYaw;
            player.setXRot(this.savedPlayerPitch);
            this.playerYawRestorePending = false;
         }
      }
   }

   public void onInputTick(LocalPlayer player) {
      if (player != null) {
         ClientInput packetRot = player.input;
         if (packetRot instanceof ClientInput) {
            if (!this.movementCorrection) {
               return;
            }

            float[] packetRotx = getPacketRotation();
            if (packetRotx == null) {
               return;
            }

            float aimYaw = SentRotation.isValid() ? SentRotation.yaw() : packetRotx[0];
            if (Math.abs(Mth.wrapDegrees(aimYaw - player.getYRot())) < 0.05F) {
               return;
            }

            if (MovementBridge.isHandlingCorrection()) {
               return;
            }

            Input keys = packetRot.keyPresses;
            if (keys != null && (keys.forward() || keys.backward() || keys.left() || keys.right())) {
               float referenceYaw = player.getYRot();
               if (this.hasAdaptiveController()) {
                  referenceYaw = ((AdaptiveRotationController)this.activeController).getReferenceYaw();
               }

               float movementYaw = this.adjustMovementYaw(referenceYaw, keys.forward(), keys.left(), keys.right(), keys.backward());
               float relativeYaw = Mth.wrapDegrees(Mth.wrapDegrees(aimYaw) - movementYaw);
               float relativeRadians = relativeYaw * (float) (Math.PI / 180.0);
               float forwardProjection = (float)Math.cos(relativeRadians);
               float leftProjection = (float)Math.sin(relativeRadians);
               boolean pressForward = forwardProjection >= 0.075F;
               boolean pressLeft = leftProjection >= 0.075F;
               boolean pressRight = leftProjection <= -0.075F;
               boolean pressBack = forwardProjection <= -0.075F;
               if (this.forwardOverride == RotationManager.OverrideState.PRESS) {
                  pressForward = true;
                  pressBack = false;
               } else if (this.forwardOverride == RotationManager.OverrideState.RELEASE) {
                  pressForward = false;
                  pressBack = false;
               }

               if (this.strafeInvert) {
                  boolean swap = pressLeft;
                  pressLeft = pressRight;
                  pressRight = swap;
               }

               packetRot.keyPresses = new Input(pressForward, pressBack, pressLeft, pressRight, keys.jump(), keys.shift(), keys.sprint());
               return;
            }

            return;
         }
      }
   }

   public float adjustMovementYaw(float yaw, boolean forward, boolean left, boolean right, boolean back) {
      float adjustedYaw = yaw;
      if (forward && left) {
         adjustedYaw = yaw - 45.0F;
      } else if (back && left) {
         adjustedYaw = yaw - 135.0F;
      } else if (left) {
         adjustedYaw = yaw - 90.0F;
      } else if (forward && right) {
         adjustedYaw = yaw + 45.0F;
      } else if (back && right) {
         adjustedYaw = yaw + 135.0F;
      } else if (right) {
         adjustedYaw = yaw + 90.0F;
      } else if (back) {
         adjustedYaw = yaw + 180.0F;
      }

      return adjustedYaw;
   }

   public void applyManagedMouseOver() {
      LocalPlayer player = mc.player;
      if (player != null && this.hasAdaptiveController()) {
         float savedYaw = player.getYRot();
         float savedPitch = player.getXRot();
         player.setYRot(this.managedYaw);
         player.yHeadRot = this.managedYaw;
         player.setXRot(this.managedPitch);
         HitResult hitResult = player.raycastHitResult(1.0F, mc.getCameraEntity());
         player.setYRot(savedYaw);
         player.yHeadRot = savedYaw;
         player.setXRot(savedPitch);
         mc.hitResult = hitResult;
         mc.crosshairPickEntity = hitResult instanceof EntityHitResult ehr ? ehr.getEntity() : null;
      }
   }

   public Entity rayTraceUsingManagedRotation() {
      LocalPlayer player = mc.player;
      if (player == null || mc.level == null) {
         return null;
      } else if (this.hasAdaptiveController()) {
         float savedYaw = player.getYRot();
         float savedPitch = player.getXRot();
         player.setYRot(this.managedYaw);
         player.yHeadRot = this.managedYaw;
         player.setXRot(this.managedPitch);
         HitResult hitResult = player.raycastHitResult(1.0F, mc.getCameraEntity());
         player.setYRot(savedYaw);
         player.yHeadRot = savedYaw;
         player.setXRot(savedPitch);
         return hitResult instanceof EntityHitResult ehr ? ehr.getEntity() : null;
      } else {
         return player.raycastHitResult(1.0F, mc.getCameraEntity()) instanceof EntityHitResult ehr ? ehr.getEntity() : null;
      }
   }

   public HitResult rayTraceHitResult() {
      LocalPlayer player = mc.player;
      if (player == null) {
         return null;
      } else if (!this.hasAdaptiveController()) {
         return player.raycastHitResult(1.0F, mc.getCameraEntity());
      } else {
         float savedYaw = player.getYRot();
         float savedPitch = player.getXRot();
         player.setYRot(this.managedYaw);
         player.yHeadRot = this.managedYaw;
         player.setXRot(this.managedPitch);

         HitResult var4;
         try {
            var4 = player.raycastHitResult(1.0F, mc.getCameraEntity());
         } finally {
            player.setYRot(savedYaw);
            player.yHeadRot = savedYaw;
            player.setXRot(savedPitch);
         }

         return var4;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPacketReceive(PacketEvent event) {
      if (event.isReceive() && !event.isCancelled() && this.hasAdaptiveController()) {
         if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            ((AdaptiveRotationController)this.activeController).setComplete(true);
         }
      }
   }

   static {
      EventBus.INSTANCE.subscribe(INSTANCE);
   }

   private static enum OverrideState {
      NONE,
      PRESS,
      RELEASE;
   }
}
