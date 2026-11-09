package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;
import net.minecraft.network.protocol.Packet;

public class PacketEvent extends Event {
   private final PacketEvent.Direction direction;
   private final Packet<?> packet;

   public PacketEvent(PacketEvent.Direction direction, Packet<?> packet) {
      super(true);
      this.direction = direction;
      this.packet = packet;
   }

   public PacketEvent.Direction getDirection() {
      return this.direction;
   }

   public boolean isSend() {
      return this.direction == PacketEvent.Direction.SEND;
   }

   public boolean isReceive() {
      return this.direction == PacketEvent.Direction.RECEIVE;
   }

   public Packet<?> getPacket() {
      return this.packet;
   }

   public static enum Direction {
      SEND,
      RECEIVE;
   }
}
