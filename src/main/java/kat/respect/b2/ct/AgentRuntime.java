package kat.respect.b2.ct;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import kat.respect.b2.injector.RespectAgent;

public final class AgentRuntime {
   private static volatile boolean clientInitDone = false;
   private static volatile ClassLoader gameLoader;
   private static final Set<String> mountedJars = ConcurrentHashMap.newKeySet();

   private AgentRuntime() {
   }

   public static ClassLoader gameLoader() {
      return gameLoader;
   }

   public static boolean mountForRunningGame(Instrumentation inst, Path agentJar) {
      if (agentJar != null && mountedJars.contains(jarKey(agentJar))) {
         return true;
      } else {
         for (Class<?> loaded : inst.getAllLoadedClasses()) {
            if ("net.minecraft.client.Minecraft".equals(loaded.getName())) {
               gameLoader = loaded.getClassLoader();
               return mountAgentJar(gameLoader, agentJar);
            }
         }

         return false;
      }
   }

   public static boolean classTransformVisibleToGame() {
      ClassLoader loader = gameLoader;
      if (loader == null) {
         return true;
      } else {
         try {
            Class.forName("net.lenni0451.classtransform.InjectionCallback", false, loader);
            return true;
         } catch (Throwable var2) {
            System.err.println("[Kuro+][agent] ClassTransform not visible to game loader " + loader + ": " + var2);
            return false;
         }
      }
   }

   public static void earlyClientInit() {
      clientInit("early");
   }

   public static boolean isRespectModLoaded() {
      try {
         Class<?> fl = Class.forName("net.fabricmc.loader.api.FabricLoader");
         Object loader = fl.getMethod("getInstance").invoke(null);
         Object result = fl.getMethod("isModLoaded", String.class).invoke(loader, "kuroplus");
         return Boolean.TRUE.equals(result);
      } catch (Throwable var3) {
         return false;
      }
   }

   public static void install(Instrumentation inst, Path agentJar) {
      Thread t = new Thread(() -> run(inst, agentJar), "KuroPlus-Agent-Init");
      t.setDaemon(true);
      t.start();
   }

   private static void run(Instrumentation inst, Path agentJar) {
      Class<?> mcClass = awaitMinecraftClass(inst);
      if (mcClass == null) {
         System.out.println("[Kuro+][agent] Minecraft class never appeared — agent init skipped");
      } else {
         gameLoader = mcClass.getClassLoader();
         mountAgentJar(gameLoader, agentJar);
         Object mc = awaitMinecraftInstance(mcClass);
         if (mc == null) {
            System.out.println("[Kuro+][agent] Minecraft instance never appeared — agent init skipped");
         } else {
            System.out.println("[Kuro+][agent] game detected (loader=" + gameLoader + ") — initializing client");
            RespectAgent.flushAgentLogToGame();
            initializeClient(mcClass, mc, gameLoader);
         }
      }
   }

   private static Class<?> awaitMinecraftClass(Instrumentation inst) {
      for (int i = 0; i < 2400; i++) {
         for (Class<?> loaded : inst.getAllLoadedClasses()) {
            if ("net.minecraft.client.Minecraft".equals(loaded.getName())) {
               return loaded;
            }
         }

         sleep(500L);
      }

      return null;
   }

   private static Object awaitMinecraftInstance(Class<?> mcClass) {
      try {
         Method getInstance = mcClass.getMethod("getInstance");

         for (int i = 0; i < 2400; i++) {
            Object mc = getInstance.invoke(null);
            if (mc != null) {
               return mc;
            }

            sleep(500L);
         }
      } catch (Throwable var4) {
         System.err.println("[Kuro+][agent] getInstance failed: " + var4);
      }

      return null;
   }

   static boolean mountAgentJar(ClassLoader loader, Path agentJar) {
      if (loader != null && agentJar != null) {
         String key = jarKey(agentJar);
         if (mountedJars.contains(key)) {
            return true;
         } else if (loader == ClassLoader.getSystemClassLoader()) {
            mountedJars.add(key);
            return true;
         } else {
            try {
               URL url = agentJar.toUri().toURL();

               try {
                  Method addUrlFwd = loader.getClass().getMethod("addUrlFwd", URL.class);
                  addUrlFwd.setAccessible(true);
                  addUrlFwd.invoke(loader, url);
                  mountedJars.add(key);
                  System.out.println("[Kuro+][agent] agent jar mounted in Fabric Knot loader: " + key);
                  return true;
               } catch (NoSuchMethodException var6) {
                  if (loader instanceof URLClassLoader ucl) {
                     Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                     addUrl.setAccessible(true);
                     addUrl.invoke(ucl, url);
                     mountedJars.add(key);
                     System.out.println("[Kuro+][agent] agent jar mounted in URLClassLoader " + loader);
                     return true;
                  } else {
                     System.out
                        .println(
                           "[Kuro+][agent] unsupported game loader "
                              + loader.getClass().getName()
                              + " — injected code may fail to resolve (use -javaagent or mods folder)"
                        );
                     return false;
                  }
               }
            } catch (Throwable var7) {
               System.err.println("[Kuro+][agent] could not mount agent jar in " + loader + ": " + var7);
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private static String jarKey(Path jar) {
      return jar.toAbsolutePath().normalize().toString();
   }

   private static void initializeClient(Class<?> mcClass, Object mc, ClassLoader loader) {
      Runnable init = () -> clientInit("late");

      try {
         Method execute = mcClass.getMethod("execute", Runnable.class);
         execute.invoke(mc, init);
      } catch (Throwable var5) {
         init.run();
      }
   }

   private static synchronized void clientInit(String phase) {
      if (!clientInitDone) {
         try {
            ClassLoader loader = gameLoader;
            if (loader == null) {
               loader = Thread.currentThread().getContextClassLoader();
            }

            if (loader == null) {
               loader = ClassLoader.getSystemClassLoader();
            }

            Class<?> modClass = Class.forName("kat.respect.b2.KuroPlusMod", true, loader);
            Object mod = modClass.getDeclaredConstructor().newInstance();
            modClass.getMethod("onInitializeClient").invoke(mod);
            clientInitDone = true;
            System.out.println("[Kuro+][agent] client initialized through agent (" + phase + ", no mods folder needed)");
         } catch (Throwable var4) {
            System.err.println("[Kuro+][agent] client init (" + phase + ") failed: " + var4);
            var4.printStackTrace();
         }
      }
   }

   private static void sleep(long ms) {
      try {
         Thread.sleep(ms);
      } catch (InterruptedException var3) {
         Thread.currentThread().interrupt();
      }
   }
}
