package kat.respect.b2.rotation.builder;

import kat.respect.b2.module.Module;
import kat.respect.b2.util.PacketUtils;
import kat.respect.b2.util.RotationEngine;
import kat.respect.b2.util.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BreakBuilder {
   private static final Minecraft mc = Minecraft.getInstance();
   private final Module requester;
   private BlockPos blockPos;
   private boolean bestTool = true;
   private boolean instant = true;
   private float speed = 360.0F;
   private boolean silent = true;
   private Runnable onBreak;

   public BreakBuilder(Module requester) {
      this.requester = requester;
   }

   public BreakBuilder at(BlockPos pos) {
      this.blockPos = pos;
      return this;
   }

   public BreakBuilder bestTool(boolean tool) {
      this.bestTool = tool;
      return this;
   }

   public BreakBuilder instant(boolean inst) {
      this.instant = inst;
      return this;
   }

   public BreakBuilder speed(float s) {
      this.speed = s;
      return this;
   }

   public BreakBuilder silent() {
      this.silent = true;
      return this;
   }

   public BreakBuilder onBreak(Runnable r) {
      this.onBreak = r;
      return this;
   }

   public boolean execute() {
      if (mc.player != null && mc.level != null && this.blockPos != null) {
         if (this.bestTool) {
            BlockState state = mc.level.getBlockState(this.blockPos);
            int toolSlot = RotationEngine.findBestToolSlot(state);
            if (toolSlot >= 0) {
               PacketUtils.setSelectedSlot(toolSlot);
            }
         }

         RotationEngine.aim(this.requester)
            .to(Vec3.atCenterOf(this.blockPos))
            .speed(this.speed)
            .silent(this.silent)
            .aimType(RotationUtils.AimType.BLATANT)
            .execute();
         if (mc.gameMode != null) {
            if (this.instant) {
               mc.gameMode.destroyBlock(this.blockPos);
            } else {
               mc.gameMode.startDestroyBlock(this.blockPos, Direction.UP);
            }
         }

         PacketUtils.sendSwing(InteractionHand.MAIN_HAND);
         if (this.onBreak != null) {
            this.onBreak.run();
         }

         return true;
      } else {
         return false;
      }
   }
}
