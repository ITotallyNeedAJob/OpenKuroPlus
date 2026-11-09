package kat.respect.b2.rotation.builder;

import kat.respect.b2.module.Module;
import kat.respect.b2.util.PacketUtils;
import kat.respect.b2.util.RotationEngine;
import kat.respect.b2.util.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class AttackBuilder {
   private static final Minecraft mc = Minecraft.getInstance();
   private final Module requester;
   private LivingEntity targetEntity;
   private Item weaponItem;
   private boolean bestWeapon = false;
   private boolean critOnly = false;
   private boolean shieldBreak = true;
   private boolean sprintReset = true;
   private float speed = 360.0F;
   private boolean silent = true;
   private RotationUtils.AimType aimType = RotationUtils.AimType.BLATANT;
   private RotationUtils.EntityPoints points = RotationUtils.EntityPoints.CLOSEST;
   private int predictTicks = 0;
   private Runnable onHit;

   public AttackBuilder(Module requester) {
      this.requester = requester;
   }

   public AttackBuilder target(LivingEntity target) {
      this.targetEntity = target;
      return this;
   }

   public AttackBuilder weapon(Item weapon) {
      this.weaponItem = weapon;
      return this;
   }

   public AttackBuilder bestWeapon(boolean best) {
      this.bestWeapon = best;
      return this;
   }

   public AttackBuilder critOnly(boolean crit) {
      this.critOnly = crit;
      return this;
   }

   public AttackBuilder shieldBreak(boolean breakShield) {
      this.shieldBreak = breakShield;
      return this;
   }

   public AttackBuilder sprintReset(boolean reset) {
      this.sprintReset = reset;
      return this;
   }

   public AttackBuilder speed(float s) {
      this.speed = s;
      return this;
   }

   public AttackBuilder silent() {
      this.silent = true;
      return this;
   }

   public AttackBuilder silent(boolean s) {
      this.silent = s;
      return this;
   }

   public AttackBuilder camera() {
      this.silent = false;
      return this;
   }

   public AttackBuilder aimType(RotationUtils.AimType type) {
      this.aimType = type;
      return this;
   }

   public AttackBuilder points(RotationUtils.EntityPoints points) {
      this.points = points;
      return this;
   }

   public AttackBuilder predict(int ticks) {
      this.predictTicks = ticks;
      return this;
   }

   public AttackBuilder onHit(Runnable callback) {
      this.onHit = callback;
      return this;
   }

   public boolean execute() {
      if (mc.player != null && this.targetEntity != null && this.targetEntity.isAlive()) {
         if (this.critOnly) {
            boolean isFalling = mc.player.fallDistance > 0.0 && !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable();
            if (!isFalling) {
               return false;
            }
         }

         boolean targetIsBlocking = this.targetEntity instanceof Player p && p.isUsingItem() && p.getUseItem().is(Items.SHIELD);
         if (this.shieldBreak && targetIsBlocking) {
            int axeSlot = RotationEngine.findAxeSlot();
            if (axeSlot >= 0) {
               PacketUtils.setSelectedSlot(axeSlot);
            }
         } else if (this.weaponItem != null) {
            int weaponSlot = RotationEngine.findItemSlot(this.weaponItem);
            if (weaponSlot >= 0) {
               PacketUtils.setSelectedSlot(weaponSlot);
            }
         } else if (this.bestWeapon) {
            int bestSlot = RotationEngine.findBestWeaponSlot();
            if (bestSlot >= 0) {
               PacketUtils.setSelectedSlot(bestSlot);
            }
         }

         RotationEngine.aim(this.requester)
            .to(this.targetEntity)
            .speed(this.speed)
            .silent(this.silent)
            .aimType(this.aimType)
            .points(this.points)
            .predict(this.predictTicks)
            .execute();
         if (this.sprintReset && mc.player.isSprinting()) {
            PacketUtils.send(new ServerboundPlayerCommandPacket(mc.player, Action.STOP_SPRINTING));
            PacketUtils.sendAttack(this.targetEntity);
            PacketUtils.sendSwing(InteractionHand.MAIN_HAND);
            PacketUtils.send(new ServerboundPlayerCommandPacket(mc.player, Action.START_SPRINTING));
         } else {
            PacketUtils.sendAttack(this.targetEntity);
            PacketUtils.sendSwing(InteractionHand.MAIN_HAND);
         }

         if (this.onHit != null) {
            this.onHit.run();
         }

         return true;
      } else {
         return false;
      }
   }
}
