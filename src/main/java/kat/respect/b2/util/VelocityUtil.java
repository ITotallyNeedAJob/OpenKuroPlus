package kat.respect.b2.util;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class VelocityUtil {
   private static final Map<Integer, VelocityUtil.State> STATES = new ConcurrentHashMap<>();
   private static final double SMOOTH_TAU_SEC = 0.08;
   private static final double MIN_DT_SEC = 0.001;
   private static final double MAX_DT_SEC = 0.25;
   private static final double TELEPORT_THRESHOLD_BLOCKS = 8.0;
   private static ClientLevel lastWorldRef = null;

   private VelocityUtil() {
   }

   public static void updateAll() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.level != null) {
         if (lastWorldRef != mc.level) {
            STATES.clear();
            lastWorldRef = mc.level;
         }

         for (Player p : mc.level.players()) {
            updateFor(p);
         }
      }
   }

   public static void updateFor(Entity e) {
      Objects.requireNonNull(e, "entity");
      long nowNs = System.nanoTime();
      Vec3 pos = new Vec3(e.getX(), e.getY(), e.getZ());
      VelocityUtil.State s = STATES.get(e.getId());
      if (s == null) {
         s = new VelocityUtil.State(pos, nowNs);
         STATES.put(e.getId(), s);
      } else {
         double dt = (nowNs - s.lastNs) / 1.0E9;
         if (!(dt <= 0.001)) {
            if (dt > 0.25) {
               s.lastPos = pos;
               s.lastNs = nowNs;
            } else {
               Vec3 dp = pos.subtract(s.lastPos);
               if (dp.length() > 8.0) {
                  s.lastPos = pos;
                  s.lastNs = nowNs;
                  s.velPerSecEma = Vec3.ZERO;
                  s.samples = 0;
               } else {
                  Vec3 vPerSec = dp.scale(1.0 / dt);
                  double alpha = emaAlpha(dt, 0.08);
                  if (s.samples == 0) {
                     s.velPerSecEma = vPerSec;
                  } else {
                     s.velPerSecEma = lerp(s.velPerSecEma, vPerSec, alpha);
                  }

                  s.samples++;
                  s.lastPos = pos;
                  s.lastNs = nowNs;
               }
            }
         }
      }
   }

   public static Vec3 get(Entity e) {
      return getPerSecond(e).scale(0.05);
   }

   public static Vec3 getPerSecond(Entity e) {
      VelocityUtil.State s = STATES.get(e.getId());
      return s != null && s.samples > 0 ? s.velPerSecEma : e.getDeltaMovement().scale(20.0);
   }

   public static double getHorizontalSpeed(Entity e) {
      Vec3 v = get(e);
      return Math.hypot(v.x, v.z);
   }

   public static double getHorizontalSpeedPerSecond(Entity e) {
      Vec3 v = getPerSecond(e);
      return Math.hypot(v.x, v.z);
   }

   public static void reset(Entity e) {
      STATES.remove(e.getId());
   }

   public static void resetAll() {
      STATES.clear();
      lastWorldRef = null;
   }

   public static boolean hasSample(Entity e) {
      VelocityUtil.State s = STATES.get(e.getId());
      return s != null && s.samples > 0;
   }

   private static double emaAlpha(double dt, double tau) {
      if (tau <= 0.0) {
         return 1.0;
      } else {
         double a = 1.0 - Math.exp(-dt / tau);
         return Math.max(0.0, Math.min(1.0, a));
      }
   }

   private static Vec3 lerp(Vec3 a, Vec3 b, double t) {
      return new Vec3(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t);
   }

   private static final class State {
      Vec3 lastPos;
      long lastNs;
      Vec3 velPerSecEma;
      int samples;

      State(Vec3 pos, long nowNs) {
         this.lastPos = pos;
         this.lastNs = nowNs;
         this.velPerSecEma = Vec3.ZERO;
         this.samples = 0;
      }
   }
}
