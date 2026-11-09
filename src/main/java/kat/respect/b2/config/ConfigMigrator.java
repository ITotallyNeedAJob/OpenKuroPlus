package kat.respect.b2.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map.Entry;
import kat.respect.b2.module.Category;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.setting.Setting;

public final class ConfigMigrator {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final ConfigPaths paths;

   public ConfigMigrator(ConfigPaths paths) {
      this.paths = paths;
   }

   public void migrateLegacyIfNeeded() {
      if (this.paths != null) {
         Path marker = this.paths.migrationMarker();
         if (!Files.exists(marker)) {
            Path legacy = this.paths.legacyRoot();
            if (!Files.isDirectory(legacy)) {
               this.writeMarker(0, 0, "No legacy directory found");
            } else {
               int profilesMigrated = 0;
               int friendsMigrated = 0;
               int failures = 0;

               try {
                  String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replaceAll("[:.]", "-");
                  Path backupDir = this.paths.migrationBackup().resolve(timestamp);
                  copyDirectoryTree(legacy, backupDir);
                  Path configsDir = legacy.resolve("configs");
                  JsonObject guiModulesFromProfiles = new JsonObject();
                  if (Files.isDirectory(configsDir)) {
                     try (DirectoryStream<Path> stream = Files.newDirectoryStream(configsDir, "*.json")) {
                        for (Path file : stream) {
                           String fileName = file.getFileName().toString();
                           if (!"hud.json".equalsIgnoreCase(fileName)) {
                              try {
                                 String content = Files.readString(file, StandardCharsets.UTF_8);
                                 JsonElement parsed = JsonParser.parseString(content);
                                 if (parsed.isJsonObject()) {
                                    JsonObject oldRoot = parsed.getAsJsonObject();
                                    String profileName = fileName.replace(".json", "");
                                    JsonObject migrated = this.migrateProfile(oldRoot, profileName, guiModulesFromProfiles);
                                    atomicWrite(this.paths.profile(profileName), GSON.toJson(migrated));
                                    profilesMigrated++;
                                 }
                              } catch (Throwable var23) {
                                 failures++;
                                 System.err.println("[Kuro+] Failed to migrate profile " + fileName + ": " + var23);
                              }
                           }
                        }
                     }
                  }

                  Path guiFile = legacy.resolve("gui.json");
                  JsonObject uiObj = new JsonObject();
                  JsonObject guiModulesObj = new JsonObject();
                  if (Files.exists(guiFile)) {
                     try {
                        String guiContent = Files.readString(guiFile, StandardCharsets.UTF_8);
                        JsonElement parsed = JsonParser.parseString(guiContent);
                        if (parsed.isJsonObject()) {
                           JsonObject guiRoot = parsed.getAsJsonObject();
                           if (guiRoot.has("panels")) {
                              uiObj.add("panels", guiRoot.get("panels"));
                           }

                           if (guiRoot.has("configPanel")) {
                              uiObj.add("config_panel", guiRoot.get("configPanel"));
                           }

                           if (guiRoot.has("panelMode")) {
                              uiObj.add("panel_mode", guiRoot.get("panelMode"));
                           }

                           if (guiRoot.has("friends") && guiRoot.get("friends").isJsonArray()) {
                              JsonArray friendsArr = guiRoot.getAsJsonArray("friends");
                              JsonObject friendsRoot = new JsonObject();
                              friendsRoot.addProperty("format_version", 4);
                              friendsRoot.addProperty("updated_at", Instant.now().toString());
                              friendsRoot.add("friends", friendsArr);
                              atomicWrite(this.paths.friends(), GSON.toJson(friendsRoot));
                              friendsMigrated = friendsArr.size();
                           }

                           for (Entry<String, JsonElement> entry : guiRoot.entrySet()) {
                              String key = entry.getKey();
                              if (!"panels".equals(key)
                                 && !"configPanel".equals(key)
                                 && !"panelMode".equals(key)
                                 && !"friends".equals(key)
                                 && entry.getValue().isJsonObject()) {
                                 Module m = ConfigSerializer.findModule(key);
                                 String id = m != null ? ConfigSerializer.getModuleId(m) : key;
                                 guiModulesObj.add(id, this.convertModuleToV4(m, entry.getValue().getAsJsonObject()));
                              }
                           }
                        }
                     } catch (Throwable var24) {
                        failures++;
                        System.err.println("[Kuro+] Failed to migrate gui.json: " + var24);
                     }
                  }

                  for (Entry<String, JsonElement> entryx : guiModulesFromProfiles.entrySet()) {
                     if (!guiModulesObj.has(entryx.getKey())) {
                        guiModulesObj.add(entryx.getKey(), entryx.getValue());
                     }
                  }

                  JsonObject clientRoot = new JsonObject();
                  clientRoot.addProperty("format_version", 4);
                  clientRoot.addProperty("client", "Kuro+");
                  clientRoot.addProperty("client_version", "1.0.0");
                  clientRoot.addProperty("active_profile", "default");
                  if (!uiObj.has("theme")) {
                     uiObj.addProperty("theme", "Larp");
                  }

                  if (!uiObj.has("render_engine")) {
                     uiObj.addProperty("render_engine", "Mobile (Shaders)");
                  }

                  if (!uiObj.has("background")) {
                     uiObj.addProperty("background", "Liquid Glass");
                  }

                  if (!uiObj.has("scale")) {
                     uiObj.addProperty("scale", 1.0);
                  }

                  clientRoot.add("ui", uiObj);
                  clientRoot.add("gui_modules", guiModulesObj);
                  atomicWrite(this.paths.client(), GSON.toJson(clientRoot));
                  Path legacyHud = legacy.resolve("configs").resolve("hud.json");
                  if (Files.exists(legacyHud)) {
                     try {
                        String hudContent = Files.readString(legacyHud, StandardCharsets.UTF_8);
                        JsonElement parsed = JsonParser.parseString(hudContent);
                        if (parsed.isJsonObject()) {
                           JsonObject hudRoot = new JsonObject();
                           hudRoot.addProperty("format_version", 4);
                           hudRoot.addProperty("updated_at", Instant.now().toString());
                           hudRoot.add("elements", parsed.getAsJsonObject());
                           atomicWrite(this.paths.hud(), GSON.toJson(hudRoot));
                        }
                     } catch (Throwable var22) {
                        failures++;
                        System.err.println("[Kuro+] Failed to migrate hud.json: " + var22);
                     }
                  }

                  this.writeMarker(profilesMigrated, friendsMigrated, failures == 0 ? "SUCCESS" : "PARTIAL_FAILURES: " + failures);
                  System.out.println("[Kuro+] Legacy migration complete: " + profilesMigrated + " profiles, " + friendsMigrated + " friends migrated.");
               } catch (Throwable var26) {
                  System.err.println("[Kuro+] Migration failed: " + var26);
                  this.writeMarker(profilesMigrated, friendsMigrated, "FAILED: " + var26.getMessage());
               }
            }
         }
      }
   }

