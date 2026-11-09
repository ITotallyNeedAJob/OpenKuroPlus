package kat.respect.b2.util;

@Deprecated
public final class ChatUtils {
   private ChatUtils() {
   }

   public static void info(String message) {
      ChatUtil.info(message);
   }

   public static void warn(String message) {
      ChatUtil.warn(message);
   }

   public static void error(String message) {
      ChatUtil.error(message);
   }

   public static void send(String message) {
      ChatUtil.send(message);
   }
}
