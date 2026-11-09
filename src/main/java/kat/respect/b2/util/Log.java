package kat.respect.b2.util;

public final class Log {
   private Log() {
   }

   public static void info(String message) {
      System.out.println("[Kuro+] " + message);
   }

   public static void info(String tag, String message) {
      System.out.println("[Kuro+][" + tag + "] " + message);
   }

   public static void warn(String message) {
      System.out.println("[Kuro+] WARN: " + message);
   }

   public static void warn(String tag, String message) {
      System.out.println("[Kuro+][" + tag + "] WARN: " + message);
   }

   public static void error(String message) {
      System.err.println("[Kuro+] ERROR: " + message);
   }

   public static void error(String message, Throwable t) {
      System.err.println("[Kuro+] ERROR: " + message);
      if (t != null) {
         t.printStackTrace(System.err);
      }
   }

   public static void error(String tag, String message) {
      System.err.println("[Kuro+][" + tag + "] ERROR: " + message);
   }

   public static void error(String tag, String message, Throwable t) {
      System.err.println("[Kuro+][" + tag + "] ERROR: " + message);
      if (t != null) {
         t.printStackTrace(System.err);
      }
   }

   public static void debug(String message) {
      if (Boolean.getBoolean("kuroplus.debug")) {
         System.out.println("[Kuro+][debug] " + message);
      }
   }

   public static void debug(String tag, String message) {
      if (Boolean.getBoolean("kuroplus.debug") || Boolean.getBoolean("kuroplus.debug." + tag)) {
         System.out.println("[Kuro+][" + tag + "][debug] " + message);
      }
   }
}
