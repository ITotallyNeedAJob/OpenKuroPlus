package kat.respect.b2.util;

import java.util.ArrayList;
import java.util.List;
import kat.respect.b2.event.EventBus;
import kat.respect.b2.event.EventHandler;
import kat.respect.b2.event.events.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

public final class TargetCache {
   public static final TargetCache INSTANCE = new TargetCache();
   public static final double RANGE = 24.0;
   public static final double RANGE_SQ = 576.0;
   private static final int SCAN_LIMIT = 256;
   private final List<LivingEntity> living = new ArrayList<>(64);
   private final List<Player> players = new ArrayList<>(16);
   private final List<LivingEntity> buffer = new ArrayList<>(256);
   private int tick;
   private int refreshedTick = -1;

   private TargetCache() {
   }

   public static void init() {
      EventBus.INSTANCE.subscribe(INSTANCE);
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getPhase() == TickEvent.Phase.PRE) {
         this.tick++;
      }
   }

   public static List<LivingEntity> living() {
      INSTANCE.ensureFresh();
      return INSTANCE.living;
   }

   public static List<Player> players() {
      INSTANCE.ensureFresh();
      return INSTANCE.players;
   }

   public static double horizontalDistanceSq(Entity entity) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer self = mc.player;
      if (self != null && entity != null) {
         double dx = entity.getX() - self.getX();
         double dz = entity.getZ() - self.getZ();
         return dx * dx + dz * dz;
      } else {
         return Double.MAX_VALUE;
      }
   }

   public static boolean inRange(Entity entity) {
      return horizontalDistanceSq(entity) <= 576.0;
   }

   private void ensureFresh() {
      if (this.refreshedTick != this.tick) {
         this.refreshedTick = this.tick;
         this.refresh();
      }
   }

   private void refresh() {
      this.living.clear();
      this.players.clear();
      this.buffer.clear();
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer self = mc.player;
      if (self != null && mc.level != null) {
         double px = self.getX();
         double pz = self.getZ();
         AABB area = new AABB(px - 24.0, mc.level.getMinY(), pz - 24.0, px + 24.0, mc.level.getMaxY(), pz + 24.0);
         mc.level.getEntities(EntityTypeTest.forClass(LivingEntity.class), area, e -> e != self && e.isAlive() && !e.isRemoved(), this.buffer, 256);

         for (int i = 0; i < this.buffer.size(); i++) {
            LivingEntity entity = this.buffer.get(i);
            double dx = entity.getX() - px;
            double dz = entity.getZ() - pz;
            if (!(dx * dx + dz * dz > 576.0)) {
               this.living.add(entity);
               if (entity instanceof Player player) {
                  this.players.add(player);
               }
            }
         }
      }
   }
}
