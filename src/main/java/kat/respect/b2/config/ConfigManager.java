package kat.respect.b2.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import kat.respect.b2.bridge.ClickGuiSettings;
import kat.respect.b2.friend.FriendManager;
import kat.respect.b2.gui.CategoryPanel;
import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.gui.hud.HudLayout;
import kat.respect.b2.module.Category;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import kat.respect.b2.util.NotificationManager;

public final class ConfigManager {
   private static ConfigManager instance;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Gson COMPACT = new Gson();
   private final ConfigPaths paths;
   private final ConfigMigrator migrator;
   private final AtomicBoolean dirty = new AtomicBoolean(false);
   private final ExecutorService io = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "kuroplus-config-io");
      t.setDaemon(true);
      return t;
   });
   private volatile long lastSaveNanos = System.nanoTime();
   private volatile long dirtySinceNanos = 0L;
   private String activeProfile = "default";
   private final Map<String, JsonObject> unknownProfileModules = new ConcurrentHashMap<>();
   private JsonObject cachedUi = new JsonObject();
   private JsonObject cachedGuiModules = new JsonObject();

   private ConfigManager() {
      this(ConfigPaths.defaultPaths());
   }

   public ConfigManager(ConfigPaths paths) {
      this.paths = paths != null ? paths : ConfigPaths.defaultPaths();
      this.migrator = new ConfigMigrator(this.paths);
      this.ensureDirectories();
   }

   public static synchronized ConfigManager getInstance() {
      if (instance == null) {
         instance = new ConfigManager();
      }

      return instance;
   }

   public static ConfigManager createForTesting(Path gameDir) {
      return new ConfigManager(new ConfigPaths(gameDir));
   }

   private void ensureDirectories() {
      try {
         Files.createDirectories(this.paths.root());
         Files.createDirectories(this.paths.profilesDir());
         Files.createDirectories(this.paths.backups());
      } catch (Throwable var2) {
      }
   }

   public Path root() {
      return this.paths.root();
   }

   public Path profilesDir() {
      return this.paths.profilesDir();
   }

   public ConfigPaths getPaths() {
      return this.paths;
   }

   public String getActiveProfile() {
      return this.activeProfile;
   }

   public boolean setActiveProfile(String name) {
      if (name != null && !name.isBlank()) {
         this.activeProfile = ConfigPaths.sanitizeName(name);
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public List<String> listProfiles() {
      List<String> list = new ArrayList<>();
      Path dir = this.paths.profilesDir();
      if (Files.isDirectory(dir)) {
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path p : stream) {
               String fname = p.getFileName().toString();
               list.add(fname.substring(0, fname.length() - 5));
            }
         } catch (Throwable var9) {
         }
      }

      Collections.sort(list);
      return list;
   }

   public synchronized boolean loadProfile(String name) {
      if (name == null || name.isBlank()) {
         name = "default";
      }

      name = ConfigPaths.sanitizeName(name);
      Path file = this.paths.profile(name);
      if (!Files.exists(file)) {
         return false;
      } else {
         JsonObject root = this.readJsonFile(file);
         if (root == null) {
            return false;
         } else {
            ModuleManager mm = ModuleManager.INSTANCE;
            if (mm != null) {
               mm.setSuppressNotifications(true);

               try {
                  this.unknownProfileModules.clear();
                  ConfigSerializer.clearAllUnknownSettings();

                  for (Module m : mm.getModules()) {
                     if (!isGuiModule(m)) {
                        m.resetToDefaults();
                     }
                  }

                  if (root.has("modules") && root.get("modules").isJsonObject()) {
                     JsonObject modulesObj = root.getAsJsonObject("modules");

                     for (Entry<String, JsonElement> entry : modulesObj.entrySet()) {
                        String modKey = entry.getKey();
                        if (entry.getValue().isJsonObject()) {
                           JsonObject modObj = entry.getValue().getAsJsonObject();
                           Module mx = ConfigSerializer.findModule(modKey);
                           if (mx != null && !isGuiModule(mx)) {
                              ConfigSerializer.deserializeModule(mx, modObj);
                           } else {
                              this.unknownProfileModules.put(modKey, modObj.deepCopy());
                           }
                        }
                     }
                  }

                  if (root.has("_unknown_modules") && root.get("_unknown_modules").isJsonObject()) {
                     JsonObject unkObj = root.getAsJsonObject("_unknown_modules");

                     for (Entry<String, JsonElement> entryx : unkObj.entrySet()) {
                        if (entryx.getValue().isJsonObject()) {
                           this.unknownProfileModules.put(entryx.getKey(), entryx.getValue().getAsJsonObject().deepCopy());
                        }
                     }
                  }
               } finally {
                  mm.setSuppressNotifications(false);
               }
            }

            this.activeProfile = name;
            return true;
         }
      }
   }

   public synchronized void saveProfile(String name) {
      if (name == null || name.isBlank()) {
         name = "default";
      }

      name = ConfigPaths.sanitizeName(name);
      this.activeProfile = name;
      String json = this.serializeProfile(name);

      try {
         this.writeWithBackup(this.paths.profile(name), json, System.currentTimeMillis());
      } catch (Throwable var4) {
         System.err.println("[Kuro+] Failed to save profile '" + name + "': " + var4);
      }
   }

   public synchronized boolean deleteProfile(String name) {
      if (name != null && !name.isBlank()) {
         name = ConfigPaths.sanitizeName(name);
         Path file = this.paths.profile(name);

         try {
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
               if (name.equals(this.activeProfile)) {
                  this.activeProfile = "default";
               }

               this.markDirty();
               return true;
            }
         } catch (Throwable var4) {
            System.err.println("[Kuro+] Failed to delete profile '" + name + "': " + var4);
         }

         return false;
      } else {
         return false;
      }
   }

   public synchronized boolean renameProfile(String oldName, String newName) {
      if (oldName != null && !oldName.isBlank() && newName != null && !newName.isBlank()) {
         oldName = ConfigPaths.sanitizeName(oldName);
         newName = ConfigPaths.sanitizeName(newName);
         Path oldFile = this.paths.profile(oldName);
         Path newFile = this.paths.profile(newName);
         if (!Files.exists(oldFile)) {
            return false;
         } else {
            try {
               Files.move(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
               if (oldName.equals(this.activeProfile)) {
                  this.activeProfile = newName;
               }

               this.markDirty();
               return true;
            } catch (Throwable var6) {
               System.err.println("[Kuro+] Failed to rename profile: " + var6);
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public synchronized boolean loadClient() {
      Path file = this.paths.client();
      if (!Files.exists(file)) {
         return false;
      } else {
         JsonObject root = this.readJsonFile(file);
         if (root == null) {
            return false;
         } else {
            if (root.has("active_profile") && root.get("active_profile").isJsonPrimitive()) {
               this.activeProfile = ConfigPaths.sanitizeName(root.get("active_profile").getAsString());
            }

            if (root.has("ui") && root.get("ui").isJsonObject()) {
               this.cachedUi = root.getAsJsonObject("ui");
               this.applyUiSettings(this.cachedUi);
            }

            if (root.has("gui_modules") && root.get("gui_modules").isJsonObject()) {
               this.cachedGuiModules = root.getAsJsonObject("gui_modules");
               this.applyGuiModules(this.cachedGuiModules);
            }

            return true;
         }
      }
   }

   public synchronized void saveClient() {
      String json = this.serializeClient();

      try {
         this.writeWithBackup(this.paths.client(), json, System.currentTimeMillis());
      } catch (Throwable var3) {
         System.err.println("[Kuro+] Failed to save client state: " + var3);
      }
   }

   public synchronized void loadFriends() {
      Path file = this.paths.friends();
      if (Files.exists(file)) {
         JsonObject root = this.readJsonFile(file);
         if (root != null) {
            JsonArray arr = null;
            if (root.has("friends") && root.get("friends").isJsonArray()) {
               arr = root.getAsJsonArray("friends");
            }

            if (arr != null) {
               FriendManager.INSTANCE.clear();

               for (JsonElement el : arr) {
                  if (el.isJsonPrimitive()) {
                     FriendManager.INSTANCE.addFriend(el.getAsString());
                  }
               }
            }
         }
      }
   }

   public synchronized void saveFriends() {
      String json = this.serializeFriends();

      try {
         this.writeWithBackup(this.paths.friends(), json, System.currentTimeMillis());
      } catch (Throwable var3) {
         System.err.println("[Kuro+] Failed to save friends: " + var3);
      }
   }

   public synchronized void loadHud() {
      Path file = this.paths.hud();
      if (Files.exists(file)) {
         JsonObject root = this.readJsonFile(file);
         if (root != null) {
            JsonObject elements = root.has("elements") && root.get("elements").isJsonObject() ? root.getAsJsonObject("elements") : root;

            for (String id : HudLayout.IDS) {
               if (elements.has(id) && elements.get(id).isJsonObject()) {
                  JsonObject node = elements.getAsJsonObject(id);
                  HudLayout.Entry entry = HudLayout.of(id);
                  if (node.has("x")) {
                     entry.offsetX = node.get("x").getAsFloat();
                  }

                  if (node.has("y")) {
                     entry.offsetY = node.get("y").getAsFloat();
                  }

                  if (node.has("scale")) {
                     entry.scale = HudLayout.clampScale(node.get("scale").getAsFloat());
                  }

                  if (node.has("enabled")) {
                     entry.enabled = node.get("enabled").getAsBoolean();
                  }
               }
            }
         }
      }
   }

   public synchronized void saveHud() {
      String json = this.serializeHud();

      try {
         this.writeWithBackup(this.paths.hud(), json, System.currentTimeMillis());
      } catch (Throwable var3) {
         System.err.println("[Kuro+] Failed to save hud: " + var3);
      }
   }

   public void markDirty() {
      if (this.dirty.compareAndSet(false, true)) {
         this.dirtySinceNanos = System.nanoTime();
      }
   }

   public boolean isDirty() {
      return this.dirty.get();
   }

   public void tickAutosave() {
      if (this.dirty.get()) {
         long now = System.nanoTime();
         long idleTime = now - this.lastSaveNanos;
         long dirtyTime = now - this.dirtySinceNanos;
         if (idleTime >= TimeUnit.SECONDS.toNanos(5L) || dirtyTime >= TimeUnit.SECONDS.toNanos(60L)) {
            this.flushAsync();
         }
      }
   }

   public CompletableFuture<Void> flushAsync() {
      if (!this.dirty.getAndSet(false)) {
         return CompletableFuture.completedFuture(null);
      } else {
         String clientJson = this.serializeClient();
         String profileJson = this.serializeProfile(this.activeProfile);
         String friendsJson = this.serializeFriends();
         String hudJson = this.serializeHud();
         long stamp = System.currentTimeMillis();
         return CompletableFuture.runAsync(() -> {
            try {
               this.writeWithBackup(this.paths.client(), clientJson, stamp);
               this.writeWithBackup(this.paths.profile(this.activeProfile), profileJson, stamp);
               this.writeWithBackup(this.paths.friends(), friendsJson, stamp);
               this.writeWithBackup(this.paths.hud(), hudJson, stamp);
               this.lastSaveNanos = System.nanoTime();
            } catch (Throwable var8) {
               this.dirty.set(true);
               System.err.println("[Kuro+] Config flush failed: " + var8);
            }
         }, this.io);
      }
   }

   public boolean flushBlocking(Duration timeout) {
      try {
         this.flushAsync().get(timeout.toMillis(), TimeUnit.MILLISECONDS);
         return true;
      } catch (Throwable var3) {
         System.err.println("[Kuro+] Config shutdown flush failed/timed out: " + var3);
         return false;
      }
   }

   public String exportProfile(String name) {
      if (name == null || name.isBlank()) {
         name = this.activeProfile;
      }

      name = ConfigPaths.sanitizeName(name);
      Path file = this.paths.profile(name);
      if (Files.exists(file)) {
         try {
            return Files.readString(file, StandardCharsets.UTF_8);
         } catch (Throwable var4) {
         }
      }

      return this.serializeProfile(name);
   }

   public boolean importProfile(String name, String content) {
      if (content != null && !content.isBlank()) {
         if (name == null || name.isBlank()) {
            name = "imported_" + System.currentTimeMillis();
         }

         name = ConfigPaths.sanitizeName(name);
         JsonObject root;
         if (!content.trim().startsWith("r1:") && content.trim().startsWith("{")) {
            try {
               JsonElement el = JsonParser.parseString(content);
               root = el.isJsonObject() ? el.getAsJsonObject() : null;
            } catch (Throwable var6) {
               return false;
            }
         } else {
            root = ConfigCodec.decode(content);
         }

         if (root == null) {
            return false;
         } else {
            root.addProperty("format_version", 4);
            root.addProperty("profile", name);
            root.addProperty("updated_at", Instant.now().toString());

            try {
               this.writeWithBackup(this.paths.profile(name), GSON.toJson(root), System.currentTimeMillis());
               return true;
            } catch (Throwable var5) {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public Path getConfigDir() {
      return this.paths.profilesDir();
   }

   public Path getGuiConfigFile() {
      return this.paths.client();
   }

   public String getActiveConfig() {
      return this.getActiveProfile();
   }

   public void setActiveConfig(String name) {
      this.setActiveProfile(name);
   }

   public void saveConfig(String name) {
      this.saveProfile(name);
   }

   public boolean loadConfig(String name) {
      return this.loadProfile(name);
   }

   public void saveGui() {
      this.saveClient();
      this.saveFriends();
   }

   public void loadGui() {
      this.loadClient();
      this.loadFriends();
   }

   public void saveDefault() {
      this.saveProfile("default");
      this.saveClient();
      this.saveFriends();
      this.saveHud();
   }

   public void saveActive() {
      this.saveProfile(this.activeProfile);
      this.saveClient();
      this.saveFriends();
      this.saveHud();
   }

   public void loadDefault() {
      this.migrator.migrateLegacyIfNeeded();
      this.ensureDefaultFiles();
      this.loadClient();
      this.loadFriends();
      this.loadHud();
      if (!this.loadProfile(this.activeProfile)) {
         if (!"default".equals(this.activeProfile)) {
            if (!this.loadProfile("default")) {
               this.saveDefault();
            }
         } else {
            this.saveDefault();
         }
      }
   }

   private void ensureDefaultFiles() {
      try {
         Path clientFile = this.paths.client();
         if (!Files.exists(clientFile)) {
            this.copyBundledResource("/assets/kuroplus/defaults/client.json", clientFile);
         }

         Path defaultProfile = this.paths.profile("default");
         if (!Files.exists(defaultProfile)) {
            this.copyBundledResource("/assets/kuroplus/defaults/default.json", defaultProfile);
         }
      } catch (Throwable var3) {
      }
   }

   private void copyBundledResource(String resourcePath, Path destination) {
      try (InputStream in = this.getClass().getResourceAsStream(resourcePath)) {
         if (in != null) {
            if (destination.getParent() != null) {
               Files.createDirectories(destination.getParent());
            }

            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
         }
      } catch (Throwable var8) {
      }
   }

   public List<String> listConfigs() {
      return this.listProfiles();
   }

   public boolean deleteConfig(String name) {
      return this.deleteProfile(name);
   }

   public String shareCurrent() {
      JsonObject root = this.buildRoot(this.activeProfile);
      return ConfigCodec.encode(root);
   }

   public String shareSaved(String name) {
      JsonObject root = this.readProfileJsonObject(name);
      return root == null ? null : ConfigCodec.encode(root);
   }

   public String importCode(String code, String name) {
      JsonObject root = ConfigCodec.decode(code);
      if (root == null) {
         return null;
      } else {
         if ((name == null || name.isBlank()) && root.has("profile") && root.get("profile").isJsonPrimitive()) {
            name = root.get("profile").getAsString();
         }

         if (name == null || name.isBlank()) {
            name = "shared_" + new SimpleDateFormat("MMdd_HHmm").format(new Date());
         }

         name = this.uniqueName(ConfigPaths.sanitizeName(name));
         root.addProperty("format_version", 4);
         root.addProperty("profile", name);
         root.addProperty("updated_at", Instant.now().toString());
         return this.writeConfig(name, GSON.toJson(root)) ? name : null;
      }
   }

   public void resetAll() {
      ModuleManager mm = ModuleManager.INSTANCE;
      if (mm != null) {
         mm.setSuppressNotifications(true);

         try {
            this.unknownProfileModules.clear();
            ConfigSerializer.clearAllUnknownSettings();

            for (Module m : mm.getModules()) {
               m.resetToDefaults();
            }
         } finally {
            mm.setSuppressNotifications(false);
         }

         this.markDirty();
      }
   }

   public boolean writeConfig(String name, String content) {
      if (name != null && !name.isBlank() && content != null) {
         name = ConfigPaths.sanitizeName(name);

         try {
            this.writeWithBackup(this.paths.profile(name), content, System.currentTimeMillis());
            return true;
         } catch (Throwable var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isGuiModule(Module module) {
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

   public String serializeProfile(String name) {
      JsonObject root = this.buildRoot(name);
      return GSON.toJson(root);
   }

   private JsonObject buildRoot(String profile) {
      JsonObject root = new JsonObject();
      root.addProperty("format_version", 4);
      root.addProperty("client", "Kuro+");
      root.addProperty("client_version", "1.0.0");
      root.addProperty("minecraft_version", "26.1.2");
      root.addProperty("profile", profile != null ? profile : "default");
      root.addProperty("updated_at", Instant.now().toString());
      JsonObject modulesObj = new JsonObject();
      ModuleManager mm = ModuleManager.INSTANCE;
      if (mm != null) {
         for (Module m : mm.getModules()) {
            if (!isGuiModule(m) && m.isModified()) {
               String id = ConfigSerializer.getModuleId(m);
               modulesObj.add(id, ConfigSerializer.serializeModule(m, true));
            }
         }
      }

      for (Entry<String, JsonObject> entry : this.unknownProfileModules.entrySet()) {
         if (!modulesObj.has(entry.getKey())) {
            modulesObj.add(entry.getKey(), (JsonElement)entry.getValue());
         }
      }

      root.add("modules", modulesObj);
      return root;
   }

   public String serializeClient() {
      JsonObject root = new JsonObject();
      root.addProperty("format_version", 4);
      root.addProperty("client", "Kuro+");
      root.addProperty("client_version", "1.0.0");
      root.addProperty("active_profile", this.activeProfile);
      JsonObject uiObj = this.snapshotUi();
      root.add("ui", uiObj);
      JsonObject guiMods = new JsonObject();
      ModuleManager mm = ModuleManager.INSTANCE;
      if (mm != null) {
         for (Module m : mm.getModules()) {
            if (isGuiModule(m) && m.isModified()) {
               String id = ConfigSerializer.getModuleId(m);
               guiMods.add(id, ConfigSerializer.serializeModule(m, true));
            }
         }
      }

      for (Entry<String, JsonElement> entry : this.cachedGuiModules.entrySet()) {
         Module mx = ConfigSerializer.findModule(entry.getKey());
         if (mx == null && !guiMods.has(entry.getKey()) && entry.getValue().isJsonObject()) {
            guiMods.add(entry.getKey(), entry.getValue().getAsJsonObject());
         }
      }

      root.add("gui_modules", guiMods);
      return GSON.toJson(root);
   }

   public String serializeFriends() {
      JsonObject root = new JsonObject();
      root.addProperty("format_version", 4);
      root.addProperty("updated_at", Instant.now().toString());
      JsonArray friendsArray = new JsonArray();

      for (String f : FriendManager.INSTANCE.getFriends()) {
         friendsArray.add(f);
      }

      root.add("friends", friendsArray);
      return GSON.toJson(root);
   }

   public String serializeHud() {
      JsonObject root = new JsonObject();
      root.addProperty("format_version", 4);
      root.addProperty("updated_at", Instant.now().toString());
      JsonObject elements = new JsonObject();

      for (String id : HudLayout.IDS) {
         HudLayout.Entry entry = HudLayout.of(id);
         JsonObject node = new JsonObject();
         node.addProperty("x", entry.offsetX);
         node.addProperty("y", entry.offsetY);
         node.addProperty("scale", entry.scale);
         node.addProperty("enabled", entry.enabled);
         elements.add(id, node);
      }

      root.add("elements", elements);
      return GSON.toJson(root);
   }

   private JsonObject snapshotUi() {
      JsonObject uiObj = this.cachedUi != null ? this.cachedUi.deepCopy() : new JsonObject();

      try {
         uiObj.addProperty("theme", ClickGuiSettings.theme.getValue());
         uiObj.addProperty("render_engine", ClickGuiSettings.renderEngine.getValue());
         uiObj.addProperty("background", ClickGuiSettings.background.getValue());
         uiObj.addProperty("scale", ClickGuiSettings.scale.getValue());
      } catch (Throwable var7) {
      }

      try {
         ClickGuiScreen screen = ClickGuiScreen.getInstance();
         if (screen != null) {
            if (screen.getPanels() != null) {
               JsonObject panelsObj = new JsonObject();

               for (CategoryPanel panel : screen.getPanels()) {
                  JsonObject pObj = new JsonObject();
                  pObj.addProperty("x", panel.getX());
                  pObj.addProperty("y", panel.getY());
                  pObj.addProperty("collapsed", panel.isCollapsed());
                  panelsObj.add(panel.getCategory().name(), pObj);
               }

               uiObj.add("panels", panelsObj);
            }

            if (screen.getConfigPanel() != null) {
               JsonObject cpObj = new JsonObject();
               cpObj.addProperty("x", screen.getConfigPanel().getX());
               cpObj.addProperty("y", screen.getConfigPanel().getY());
               cpObj.addProperty("collapsed", screen.getConfigPanel().isCollapsed());
               uiObj.add("config_panel", cpObj);
            }
         }
      } catch (Throwable var8) {
      }

      return uiObj;
   }

   private void applyUiSettings(JsonObject ui) {
      if (ui != null) {
         try {
            if (ui.has("theme")) {
               ClickGuiSettings.theme.setValue(ui.get("theme").getAsString());
            }

            if (ui.has("render_engine")) {
               ClickGuiSettings.renderEngine.setValue(ui.get("render_engine").getAsString());
            }

            if (ui.has("background")) {
               ClickGuiSettings.background.setValue(ui.get("background").getAsString());
            }

            if (ui.has("scale")) {
               ClickGuiSettings.scale.setValue(ui.get("scale").getAsDouble());
            }
         } catch (Throwable var8) {
         }

         try {
            ClickGuiScreen screen = ClickGuiScreen.getInstance();
            if (screen != null) {
               if (ui.has("panels") && ui.get("panels").isJsonObject() && screen.getPanels() != null) {
                  JsonObject panelsObj = ui.getAsJsonObject("panels");

                  for (CategoryPanel panel : screen.getPanels()) {
                     String catName = panel.getCategory().name();
                     if (panelsObj.has(catName)) {
                        JsonObject pObj = panelsObj.getAsJsonObject(catName);
                        if (pObj.has("x")) {
                           panel.setX(pObj.get("x").getAsInt());
                        }

                        if (pObj.has("y")) {
                           panel.setY(pObj.get("y").getAsInt());
                        }

                        if (pObj.has("collapsed")) {
                           panel.setCollapsed(pObj.get("collapsed").getAsBoolean());
                        }
                     }
                  }
               }

               JsonObject cpObj = ui.has("config_panel")
                  ? ui.getAsJsonObject("config_panel")
                  : (ui.has("configPanel") ? ui.getAsJsonObject("configPanel") : null);
               if (cpObj != null && screen.getConfigPanel() != null) {
                  if (cpObj.has("x")) {
                     screen.getConfigPanel().setX(cpObj.get("x").getAsInt());
                  }

                  if (cpObj.has("y")) {
                     screen.getConfigPanel().setY(cpObj.get("y").getAsInt());
                  }

                  if (cpObj.has("collapsed")) {
                     screen.getConfigPanel().setCollapsed(cpObj.get("collapsed").getAsBoolean());
                  }
               }
            }
         } catch (Throwable var9) {
         }
      }
   }

   private void applyGuiModules(JsonObject guiMods) {
      if (guiMods != null) {
         ModuleManager mm = ModuleManager.INSTANCE;
         if (mm != null) {
            for (Entry<String, JsonElement> entry : guiMods.entrySet()) {
               if (entry.getValue().isJsonObject()) {
                  Module m = ConfigSerializer.findModule(entry.getKey());
                  if (m != null && isGuiModule(m)) {
                     ConfigSerializer.deserializeModule(m, entry.getValue().getAsJsonObject());
                  }
               }
            }
         }
      }
   }

   public void writeWithBackup(Path target, String content, long timestamp) throws IOException {
      Path parent = target.getParent();
      if (parent != null) {
         Files.createDirectories(parent);
      }

      String fileName = target.getFileName().toString();
      String baseName = fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
      if (Files.exists(target)) {
         try {
            Path backupFile = this.paths.backups().resolve(baseName + "." + timestamp + ".json");
            Files.createDirectories(this.paths.backups());
            Files.copy(target, backupFile, StandardCopyOption.REPLACE_EXISTING);
         } catch (Throwable var11) {
            System.err.println("[Kuro+] Warning: Failed to create backup for " + fileName + ": " + var11);
         }
      }

      Path tmp = target.resolveSibling(fileName + ".tmp");
      Files.writeString(tmp, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

      try {
         Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException var10) {
         Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
      }

      this.pruneBackups(baseName);
   }

   private void pruneBackups(String baseName) {
      try {
         Path bDir = this.paths.backups();
         if (!Files.isDirectory(bDir)) {
            return;
         }

         List<Path> backups = new ArrayList<>();

         try (DirectoryStream<Path> stream = Files.newDirectoryStream(bDir, baseName + ".*.json")) {
            for (Path p : stream) {
               backups.add(p);
            }
         }

         if (backups.size() > 5) {
            backups.sort((p1, p2) -> {
               try {
                  return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
               } catch (IOException var3x) {
                  return p1.getFileName().toString().compareTo(p2.getFileName().toString());
               }
            });
            int toDelete = backups.size() - 5;

            for (int i = 0; i < toDelete; i++) {
               Files.deleteIfExists(backups.get(i));
            }
         }
      } catch (Throwable var9) {
         System.err.println("[Kuro+] Warning: Failed to prune backups for " + baseName + ": " + var9);
      }
   }

   public JsonObject readJsonFile(Path file) {
      if (!Files.exists(file)) {
         return null;
      } else {
         String content;
         try {
            content = Files.readString(file, StandardCharsets.UTF_8);
         } catch (Throwable var5) {
            this.handleCorruptFile(file, "Cannot read file: " + var5.getMessage());
            return null;
         }

         try {
            JsonElement parsed = JsonParser.parseString(content);
            if (parsed != null && parsed.isJsonObject()) {
               return parsed.getAsJsonObject();
            } else {
               this.handleCorruptFile(file, "Root element is not a JSON object");
               return null;
            }
         } catch (Throwable var4) {
            this.handleCorruptFile(file, var4.getMessage());
            return null;
         }
      }
   }

   private void handleCorruptFile(Path file, String reason) {
      long now = System.currentTimeMillis();
      Path corruptPath = file.resolveSibling(file.getFileName().toString() + ".corrupt-" + now);

      try {
         Files.move(file, corruptPath, StandardCopyOption.REPLACE_EXISTING);
         System.err.println("[Kuro+] Corrupt config file found: " + file.getFileName() + " (" + reason + "), renamed to " + corruptPath.getFileName());
         NotificationManager.push("Config", "Corrupt config: " + file.getFileName() + ", restored defaults", NotificationManager.Type.ERROR);
      } catch (Throwable var7) {
         System.err.println("[Kuro+] Failed to rename corrupt config file: " + file + ": " + var7);
      }
   }

   private JsonObject readProfileJsonObject(String name) {
      if (name != null && !name.isBlank()) {
         name = ConfigPaths.sanitizeName(name);
         Path file = this.paths.profile(name);
         return this.readJsonFile(file);
      } else {
         return null;
      }
   }

   private String uniqueName(String name) {
      if (!Files.exists(this.paths.profile(name))) {
         return name;
      } else {
         for (int i = 2; i < 1000; i++) {
            String candidate = name + "_" + i;
            if (!Files.exists(this.paths.profile(candidate))) {
               return candidate;
            }
         }

         return name;
      }
   }
}
