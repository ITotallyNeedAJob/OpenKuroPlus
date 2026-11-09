package kat.respect.b2.module;

import java.util.ArrayList;
import java.util.List;
import kat.respect.b2.config.ConfigSerializer;
import kat.respect.b2.event.EventBus;
import kat.respect.b2.module.setting.Setting;
import kat.respect.b2.util.Log;
import net.minecraft.client.Minecraft;

public abstract class Module {
   protected final Minecraft mc = Minecraft.getInstance();
   private final String name;
   private final String description;
   private final Category category;
   private String moduleId;
   private int key;
   private boolean enabled;
   private boolean changingState;
   private boolean subscribed;
   private boolean cancelEnableRequested;
   private boolean holdMode;
   private final List<Setting<?>> settings = new ArrayList<>();
   private boolean defaultsCaptured;
   private boolean defaultEnabled;
   private int defaultKey;

   protected Module(String name, String description, Category category) {
      this(name, description, category, -1);
   }

   protected Module(String name, String description, Category category, int key) {
      this.name = name;
      this.description = description;
      this.category = category;
      this.key = key;
      this.defaultKey = key;
      this.defaultEnabled = false;
   }

   public final String getId() {
      return this.moduleId != null ? this.moduleId : this.getClass().getName();
   }

   public final void attachModuleId(String id) {
      if (this.moduleId != null && !this.moduleId.equals(id)) {
         throw new IllegalStateException("Module id already attached: " + this.moduleId);
      } else if (id != null && !id.isBlank()) {
         this.moduleId = id;
      } else {
         throw new IllegalArgumentException("Blank module id for " + this.getClass().getName());
      }
   }

   public final void toggle() {
      this.setEnabled(!this.enabled);
   }

   public final void setEnabled(boolean value) {
      if (this.enabled != value) {
         if (this.changingState) {
            if (!value) {
               this.cancelEnableRequested = true;
            }
         } else {
            this.changingState = true;

            try {
               if (value) {
                  this.enabled = true;

                  try {
                     this.onEnable();
                     if (this.cancelEnableRequested) {
                        this.cancelEnableRequested = false;
                        this.enabled = false;
                        if (this.subscribed) {
                           EventBus.INSTANCE.unsubscribe(this);
                           this.subscribed = false;
                        }

                        return;
                     }

                     if (!this.subscribed) {
                        EventBus.INSTANCE.subscribe(this);
                        this.subscribed = true;
                     }
                  } catch (Throwable var19) {
                     this.enabled = false;
                     this.cancelEnableRequested = false;
                     if (this.subscribed) {
                        EventBus.INSTANCE.unsubscribe(this);
                        this.subscribed = false;
                     }

                     try {
                        this.onDisable();
                     } catch (Throwable var16) {
                        Log.error("onDisable cleanup failed for " + this.name, var16);
                     }

                     Log.error("onEnable failed for " + this.name, var19);
                     return;
                  }
               } else {
                  this.enabled = false;

                  try {
                     this.onDisable();
                  } catch (Throwable var17) {
                     Log.error("onDisable failed for " + this.name, var17);
                  } finally {
                     if (this.subscribed) {
                        EventBus.INSTANCE.unsubscribe(this);
                        this.subscribed = false;
                     }
                  }
               }
            } finally {
               this.changingState = false;
            }

            if (ModuleManager.INSTANCE != null) {
               ModuleManager.INSTANCE.onStateChanged(this);
            }
         }
      }
   }

   public final void applyLoadedEnabledState(boolean value) {
      this.setEnabled(value);
   }

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   protected void addSetting(Setting<?> setting) {
      if (setting != null) {
         for (Setting<?> existing : this.settings) {
            if (existing.getName().equalsIgnoreCase(setting.getName())) {
               Log.warn("Duplicate setting name '" + setting.getName() + "' in module " + this.getName());
               return;
            }
         }

         this.settings.add(setting);
         setting.captureDefault();
      }
   }

   protected void addSettings(Setting<?>... settings) {
      for (Setting<?> s : settings) {
         this.addSetting(s);
      }
   }

   public void addPublicSettings(Setting<?>... settings) {
      this.addSettings(settings);
   }

   public List<Setting<?>> getSettings() {
      return this.settings;
   }

   public <T extends Setting<?>> T getSetting(String name) {
      for (Setting<?> s : this.settings) {
         if (s.getName().equals(name)) {
            return (T)s;
         }
      }

      return null;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public Category getCategory() {
      return this.category;
   }

   public int getKey() {
      return this.key;
   }

   public void setKey(int key) {
      this.key = key;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isHoldMode() {
      return this.holdMode;
   }

   protected void setHoldMode(boolean holdMode) {
      this.holdMode = holdMode;
   }

   public void captureDefaults() {
      this.defaultsCaptured = true;
      this.defaultEnabled = this.enabled;
      this.defaultKey = this.key;

      for (Setting<?> setting : this.settings) {
         setting.captureDefault();
      }
   }

   public boolean isEnabledChanged() {
      return this.enabled != this.defaultEnabled;
   }

   public boolean isKeyChanged() {
      return this.key != this.defaultKey;
   }

   public boolean hasAnyModifiedSetting() {
      for (Setting<?> setting : this.settings) {
         if (setting.isModified()) {
            return true;
         }
      }

      return false;
   }

   public boolean isModified() {
      if (this.isEnabledChanged()) {
         return true;
      } else if (this.isKeyChanged()) {
         return true;
      } else {
         return this.hasAnyModifiedSetting() ? true : ConfigSerializer.hasUnknownSettings(this);
      }
   }

   public void resetToDefaults() {
      if (this.enabled != this.defaultEnabled) {
         this.setEnabled(this.defaultEnabled);
      }

      this.key = this.defaultKey;

      for (Setting<?> setting : this.settings) {
         setting.resetToDefault();
      }

      ConfigSerializer.clearUnknownSettings(ConfigSerializer.getModuleId(this));
   }
}