   private JsonObject migrateProfile(JsonObject oldRoot, String profileName, JsonObject guiModulesCollector) {
      JsonObject v4 = new JsonObject();
      v4.addProperty("format_version", 4);
      v4.addProperty("client", "Kuro+");
      v4.addProperty("client_version", "1.0.0");
      v4.addProperty("minecraft_version", "26.1.2");
      v4.addProperty("profile", profileName);
      v4.addProperty("updated_at", Instant.now().toString());
      JsonObject v4Modules = new JsonObject();
      JsonObject unknownModules = new JsonObject();
      JsonElement modulesEl = (JsonElement)(oldRoot.has("modules") ? oldRoot.get("modules") : oldRoot);
      if (modulesEl.isJsonObject()) {
         JsonObject modulesObj = modulesEl.getAsJsonObject();

         for (Entry<String, JsonElement> entry : modulesObj.entrySet()) {
            String key = entry.getKey();
            if (!"format_version".equals(key)
               && !"profile".equals(key)
               && !"client".equals(key)
               && !"client_version".equals(key)
               && !"minecraft_version".equals(key)
               && !"updated_at".equals(key)
               && entry.getValue().isJsonObject()) {
               JsonObject modObj = entry.getValue().getAsJsonObject();
               Module m = ConfigSerializer.findModule(key);
               if (m != null && isGuiModule(m)) {
                  String id = ConfigSerializer.getModuleId(m);
                  guiModulesCollector.add(id, this.convertModuleToV4(m, modObj));
               } else if (m != null) {
                  String id = ConfigSerializer.getModuleId(m);
                  v4Modules.add(id, this.convertModuleToV4(m, modObj));
               } else {
                  unknownModules.add(key, modObj.deepCopy());
               }
            }
         }
      }

      v4.add("modules", v4Modules);
      if (!unknownModules.isEmpty()) {
         v4.add("_unknown_modules", unknownModules);
      }

      return v4;
   }

