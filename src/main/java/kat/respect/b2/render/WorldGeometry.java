package kat.respect.b2.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import kat.respect.b2.util.CameraCompat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;

public final class WorldGeometry {
   private static final List<WorldGeometry.Pending> PENDING = new ArrayList<>();

   private WorldGeometry() {
   }

   public static void set(Object owner, RenderType type, GeometryRecorder recorder) {
      if (owner != null && type != null && recorder != null && !recorder.isEmpty()) {
         Vec3 camera = cameraPosition();

         for (int i = 0; i < PENDING.size(); i++) {
            WorldGeometry.Pending pending = PENDING.get(i);
            if (pending.owner == owner && pending.type == type) {
               PENDING.remove(i);
               break;
            }
         }

         PENDING.add(new WorldGeometry.Pending(owner, type, recorder, camera));
      }
   }

   public static void clear(Object owner) {
      if (owner != null) {
         PENDING.removeIf(pending -> pending.owner == owner);
      }
   }

   public static boolean hasPending() {
      return !PENDING.isEmpty();
   }

   public static void submitAll(SubmitNodeStorage storage, Vec3 cameraNow) {
      if (storage != null && !PENDING.isEmpty()) {
         List<WorldGeometry.Pending> batch = new ArrayList<>(PENDING);
         PENDING.clear();

         for (WorldGeometry.Pending pending : batch) {
            try {
               PoseStack pose = new PoseStack();
               if (pending.camera != null && cameraNow != null) {
                  pose.translate((float)(pending.camera.x - cameraNow.x), (float)(pending.camera.y - cameraNow.y), (float)(pending.camera.z - cameraNow.z));
               }

               GeometryRecorder recorder = pending.recorder;
               storage.submitCustomGeometry(pose, pending.type, (p, consumer) -> recorder.replay(p, consumer));
            } catch (Throwable var7) {
            }
         }
      }
   }

   private static Vec3 cameraPosition() {
      try {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft == null || minecraft.gameRenderer == null) {
            return null;
         }

         Camera camera = CameraCompat.mainCamera(minecraft.gameRenderer);
         if (camera != null) {
            return camera.position();
         }
      } catch (Throwable var2) {
      }

      return null;
   }

   private static final class Pending {
      final Object owner;
      final RenderType type;
      final GeometryRecorder recorder;
      final Vec3 camera;

      Pending(Object owner, RenderType type, GeometryRecorder recorder, Vec3 camera) {
         this.owner = owner;
         this.type = type;
         this.recorder = recorder;
         this.camera = camera;
      }
   }
}
