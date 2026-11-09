package kat.respect.b2.util;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.protocol.Packet;

public final class PacketDispatchGuard {
   private static final Set<Packet<?>> IN_FLIGHT = ConcurrentHashMap.newKeySet();

   private PacketDispatchGuard() {
   }

   public static boolean isGuarded(Packet<?> packet) {
      return packet != null && IN_FLIGHT.contains(packet);
   }

   public static boolean mark(Packet<?> packet) {
      return packet != null && IN_FLIGHT.add(packet);
   }

   public static void unmark(Packet<?> packet) {
      if (packet != null) {
         IN_FLIGHT.remove(packet);
      }
   }

   public static void send(Packet<?> packet) {
      if (packet != null) {
         mark(packet);

         try {
            PacketUtils.send(packet);
         } finally {
            unmark(packet);
         }
      }
   }

   public static void sendAll(Collection<? extends Packet<?>> packets) {
      if (packets != null && !packets.isEmpty()) {
         for (Packet<?> packet : packets) {
            send(packet);
         }
      }
   }
}
