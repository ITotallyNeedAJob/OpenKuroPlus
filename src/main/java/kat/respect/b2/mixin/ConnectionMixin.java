package kat.respect.b2.mixin;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.util.Set;
import kat.respect.b2.event.ClientEventDispatcher;
import kat.respect.b2.event.events.PacketEvent;
import kat.respect.b2.rotation.RotationManager;
import kat.respect.b2.util.SentRotation;
import net.lenni0451.classtransform.annotations.CInline;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Connection.class})
public class ConnectionMixin {
   @Unique
   private static float respectB2$lastSentYaw = Float.NaN;
   @Unique
   private static float respectB2$lastSentPitch = Float.NaN;

   @Inject(
      method = {"channelRead0"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void onPacketRecv(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
      respectB2$trackForcedRotation(packet);
      PacketEvent event = ClientEventDispatcher.onPacketReceive(packet);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Unique
   private static void respectB2$trackForcedRotation(Packet<?> packet) {
      try {
         if (packet instanceof ClientboundPlayerPositionPacket pos) {
            PositionMoveRotation change = pos.change();
            Set<Relative> relatives = pos.relatives();
            float yaw = change.yRot()
               + (relatives != null && relatives.contains(Relative.Y_ROT) && !Float.isNaN(respectB2$lastSentYaw) ? respectB2$lastSentYaw : 0.0F);
            float pitch = Mth.clamp(
               change.xRot()
                  + (relatives != null && relatives.contains(Relative.X_ROT) && !Float.isNaN(respectB2$lastSentPitch) ? respectB2$lastSentPitch : 0.0F),
               -90.0F,
               90.0F
            );
            respectB2$lastSentYaw = yaw;
            respectB2$lastSentPitch = pitch;
            SentRotation.update(yaw, pitch, false);
         } else if (packet instanceof ClientboundPlayerRotationPacket rot) {
            float yaw = rot.yRot() + (rot.relativeY() && !Float.isNaN(respectB2$lastSentYaw) ? respectB2$lastSentYaw : 0.0F);
            float pitch = Mth.clamp(rot.xRot() + (rot.relativeX() && !Float.isNaN(respectB2$lastSentPitch) ? respectB2$lastSentPitch : 0.0F), -90.0F, 90.0F);
            respectB2$lastSentYaw = yaw;
            respectB2$lastSentPitch = pitch;
            SentRotation.update(yaw, pitch, false);
         }
      } catch (LinkageError var7) {
      }
   }

   @Inject(
      method = {"send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   @CInline
   private void onPacketSend(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
      PacketEvent event = ClientEventDispatcher.onPacketSend(packet);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"},
      at = {@At("TAIL")}
   )
   @CInline
   private void respectB2$trackSentRotation(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
      try {
         if (packet instanceof ServerboundMovePlayerPacket move && move.hasRotation()) {
            float yaw = move.getYRot(0.0F);
            float pitch = Mth.clamp(move.getXRot(0.0F), -90.0F, 90.0F);
            respectB2$lastSentYaw = yaw;
            respectB2$lastSentPitch = pitch;
            boolean spoofed = RotationManager.getPacketRotation() != null;
            SentRotation.update(yaw, pitch, spoofed);
         }
      } catch (LinkageError var9) {
      }
   }
}
