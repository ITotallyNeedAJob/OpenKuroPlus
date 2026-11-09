package kat.respect.b2.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import kat.respect.b2.gui.components.BooleanComponent;
import kat.respect.b2.gui.components.ColorPickerComponent;
import kat.respect.b2.gui.components.KeyComponent;
import kat.respect.b2.gui.components.ModeComponent;
import kat.respect.b2.gui.components.NumberComponent;
import kat.respect.b2.gui.components.RangeComponent;
import kat.respect.b2.gui.components.StringComponent;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.setting.BooleanSetting;
import kat.respect.b2.module.setting.ColorSetting;
import kat.respect.b2.module.setting.EnumSetting;
import kat.respect.b2.module.setting.KeySetting;
import kat.respect.b2.module.setting.ModeSetting;
import kat.respect.b2.module.setting.NumberSetting;
import kat.respect.b2.module.setting.RangeSetting;
import kat.respect.b2.module.setting.Setting;
import kat.respect.b2.module.setting.StringSetting;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class ModuleSettingsWindow {
   private Module module;
   private final List<SettingComponent> settingComponents = new ArrayList<>();
   private KeyComponent moduleKeybindComponent;

   public void setModule(final Module module) {
      this.module = module;
      this.settingComponents.clear();
      this.moduleKeybindComponent = null;
      if (module != null) {
         for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting bs) {
               this.settingComponents.add(new BooleanComponent(bs, 0, 0, 0, 15));
            } else if (setting instanceof NumberSetting ns) {
               this.settingComponents.add(new NumberComponent(ns, 0, 0, 0, 18));
            } else if (setting instanceof ModeSetting ms) {
               this.settingComponents.add(new ModeComponent(ms, 0, 0, 0, 15));
            } else if (setting instanceof EnumSetting<?> es) {
               this.settingComponents.add(new ModeComponent(es, 0, 0, 0, 15));
            } else if (setting instanceof ColorSetting cs) {
               this.settingComponents.add(new ColorPickerComponent(cs, 0, 0, 0, 108));
            } else if (setting instanceof KeySetting ks) {
               this.settingComponents.add(new KeyComponent(ks, 0, 0, 0, 15));
            } else if (setting instanceof StringSetting ss) {
               this.settingComponents.add(new StringComponent(ss, 0, 0, 0, 20));
            } else if (setting instanceof RangeSetting rs) {
               this.settingComponents.add(new RangeComponent(rs, 0, 0, 0, 24));
            }
         }

         KeySetting keySetting = new KeySetting("Keybind", module.getKey()) {
            {
               Objects.requireNonNull(ModuleSettingsWindow.this);
            }

            @Override
            public Integer getValue() {
               return module.getKey();
            }

            @Override
            public void setValue(Integer value) {
               module.setKey(value);
            }
         };
         this.moduleKeybindComponent = new KeyComponent(keySetting, 0, 0, 0, 15);
      }
   }

   public Module getModule() {
      return this.module;
   }

   public float getHeight() {
      if (this.module == null) {
         return 0.0F;
      } else {
         float headerH = 22.0F;
         float subHeaderH = 16.0F;
         float h = headerH + subHeaderH + 4.0F;
         if (this.moduleKeybindComponent != null) {
            h += this.moduleKeybindComponent.getHeight();
         }

         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible()) {
               h += sc.getHeight();
            }
         }

         return h;
      }
   }

   public void render(float mouseX, float mouseY, float windowX, float windowY, float windowW, int headerColor, int fontColor, int accentColor) {
      if (this.module != null) {
         float headerH = 22.0F;
         float subHeaderH = 16.0F;
         NvgUtil.fillRect(windowX, windowY, windowW, headerH, -14935012);
         NvgUtil.fillRect(windowX, windowY, windowW, 1.5F, accentColor | 0xFF000000);
         NvgUtil.drawText(this.module.getName(), windowX + 8.0F, windowY + 5.0F, 10.5F, fontColor | 0xFF000000, 9);
         float currentY = windowY + headerH;
         NvgUtil.fillRect(windowX, currentY, windowW, subHeaderH, -15198184);
         NvgUtil.drawText("— Settings —", windowX + windowW / 2.0F, currentY + 3.5F, 9.5F, -8750470, 10);
         currentY += subHeaderH + 2.0F;
         if (this.moduleKeybindComponent != null) {
            this.moduleKeybindComponent.getSetting().setValue(this.module.getKey());
            this.moduleKeybindComponent.setPosition((int)windowX, (int)currentY, (int)windowW);
            this.moduleKeybindComponent.render(mouseX, mouseY, fontColor, accentColor);
            currentY += this.moduleKeybindComponent.getHeight();
         }

         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible()) {
               sc.setPosition((int)windowX, (int)currentY, (int)windowW);
               sc.render(mouseX, mouseY, fontColor, accentColor);
               currentY += sc.getHeight();
            }
         }
      }
   }

   public boolean mouseClicked(MouseButtonEvent event, float windowX, float windowY, float windowW) {
      if (this.module == null) {
         return false;
      } else {
         int clickX = (int)event.x();
         int clickY = (int)event.y();
         float headerH = 22.0F;
         float subHeaderH = 16.0F;
         float currentY = windowY + headerH + subHeaderH + 2.0F;
         if (this.moduleKeybindComponent != null) {
            this.moduleKeybindComponent.setPosition((int)windowX, (int)currentY, (int)windowW);
            if (this.moduleKeybindComponent.mouseClicked(event, false)) {
               return true;
            }

            currentY += this.moduleKeybindComponent.getHeight();
         }

         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible()) {
               sc.setPosition((int)windowX, (int)currentY, (int)windowW);
               if (sc.mouseClicked(event, false)) {
                  return true;
               }

               currentY += sc.getHeight();
            }
         }

         return clickX >= windowX && clickX <= windowX + windowW && clickY >= windowY && clickY <= windowY + this.getHeight();
      }
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.moduleKeybindComponent != null) {
         this.moduleKeybindComponent.mouseReleased(event);
      }

      for (SettingComponent sc : this.settingComponents) {
         if (sc.getSetting().isVisible()) {
            sc.mouseReleased(event);
         }
      }

      return false;
   }

   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      if (this.moduleKeybindComponent != null && this.moduleKeybindComponent.mouseDragged(event, dragX, dragY)) {
         return true;
      } else {
         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible() && sc.mouseDragged(event, dragX, dragY)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean keyPressed(KeyEvent event) {
      if (this.moduleKeybindComponent != null && this.moduleKeybindComponent.keyPressed(event)) {
         return true;
      } else {
         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible() && sc.keyPressed(event)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean charTyped(CharacterEvent event) {
      if (this.moduleKeybindComponent != null && this.moduleKeybindComponent.charTyped(event)) {
         return true;
      } else {
         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible() && sc.charTyped(event)) {
               return true;
            }
         }

         return false;
      }
   }
}
