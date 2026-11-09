package kat.respect.b2.util;

import net.minecraft.world.phys.Vec3;

@Deprecated
public final class ServerRotationHandler {
   private ServerRotationHandler() {
   }

   public static float getServerYaw() {
      return ServerRot.yaw();
   }

   public static void setServerYaw(float serverYaw) {
      ServerRot.setYaw(serverYaw);
   }

   public static float getServerPitch() {
      return ServerRot.pitch();
   }

   public static void setServerPitch(float serverPitch) {
      ServerRot.setPitch(serverPitch);
   }

   public static float getPrevServerYaw() {
      return ServerRot.prevYaw();
   }

   public static float getPrevServerPitch() {
      return ServerRot.prevPitch();
   }

   public static float getServerDeltaYaw() {
      return ServerRot.deltaYaw();
   }

   public static void setServerDeltaYaw(float serverDeltaYaw) {
      ServerRot.setDeltaYaw(serverDeltaYaw);
   }

   public static boolean isHasSilentRotation() {
      return ServerRot.hasSilent();
   }

   public static void setHasSilentRotation(boolean hasSilentRotation) {
      ServerRot.setSilent(hasSilentRotation);
   }

   public static void updateServerRotations(float yaw, float pitch) {
      ServerRot.update(yaw, pitch);
   }

   public static void setServerRotationsWithDelta(float yaw, float pitch) {
      ServerRot.setWithDelta(yaw, pitch);
   }

   public static void reset() {
      ServerRot.reset();
   }

   public static void syncToPlayer(float playerYaw, float playerPitch) {
      ServerRot.sync(playerYaw, playerPitch);
   }

   public static Vec3 getRotationVector(float pitch, float yaw) {
      return ServerRot.rotVec(pitch, yaw);
   }
}
