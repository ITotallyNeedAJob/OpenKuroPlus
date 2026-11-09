package kat.respect.b2.util.simulation;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class WindChargeSimulation {
   private static final Minecraft mc = Minecraft.getInstance();
   private static final float INERTIA = 1.0F;
   private static final float WATER_INERTIA = 1.0F;
   private static final double ENTITY_CHECK_MARGIN = 1.0;
   private Vec3 position;
   private Vec3 velocity;
   private final Entity sourceEntity;
   private final Entity ownerEntity;
   private boolean leftOwner;
   private boolean hit;

   public WindChargeSimulation(Vec3 position, Vec3 velocity) {
      this(position, velocity, mc.player);
   }

   public WindChargeSimulation(Vec3 position, Vec3 velocity, Entity ignoredEntity) {
      this.position = position;
      this.velocity = velocity;
      this.sourceEntity = ignoredEntity;
      this.ownerEntity = this.resolveOwner(ignoredEntity);
      this.leftOwner = this.ownerEntity == null;
      this.hit = false;
   }

   public void simulateTick() {
      if (!this.hit && mc.level != null) {
         boolean inWater = !mc.level.getFluidState(BlockPos.containing(this.position)).isEmpty();
         Vec3 nextPos = this.position.add(this.velocity);
         this.updateLeftOwner(nextPos);
         BlockHitResult blockHit = mc.level
            .clip(new ClipContext(this.position, nextPos, Block.COLLIDER, Fluid.NONE, (Entity)(this.sourceEntity != null ? this.sourceEntity : mc.player)));
         EntityHitResult entityHit = this.raycastEntity(nextPos);
         if (entityHit == null
            || blockHit.getType() != Type.MISS
               && !(this.position.distanceToSqr(entityHit.getLocation()) <= this.position.distanceToSqr(blockHit.getLocation()))) {
            if (blockHit.getType() != Type.MISS) {
               this.position = blockHit.getLocation();
               this.hit = true;
            } else {
               this.position = nextPos;
               this.velocity = this.velocity.scale(inWater ? 1.0 : 1.0);
            }
         } else {
            this.position = entityHit.getLocation();
            this.hit = true;
         }
      }
   }

   public void simulateTicks(int ticks) {
      for (int i = 0; i < ticks && !this.hit; i++) {
         this.simulateTick();
      }
   }

   public BlockHitResult simulateUntilHit(int maxTicks) {
      for (int i = 0; i < maxTicks && !this.hit; i++) {
         Vec3 prevPos = this.position;
         this.simulateTick();
         if (this.hit) {
            return mc.level.clip(new ClipContext(prevPos, this.position, Block.COLLIDER, Fluid.NONE, mc.player));
         }
      }

      return null;
   }

   public Vec3 getPosition() {
      return this.position;
   }

   public Vec3 getVelocity() {
      return this.velocity;
   }

   public boolean hasHit() {
      return this.hit;
   }

   private EntityHitResult raycastEntity(Vec3 nextPos) {
      AABB searchBox = new AABB(this.position, nextPos).inflate(1.0);
      return ProjectileUtil.getEntityHitResult(
         (Entity)(this.sourceEntity != null ? this.sourceEntity : mc.player),
         this.position,
         nextPos,
         searchBox,
         this::canHitEntity,
         this.position.distanceToSqr(nextPos)
      );
   }

   private boolean canHitEntity(Entity entity) {
      if (entity.canBeHitByProjectile() && !entity.noPhysics) {
         return !(entity instanceof AbstractWindCharge) && entity.getType() != EntityType.END_CRYSTAL
            ? this.ownerEntity == null || this.leftOwner || !this.ownerEntity.isPassengerOfSameVehicle(entity)
            : false;
      } else {
         return false;
      }
   }

   private Entity resolveOwner(Entity sourceEntity) {
      return sourceEntity instanceof Projectile projectile ? projectile.getOwner() : sourceEntity;
   }

   private void updateLeftOwner(Vec3 nextPos) {
      if (!this.leftOwner && this.ownerEntity != null) {
         AABB ownerCheckBox = new AABB(this.position, nextPos).inflate(1.0);
         this.leftOwner = this.ownerEntity.getRootVehicle().getSelfAndPassengers().noneMatch(passenger -> ownerCheckBox.intersects(passenger.getBoundingBox()));
      }
   }
}
