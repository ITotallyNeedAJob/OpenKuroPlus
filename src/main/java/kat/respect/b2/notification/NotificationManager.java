package kat.respect.b2.notification;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import kat.respect.b2.bridge.NotificationSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class NotificationManager {
   public static final NotificationManager INSTANCE = new NotificationManager();
   private static final int MAX_QUEUE = 32;
   private final Deque<NotificationManager.Notification> queue = new ArrayDeque<>();

   private NotificationManager() {
   }

   public long getDefaultDurationMs() {
      try {
         return (long)(NotificationSettings.duration.getValue() * 1000.0);
      } catch (Throwable var2) {
         return 3500L;
      }
   }

   public void push(String title, String message) {
      this.push(title, message, NotificationManager.Type.INFO, this.getDefaultDurationMs());
   }

   public void push(String title, String message, NotificationManager.Type type) {
      this.push(title, message, type, this.getDefaultDurationMs());
   }

   public void push(String title, String message, long durationMs) {
      this.push(title, message, NotificationManager.Type.INFO, durationMs);
   }

   public synchronized void push(String title, String message, NotificationManager.Type type, long durationMs) {
      this.prune();
      this.queue.addLast(new NotificationManager.Notification(title, message, type, System.currentTimeMillis(), durationMs));

      try {
         if (NotificationSettings.sound.getValue()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getSoundManager() != null) {
               mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.6F, 0.4F));
            }
         }
      } catch (Throwable var7) {
      }
   }

   public void info(String title, String message) {
      this.push(title, message, NotificationManager.Type.INFO);
   }

   public void success(String title, String message) {
      this.push(title, message, NotificationManager.Type.SUCCESS);
   }

   public void warning(String title, String message) {
      this.push(title, message, NotificationManager.Type.WARNING);
   }

   public void error(String title, String message) {
      this.push(title, message, NotificationManager.Type.ERROR);
   }

   public synchronized List<NotificationManager.Notification> snapshot() {
      this.prune();
      return new ArrayList<>(this.queue);
   }

   public synchronized void prune() {
      long now = System.currentTimeMillis();
      this.queue.removeIf(n -> n.isExpired(now));
      int max = 8;

      try {
         max = NotificationSettings.maxDisplay.getValueInt();
      } catch (Throwable var5) {
      }

      int cap = Math.min(32, Math.max(1, max));

      while (this.queue.size() > cap) {
         this.queue.removeFirst();
      }
   }

   public synchronized void clear() {
      this.queue.clear();
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
