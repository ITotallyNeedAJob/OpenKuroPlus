package kat.respect.b2.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public final class MouseApplier {
   private static final float MOUSE_STEP = 0.15F;

   private MouseApplier() {
   }

   public static void applyDelta(LocalPlayer player, float yawDelta, float pitchDelta) {
      if (player != null) {
         float prevYaw = player.getYRot();
         float prevPitch = player.getXRot();
         player.setYRot(prevYaw + yawDelta * 0.15F);
         player.setXRot(Mth.clamp(prevPitch - pitchDelta * 0.15F, -90.0F, 90.0F));
         player.yRotO = player.yRotO + (player.getYRot() - prevYaw);
         player.xRotO = player.xRotO + (player.getXRot() - prevPitch);
         player.yHeadRotO = player.yHeadRot;
         player.yHeadRot = player.getYRot();
      }
   }

   public static void setRot(LocalPlayer player, float yaw, float pitch) {
      if (player != null) {
         float prevYaw = player.getYRot();
         float prevPitch = player.getXRot();
         player.setYRot(yaw);
         player.setXRot(Mth.clamp(pitch, -90.0F, 90.0F));
         player.yRotO = player.yRotO + (player.getYRot() - prevYaw);
         player.xRotO = player.xRotO + (player.getXRot() - prevPitch);
      }
   }
}
