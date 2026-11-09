package kat.respect.b2.injector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public final class RespectInjector {
   private static final String[] GAME_HINTS = new String[]{
      "minecraft",
      "prismlauncher",
      "multimc",
      "curseforge",
      "gdlauncher",
      "modrinth",
      "atl",
      "technic",
      "lunarclient",
      "badlion",
      "feather",
      "labymod",
      "fabric",
      "forge",
      "neoforge",
      "optifine"
   };
   static final String CT_MARKER = "net/lenni0451/classtransform/InjectionCallback.class";

   private RespectInjector() {
   }

   public static void main(String[] args) throws Exception {
      if (args.length == 0) {
         if (InjectorGui.isAvailable()) {
            InjectorGui.show();
         } else {
            printUsage();
         }
      } else if (!args[0].equals("--help") && !args[0].equals("-h")) {
         String var1 = args[0];
         switch (var1) {
            case "--gui":
               if (InjectorGui.isAvailable()) {
                  InjectorGui.show();
               } else {
                  System.err.println("no display available — use --list / --attach instead");
                  printUsage();
               }
               break;
            case "--list":
               listJvms();
               break;
            case "--watch":
               String needle = args.length > 1 ? args[1] : "prismlauncher.EntryPoint";
               long delaySec = args.length > 2 ? Long.parseLong(args[2]) : 15L;
               watch(needle, delaySec);
               break;
            case "--attach":
               if (args.length < 2) {
                  System.err.println("missing <pid> (see --list)");
                  printUsage();
                  return;
               }

               attachByPid(args[1], agentArgs(args, 2));
               break;
            case "--attach-name":
               if (args.length < 2) {
                  System.err.println("missing <name-substring>");
                  printUsage();
                  return;
               }

               attachByName(args[1], agentArgs(args, 2));
               break;
            default:
               System.err.println("unknown arg: " + args[0]);
               printUsage();
         }
      } else {
         printUsage();
      }
   }

   private static void printUsage() {
      System.out.println("Kuro+ cross-platform injector");
      System.out.println();
      System.out.println("GUI (no command line needed) — run with no arguments:");
      System.out.println("  java -jar <this-jar>            # pick the game process, press Inject");
      System.out.println("  java -jar <this-jar> --gui      # same, explicitly");
      System.out.println();
      System.out.println("PREFERRED (all OS, full power) — launch the game with the agent:");
      System.out.println("  ./gradlew agentJar");
      System.out.println("  java -javaagent:build/libs/<name>-agent.jar -jar <minecraft-or-launcher>");
      System.out.println("  (Mojang launcher: Settings -> Java -> JVM arguments, append the -javaagent flag)");
      System.out.println();
      System.out.println("DYNAMIC (game already running; retransform limits apply):");
      System.out.println("  java -jar <this-jar> --list");
      System.out.println("  java -jar <this-jar> --attach <pid>");
      System.out.println("  java -jar <this-jar> --attach-name Minecraft");
      System.out.println("  (the mod jar cannot be attached — ClassTransform lives in META-INF/jars;");
      System.out.println("   --attach automatically uses the sibling *-agent.jar instead)");
      System.out.println();
      System.out.println("AUTO (attach to the next game launch, leave it running):");
      System.out.println("  java -jar <this-jar> --watch [name-substring] [delay-seconds]");
      System.out.println();
      System.out.println("Fabric path is unchanged: put the regular mod jar in mods/ and it");
      System.out.println("loads via Fabric loader + kuroplus.mixins.json as before.");
   }

   private static void watch(String needle, long delaySec) throws Exception {
      Set<String> seen = new HashSet<>();
      Map<String, Long> firstSeen = new HashMap<>();

      for (Object vm : virtualMachines()) {
         seen.add(pidOf(vm));
      }

      System.out.println("[Kuro+][watch] watching for '" + needle + "' (delay " + delaySec + "s); ignoring " + seen.size() + " already-running JVM(s)");
      String lower = needle.toLowerCase(Locale.ROOT);

      while (true) {
         long now = System.currentTimeMillis();

         for (Object vm : virtualMachines()) {
            String pid = pidOf(vm);
            String name = displayNameOf(vm);
            if (name != null && name.toLowerCase(Locale.ROOT).contains(lower) && !seen.contains(pid)) {
               firstSeen.putIfAbsent(pid, now);
               if (now - firstSeen.get(pid) >= delaySec * 1000L) {
                  seen.add(pid);
                  System.out.println("[Kuro+][watch] found game pid=" + pid + " (" + name + ") — attaching");

                  try {
                     attachByPid(pid, null);
                  } catch (Throwable var13) {
                     System.err.println("[Kuro+][watch] attach failed for pid=" + pid + ": " + var13);
                  }
               }
            }
         }

         Thread.sleep(3000L);
      }
   }

   private static String agentArgs(String[] args, int from) {
      if (from >= args.length) {
         return null;
      } else {
         StringBuilder sb = new StringBuilder();

         for (int i = from; i < args.length; i++) {
            if (i > from) {
               sb.append(' ');
            }

            sb.append(args[i]);
         }

         return sb.toString();
      }
   }

   static List<RespectInjector.VmInfo> describeJvms() throws Exception {
      String selfPid = String.valueOf(ProcessHandle.current().pid());
      List<RespectInjector.VmInfo> out = new ArrayList<>();

      for (Object vm : virtualMachines()) {
         String pid = pidOf(vm);
         String name = displayNameOf(vm);
         String cmd = commandLineOf(pid);
         boolean self = selfPid.equals(pid);
         out.add(new RespectInjector.VmInfo(pid, name == null ? "<unknown>" : name, cmd, self, looksLikeGame(name, cmd)));
      }

      out.sort(Comparator.comparing(RespectInjector.VmInfo::game).reversed().thenComparing(RespectInjector.VmInfo::displayName, String.CASE_INSENSITIVE_ORDER));
      return out;
   }

   private static String commandLineOf(String pid) {
      try {
         return ProcessHandle.of(Long.parseLong(pid)).flatMap(h -> h.info().commandLine()).orElse(null);
      } catch (Throwable var2) {
         return null;
      }
   }

   private static boolean looksLikeGame(String displayName, String commandLine) {
      String haystack = ((displayName == null ? "" : displayName) + " " + (commandLine == null ? "" : commandLine)).toLowerCase(Locale.ROOT);

      for (String hint : GAME_HINTS) {
         if (haystack.contains(hint)) {
            return true;
         }
      }

      return false;
   }

   private static void listJvms() throws Exception {
      List<RespectInjector.VmInfo> vms = describeJvms();
      if (vms.isEmpty()) {
         System.out.println("no JVMs found (Attach API returned empty list)");
      } else {
         System.out.println("running JVMs (★ = looks like the game, 'this' = the injector itself):");

         for (RespectInjector.VmInfo vm : vms) {
            System.out.println("  pid=" + vm.pid() + (vm.game() ? " ★" : (vm.self() ? " (this)" : "")) + "  " + vm.displayName());
            if (vm.commandLine() != null) {
               System.out.println("      " + vm.commandLine());
            }
         }
      }
   }

   static void attachByPid(String pid, String agentArgs) throws Exception {
      Path agent = resolveAgentJar();
      System.out.println("[Kuro+][inject] attaching to pid=" + pid + " with agent=" + agent);
      Object vm = attach(pid);

      try {
         loadAgent(vm, agent.toString(), withJarArg(agent, agentArgs));
         System.out.println("[Kuro+][inject] agent loaded — check the game log for [Kuro+][CT]");
      } finally {
         detach(vm);
      }
   }

   private static void attachByName(String needle, String agentArgs) throws Exception {
      long selfPid = ProcessHandle.current().pid();
      List<?> vms = virtualMachines();
      Object match = null;

      for (Object vm : vms) {
         String pid = pidOf(vm);
         if (!String.valueOf(selfPid).equals(pid)) {
            String name = displayNameOf(vm);
            if (name != null && name.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT))) {
               match = vm;
               break;
            }
         }
      }

      if (match == null) {
         System.err.println("no JVM with display name containing '" + needle + "' (see --list)");
      } else {
         attachByPid(pidOf(match), agentArgs);
      }
   }

   static Path selfJar() throws Exception {
      CodeSource src = RespectInjector.class.getProtectionDomain().getCodeSource();
      if (src != null && src.getLocation() != null) {
         return Paths.get(src.getLocation().toURI());
      } else {
         throw new IllegalStateException("cannot locate own jar (are you running from classes/? build a jar first)");
      }
   }

   static boolean isAgentJar(Path jar) {
      if (jar != null && Files.isRegularFile(jar)) {
         try {
            boolean var2;
            try (ZipFile zip = new ZipFile(jar.toFile())) {
               var2 = zip.getEntry("net/lenni0451/classtransform/InjectionCallback.class") != null;
            }

            return var2;
         } catch (Throwable var6) {
            return false;
         }
      } else {
         return false;
      }
   }

   static Path resolveAgentJar() throws Exception {
      Path self = selfJar();
      if (isAgentJar(self)) {
         return self;
      } else {
         Path sibling = findSiblingAgentJar(self);
         if (sibling != null) {
            System.out
               .println("[Kuro+][inject] " + self.getFileName() + " has no top-level ClassTransform (mod jar) — using " + sibling.getFileName() + " instead");
            return sibling;
         } else {
            throw new IllegalStateException(
               "no attachable agent jar: "
                  + self
                  + " bundles ClassTransform as Jar-in-Jar and no *-agent.jar was found next to it. Build one with ./gradlew agentJar and keep it beside this jar."
            );
         }
      }
   }

   private static Path findSiblingAgentJar(Path self) {
      try {
         Path dir = self.toAbsolutePath().normalize().getParent();
         if (dir != null && Files.isDirectory(dir)) {
            Path var3;
            try (Stream<Path> stream = Files.list(dir)) {
               var3 = stream.filter(p -> p.getFileName().toString().endsWith("-agent.jar"))
                  .filter(RespectInjector::isAgentJar)
                  .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                  .orElse(null);
            }

            return var3;
         } else {
            return null;
         }
      } catch (Throwable var7) {
         return null;
      }
   }

   static String withJarArg(Path agentJar, String agentArgs) {
      String token = "jar=" + agentJar.toAbsolutePath().normalize();
      if (agentArgs == null || agentArgs.isBlank()) {
         return token;
      } else {
         return agentArgs.contains("jar=") ? agentArgs : token + " " + agentArgs;
      }
   }

   static List<?> virtualMachines() throws Exception {
      try {
         Class<?> vmCls = Class.forName("com.sun.tools.attach.VirtualMachine");
         return (List<?>)vmCls.getMethod("list").invoke(null);
      } catch (ClassNotFoundException var1) {
         throw new IllegalStateException("jdk.attach not available — run with a JDK (17+) that ships the Attach API", var1);
      }
   }

   static String pidOf(Object vmDescriptor) throws Exception {
      return (String)vmDescriptor.getClass().getMethod("id").invoke(vmDescriptor);
   }

   static String displayNameOf(Object vmDescriptor) throws Exception {
      try {
         return (String)vmDescriptor.getClass().getMethod("displayName").invoke(vmDescriptor);
      } catch (Exception var2) {
         return "<unknown>";
      }
   }

   private static Object attach(String pid) throws Exception {
      Class<?> vmCls = Class.forName("com.sun.tools.attach.VirtualMachine");
      return vmCls.getMethod("attach", String.class).invoke(null, pid);
   }

   private static void loadAgent(Object vm, String agentJar, String agentArgs) throws Exception {
      Class<?> vmCls = Class.forName("com.sun.tools.attach.VirtualMachine");
      vmCls.getMethod("loadAgent", String.class, String.class).invoke(vm, agentJar, agentArgs);
   }

   private static void detach(Object vm) {
      try {
         Class<?> vmCls = Class.forName("com.sun.tools.attach.VirtualMachine");
         vmCls.getMethod("detach").invoke(vm);
      } catch (Exception var2) {
         System.err.println("[Kuro+][inject] detach warning: " + var2);
      }
   }

   record VmInfo(String pid, String displayName, String commandLine, boolean self, boolean game) {
   }
}
