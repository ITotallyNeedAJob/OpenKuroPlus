package kat.respect.b2.util;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

public final class ProjectionUtil {
   private static final Minecraft mc = Minecraft.getInstance();

   private ProjectionUtil() {
   }

   public static float[] worldToScreen(double x, double y, double z) {
      if (mc.gameRenderer != null && mc.getWindow() != null) {
         CameraRenderState cameraState = cameraState();
         if (cameraState != null && cameraState.pos != null && cameraState.projectionMatrix != null && cameraState.viewRotationMatrix != null) {
            Vector4f point = new Vector4f((float)(x - cameraState.pos.x), (float)(y - cameraState.pos.y), (float)(z - cameraState.pos.z), 1.0F);
            cameraState.viewRotationMatrix.transform(point);
            cameraState.projectionMatrix.transform(point);
            if (point.w <= 1.0E-4F) {
               return null;
            } else {
               float ndcX = point.x / point.w;
               float ndcY = point.y / point.w;
               double screenW = mc.getWindow().getGuiScaledWidth();
               double screenH = mc.getWindow().getGuiScaledHeight();
               return new float[]{(float)((ndcX * 0.5 + 0.5) * screenW), (float)((0.5 - ndcY * 0.5) * screenH), -point.z};
            }
         } else {
            Camera camera = CameraCompat.mainCamera(mc.gameRenderer);
            Vec3 cam = camera.position();
            float yaw = camera.yRot();
            float pitch = camera.xRot();
            double dx = x - cam.x;
            double dy = y - cam.y;
            double dz = z - cam.z;
            float yawRad = yaw * (float) (Math.PI / 180.0);
            float pitchRad = pitch * (float) (Math.PI / 180.0);
            float cosYaw = Mth.cos(yawRad);
            float sinYaw = Mth.sin(yawRad);
            float cosPitch = Mth.cos(pitchRad);
            float sinPitch = Mth.sin(pitchRad);
            double fx = -sinYaw * cosPitch;
            double fy = -sinPitch;
            double fz = cosYaw * cosPitch;
            double rx = -cosYaw;
            double rz = -sinYaw;
            double ux = -rz * fy;
            double uy = rz * fx - rx * fz;
            double uz = rx * fy;
            double depth = dx * fx + dy * fy + dz * fz;
            if (depth <= 0.05) {
               return null;
            } else {
               double cx = dx * rx + dz * rz;
               double cy = dx * ux + dy * uy + dz * uz;
               float fov = camera != null ? camera.getFov() : 0.0F;
               if (fov <= 0.0F) {
                  fov = mc.options.fov().get().floatValue();
               }

               double screenW = mc.getWindow().getGuiScaledWidth();
               double screenH = mc.getWindow().getGuiScaledHeight();
               double focal = screenH * 0.5 / Math.tan(Math.toRadians(fov) * 0.5);
               return new float[]{(float)(screenW * 0.5 + cx / depth * focal), (float)(screenH * 0.5 - cy / depth * focal), (float)depth};
            }
         }
      } else {
         return null;
      }
   }

   public static float[] projectBox(AABB box) {
      float minX = Float.MAX_VALUE;
      float minY = Float.MAX_VALUE;
      float maxX = -Float.MAX_VALUE;
      float maxY = -Float.MAX_VALUE;
      boolean anyVisible = false;

      for (int corner = 0; corner < 8; corner++) {
         double x = (corner & 1) == 0 ? box.minX : box.maxX;
         double y = (corner & 2) == 0 ? box.minY : box.maxY;
         double z = (corner & 4) == 0 ? box.minZ : box.maxZ;
         float[] point = worldToScreen(x, y, z);
         if (point != null) {
            anyVisible = true;
            if (point[0] < minX) {
               minX = point[0];
            }

            if (point[0] > maxX) {
               maxX = point[0];
            }

            if (point[1] < minY) {
               minY = point[1];
            }

            if (point[1] > maxY) {
               maxY = point[1];
            }
         }
      }

      return !anyVisible ? null : new float[]{minX, minY, maxX, maxY};
   }

   private static CameraRenderState cameraState() {
      try {
         if (mc.gameRenderer == null) {
            return null;
         } else {
            GameRenderState state = mc.gameRenderer.getGameRenderState();
            return state != null && state.levelRenderState != null ? state.levelRenderState.cameraRenderState : null;
         }
      } catch (Throwable var1) {
         return null;
      }
   }

   public static AABB interpolatedBox(Entity entity, float partialTick) {
      double x = Mth.lerp((double)partialTick, entity.xo, entity.getX());
      double y = Mth.lerp((double)partialTick, entity.yo, entity.getY());
      double z = Mth.lerp((double)partialTick, entity.zo, entity.getZ());
      return entity.getBoundingBox().move(x - entity.getX(), y - entity.getY(), z - entity.getZ());
   }

   public static Vec3 interpolatedPosition(Entity entity, float partialTick) {
      return new Vec3(
         Mth.lerp((double)partialTick, entity.xo, entity.getX()),
         Mth.lerp((double)partialTick, entity.yo, entity.getY()),
         Mth.lerp((double)partialTick, entity.zo, entity.getZ())
      );
   }

   public static float partialTick() {
      return mc.getDeltaTracker() != null ? mc.getDeltaTracker().getGameTimeDeltaPartialTick(false) : 0.0F;
   }
}
