package kat.respect.b2.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@Deprecated
public final class PacketUtils {
   private PacketUtils() {
   }

   public static void send(Packet<?> packet) {
      PacketUtil.send(packet);
   }

   public static ClientPacketListener getConnection() {
      return PacketUtil.connection();
   }

   public static boolean isConnected() {
      return PacketUtil.isConnected();
   }

   public static void sendPositionAndRotation(double x, double y, double z, float yaw, float pitch, boolean onGround) {
      PacketUtil.sendPosRot(x, y, z, yaw, pitch, onGround);
   }

   public static void sendPosition(double x, double y, double z, boolean onGround) {
      PacketUtil.sendPos(x, y, z, onGround);
   }

   public static void sendRotation(float yaw, float pitch, boolean onGround) {
      PacketUtil.sendRot(yaw, pitch, onGround);
   }

   public static void sendGround(boolean onGround) {
      PacketUtil.sendGround(onGround);
   }

   public static void sendCurrentPositionAndRotation() {
      PacketUtil.sendCurrentPosRot();
   }

   public static void sendRelativePosition(double dx, double dy, double dz, boolean onGround) {
      PacketUtil.sendRelPos(dx, dy, dz, onGround);
   }

   public static void sendAttack(Entity entity) {
      PacketUtil.sendAttack(entity);
   }

   public static void sendInteract(Entity entity, InteractionHand hand) {
      PacketUtil.sendInteract(entity, hand);
   }

   public static void sendInteractAt(Entity entity, InteractionHand hand, Vec3 hitVec) {
      PacketUtil.sendInteractAt(entity, hand, hitVec);
   }

   public static void sendSwing(InteractionHand hand) {
      PacketUtil.sendSwing(hand);
   }

   public static void sendSwing() {
      PacketUtil.sendSwing();
   }

   public static boolean isKeyPressed(int key) {
      return PacketUtil.isKeyPressed(key);
   }

   public static BlockHitResult findNeighborPlacement(BlockHitResult hit) {
      return PacketUtil.findNeighborPlacement(hit);
   }

   public static boolean placeBlock(BlockHitResult hit) {
      return PacketUtil.placeBlock(hit);
   }

   public static boolean placeBlock(BlockHitResult hit, InteractionHand hand) {
      return PacketUtil.placeBlock(hit, hand);
   }

   public static void attackEntity(Entity entity) {
      PacketUtil.attackEntity(entity);
   }

   public static void sendUseItemOn(BlockHitResult hitResult, InteractionHand hand) {
      PacketUtil.sendUseItemOn(hitResult, hand);
   }

   public static void sendStartDigging(BlockPos pos, Direction face) {
      PacketUtil.sendStartDigging(pos, face);
   }

   public static void sendAbortDigging(BlockPos pos, Direction face) {
      PacketUtil.sendAbortDigging(pos, face);
   }

   public static void sendFinishDigging(BlockPos pos, Direction face) {
      PacketUtil.sendFinishDigging(pos, face);
   }

   public static void sendDropItem(boolean fullStack) {
      PacketUtil.sendDropItem(fullStack);
   }

   public static void sendHotbarSlot(int slot) {
      PacketUtil.sendHotbarSlot(slot);
   }

   public static int getSelectedSlot() {
      return PacketUtil.getSelectedSlot();
   }

   public static void setSelectedSlot(int slot) {
      PacketUtil.setSelectedSlot(slot);
   }

   public static void sendUseItem(InteractionHand hand) {
      PacketUtil.sendUseItem(hand);
   }

   public static void sendUseItem(InteractionHand hand, float yaw, float pitch) {
      PacketUtil.sendUseItem(hand, yaw, pitch);
   }

   public static void sendUseItem() {
      PacketUtil.sendUseItem();
   }

   public static void sendContainerClick(
      int containerId, int stateId, short slot, byte button, ContainerInput containerInput, Int2ObjectMap<HashedStack> changedSlots, HashedStack carriedItem
   ) {
      PacketUtil.sendContainerClick(containerId, stateId, slot, button, containerInput, changedSlots, carriedItem);
   }

   public static void sendContainerClick(int containerId, int stateId, short slot, byte button, ContainerInput input) {
      PacketUtil.sendContainerClick(containerId, stateId, slot, button, input);
   }

   public static void handleSlotClick(AbstractContainerMenu menu, int slotId, int button, ContainerInput input) {
      PacketUtil.handleSlotClick(menu, slotId, button, input);
   }

   public static void sendCloseContainer() {
      PacketUtil.sendCloseContainer();
   }

   public static void sendChat(String message) {
      PacketUtil.sendChat(message);
   }

   public static void sendCommand(String command) {
      PacketUtil.sendCommand(command);
   }

   public static void sendAbilities() {
      PacketUtil.sendAbilities();
   }

   public static void sendSprint(boolean sprinting) {
      PacketUtil.sendSprint(sprinting);
   }

   public static void sendFallFlying() {
      PacketUtil.sendFallFlying();
   }

   public static void sendOpenInventory() {
      PacketUtil.sendOpenInventory();
   }
}
