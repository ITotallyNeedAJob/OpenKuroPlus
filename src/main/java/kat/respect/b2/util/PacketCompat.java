package kat.respect.b2.util;

import it.unimi.dsi.fastutil.ints.IntList;
import java.lang.reflect.Constructor;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class PacketCompat {
   private PacketCompat() {
   }

   public static int getMotionId(ClientboundSetEntityMotionPacket packet) {
      return packet == null ? -1 : packet.id();
   }

   public static Vec3 getMotionMovement(ClientboundSetEntityMotionPacket packet) {
      return packet == null ? Vec3.ZERO : packet.movement();
   }

   public static InteractionHand getInteractHand(ServerboundInteractPacket packet) {
      return packet == null ? null : packet.hand();
   }

   public static int getInteractEntityId(ServerboundInteractPacket packet) {
      return packet == null ? -1 : packet.entityId();
   }

   public static void sendInteract(Entity entity, InteractionHand hand, Vec3 hitVec, boolean secondary) {
      if (entity != null && Minecraft.getInstance().player != null) {
         PacketUtil.send(new ServerboundInteractPacket(entity.getId(), hand, hitVec != null ? hitVec : Vec3.ZERO, secondary));
      }
   }

   public static void sendSwing(InteractionHand hand) {
      try {
         Class<?> cls = Class.forName("net.minecraft.network.protocol.game.ServerboundPunchPacket");
         if (cls.getField("INSTANCE").get(null) instanceof Packet<?> swingPacket) {
            PacketUtil.send(swingPacket);
            return;
         }
      } catch (Throwable var5) {
      }

      try {
         PacketUtil.send(new ServerboundSwingPacket(hand != null ? hand : InteractionHand.MAIN_HAND));
      } catch (Throwable var4) {
      }
   }

   public static InteractionHand useItemHand(ServerboundUseItemPacket packet) {
      if (packet == null) {
         return InteractionHand.MAIN_HAND;
      } else {
         try {
            return packet.getHand();
         } catch (LinkageError var4) {
            try {
               if (ServerboundUseItemPacket.class.getMethod("hand").invoke(packet) instanceof InteractionHand hand) {
                  return hand;
               }
            } catch (Throwable var3) {
            }

            return InteractionHand.MAIN_HAND;
         }
      }
   }

   public static int useItemSequence(ServerboundUseItemPacket packet, int fallback) {
      if (packet == null) {
         return fallback;
      } else {
         try {
            return packet.getSequence();
         } catch (LinkageError var5) {
            try {
               if (ServerboundUseItemPacket.class.getMethod("sequence").invoke(packet) instanceof Integer sequence) {
                  return sequence;
               }
            } catch (Throwable var4) {
            }

            return fallback;
         }
      }
   }

   public static IntList removeEntityIds(Object packet) {
      if (packet == null) {
         return null;
      } else {
         try {
            if (packet instanceof ClientboundRemoveEntitiesPacket remove) {
               return remove.getEntityIds();
            }
         } catch (LinkageError var4) {
         }

         try {
            if (Class.forName("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket").getMethod("entityIds").invoke(packet) instanceof IntList ids
               )
             {
               return ids;
            }
         } catch (Throwable var3) {
         }

         return null;
      }
   }

   public static ClientboundExplodePacket rebuildExplode(ClientboundExplodePacket packet, Vec3 knockback) {
      if (packet != null && knockback != null) {
         Vec3 center;
         float radius;
         int blockCount;
         ParticleOptions particle;
         Holder<SoundEvent> sound;
         WeightedList<ExplosionParticleInfo> blockParticles;
         try {
            center = packet.center();
            radius = packet.radius();
            blockCount = packet.blockCount();
            particle = packet.explosionParticle();
            sound = packet.explosionSound();
            blockParticles = packet.blockParticles();
         } catch (LinkageError var19) {
            return null;
         }

         Optional<Vec3> knock = Optional.of(knockback);

         try {
            for (Constructor<?> ctor : ClientboundExplodePacket.class.getConstructors()) {
               Class<?>[] params = ctor.getParameterTypes();
               if (params.length == 8 && params[7] == boolean.class) {
                  boolean playSound = true;

                  try {
                     if (ClientboundExplodePacket.class.getMethod("playSound").invoke(packet) instanceof Boolean flag) {
                        playSound = flag;
                     }
                  } catch (Throwable var18) {
                  }

                  if (ctor.newInstance(center, radius, blockCount, knock, particle, sound, blockParticles, playSound) instanceof ClientboundExplodePacket result
                     )
                   {
                     return result;
                  }
               }
            }
         } catch (Throwable var20) {
         }

         try {
            return new ClientboundExplodePacket(center, radius, blockCount, knock, particle, sound, blockParticles);
         } catch (Throwable var17) {
            return null;
         }
      } else {
         return null;
      }
   }
}
