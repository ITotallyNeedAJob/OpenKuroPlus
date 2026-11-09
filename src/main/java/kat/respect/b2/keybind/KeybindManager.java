package kat.respect.b2.keybind;

import kat.respect.b2.event.EventBus;
import kat.respect.b2.event.EventHandler;
import kat.respect.b2.event.events.KeyEvent;
import kat.respect.b2.event.events.MouseButtonEvent;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import kat.respect.b2.module.setting.KeySetting;
import kat.respect.b2.module.setting.Setting;
import kat.respect.b2.util.ScreenUtil;
import net.minecraft.client.Minecraft;

public final class KeybindManager {
   public static final KeybindManager INSTANCE = new KeybindManager();
   public static final int MOUSE_OFFSET = 1000;
   private Module bindingModule = null;
   private boolean initialized = false;

   private KeybindManager() {
   }

   public synchronized void init() {
      if (!this.initialized) {
         EventBus.INSTANCE.subscribe(this);
         this.initialized = true;
      }
   }

   public void startBinding(Module module) {
      this.bindingModule = module;
   }

   public void stopBinding() {
      this.bindingModule = null;
   }

   public Module getBindingModule() {
      return this.bindingModule;
   }

   public boolean isCapturing() {
      if (this.bindingModule != null) {
         return true;
      } else {
         for (Module module : ModuleManager.INSTANCE.getModules()) {
            for (Setting<?> s : module.getSettings()) {
               if (s instanceof KeySetting ks && ks.isListening()) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   @EventHandler
   public void onKey(KeyEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || !ScreenUtil.hasScreen(mc)) {
         if (this.bindingModule != null) {
            if (event.isPress()) {
               if (event.getKey() != 256) {
                  if (event.getKey() == 259) {
                     this.bindingModule.setKey(-1);
                  } else {
                     this.bindingModule.setKey(event.getKey());
                  }
               }

               this.bindingModule = null;
            }
         } else {
            for (Module module : ModuleManager.INSTANCE.getModules()) {
               for (Setting<?> s : module.getSettings()) {
                  if (s instanceof KeySetting ks && ks.isListening()) {
                     if (!event.isPress()) {
                        return;
                     }

                     ks.onKey(event.getKey());
                     return;
                  }
               }
            }

            if (mc == null || !ScreenUtil.hasScreen(mc)) {
               int eventKey = event.getKey();

               for (Module module : ModuleManager.INSTANCE.getModules()) {
                  int moduleKey = module.getKey();
                  boolean match = moduleKey == eventKey
                     || moduleKey == 344 && eventKey == 229
                     || moduleKey == 229 && eventKey == 344
                     || moduleKey == 340 && eventKey == 225
                     || moduleKey == 225 && eventKey == 340;
                  if (match && eventKey != -1) {
                     if (module.isHoldMode()) {
                        if (event.isPress()) {
                           module.setEnabled(true);
                        } else if (event.isRelease()) {
                           module.setEnabled(false);
                        }
                     } else if (event.isPress()) {
                        module.toggle();
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onMouseButton(MouseButtonEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || !ScreenUtil.hasScreen(mc)) {
         int mouseKey = 1000 + event.getButton();
         if (this.bindingModule != null) {
            if (event.isPress()) {
               if (event.getButton() != 0) {
                  this.bindingModule.setKey(mouseKey);
                  this.bindingModule = null;
               }
            }
         } else {
            for (Module module : ModuleManager.INSTANCE.getModules()) {
               for (Setting<?> s : module.getSettings()) {
                  if (s instanceof KeySetting ks && ks.isListening()) {
                     if (!event.isPress()) {
                        return;
                     }

                     ks.onKey(mouseKey);
                     return;
                  }
               }
            }

            if (mc == null || !ScreenUtil.hasScreen(mc)) {
               if (event.getButton() != 0) {
                  for (Module module : ModuleManager.INSTANCE.getModules()) {
                     if (module.getKey() == mouseKey) {
                        if (module.isHoldMode()) {
                           if (event.isPress()) {
                              module.setEnabled(true);
                           } else if (event.isRelease()) {
                              module.setEnabled(false);
                           }
                        } else if (event.isPress()) {
                           module.toggle();
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
