package kat.respect.b2.event;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import kat.respect.b2.event.events.Render2DEvent;
import kat.respect.b2.event.events.Render3DEvent;
import kat.respect.b2.util.Log;
import net.minecraft.client.Minecraft;

public final class EventBus {
   public static final EventBus INSTANCE = new EventBus();
   private final Object lock = new Object();
   private final Map<Object, List<EventBus.Listener>> byOwner = new LinkedHashMap<>();
   private final Map<Class<?>, EventBus.Listener[]> resolvedCache = new ConcurrentHashMap<>();
   private final AtomicInteger order = new AtomicInteger();
   private final Lookup lookup = MethodHandles.lookup();
   private volatile BiConsumer<Throwable, EventBus.ListenerInfo> errorHandler;

   public static EventBus createForTesting() {
      return new EventBus();
   }

   public void setErrorHandler(BiConsumer<Throwable, EventBus.ListenerInfo> errorHandler) {
      this.errorHandler = errorHandler;
   }

   public void subscribe(Object owner) {
      if (owner != null) {
         List<EventBus.Listener> found = new ArrayList<>();
         Set<String> seenSignatures = new HashSet<>();

         for (Class<?> current = owner.getClass(); current != null && current != Object.class; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
               EventHandler handler = method.getAnnotation(EventHandler.class);
               if (handler != null && !Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 1) {
                  Class<?> parameterType = method.getParameterTypes()[0];
                  if (Event.class.isAssignableFrom(parameterType)) {
                     String signature = method.getName() + Arrays.toString((Object[])method.getParameterTypes());
                     if (seenSignatures.add(signature)) {
                        EventConsumer consumer = this.compile(owner, current, method, parameterType);
                        if (consumer != null) {
                           EventPriority priority = handler.priority() != null ? handler.priority() : EventPriority.NORMAL;
                           EventBus.Listener listener = new EventBus.Listener(
                              owner, (Class<? extends Event>)parameterType, priority, handler.receiveCancelled(), this.order.getAndIncrement(), consumer
                           );
                           found.add(listener);
                           EventDebug.logRegistration(owner, parameterType, priority);
                        }
                     }
                  }
               }
            }
         }

         if (!found.isEmpty()) {
            synchronized (this.lock) {
               this.byOwner.put(owner, List.copyOf(found));
               this.resolvedCache.clear();
            }
         }
      }
   }

   private EventConsumer compile(Object owner, Class<?> targetClass, Method method, Class<?> parameterType) {
      try {
         method.setAccessible(true);
         Lookup privateLookup = MethodHandles.privateLookupIn(targetClass, this.lookup);
         MethodHandle target = privateLookup.unreflect(method);

         try {
            CallSite site = LambdaMetafactory.metafactory(
               privateLookup,
               "accept",
               MethodType.methodType(EventConsumer.class, targetClass),
               MethodType.methodType(void.class, Event.class),
               target,
               MethodType.methodType(void.class, parameterType)
            );
            return (EventConsumer)site.getTarget().invoke((Object)owner);
         } catch (Throwable var10) {
            MethodHandle adapted = target.asType(MethodType.methodType(void.class, Object.class, Event.class));
            return event -> {
               try {
                  adapted.invokeExact((Object)owner, (Event)event);
               } catch (Throwable var5x) {
                  if (var5x instanceof RuntimeException re) {
                     throw re;
                  } else {
                     throw new RuntimeException("event invoke failed", var5x);
                  }
               }
            };
         }
      } catch (Throwable var11) {
         try {
            method.setAccessible(true);
            return event -> {
               try {
                  method.invoke(owner, event);
               } catch (Throwable var6x) {
                  Throwable cause = var6x instanceof InvocationTargetException ite && ite.getCause() != null ? ite.getCause() : var6x;
                  if (cause instanceof RuntimeException re) {
                     throw re;
                  } else {
                     throw new RuntimeException("event invoke failed", cause);
                  }
               }
            };
         } catch (Throwable var9) {
            EventDebug.error("Could not compile listener " + targetClass.getName() + "#" + method.getName(), var9);
            return null;
         }
      }
   }

   public void unsubscribe(Object owner) {
      if (owner != null) {
         synchronized (this.lock) {
            if (this.byOwner.remove(owner) != null) {
               this.resolvedCache.clear();
            }
         }
      }
   }

   public boolean hasListeners(Class<? extends Event> eventType) {
      return eventType != null && this.resolve(eventType).length > 0;
   }

   public int listenerCount() {
      synchronized (this.lock) {
         return this.byOwner.values().stream().mapToInt(List::size).sum();
      }
   }

   public <T extends Event> T post(T event) {
      if (event == null) {
         return null;
      } else {
         if (event instanceof Render2DEvent || event instanceof Render3DEvent) {
            this.checkRenderThread();
         }

         EventBus.Listener[] listeners = this.resolve(event.getClass());
         if (listeners.length == 0) {
            return event;
         } else {
            EventDebug.logPost(event, listeners.length);
            boolean cancellable = event.isCancellable();

            for (EventBus.Listener listener : listeners) {
               if (!cancellable || !event.isCancelled() || listener.receiveCancelled()) {
                  try {
                     listener.consumer().accept(event);
                  } catch (Throwable var12) {
                     Throwable t = var12;
                     EventDebug.error(
                        "Listener failed: owner="
                           + listener.owner().getClass().getName()
                           + " event="
                           + event.getClass().getName()
                           + " priority="
                           + listener.priority(),
                        var12
                     );
                     BiConsumer<Throwable, EventBus.ListenerInfo> handler = this.errorHandler;
                     if (handler != null) {
                        try {
                           handler.accept(
                              t, new EventBus.ListenerInfo(listener.owner(), listener.eventType(), listener.priority(), listener.receiveCancelled())
                           );
                        } catch (Throwable var11) {
                        }
                     }
                  }
               }
            }

            return event;
         }
      }
   }

   private void checkRenderThread() {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && !mc.isSameThread()) {
            Log.warn("events", "Render event posted from non-render thread: " + Thread.currentThread().getName());
         }
      } catch (Throwable var2) {
      }
   }

   private EventBus.Listener[] resolve(Class<?> postedType) {
      EventBus.Listener[] cached = this.resolvedCache.get(postedType);
      if (cached != null) {
         return cached;
      } else {
         synchronized (this.lock) {
            cached = this.resolvedCache.get(postedType);
            if (cached != null) {
               return cached;
            } else {
               List<EventBus.Listener> matched = new ArrayList<>();

               for (List<EventBus.Listener> listeners : this.byOwner.values()) {
                  for (EventBus.Listener listener : listeners) {
                     if (listener.eventType().isAssignableFrom(postedType)) {
                        matched.add(listener);
                     }
                  }
               }

               matched.sort(Comparator.<EventBus.Listener>comparingInt(l -> l.priority().ordinal()).thenComparingInt(EventBus.Listener::order));
               cached = matched.toArray(EventBus.Listener[]::new);
               this.resolvedCache.put(postedType, cached);
               return cached;
            }
         }
      }
   }

   public void clear() {
      synchronized (this.lock) {
         this.byOwner.clear();
         this.resolvedCache.clear();
      }
   }

   public void dumpListeners() {
      synchronized (this.lock) {
         Log.info("events", "=== Registered Listeners (" + this.listenerCount() + " total) ===");

         for (Entry<Object, List<EventBus.Listener>> entry : this.byOwner.entrySet()) {
            Log.info("events", " Owner: " + entry.getKey().getClass().getName());

            for (EventBus.Listener l : entry.getValue()) {
               Log.info(
                  "events",
                  "   -> "
                     + l.eventType().getSimpleName()
                     + " [priority="
                     + l.priority()
                     + ", receiveCancelled="
                     + l.receiveCancelled()
                     + ", order="
                     + l.order()
                     + "]"
               );
            }
         }

         Log.info("events", "=== Cached Resolved Dispatch (" + this.resolvedCache.size() + " types) ===");

         for (Entry<Class<?>, EventBus.Listener[]> entry : this.resolvedCache.entrySet()) {
            Log.info("events", " Event: " + entry.getKey().getSimpleName() + " -> " + entry.getValue().length + " listeners");
         }
      }
   }

   public record Listener(Object owner, Class<? extends Event> eventType, EventPriority priority, boolean receiveCancelled, int order, EventConsumer consumer) {
   }

   public record ListenerInfo(Object owner, Class<? extends Event> eventType, EventPriority priority, boolean receiveCancelled) {
   }
}
