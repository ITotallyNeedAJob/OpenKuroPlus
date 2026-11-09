package kat.respect.b2.util;

import java.lang.reflect.Field;
import java.util.Optional;
import kat.respect.b2.mixin.IInventoryAccessor;
import kat.respect.b2.mixin.IMultiPlayerGameModeAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ShieldSwapUtil {
   private static final Minecraft mc = Minecraft.getInstance();
   private static Field cachedSlotField = null;
   private static boolean slotFieldResolved = false;

   private ShieldSwapUtil() {
   }

   public static void send(Packet<?> packet) {
      if (packet != null && mc.player != null) {
         try {
            mc.player.connection.send(packet);
         } catch (Throwable var2) {
         }
      }
   }

   private static Field resolveSlotField() {
      if (slotFieldResolved) {
         return cachedSlotField;
      } else {
         slotFieldResolved = true;

         try {
            Field f = Inventory.class.getDeclaredField("selected");
            f.setAccessible(true);
            cachedSlotField = f;
         } catch (Exception var3) {
            try {
               Field fx = Inventory.class.getDeclaredField("selectedSlot");
               fx.setAccessible(true);
               cachedSlotField = fx;
            } catch (Exception var2) {
               cachedSlotField = null;
            }
         }

         return cachedSlotField;
      }
   }

   public static int getSelectedSlot() {
      if (mc.player == null) {
         return 0;
      } else {
         try {
            return ((IInventoryAccessor)mc.player.getInventory()).getSelected();
         } catch (Throwable var4) {
            Field f = resolveSlotField();
            if (f != null) {
               try {
                  return f.getInt(mc.player.getInventory());
               } catch (Exception var3) {
               }
            }

            return 0;
         }
      }
   }

   public static void setSelectedSlot(int slot) {
      if (mc.player != null) {
         if (slot >= 0 && slot <= 8) {
            if (getSelectedSlot() != slot) {
               try {
                  ((IInventoryAccessor)mc.player.getInventory()).setSelected(slot);
               } catch (Throwable var6) {
                  Field f = resolveSlotField();
                  if (f != null) {
                     try {
                        f.setInt(mc.player.getInventory(), slot);
                     } catch (Exception var4) {
                     }
                  }
               }
            }

            if (mc.gameMode != null) {
               try {
                  ((IMultiPlayerGameModeAccessor)mc.gameMode).invokeEnsureHasSentCarriedItem();
                  return;
               } catch (Throwable var5) {
               }
            }

            send(new ServerboundSetCarriedItemPacket(slot));
         }
      }
   }

   public static void clearClick(KeyMapping mapping) {
      if (mapping != null) {
         try {
            Field f = KeyMapping.class.getDeclaredField("clickCount");
            f.setAccessible(true);
            f.setInt(mapping, 0);
         } catch (Exception var2) {
         }
      }
   }

   public static void clearAttackClick() {
      if (mc.options != null) {
         clearClick(mc.options.keyAttack);
      }
   }

   public static boolean isShielding(LivingEntity target) {
      if (target == null) {
         return false;
      } else if (target.isBlocking()) {
         return true;
      } else {
         return !target.isUsingItem() ? false : !target.getUseItem().isEmpty() && target.getUseItem().getItem() instanceof ShieldItem;
      }
   }

   public static boolean isShieldFacingPlayer(LivingEntity target) {
      if (mc.player != null && target != null) {
         Vec3 playerPos = mc.player.getEyePosition();
         Vec3 targetPos = target.getEyePosition();
         Vec3 toPlayer = playerPos.subtract(targetPos);
         Vec3 toPlayerHorizontal = new Vec3(toPlayer.x, 0.0, toPlayer.z);
         if (toPlayerHorizontal.lengthSqr() < 1.0E-6) {
            return true;
         } else {
            toPlayerHorizontal = toPlayerHorizontal.normalize();

            Vec3 facing;
            try {
               facing = target.calculateViewVector(0.0F, target.getYHeadRot());
            } catch (Throwable var7) {
               facing = new Vec3(-Math.sin(Math.toRadians(target.getYHeadRot())), 0.0, Math.cos(Math.toRadians(target.getYHeadRot())));
            }

            Vec3 facingHorizontal = new Vec3(facing.x, 0.0, facing.z);
            if (facingHorizontal.lengthSqr() < 1.0E-6) {
               return true;
            } else {
               facingHorizontal = facingHorizontal.normalize();
               return facingHorizontal.dot(toPlayerHorizontal) >= 0.0;
            }
         }
      } else {
         return false;
      }
   }

   public static double getEyeToBoxDistance(Player player, Entity target) {
      if (player != null && target != null) {
         Vec3 eye = player.getEyePosition();
         AABB box = target.getBoundingBox();
         double x = Math.max(box.minX, Math.min(eye.x, box.maxX));
         double y = Math.max(box.minY, Math.min(eye.y, box.maxY));
         double z = Math.max(box.minZ, Math.min(eye.z, box.maxZ));
         double dx = eye.x - x;
         double dy = eye.y - y;
         double dz = eye.z - z;
         return Math.sqrt(dx * dx + dy * dy + dz * dz);
      } else {
         return Double.MAX_VALUE;
      }
   }

   public static double getSafeReach(Player player) {
      double safeReach = 2.9;
      if (player == null) {
         return safeReach;
      } else {
         Vec3 vel = player.getDeltaMovement();
         if (!player.onGround() && vel.y < 0.0) {
            safeReach -= Math.min(0.3, Math.abs(vel.y));
         }

         double horizSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
         if (horizSpeed > 0.05) {
            safeReach -= Math.min(0.2, horizSpeed * 0.6);
         }

         return safeReach;
      }
   }

   public static boolean canHitEntity(Player player, Entity target, double safeReach) {
      if (player != null && target != null && mc.level != null) {
         if (getEyeToBoxDistance(player, target) > safeReach) {
            return false;
         } else {
            Vec3 eye = player.getEyePosition();

            Vec3 look;
            try {
               look = player.getViewVector(1.0F);
            } catch (Throwable var10) {
               return true;
            }

            Vec3 end = eye.add(look.scale(safeReach));
            Optional<Vec3> clip = target.getBoundingBox().clip(eye, end);
            if (clip.isEmpty()) {
               return false;
            } else {
               Vec3 hitVec = clip.get();
               BlockHitResult blockHit = mc.level.clip(new ClipContext(eye, hitVec, Block.COLLIDER, Fluid.NONE, player));
               return blockHit.getType() == Type.MISS || !(blockHit.getLocation().distanceToSqr(eye) < hitVec.distanceToSqr(eye) - 0.01);
            }
         }
      } else {
         return false;
      }
   }

   public static int shieldHeldTicks(LivingEntity target) {
      if (target != null && target.isUsingItem()) {
         ItemStack use = target.getUseItem();
         return !use.isEmpty() && use.getItem() instanceof ShieldItem ? 72000 - target.getUseItemRemainingTicks() : 0;
      } else {
         return 0;
      }
   }

   public static boolean isSmashReady(LivingEntity target) {
      return shieldHeldTicks(target) > 5;
   }

   public static boolean willSmash(Player player) {
      return player != null && !player.onGround() && player.fallDistance >= 1.5;
   }
}
