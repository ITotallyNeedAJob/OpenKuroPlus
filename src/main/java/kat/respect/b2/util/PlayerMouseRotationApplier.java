package kat.respect.b2.util;

import net.minecraft.client.player.LocalPlayer;

@Deprecated
public final class PlayerMouseRotationApplier {
   private PlayerMouseRotationApplier() {
   }

   public static void applyMouseDelta(LocalPlayer player, float yawDelta, float pitchDelta) {
      MouseApplier.applyDelta(player, yawDelta, pitchDelta);
   }

   public static void setLocalPlayerRotation(LocalPlayer player, float yaw, float pitch) {
      MouseApplier.setRot(player, yaw, pitch);
   }
}
