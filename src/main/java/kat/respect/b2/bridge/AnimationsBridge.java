package kat.respect.b2.bridge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public final class AnimationsBridge {
   private static AnimationsBridge.Hook activeHook = null;

   public static void register(AnimationsBridge.Hook hook) {
      activeHook = hook;
   }

   public static void unregister(AnimationsBridge.Hook hook) {
      if (activeHook == hook) {
         activeHook = null;
      }
   }

   public static boolean isCustomSwingActive() {
      return activeHook != null && activeHook.isCustomSwingActive();
   }

   public static void applySwing(float swingProgress, PoseStack matrices, int i, HumanoidArm arm) {
      if (activeHook != null) {
         activeHook.applySwing(swingProgress, matrices, i, arm);
      }
   }

   public static void applyTransform(InteractionHand hand, PoseStack matrices) {
      if (activeHook != null) {
         activeHook.applyTransform(hand, matrices);
      }
   }

   public interface Hook {
      boolean isCustomSwingActive();

      void applySwing(float var1, PoseStack var2, int var3, HumanoidArm var4);

      void applyTransform(InteractionHand var1, PoseStack var2);
   }
}
