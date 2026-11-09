package kat.respect.b2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ChatUtil {
   private static final String PREFIX = "§7[§dRespect§7] §f";

   private ChatUtil() {
   }

   public static void info(String msg) {
      send("§7[§dRespect§7] §f" + msg);
   }

   public static void info(Component body) {
      send(Component.literal("§7[§dRespect§7] §f").append(body));
   }

   public static void warn(String msg) {
      send("§7[§dRespect§7] §f§e" + msg);
   }

   public static void error(String msg) {
      send("§7[§dRespect§7] §f§c" + msg);
   }

   public static void send(String msg) {
      send(Component.literal(msg));
   }

   public static void send(Component component) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         mc.player.sendSystemMessage(component);
      }
   }
}
