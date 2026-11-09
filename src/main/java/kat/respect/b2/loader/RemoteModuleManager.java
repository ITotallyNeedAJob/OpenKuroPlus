package kat.respect.b2.loader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import kat.respect.b2.util.Log;

public final class RemoteModuleManager {
   public static final RemoteModuleManager INSTANCE = new RemoteModuleManager();
   private static final Gson GSON = new Gson();
   private static final String BUNDLED_PATH = "assets/kuroplus/modules/";
   private final List<Module> loaded = new ArrayList<>();
   private ModuleClassLoader loader;

   private RemoteModuleManager() {
   }

   public List<Module> getLoaded() {
      return this.loaded;
   }

   public void loadBundled() throws Exception {
      ClassLoader cl = RemoteModuleManager.class.getClassLoader();
      String basePath = null;

      for (String candidate : List.of("assets/kuroplus/modules/")) {
         if (cl.getResource(candidate + "manifest.json") != null) {
            basePath = candidate;
            break;
         }
      }

      if (basePath == null) {
         throw new IllegalStateException("bundled modules missing");
      } else {
         try (InputStream in = cl.getResourceAsStream(basePath + "manifest.json")) {
            if (in == null) {
               throw new IllegalStateException("bundled modules manifest stream missing");
            }

            JsonArray modules = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonArray();
            this.loader = new ModuleClassLoader(cl);
            int jarSuccess = 0;
            int jarFailed = 0;

            for (JsonElement el : modules) {
               JsonObject mod = el.getAsJsonObject();
               if (mod.has("jar")) {
                  String jarFile = mod.get("jar").getAsString();

                  try (InputStream jarIn = cl.getResourceAsStream(basePath + jarFile)) {
                     if (jarIn != null) {
                        this.defineJar(jarIn.readAllBytes());
                        jarSuccess++;
                     } else {
                        Log.error("loader", "Bundled module jar missing: " + jarFile);
                        jarFailed++;
                     }
                  } catch (Throwable var17) {
                     Log.error("loader", "Failed to define jar " + jarFile, var17);
                     jarFailed++;
                  }
               }
            }

            int loadedBefore = this.loaded.size();

            for (JsonElement elx : modules) {
               this.instantiate(elx.getAsJsonObject());
            }

            int loadedNow = this.loaded.size() - loadedBefore;
            Log.info("loader", "Bundled modules loaded: " + loadedNow + " (" + jarSuccess + " jars defined, " + jarFailed + " failed)");
         }
      }
   }

   public void loadDev() throws Exception {
      Path dir = this.resolveDevModulesDir();
      Path manifestFile = dir.resolve("manifest.json");
      if (!Files.exists(manifestFile)) {
         throw new IllegalStateException("dev modules missing at " + dir);
      } else {
         JsonArray modules = JsonParser.parseString(Files.readString(manifestFile)).getAsJsonArray();
         this.loader = new ModuleClassLoader(RemoteModuleManager.class.getClassLoader());
         int jarSuccess = 0;
         int jarFailed = 0;

         for (JsonElement el : modules) {
            JsonObject mod = el.getAsJsonObject();
            if (mod.has("jar")) {
               String jarName = mod.get("jar").getAsString();
               Path jar = dir.resolve(jarName);
               if (!Files.exists(jar)) {
                  Log.error("loader", "Dev module jar missing: " + jar);
                  jarFailed++;
               } else {
                  try (InputStream in = Files.newInputStream(jar)) {
                     this.defineJar(in.readAllBytes());
                     jarSuccess++;
                  } catch (Throwable var16) {
                     Log.error("loader", "Failed to define dev jar " + jarName, var16);
                     jarFailed++;
                  }
               }
            }
         }

         int loadedBefore = this.loaded.size();

         for (JsonElement elx : modules) {
            JsonObject mod = elx.getAsJsonObject();
            this.instantiate(mod);
         }

         int loadedNow = this.loaded.size() - loadedBefore;
         Log.info("loader", "Dev modules loaded: " + loadedNow + " (" + jarSuccess + " jars defined, " + jarFailed + " failed)");
      }
   }

   private void defineJar(byte[] jar) throws Exception {
      Map<String, byte[]> pending = new LinkedHashMap<>();

      try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(jar))) {
         byte[] buf = new byte[8192];

         ZipEntry entry;
         while ((entry = zis.getNextEntry()) != null) {
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
               String className = entry.getName().replace('/', '.').replace('\\', '.');
               className = className.substring(0, className.length() - 6);
               ByteArrayOutputStream baos = new ByteArrayOutputStream();

               int r;
               while ((r = zis.read(buf)) != -1) {
                  baos.write(buf, 0, r);
               }

               pending.put(className, baos.toByteArray());
            }
         }
      }

      int var13 = pending.size();

      for (int var14 = 0; var14 < 8 && !pending.isEmpty(); var14++) {
         List<String> toRemove = new ArrayList<>();

         for (Entry<String, byte[]> e : pending.entrySet()) {
            try {
               this.loader.defineClassDirect(e.getKey(), e.getValue());
               toRemove.add(e.getKey());
            } catch (NoClassDefFoundError var11) {
            }
         }

         for (String key : toRemove) {
            pending.remove(key);
         }

         if (pending.size() == var13 && var14 > 0) {
            for (Entry<String, byte[]> e : pending.entrySet()) {
               try {
                  this.loader.defineClassDirect(e.getKey(), e.getValue());
               } catch (Throwable var10) {
               }
            }

            pending.clear();
            break;
         }

         var13 = pending.size();
      }
   }

   private void instantiate(JsonObject mod) {
      if (!mod.has("mainClass")) {
         Log.error("loader", "Manifest entry missing 'mainClass'");
      } else {
         String id = mod.has("id") ? mod.get("id").getAsString() : null;
         String mainClass = mod.get("mainClass").getAsString();

         try {
            Class<?> clazz = Class.forName(mainClass, true, this.loader);
            Module instance = (Module)clazz.getDeclaredConstructor().newInstance();
            if (id == null || id.isBlank()) {
               Log.warn("loader", "Module manifest missing id for " + mainClass + "; using class name");
            }

            ModuleManager.INSTANCE.register(instance, id);
            this.loaded.add(instance);
         } catch (Throwable var6) {
            Throwable cause = var6 instanceof InvocationTargetException && var6.getCause() != null ? var6.getCause() : var6;
            Log.error("loader", "Failed to load module " + (id != null ? id : mainClass) + " (" + mainClass + ")", cause);
         }
      }
   }

   private Path resolveDevModulesDir() {
      List<Path> candidates = new ArrayList<>();

      try {
         Path classes = Path.of(RemoteModuleManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
         candidates.add(classes.getParent().getParent().getParent().resolve("modules/build/libs"));
         candidates.add(classes.getParent().getParent().resolve("modules/build/libs"));
      } catch (Exception var6) {
      }

      candidates.add(Path.of("modules", "build", "libs"));
      candidates.add(Path.of("..", "modules", "build", "libs"));

      for (Path c : candidates) {
         try {
            if (Files.exists(c.resolve("manifest.json"))) {
               return c.toAbsolutePath().normalize();
            }
         } catch (Exception var5) {
         }
      }

      return candidates.get(0).toAbsolutePath().normalize();
   }

   public void lockAll() {
      for (Module mod : this.loaded) {
         if (mod.isEnabled()) {
            mod.setEnabled(false);
         }
      }
   }

   public void reset() {
      ModuleManager.INSTANCE.clear();
      this.loaded.clear();
      if (this.loader != null) {
         this.loader.wipeAndClean();
         this.loader = null;
      }
   }
}
