package kat.respect.b2.bridge;

import kat.respect.b2.rotation.RotationManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;

public final class MovementBridge {
   private static MovementBridge.Hook hook;
   private static int inputFreezeTicks = 0;
   private static boolean hasCorrection;
   private static float correctionCos = 1.0F;
   private static float correctionSin = 0.0F;
   private static boolean rotationSwapped;
   private static float savedYaw;
   private static float savedPitch;

   public static void register(MovementBridge.Hook h) {
      hook = h;
   }

   public static void freezeInput(int ticks) {
      inputFreezeTicks = Math.max(inputFreezeTicks, ticks);
   }

   public static float getMovementYaw(float vanillaYaw) {
      return hook == null ? vanillaYaw : hook.getMovementYaw(vanillaYaw);
   }

   public static Input correctInput(Input original) {
      if (inputFreezeTicks > 0) {
         inputFreezeTicks--;
         return Input.EMPTY;
      } else {
         Input input = hook == null ? original : hook.correctInput(original);
         if (!hasCorrection) {
            return input;
         } else {
            hasCorrection = false;
            Input corrected = rotate(input, correctionCos, correctionSin);
            if (hook != null) {
               hook.onInputCorrected(!corrected.equals(input));
            }

            return corrected;
         }
      }
   }

   private static Input rotate(Input original, float cos, float sin) {
      if (original == null) {
         return Input.EMPTY;
      } else {
         float x = (original.left() ? 1.0F : 0.0F) - (original.right() ? 1.0F : 0.0F);
         float z = (original.forward() ? 1.0F : 0.0F) - (original.backward() ? 1.0F : 0.0F);
         int sideways = Math.round(x * cos - z * sin);
         int forward = Math.round(z * cos + x * sin);
         return new Input(forward > 0, forward < 0, sideways > 0, sideways < 0, original.jump(), original.shift(), original.sprint());
      }
   }

   public static boolean shouldKeepSprint() {
      return hook != null && hook.shouldKeepSprint();
   }

   public static boolean rawHasForwardImpulse() {
      return hook != null && hook.rawHasForwardImpulse();
   }

   public static boolean isSprintFixActive() {
      return hook != null && hook.isSprintFixActive();
   }

   public static boolean isChangeLook() {
      return hook != null && hook.isChangeLook();
   }

   public static boolean isHandlingCorrection() {
      return hook != null && hook.isHandlingCorrection();
   }

   public static void prepareMovementCorrection(LocalPlayer player) {
      hasCorrection = false;
      if (hook != null && hook.isHandlingCorrection()) {
         hook.prepareMovementCorrection(player);
      } else {
         publishDefaultCorrection(player);
      }

      applyTickRotation(player);
   }

   private static void publishDefaultCorrection(LocalPlayer player) {
      if (player != null) {
         float[] aim = RotationManager.getPacketRotation();
         if (aim != null) {
            float delta = Mth.wrapDegrees(aim[0] - player.getYRot());
            float pitchDelta = Mth.clamp(aim[1], -90.0F, 90.0F) - player.getXRot();
            if (!(Math.abs(delta) < 0.05F) || !(Math.abs(pitchDelta) < 0.05F)) {
               setInputCorrection(delta);
            }
         }
      }
   }

   public static void setInputCorrection(float yawDeltaDegrees) {
      float radians = (float)Math.toRadians(-yawDeltaDegrees);
      correctionCos = (float)Math.cos(radians);
      correctionSin = (float)Math.sin(radians);
      hasCorrection = true;
   }

   private static void applyTickRotation(LocalPlayer player) {
      if (player == null) {
         rotationSwapped = false;
      } else {
         float[] aim = RotationManager.getPacketRotation();
         if (aim == null) {
            restoreTickRotation(player);
         } else {
            float realYaw = player.getYRot();
            float realPitch = player.getXRot();
            float aimYaw = aim[0];
            float aimPitch = Mth.clamp(aim[1], -90.0F, 90.0F);
            if (Math.abs(Mth.wrapDegrees(aimYaw - realYaw)) < 0.05F && Math.abs(aimPitch - realPitch) < 0.05F) {
               restoreTickRotation(player);
            } else {
               if (!rotationSwapped) {
                  savedYaw = realYaw;
                  savedPitch = realPitch;
                  rotationSwapped = true;
               }

               player.setYRot(aimYaw);
               player.setXRot(aimPitch);
            }
         }
      }
   }

   public static void restoreTickRotation(LocalPlayer player) {
      if (rotationSwapped) {
         rotationSwapped = false;
         if (player != null) {
            player.setYRot(savedYaw);
            player.setXRot(savedPitch);
         }
      }
   }

   public static boolean isTickRotationActive() {
      return rotationSwapped;
   }

   private MovementBridge() {
   }

   public interface Hook {
      float getMovementYaw(float var1);

      Input correctInput(Input var1);

      boolean shouldKeepSprint();

      boolean rawHasForwardImpulse();

      boolean isSprintFixActive();

      boolean isChangeLook();

      boolean isHandlingCorrection();

      void prepareMovementCorrection(LocalPlayer var1);

      void onInputCorrected(boolean var1);
   }
}
