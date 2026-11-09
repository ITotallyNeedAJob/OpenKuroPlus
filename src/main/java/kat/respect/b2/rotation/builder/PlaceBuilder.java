package kat.respect.b2.rotation.builder;

import kat.respect.b2.module.Module;
import kat.respect.b2.util.BlockUtils;
import kat.respect.b2.util.PacketUtils;
import kat.respect.b2.util.RotationEngine;
import kat.respect.b2.util.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PlaceBuilder {
   private static final Minecraft mc = Minecraft.getInstance();
   private final Module requester;
   private BlockPos targetPos;
   private Item placeItem;
   private int explicitSlot = -1;
   private boolean autoNeighbor = true;
   private boolean autoSneak = true;
   private boolean absorbReplaceable = true;
   private boolean grimBypass = true;
   private boolean requireRaycast = true;
   private boolean swapBack = false;
   private int swapBackDelayTicks = 0;
   private float speed = 360.0F;
   private boolean silent = true;
   private RotationUtils.Priority priority = RotationUtils.Priority.HIGH;
   private Runnable onPlace;

   public PlaceBuilder(Module requester) {
      this.requester = requester;
   }

   public PlaceBuilder at(BlockPos pos) {
      this.targetPos = pos;
      return this;
   }

   public PlaceBuilder withItem(Item item) {
      this.placeItem = item;
      return this;
   }

   public PlaceBuilder withSlot(int slot) {
      this.explicitSlot = slot;
      return this;
   }

   public PlaceBuilder autoNeighbor(boolean auto) {
      this.autoNeighbor = auto;
      return this;
   }

   public PlaceBuilder autoSneak(boolean sneak) {
      this.autoSneak = sneak;
      return this;
   }

   public PlaceBuilder absorbReplaceable(boolean absorb) {
      this.absorbReplaceable = absorb;
      return this;
   }

   public PlaceBuilder grimBypass(boolean grim) {
      this.grimBypass = grim;
      return this;
   }

   public PlaceBuilder requireRaycast(boolean raycast) {
      this.requireRaycast = raycast;
      return this;
   }

   public PlaceBuilder swapBack(boolean back, int delayTicks) {
      this.swapBack = back;
      this.swapBackDelayTicks = delayTicks;
      return this;
   }

   public PlaceBuilder speed(float s) {
      this.speed = s;
      return this;
   }

   public PlaceBuilder silent() {
      this.silent = true;
      return this;
   }

   public PlaceBuilder silent(boolean s) {
      this.silent = s;
      return this;
   }

   public PlaceBuilder camera() {
      this.silent = false;
      return this;
   }

   public PlaceBuilder priority(RotationUtils.Priority p) {
      this.priority = p;
      return this;
   }

   public PlaceBuilder onPlace(Runnable callback) {
      this.onPlace = callback;
      return this;
   }

   public boolean execute() {
      if (mc.player != null && mc.level != null && this.targetPos != null) {
         BlockPos actualPlacePos = this.targetPos;
         BlockState currentState = mc.level.getBlockState(actualPlacePos);
         if (this.absorbReplaceable && !currentState.isAir() && RotationEngine.isReplaceableBlock(currentState)) {
         }

         BlockUtils.PlacementHit placementHit = null;
         if (this.autoNeighbor) {
            placementHit = BlockUtils.findPlacementHit(actualPlacePos, this.requireRaycast);
         }

         if (placementHit == null) {
            BlockPos below = actualPlacePos.below();
            if (mc.level.getBlockState(below).isFaceSturdy(mc.level, below, Direction.UP)) {
               placementHit = new BlockUtils.PlacementHit(
                  actualPlacePos, new BlockHitResult(Vec3.atCenterOf(below).add(0.0, 0.5, 0.0), Direction.UP, below, false)
               );
            }
         }

         if (placementHit == null) {
            return false;
         } else {
            int prevSlot = PacketUtils.getSelectedSlot();
            int targetSlot = this.explicitSlot;
            if (targetSlot < 0 && this.placeItem != null) {
               targetSlot = RotationEngine.findItemSlot(this.placeItem);
            }

            if (targetSlot >= 0 && targetSlot != prevSlot) {
               PacketUtils.setSelectedSlot(targetSlot);
            }

            Vec3 hitVec = placementHit.hitResult().getLocation();
            RotationEngine.aim(this.requester)
               .to(hitVec)
               .speed(this.speed)
               .silent(this.silent)
               .priority(this.priority)
               .aimType(RotationUtils.AimType.BLATANT)
               .execute();
            BlockPos clickedBlockPos = placementHit.hitResult().getBlockPos();
            BlockState clickedBlockState = mc.level.getBlockState(clickedBlockPos);
            boolean needsSneak = this.autoSneak && RotationEngine.isInteractableBlock(clickedBlockState) && !mc.player.isShiftKeyDown();
            if (needsSneak) {
               RotationEngine.setSneaking(true);
            }

            if (this.grimBypass) {
               RotationEngine.getConvergenceTracker().markInteraction();
            }

            InteractionResult result = BlockUtils.interactWithBlock(placementHit.hitResult(), true);
            if (needsSneak) {
               RotationEngine.setSneaking(false);
            }

            if (this.swapBack && targetSlot >= 0 && targetSlot != prevSlot) {
               PacketUtils.setSelectedSlot(prevSlot);
            }

            if (result.consumesAction()) {
               if (this.onPlace != null) {
                  this.onPlace.run();
               }

               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }
}
