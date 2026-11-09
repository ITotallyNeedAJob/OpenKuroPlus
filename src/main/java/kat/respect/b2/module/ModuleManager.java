package kat.respect.b2.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import kat.respect.b2.bridge.NotificationSettings;
import kat.respect.b2.event.ClientEventDispatcher;
import kat.respect.b2.util.Log;
import kat.respect.b2.util.NotificationManager;

public final class ModuleManager {
   public static final ModuleManager INSTANCE = new ModuleManager();
   private final List<Module> modules = new CopyOnWriteArrayList<>();
   private final List<Module> enabledModules = new CopyOnWriteArrayList<>();
   private final Map<Class<? extends Module>, Module> classMap = new ConcurrentHashMap<>();
   private final Map<String, Module> nameMap = new ConcurrentHashMap<>();
   private final Map<String, Module> idMap = new ConcurrentHashMap<>();
   private final Map<Category, List<Module>> categoryMap = new ConcurrentHashMap<>();
   private final Set<String> quarantinedModules = ConcurrentHashMap.newKeySet();
   private volatile boolean suppressNotifications;

   private ModuleManager() {
   }

   public synchronized void register(Module mod) {
      this.register(mod, null);
   }

   public synchronized void register(Module mod, String manifestId) {
      if (mod != null) {
         String id = manifestId != null && !manifestId.isBlank() ? manifestId : mod.getClass().getName();

         try {
            mod.attachModuleId(id);
         } catch (Throwable var7) {
            Log.error("loader", "Failed to attach module id '" + id + "' to " + mod.getName(), var7);
            this.quarantine(mod, var7);
            return;
         }

         if (this.idMap.containsKey(id)) {
            Log.error("loader", "Duplicate module id rejected: " + id + " for class " + mod.getClass().getName());
         } else {
            String lowerName = mod.getName().toLowerCase(Locale.ROOT);
            if (this.nameMap.containsKey(lowerName)) {
               Log.warn("loader", "Duplicate module display name: " + mod.getName());
            }

            this.modules.add(mod);
            this.classMap.put((Class<? extends Module>)mod.getClass(), mod);
            this.nameMap.put(lowerName, mod);
            this.idMap.put(id, mod);
            this.categoryMap.computeIfAbsent(mod.getCategory(), k -> new CopyOnWriteArrayList<>()).add(mod);

            try {
               mod.captureDefaults();
            } catch (Throwable var6) {
               Log.error("loader", "Failed to capture defaults for module " + id, var6);
            }

            if (mod.isEnabled() && !this.enabledModules.contains(mod)) {
               this.enabledModules.add(mod);
            }
         }
      }
   }

   public synchronized void registerAll(Module... mods) {
      for (Module mod : mods) {
         this.register(mod);
      }
   }

   public void quarantine(Module module, Throwable cause) {
      if (module != null) {
         String id = module.getId();
         this.quarantinedModules.add(id);
         Log.error("module", "Module quarantined due to error: " + id + " (" + module.getName() + ")", cause);

         try {
            if (module.isEnabled()) {
               module.setEnabled(false);
            }
         } catch (Throwable var5) {
         }
      }
   }

   public boolean isQuarantined(Module module) {
      return module != null && this.quarantinedModules.contains(module.getId());
   }

   public boolean isQuarantined(String moduleId) {
      return moduleId != null && this.quarantinedModules.contains(moduleId);
   }

   public Set<String> getQuarantinedModules() {
      return Collections.unmodifiableSet(this.quarantinedModules);
   }

   public synchronized void clear() {
      for (Module mod : this.modules) {
         if (mod.isEnabled()) {
            mod.setEnabled(false);
         }
      }

      this.modules.clear();
      this.enabledModules.clear();
      this.classMap.clear();
      this.nameMap.clear();
      this.idMap.clear();
      this.categoryMap.clear();
      this.quarantinedModules.clear();
   }

   public List<Module> getModules() {
      List<Module> sorted = new ArrayList<>(this.modules);
      sorted.sort(Comparator.<Module>comparingInt(m -> m.getCategory().ordinal()).thenComparing(Module::getName, String.CASE_INSENSITIVE_ORDER));
      return Collections.unmodifiableList(sorted);
   }

   public List<Module> getByCategory(Category category) {
      List<Module> list = this.categoryMap.get(category);
      if (list == null) {
         return Collections.emptyList();
      } else {
         List<Module> sorted = new ArrayList<>(list);
         sorted.sort(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER));
         return Collections.unmodifiableList(sorted);
      }
   }

   public List<Module> getEnabledModules() {
      return Collections.unmodifiableList(this.enabledModules);
   }

   public <T extends Module> T getModule(Class<T> clazz) {
      return (T)this.classMap.get(clazz);
   }

   public Module getModuleByName(String name) {
      return name == null ? null : this.nameMap.get(name.toLowerCase(Locale.ROOT));
   }

   public Module getModuleById(String id) {
      return id == null ? null : this.idMap.get(id);
   }

   public void setSuppressNotifications(boolean suppress) {
      this.suppressNotifications = suppress;
   }

   public boolean isSuppressNotifications() {
      return this.suppressNotifications;
   }

   public void onStateChanged(Module module) {
      if (module != null) {
         if (module.isEnabled()) {
            if (!this.enabledModules.contains(module)) {
               this.enabledModules.add(module);
            }
         } else {
            this.enabledModules.remove(module);
         }

         ClientEventDispatcher.onModuleStateChange(module, module.isEnabled());
         if (!this.suppressNotifications) {
            try {
               if (NotificationSettings.moduleToggles.getValue()) {
                  String modName = module.getName();
                  if ("Notifications".equalsIgnoreCase(modName) || "ClickGUI".equalsIgnoreCase(modName)) {
                     return;
                  }

                  long duration = (long)(NotificationSettings.duration.getValue() * 1000.0);
                  if (module.isEnabled()) {
                     NotificationManager.push(modName, "Module enabled", NotificationManager.Type.SUCCESS, duration);
                  } else {
                     NotificationManager.push(modName, "Module disabled", NotificationManager.Type.ERROR, duration);
                  }
               }
            } catch (Throwable var5) {
            }
         }
      }
   }
}
