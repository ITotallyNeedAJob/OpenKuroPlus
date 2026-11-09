package kat.respect.b2.event;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import kat.respect.b2.util.Log;

public final class EventDebug {
   private static final Set<Class<?>> WARNED_NON_CANCELLABLE = ConcurrentHashMap.newKeySet();

   private EventDebug() {
   }

   public static boolean isDebugEnabled() {
      return Boolean.getBoolean("kuroplus.debug.events") || Boolean.getBoolean("kuroplus.debug");
   }

   public static void warnNonCancellable(Event event) {
      if (event != null) {
         if (WARNED_NON_CANCELLABLE.add(event.getClass())) {
            Log.warn("events", "Attempted to cancel non-cancellable event: " + event.getClass().getName());
         }
      }
   }

   public static void logRegistration(Object owner, Class<?> eventType, EventPriority priority) {
      if (isDebugEnabled()) {
         Log.debug("events", "Registered listener: " + owner.getClass().getSimpleName() + " for " + eventType.getSimpleName() + " (priority=" + priority + ")");
      }
   }

   public static void logPost(Event event, int listenerCount) {
      if (isDebugEnabled()) {
         Log.debug("events", "Posted " + event.getClass().getSimpleName() + " to " + listenerCount + " listeners");
      }
   }

   public static void error(String message, Throwable t) {
      Log.error("events", message, t);
   }
}