   private JsonObject convertModuleToV4(Module m, JsonObject oldMod) {
      JsonObject v4Mod = new JsonObject();
      boolean enabled = false;
      int key = -1;
      if (oldMod.has("enabled")) {
         enabled = oldMod.get("enabled").getAsBoolean();
      } else if (oldMod.has("e")) {
         enabled = oldMod.get("e").getAsBoolean();
      } else if (m != null) {
         enabled = m.isEnabled();
      }

      if (oldMod.has("key")) {
         key = oldMod.get("key").getAsInt();
      } else if (oldMod.has("k")) {
         key = oldMod.get("k").getAsInt();
      } else if (m != null) {
         key = m.getKey();
      }

      v4Mod.addProperty("enabled", enabled);
      v4Mod.addProperty("key", key);
      JsonObject settingsOut = new JsonObject();
      JsonObject unknownSettingsOut = new JsonObject();
      JsonElement settingsEl = oldMod.has("settings") ? oldMod.get("settings") : (oldMod.has("s") ? oldMod.get("s") : null);
      if (settingsEl != null && settingsEl.isJsonObject()) {
         JsonObject sObj = settingsEl.getAsJsonObject();

         for (Entry<String, JsonElement> sEntry : sObj.entrySet()) {
            String sName = sEntry.getKey();
            JsonElement sVal = sEntry.getValue();
            Setting<?> matched = null;
            if (m != null) {
               for (Setting<?> s : m.getSettings()) {
                  if (s.getName().equalsIgnoreCase(sName)) {
                     matched = s;
                     break;
                  }
               }
            }

            if (matched != null) {
               ConfigSerializer.deserializeSetting(matched, sVal);
               settingsOut.add(matched.getName(), ConfigSerializer.serializeSetting(matched));
            } else {
               unknownSettingsOut.add(sName, sVal.deepCopy());
            }
         }
      }

      v4Mod.add("settings", settingsOut);
      if (!unknownSettingsOut.isEmpty()) {
         v4Mod.add("_unknown", unknownSettingsOut);
      }

      return v4Mod;
   }

   private static boolean isGuiModule(Module module) {
      if (module == null) {
         return false;
      } else {
         String name = module.getName();
         return "ClickGUI".equalsIgnoreCase(name)
            || "Click GUI".equalsIgnoreCase(name)
            || "ArrayLists".equalsIgnoreCase(name)
            || "ArrayList".equalsIgnoreCase(name)
            || module.getCategory() == Category.INTERFACE;
      }
   }

   private void writeMarker(int profiles, int friends, String status) {
      try {
         JsonObject marker = new JsonObject();
         marker.addProperty("format_version", 4);
         marker.addProperty("migrated_at", Instant.now().toString());
         marker.addProperty("source", this.paths.legacyRoot().toAbsolutePath().toString());
         marker.addProperty("profiles_migrated", profiles);
         marker.addProperty("friends_migrated", friends);
         marker.addProperty("status", status);
         Path markerPath = this.paths.migrationMarker();
         Files.createDirectories(markerPath.getParent());
         Files.writeString(
            markerPath, GSON.toJson(marker), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
         );
      } catch (Throwable var6) {
         System.err.println("[Kuro+] Failed to write migration marker: " + var6);
      }
   }

   private static void copyDirectoryTree(final Path source, final Path target) throws IOException {
      Files.createDirectories(target);
      Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path relative = source.relativize(dir);
            Path destDir = target.resolve(relative);
            Files.createDirectories(destDir);
            return FileVisitResult.CONTINUE;
         }

         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path relative = source.relativize(file);
            Path destFile = target.resolve(relative);
            Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
         }
      });
   }

   private static void atomicWrite(Path target, String content) throws IOException {
      Path parent = target.getParent();
      if (parent != null) {
         Files.createDirectories(parent);
      }

      Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
      Files.writeString(tmp, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

      try {
         Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException var5) {
         Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
      }
   }
}
