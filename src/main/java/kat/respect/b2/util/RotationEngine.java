package kat.respect.b2.util;

import kat.respect.b2.mixin.IInventoryAccessor;
import kat.respect.b2.module.Module;
import kat.respect.b2.rotation.builder.AimBuilder;
import kat.respect.b2.rotation.builder.AttackBuilder;
import kat.respect.b2.rotation.builder.BreakBuilder;
import kat.respect.b2.rotation.builder.InteractBuilder;
import kat.respect.b2.rotation.builder.PlaceBuilder;
import kat.respect.b2.rotation.builder.RestoreBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CartographyTableBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LoomBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SmithingTableBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class RotationEngine {
   private static final Minecraft mc = Minecraft.getInstance();
   private static final RotationConvergenceTracker convergenceTracker = new RotationConvergenceTracker();

   private RotationEngine() {
   }

   public static RotationConvergenceTracker getConvergenceTracker() {
      return convergenceTracker;
   }

   public static void setSneaking(boolean sneak) {
      if (mc.player != null) {
         Input current = mc.player.input != null && mc.player.input.keyPresses != null ? mc.player.input.keyPresses : Input.EMPTY;
         Input updated = new Input(current.forward(), current.backward(), current.left(), current.right(), current.jump(), sneak, current.sprint());
         PacketUtils.send(new ServerboundPlayerInputPacket(updated));
      }
   }

   public static AimBuilder aim(Module requester) {
      return new AimBuilder(requester);
   }

   public static PlaceBuilder place(Module requester) {
      return new PlaceBuilder(requester);
   }

   public static AttackBuilder attack(Module requester) {
      return new AttackBuilder(requester);
   }

   public static InteractBuilder interact(Module requester) {
      return new InteractBuilder(requester);
   }

   public static InteractBuilder use(Module requester) {
      return new InteractBuilder(requester);
   }

   public static BreakBuilder breakBlock(Module requester) {
      return new BreakBuilder(requester);
   }

   public static RestoreBuilder restore(Module requester) {
      return new RestoreBuilder(requester);
   }

   public static boolean aim(LivingEntity target, float speed) {
      return aim(null).to(target).speed(speed).execute();
   }

   public static boolean aim(Vec3 targetPos, float speed) {
      return aim(null).to(targetPos).speed(speed).execute();
   }

   public static boolean place(BlockPos pos, Item item, float speed) {
      return place(null).at(pos).withItem(item).speed(speed).execute();
   }

   public static boolean interact(BlockPos pos, float speed) {
      return interact(null).on(pos).speed(speed).execute();
   }

   public static boolean attack(LivingEntity target, float speed) {
      return attack(null).target(target).speed(speed).execute();
   }

   public static boolean restore(float speed) {
      return restore(null).speed(speed).execute();
   }

   public static int findItemSlot(Item item) {
      if (mc.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).is(item)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static int findAxeSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).is(ItemTags.AXES)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static int findBestWeaponSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         int bestSlot = -1;
         float bestDamage = -1.0F;

         for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            Item item = stack.getItem();
            if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || item instanceof MaceItem) {
               float dmg = 4.0F;
               if (item instanceof MaceItem) {
                  dmg = 10.0F;
               } else if (item == Items.NETHERITE_SWORD) {
                  dmg = 8.0F;
               } else if (item == Items.DIAMOND_SWORD) {
                  dmg = 7.0F;
               } else if (stack.is(ItemTags.AXES)) {
                  dmg = 6.0F;
               }

               if (dmg > bestDamage) {
                  bestDamage = dmg;
                  bestSlot = i;
               }
            }
         }

         return bestSlot;
      }
   }

   public static float weaponCharge(LocalPlayer player, int slot) {
      if (player != null && slot >= 0 && slot <= 8) {
         int current = PacketUtil.getSelectedSlot();
         if (current == slot) {
            return player.getAttackStrengthScale(0.0F);
         } else {
            IInventoryAccessor inventory = (IInventoryAccessor)player.getInventory();
            inventory.setSelected(slot);

            float var4;
            try {
               var4 = player.getAttackStrengthScale(0.0F);
            } finally {
               inventory.setSelected(current);
            }

            return var4;
         }
      } else {
         return 0.0F;
      }
   }

   public static int findBestSwordOrAxeSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         int bestSlot = -1;
         float bestDamage = -1.0F;

         for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            Item item = stack.getItem();
            if (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES)) {
               float dmg = 5.0F;
               if (item == Items.NETHERITE_SWORD) {
                  dmg = 8.0F;
               } else if (item == Items.DIAMOND_SWORD) {
                  dmg = 7.0F;
               } else if (stack.is(ItemTags.AXES)) {
                  dmg = 6.0F;
               }

               if (dmg > bestDamage) {
                  bestDamage = dmg;
                  bestSlot = i;
               }
            }
         }

         return bestSlot;
      }
   }

   public static int findBestToolSlot(BlockState state) {
      if (mc.player == null) {
         return -1;
      } else {
         int bestSlot = -1;
         float bestSpeed = 1.0F;

         for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
               bestSpeed = speed;
               bestSlot = i;
            }
         }

         return bestSlot;
      }
   }

   public static boolean isReplaceableBlock(BlockState state) {
      Block block = state.getBlock();
      return block == Blocks.FIRE
         || block == Blocks.SOUL_FIRE
         || block == Blocks.SHORT_GRASS
         || block == Blocks.TALL_GRASS
         || block == Blocks.SEAGRASS
         || block == Blocks.SNOW
         || block == Blocks.WATER
         || state.canBeReplaced();
   }

   public static boolean isInteractableBlock(BlockState state) {
      Block block = state.getBlock();
      return block instanceof ChestBlock
         || block instanceof EnderChestBlock
         || block instanceof BarrelBlock
         || block instanceof ShulkerBoxBlock
         || block instanceof CraftingTableBlock
         || block instanceof AbstractFurnaceBlock
         || block instanceof AnvilBlock
         || block instanceof HopperBlock
         || block instanceof DispenserBlock
         || block instanceof DropperBlock
         || block instanceof EnchantingTableBlock
         || block instanceof BrewingStandBlock
         || block instanceof BeaconBlock
         || block instanceof LoomBlock
         || block instanceof CartographyTableBlock
         || block instanceof GrindstoneBlock
         || block instanceof StonecutterBlock
         || block instanceof SmithingTableBlock
         || block instanceof LecternBlock
         || block instanceof DoorBlock
         || block instanceof TrapDoorBlock
         || block instanceof FenceGateBlock
         || block instanceof LeverBlock
         || block instanceof ButtonBlock
         || block instanceof NoteBlock
         || block instanceof RepeaterBlock
         || block instanceof ComparatorBlock
         || block instanceof DaylightDetectorBlock
         || block instanceof BedBlock
         || block instanceof RespawnAnchorBlock
         || block instanceof BellBlock
         || block instanceof JukeboxBlock
         || block instanceof CakeBlock;
   }
}
