package kat.respect.b2.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.lang.reflect.Field;
import kat.respect.b2.mixin.IInventoryAccessor;
import kat.respect.b2.mixin.IMultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PacketUtil {
   private static final Minecraft mc = Minecraft.getInstance();
   private static Field cachedSlotField = null;
   private static boolean slotFieldResolved = false;

   private PacketUtil() {
   }

   public static void send(Packet<?> packet) {
      ClientPacketListener conn = connection();
      if (conn != null) {
         conn.send(packet);
      }
   }

   public static ClientPacketListener connection() {
      return mc.player == null ? null : mc.player.connection;
   }

   public static boolean isConnected() {
      return connection() != null;
   }

   public static void sendPosRot(double x, double y, double z, float yaw, float pitch, boolean onGround) {
      send(new PosRot(x, y, z, yaw, pitch, onGround, false));
   }

   public static void sendPos(double x, double y, double z, boolean onGround) {
      send(new Pos(x, y, z, onGround, false));
   }

   public static void sendRot(float yaw, float pitch, boolean onGround) {
      send(new Rot(yaw, pitch, onGround, false));
   }

   public static void sendGround(boolean onGround) {
      send(new StatusOnly(onGround, false));
   }

   public static void sendCurrentPosRot() {
      if (mc.player != null) {
         sendPosRot(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround());
      }
   }

   public static void sendRelPos(double dx, double dy, double dz, boolean onGround) {
      if (mc.player != null) {
         sendPos(mc.player.getX() + dx, mc.player.getY() + dy, mc.player.getZ() + dz, onGround);
      }
   }

   public static void sendAttack(Entity entity) {
      if (mc.player != null && entity != null) {
         mc.player.attack(entity);
      }
   }

   public static void sendInteract(Entity entity, InteractionHand hand) {
      if (entity != null && mc.player != null) {
         PacketCompat.sendInteract(entity, hand, Vec3.ZERO, mc.player.isShiftKeyDown());
      }
   }

   public static void sendInteractAt(Entity entity, InteractionHand hand, Vec3 hitVec) {
      if (entity != null && mc.player != null) {
         PacketCompat.sendInteract(entity, hand, hitVec, mc.player.isShiftKeyDown());
      }
   }

   public static void sendSwing(InteractionHand hand) {
      PacketCompat.sendSwing(hand);
   }

   public static void sendSwing() {
      PacketCompat.sendSwing(InteractionHand.MAIN_HAND);
   }

   public static boolean isKeyPressed(int key) {
      return KeyUtil.isDown(key);
   }

   public static BlockHitResult findNeighborPlacement(BlockHitResult hit) {
      if (mc.level != null && hit != null) {
         BlockPos pos = hit.getBlockPos();
         BlockState state = mc.level.getBlockState(pos);
         boolean fluid = !state.getFluidState().isEmpty();
         if (!state.isAir() && !fluid) {
            return hit;
         } else {
            for (Direction dir : Direction.values()) {
               BlockPos neighbor = pos.relative(dir);
               BlockState neighborState = mc.level.getBlockState(neighbor);
               if (!neighborState.isAir() && neighborState.getFluidState().isEmpty() && !neighborState.canBeReplaced()) {
                  Direction face = dir.getOpposite();
                  Vec3 hitVec = Vec3.atCenterOf(neighbor).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
                  return new BlockHitResult(hitVec, face, neighbor, false);
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean isPlacementTargetValid(BlockHitResult hit) {
      if (mc.level != null && hit != null) {
         BlockState state = mc.level.getBlockState(hit.getBlockPos());
         return state.isAir() ? false : state.getFluidState().isEmpty();
      } else {
         return false;
      }
   }

   public static boolean placeBlock(BlockHitResult hit) {
      return placeBlock(hit, InteractionHand.MAIN_HAND);
   }

   public static boolean placeBlock(BlockHitResult hit, InteractionHand hand) {
      if (mc.gameMode != null && mc.player != null && mc.level != null && hit != null) {
         BlockHitResult placementHit = findNeighborPlacement(hit);
         if (placementHit == null) {
            return false;
         } else if (!isPlacementTargetValid(placementHit)) {
            return false;
         } else if (!mc.player.isWithinBlockInteractionRange(placementHit.getBlockPos(), 1.0)) {
            return false;
         } else if (mc.gameMode.useItemOn(mc.player, hand, placementHit).consumesAction()) {
            SwingCompat.swing(mc.player, hand);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void attackEntity(Entity entity) {
      if (mc.gameMode != null && mc.player != null && entity != null) {
         mc.gameMode.attack(mc.player, entity);
         SwingCompat.swing(mc.player, InteractionHand.MAIN_HAND);
      }
   }

   public static void sendUseItemOn(BlockHitResult hitResult, InteractionHand hand) {
      if (hitResult != null) {
         send(new ServerboundUseItemOnPacket(hand, hitResult, 0));
      }
   }

   public static void sendStartDigging(BlockPos pos, Direction face) {
      if (pos != null) {
         send(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, pos, face));
      }
   }

   public static void sendAbortDigging(BlockPos pos, Direction face) {
      if (pos != null) {
         send(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, pos, face));
      }
   }

   public static void sendFinishDigging(BlockPos pos, Direction face) {
      if (pos != null) {
         send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, pos, face));
      }
   }

   public static void sendDropItem(boolean fullStack) {
      Action action = fullStack ? Action.DROP_ALL_ITEMS : Action.DROP_ITEM;
      send(new ServerboundPlayerActionPacket(action, BlockPos.ZERO, Direction.DOWN));
   }

   public static void sendHotbarSlot(int slot) {
      setSelectedSlot(slot);
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

   public static void sendUseItem(InteractionHand hand) {
      if (mc.player != null) {
         send(new ServerboundUseItemPacket(hand, 0, mc.player.getYRot(), mc.player.getXRot()));
      }
   }

   public static void sendUseItem(InteractionHand hand, float yaw, float pitch) {
      if (mc.player != null) {
         send(new ServerboundUseItemPacket(hand, 0, yaw, pitch));
      }
   }

   public static void sendUseItem() {
      sendUseItem(InteractionHand.MAIN_HAND);
   }

   public static void sendContainerClick(
      int containerId, int stateId, short slot, byte button, ContainerInput containerInput, Int2ObjectMap<HashedStack> changedSlots, HashedStack carriedItem
   ) {
      send(new ServerboundContainerClickPacket(containerId, stateId, slot, button, containerInput, changedSlots, carriedItem));
   }

   public static void sendContainerClick(int containerId, int stateId, short slot, byte button, ContainerInput input) {
      sendContainerClick(containerId, stateId, slot, button, input, new Int2ObjectArrayMap(), HashedStack.EMPTY);
   }

   public static void handleSlotClick(AbstractContainerMenu menu, int slotId, int button, ContainerInput input) {
      if (mc.player != null && menu != null) {
         sendContainerClick(menu.containerId, menu.getStateId(), (short)slotId, (byte)button, input, new Int2ObjectArrayMap(), HashedStack.EMPTY);

         try {
            menu.clicked(slotId, button, input, mc.player);
         } catch (Exception var5) {
         }
      }
   }

   public static void sendCloseContainer() {
      if (mc.player != null) {
         send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
      }
   }

   public static void sendChat(String message) {
      if (mc.player != null && message != null && !message.isEmpty()) {
         mc.player.connection.sendChat(message);
      }
   }

   public static void sendCommand(String command) {
      if (mc.player != null && command != null && !command.isEmpty()) {
         mc.player.connection.sendCommand(command.startsWith("/") ? command.substring(1) : command);
      }
   }

   public static void sendAbilities() {
      if (mc.player != null) {
         send(new ServerboundPlayerAbilitiesPacket(mc.player.getAbilities()));
      }
   }

   public static void sendSprint(boolean sprinting) {
      if (mc.player != null) {
         net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action action = sprinting
            ? net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.START_SPRINTING
            : net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.STOP_SPRINTING;
         send(new ServerboundPlayerCommandPacket(mc.player, action));
      }
   }

   public static void sendFallFlying() {
      if (mc.player != null) {
         send(new ServerboundPlayerCommandPacket(mc.player, net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
      }
   }

   public static void sendOpenInventory() {
      if (mc.player != null) {
         send(new ServerboundPlayerCommandPacket(mc.player, net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY));
      }
   }
}
