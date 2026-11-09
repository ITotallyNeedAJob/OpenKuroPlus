package kat.respect.b2.rotation.builder;

import kat.respect.b2.module.Module;
import kat.respect.b2.util.BlockUtils;
import kat.respect.b2.util.PacketUtils;
import kat.respect.b2.util.RotationEngine;
import kat.respect.b2.util.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class InteractBuilder {
   private static final Minecraft mc = Minecraft.getInstance();
   private final Module requester;
   private BlockPos blockPos;
   private Entity entityTarget;
   private Item useItem;
   private InteractionHand hand = InteractionHand.MAIN_HAND;
   private boolean autoSneak = true;
   private boolean swapBack = false;
   private int swapBackDelay = 0;
   private float speed = 360.0F;
   private boolean silent = true;
   private Runnable onComplete;

   public InteractBuilder(Module requester) {
      this.requester = requester;
   }

   public InteractBuilder on(BlockPos pos) {
      this.blockPos = pos;
      return this;
   }

   public InteractBuilder on(Entity entity) {
      this.entityTarget = entity;
      return this;
   }

   public InteractBuilder withItem(Item item) {
      this.useItem = item;
      return this;
   }

   public InteractBuilder hand(InteractionHand hand) {
      this.hand = hand;
      return this;
   }

   public InteractBuilder autoSneak(boolean sneak) {
      this.autoSneak = sneak;
      return this;
   }

   public InteractBuilder swapBack(boolean back, int delay) {
      this.swapBack = back;
      this.swapBackDelay = delay;
      return this;
   }

   public InteractBuilder speed(float s) {
      this.speed = s;
      return this;
   }

   public InteractBuilder silent() {
      this.silent = true;
      return this;
   }

   public InteractBuilder silent(boolean s) {
      this.silent = s;
      return this;
   }

   public InteractBuilder camera() {
      this.silent = false;
      return this;
   }

   public InteractBuilder onComplete(Runnable r) {
      this.onComplete = r;
      return this;
   }

   public boolean execute() {
      if (mc.player == null) {
         return false;
      } else {
         int prevSlot = PacketUtils.getSelectedSlot();
         int targetSlot = this.useItem != null ? RotationEngine.findItemSlot(this.useItem) : -1;
         if (targetSlot >= 0 && targetSlot != prevSlot) {
            PacketUtils.setSelectedSlot(targetSlot);
         }

         if (this.blockPos != null && mc.level != null) {
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(this.blockPos), Direction.UP, this.blockPos, false);
            RotationEngine.aim(this.requester).to(hit.getLocation()).speed(this.speed).silent(this.silent).aimType(RotationUtils.AimType.BLATANT).execute();
            BlockState state = mc.level.getBlockState(this.blockPos);
            boolean needsSneak = this.autoSneak && RotationEngine.isInteractableBlock(state) && !mc.player.isShiftKeyDown();
            if (needsSneak) {
               RotationEngine.setSneaking(true);
            }

            BlockUtils.interactWithBlock(hit, true);
            if (needsSneak) {
               RotationEngine.setSneaking(false);
            }
         } else if (this.entityTarget != null) {
            RotationEngine.aim(this.requester)
               .to(this.entityTarget.position().add(0.0, this.entityTarget.getBbHeight() / 2.0F, 0.0))
               .speed(this.speed)
               .silent(this.silent)
               .aimType(RotationUtils.AimType.BLATANT)
               .execute();
            PacketUtils.sendInteract(this.entityTarget, this.hand);
         } else if (mc.gameMode != null) {
            mc.gameMode.useItem(mc.player, this.hand);
         }

         if (this.swapBack && targetSlot >= 0 && targetSlot != prevSlot) {
            PacketUtils.setSelectedSlot(prevSlot);
         }

         if (this.onComplete != null) {
            this.onComplete.run();
         }

         return true;
      }
   }
}
