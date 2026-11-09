package kat.respect.b2.util;

import java.util.ArrayList;
import java.util.List;

public final class NotificationManager {
   private NotificationManager() {
   }

   public static long getDefaultDurationMs() {
      return kat.respect.b2.notification.NotificationManager.INSTANCE.getDefaultDurationMs();
   }

   public static void push(String title, String message) {
      kat.respect.b2.notification.NotificationManager.INSTANCE.push(title, message);
   }

   public static void push(String title, String message, NotificationManager.Type type) {
      kat.respect.b2.notification.NotificationManager.INSTANCE.push(title, message, toModelType(type));
   }

   public static void push(String title, String message, long durationMs) {
      kat.respect.b2.notification.NotificationManager.INSTANCE.push(title, message, durationMs);
   }

   public static void push(String title, String message, NotificationManager.Type type, long durationMs) {
      kat.respect.b2.notification.NotificationManager.INSTANCE.push(title, message, toModelType(type), durationMs);
   }

   public static List<NotificationManager.Notification> snapshot() {
      List<kat.respect.b2.notification.NotificationManager.Notification> raw = kat.respect.b2.notification.NotificationManager.INSTANCE.snapshot();
      List<NotificationManager.Notification> converted = new ArrayList<>(raw.size());

      for (kat.respect.b2.notification.NotificationManager.Notification n : raw) {
         converted.add(new NotificationManager.Notification(n.title(), n.message(), fromModelType(n.type()), n.createdAt(), n.durationMs()));
      }

      return converted;
   }

   public static void prune() {
      kat.respect.b2.notification.NotificationManager.INSTANCE.prune();
   }

   public static void clear() {
      kat.respect.b2.notification.NotificationManager.INSTANCE.clear();
   }

   private static kat.respect.b2.notification.NotificationManager.Type toModelType(NotificationManager.Type type) {
      if (type == null) {
         return kat.respect.b2.notification.NotificationManager.Type.INFO;
      } else {
         return switch (type) {
            case SUCCESS -> kat.respect.b2.notification.NotificationManager.Type.SUCCESS;
            case WARNING -> kat.respect.b2.notification.NotificationManager.Type.WARNING;
            case ERROR -> kat.respect.b2.notification.NotificationManager.Type.ERROR;
            default -> kat.respect.b2.notification.NotificationManager.Type.INFO;
         };
      }
   }

   private static NotificationManager.Type fromModelType(kat.respect.b2.notification.NotificationManager.Type type) {
      if (type == null) {
         return NotificationManager.Type.INFO;
      } else {
         return switch (type) {
            case SUCCESS -> NotificationManager.Type.SUCCESS;
            case WARNING -> NotificationManager.Type.WARNING;
            case ERROR -> NotificationManager.Type.ERROR;
            default -> NotificationManager.Type.INFO;
         };
      }
   }

   public record Notification(String title, String message, NotificationManager.Type type, long createdAt, long durationMs) {
      public long getAge(long now) {
         return now - this.createdAt;
      }

      public boolean isExpired(long now) {
         return this.getAge(now) >= this.durationMs;
      }
   }

   public static enum Type {
      INFO,
      SUCCESS,
      WARNING,
      ERROR;
   }
}
