package kat.respect.b2.rotation.builder;

import java.util.Optional;
import java.util.function.Consumer;
import kat.respect.b2.module.Module;
import kat.respect.b2.rotation.AdaptiveRotationController;
import kat.respect.b2.rotation.MouseRotationController;
import kat.respect.b2.rotation.RotationAngles;
import kat.respect.b2.rotation.RotationManager;
import kat.respect.b2.util.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class AimBuilder {
   private static final Minecraft mc = Minecraft.getInstance();
   private final Module requester;
   private LivingEntity entityTarget;
   private Vec3 vecTarget;
   private Float directYaw;
   private Float directPitch;
   private float yawSpeed = 180.0F;
   private float pitchSpeed = 180.0F;
   private boolean silent = false;
   private RotationUtils.AimType aimType = RotationUtils.AimType.REGULAR;
   private RotationUtils.Priority priority = RotationUtils.Priority.NORMAL;
   private RotationUtils.EntityPoints points = RotationUtils.EntityPoints.CLOSEST;
   private float multipoint = 50.0F;
   private int predictTicks = 0;
   private boolean random = false;
   private boolean throughWalls = false;
   private Runnable onComplete;
   private Consumer<float[]> onTick;

   public AimBuilder(Module requester) {
      this.requester = requester;
   }

   public AimBuilder to(LivingEntity entity) {
      this.entityTarget = entity;
      this.vecTarget = null;
      return this;
   }

   public AimBuilder to(Vec3 pos) {
      this.vecTarget = pos;
      this.entityTarget = null;
      return this;
   }

   public AimBuilder to(BlockPos pos) {
      this.vecTarget = Vec3.atCenterOf(pos);
      this.entityTarget = null;
      return this;
   }

   public AimBuilder to(float yaw, float pitch) {
      this.directYaw = yaw;
      this.directPitch = pitch;
      return this;
   }

   public AimBuilder speed(float speed) {
      this.yawSpeed = speed;
      this.pitchSpeed = speed;
      return this;
   }

   public AimBuilder speed(float yawSpeed, float pitchSpeed) {
      this.yawSpeed = yawSpeed;
      this.pitchSpeed = pitchSpeed;
      return this;
   }

   public AimBuilder silent() {
      this.silent = true;
      return this;
   }

   public AimBuilder silent(boolean s) {
      this.silent = s;
      return this;
   }

   public AimBuilder camera() {
      this.silent = false;
      return this;
   }

   public AimBuilder priority(RotationUtils.Priority priority) {
      this.priority = priority;
      return this;
   }

   public AimBuilder aimType(RotationUtils.AimType aimType) {
      this.aimType = aimType;
      return this;
   }

   public AimBuilder windMouse() {
      this.aimType = RotationUtils.AimType.WINDMOUSE;
      return this;
   }

   public AimBuilder blatant() {
      this.aimType = RotationUtils.AimType.BLATANT;
      return this;
   }

   public AimBuilder points(RotationUtils.EntityPoints points) {
      this.points = points;
      return this;
   }

   public AimBuilder multipoint(float factor) {
      this.multipoint = factor;
      return this;
   }

   public AimBuilder predict(int ticks) {
      this.predictTicks = ticks;
      return this;
   }

   public AimBuilder random(boolean r) {
      this.random = r;
      return this;
   }

   public AimBuilder throughWalls(boolean t) {
      this.throughWalls = t;
      return this;
   }

   public AimBuilder onComplete(Runnable r) {
      this.onComplete = r;
      return this;
   }

   public AimBuilder onTick(Consumer<float[]> consumer) {
      this.onTick = consumer;
      return this;
   }

   public boolean execute() {
      if (mc.player == null) {
         return false;
      } else if (this.directYaw != null && this.directPitch != null) {
         return this.aimFixed(this.directYaw, this.directPitch);
      } else if (this.entityTarget != null) {
         Vec3 aimPoint;
         if (this.predictTicks > 0) {
            aimPoint = RotationUtils.predictAimPoint(this.entityTarget, this.predictTicks, this.points);
         } else {
            float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float shrink = this.multipoint * 0.01F;
            Optional<Vec3> opt = RotationUtils.getPossiblePoints(this.entityTarget, shrink, this.points, this.random, this.throughWalls, pt);
            if (opt.isEmpty()) {
               return false;
            }

            aimPoint = opt.get();
         }

         return this.aimAt(aimPoint);
      } else {
         return this.vecTarget != null ? this.aimAt(this.vecTarget) : false;
      }
   }

   private boolean aimAt(Vec3 point) {
      RotationManager rm = RotationManager.INSTANCE;
      rm.rotationControlClaim.setPriority(this.requester, this.priority.getValue());
      if (!rm.rotationControlClaim.acquire(this.requester, true)) {
         return false;
      } else if (this.reuseOrCreate(rm) instanceof AdaptiveRotationController adaptive) {
         adaptive.setTarget(point);
         this.configureController(adaptive);
         rm.setController(adaptive);
         rm.setCameraMode(!this.silent);
         if (this.aimType == RotationUtils.AimType.BLATANT) {
            RotationAngles target = adaptive.calculateRotation(point);
            rm.snapControllerToTarget(target.getYaw(), target.getPitch());
         } else {
            rm.advanceAndApplyNow(1);
         }

         this.fireCallbacks();
         return true;
      } else {
         return false;
      }
   }

   private boolean aimFixed(float yaw, float pitch) {
      RotationManager rm = RotationManager.INSTANCE;
      rm.rotationControlClaim.setPriority(this.requester, this.priority.getValue());
      if (!rm.rotationControlClaim.acquire(this.requester, true)) {
         return false;
      } else if (this.reuseOrCreate(rm) instanceof AdaptiveRotationController adaptive) {
         adaptive.setTargetRotation(yaw, pitch);
         this.configureController(adaptive);
         rm.setController(adaptive);
         rm.setCameraMode(!this.silent);
         if (this.aimType == RotationUtils.AimType.BLATANT) {
            rm.snapControllerToTarget(yaw, pitch);
         } else {
            rm.advanceAndApplyNow(1);
         }

         this.fireCallbacks();
         return true;
      } else {
         return false;
      }
   }

   private MouseRotationController reuseOrCreate(RotationManager rm) {
      MouseRotationController active = rm.getActiveController();
      return (MouseRotationController)(active instanceof AdaptiveRotationController && rm.rotationControlClaim.isOwnedBy(this.requester)
         ? active
         : new AdaptiveRotationController());
   }

   private void configureController(AdaptiveRotationController controller) {
      controller.setSpeed(Math.max(this.yawSpeed, this.pitchSpeed) / 37.5F);
      controller.setTolerance(3.0F);
      switch (this.aimType) {
         case WINDMOUSE:
            controller.setLinearAcceleration(true);
            controller.setRandomizeMovement(true);
            break;
         case BLATANT:
            controller.setClampStepToRemaining(true);
         case REGULAR:
      }
   }

   private void fireCallbacks() {
      if (this.onTick != null) {
         this.onTick.accept(new float[]{RotationManager.INSTANCE.getManagedYaw(), RotationManager.INSTANCE.getManagedPitch()});
      }

      if (this.onComplete != null) {
         this.onComplete.run();
      }
   }
}
