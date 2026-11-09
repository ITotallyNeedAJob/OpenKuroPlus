package kat.respect.b2.injector;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.List;
import java.util.jar.JarFile;
import kat.respect.b2.ct.AgentRuntime;
import kat.respect.b2.ct.RespectCTBootstrap;
import kat.respect.b2.debug.ClientLogBuffer;

public final class RespectAgent {
   private static volatile boolean injected = false;

   private RespectAgent() {
   }

   public static void premain(String agentArgs, Instrumentation inst) {
      ClientLogBuffer.install();
      System.out.println("[Kuro+][agent] premain(args=" + agentArgs + ") — launch-time injection");
      Path agentJar = resolveAgentJar(agentArgs);
      if (!hasBundledClassTransform(agentJar)) {
         System.err.println("[Kuro+][agent] refusing to inject: " + agentJar + " has no top-level ClassTransform. Use the fat agent jar (./gradlew agentJar).");
      } else if (AgentRuntime.isRespectModLoaded()) {
         System.out.println("[Kuro+][agent] Fabric mod detected — native Mixin path active, agent steps aside");
      } else {
         RespectCTBootstrap.bootstrap(inst, false);
         AgentRuntime.install(inst, agentJar);
      }
   }

   public static void agentmain(String agentArgs, Instrumentation inst) {
      ClientLogBuffer.install();
      System.out.println("[Kuro+][agent] agentmain(args=" + agentArgs + ") — dynamic attach (hotswap=true)");
      if (injected) {
         System.out.println("[Kuro+][agent] Already injected into this JVM instance, skipping duplicate attach.");
      } else {
         Path agentJar = resolveAgentJar(agentArgs);
         if (!hasBundledClassTransform(agentJar)) {
            System.err
               .println(
                  "[Kuro+][agent] refusing to inject: "
                     + agentJar
                     + " has no top-level ClassTransform (the plain Fabric mod jar keeps it in META-INF/jars, which only Fabric's own loader unpacks). Attach the *-agent.jar build instead — the native injector picks it automatically when it sits beside the mod jar."
               );
         } else {
            appendJarToSystemSearch(inst, agentJar);
            if (AgentRuntime.isRespectModLoaded()) {
               System.out.println("[Kuro+][agent] Fabric mod detected — native Mixin path active, agent steps aside");
            } else {
               if (agentJar != null) {
                  AgentRuntime.mountForRunningGame(inst, agentJar);
               }

               if (!AgentRuntime.classTransformVisibleToGame()) {
                  System.err
                     .println(
                        "[Kuro+][agent] aborting injection: ClassTransform is not resolvable from the game classloader, so every transformed class would crash it later. Restart the game and inject again."
                     );
               } else {
                  RespectCTBootstrap.bootstrap(inst, true);
                  AgentRuntime.install(inst, agentJar);
                  injected = true;
                  System.out.println("[Kuro+][agent] Dynamic injection pipeline complete. Press [Right Shift] in game to open ClickGUI!");
               }
            }
         }
      }
   }

   private static Path resolveAgentJar(String agentArgs) {
      if (agentArgs != null) {
         for (String token : agentArgs.split("\\s+")) {
            if (token.startsWith("jar=")) {
               try {
                  return Paths.get(token.substring("jar=".length()));
               } catch (Throwable var6) {
               }
            }
         }
      }

      return selfJar();
   }

   private static boolean hasBundledClassTransform(Path jar) {
      if (jar != null && Files.isRegularFile(jar)) {
         try {
            boolean var2;
            try (JarFile jf = new JarFile(jar.toFile())) {
               var2 = jf.getEntry("net/lenni0451/classtransform/InjectionCallback.class") != null;
            }

            return var2;
         } catch (Throwable var6) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void appendJarToSystemSearch(Instrumentation inst, Path agentJar) {
      try {
         if (agentJar != null && Files.isRegularFile(agentJar)) {
            inst.appendToSystemClassLoaderSearch(new JarFile(agentJar.toFile()));
            System.out.println("[Kuro+][agent] Agent jar appended to system classpath search.");
         }
      } catch (Throwable var3) {
         System.err.println("[Kuro+][agent] Could not append agent jar to system search: " + var3);
      }
   }

   public static void flushAgentLogToGame() {
      try {
         ClassLoader gl = AgentRuntime.gameLoader();
         if (gl == null) {
            return;
         }

         List<String> backlog = ClientLogBuffer.snapshot();
         if (backlog.isEmpty()) {
            return;
         }

         Class<?> gameCopy = Class.forName("kat.respect.b2.debug.ClientLogBuffer", true, gl);
         gameCopy.getMethod("absorb", List.class).invoke(null, backlog);
         System.out.println("[Kuro+] Agent log bridged (" + backlog.size() + " lines)");
      } catch (Throwable var3) {
         System.err.println("[Kuro+] Log bridge failed: " + var3);
      }
   }

   static Path selfJar() {
      try {
         CodeSource src = RespectAgent.class.getProtectionDomain().getCodeSource();
         if (src != null && src.getLocation() != null) {
            URL location = src.getLocation();
            if ("file".equals(location.getProtocol())) {
               return Paths.get(location.toURI());
            }
         }
      } catch (Throwable var2) {
      }

      return null;
   }
}
