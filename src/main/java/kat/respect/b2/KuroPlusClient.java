package kat.respect.b2;

import kat.respect.b2.client.ClickGuiKeybinds;
import kat.respect.b2.client.ClickGuiOpener;
import kat.respect.b2.command.CommandManager;
import kat.respect.b2.config.ConfigManager;
import kat.respect.b2.event.EventBus;
import kat.respect.b2.friend.FriendManager;
import kat.respect.b2.gui.hud.HudLayout;
import kat.respect.b2.gui.notification.NotificationRenderer;
import kat.respect.b2.keybind.KeybindManager;
import kat.respect.b2.loader.RemoteModuleManager;
import kat.respect.b2.module.ModuleManager;
import kat.respect.b2.notification.NotificationManager;
import kat.respect.b2.rotation.RotationManager;
import kat.respect.b2.util.TargetCache;
import net.minecraft.client.Minecraft;

public final class KuroPlusClient {
   public static final String NAME = "Kuro+";
   public static final String VERSION = "1.0.0";
   public static final String CONFIG_DIR = "kuroplus";
   private static KuroPlusClient instance;
   private boolean initialized = false;
   private long lastAutosave = 0L;

   private KuroPlusClient() {
   }

   public static synchronized KuroPlusClient getInstance() {
      if (instance == null) {
         instance = new KuroPlusClient();
      }

      return instance;
   }

   public synchronized void init() {
      if (!this.initialized) {
         KeybindManager.INSTANCE.init();
         CommandManager.INSTANCE.init();
         NotificationRenderer.init();
         TargetCache.init();
         HudLayout.get().load();
         ClickGuiKeybinds.register();
         ClickGuiKeybinds.syncFromModule();
         ClickGuiOpener.register();
         Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "KuroPlus-Shutdown-Hook"));
         Thread t = new Thread(() -> {
            try {
               try {
                  RemoteModuleManager.INSTANCE.loadBundled();
               } catch (Throwable var1x) {
                  RemoteModuleManager.INSTANCE.loadDev();
               }

               ConfigManager.getInstance().loadDefault();
            } catch (Exception var2) {
               System.err.println("[Kuro+] Failed to load modules: " + var2);
            }
         }, "kuroplus-module-loader");
         t.setDaemon(true);
         t.start();
         this.initialized = true;
      }
   }

   public void onUpdate() {
      if (this.initialized) {
         try {
            ConfigManager.getInstance().tickAutosave();
         } catch (Throwable var4) {
         }

         long now = System.currentTimeMillis();
         if (now - this.lastAutosave > 60000L) {
            this.lastAutosave = now;
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null) {
               ConfigManager.getInstance().saveActive();
            }
         }
      }
   }

   public void shutdown() {
      try {
         ConfigManager.getInstance().saveActive();
      } catch (Throwable var2) {
      }
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public ModuleManager getModuleManager() {
      return ModuleManager.INSTANCE;
   }

   public CommandManager getCommandManager() {
      return CommandManager.INSTANCE;
   }

   public ConfigManager getConfigManager() {
      return ConfigManager.getInstance();
   }

   public FriendManager getFriendManager() {
      return FriendManager.INSTANCE;
   }

   public KeybindManager getKeybindManager() {
      return KeybindManager.INSTANCE;
   }

   public RotationManager getRotationManager() {
      return RotationManager.INSTANCE;
   }

   public NotificationManager getNotificationManager() {
      return NotificationManager.INSTANCE;
   }

   public EventBus getEventBus() {
      return EventBus.INSTANCE;
   }
}
