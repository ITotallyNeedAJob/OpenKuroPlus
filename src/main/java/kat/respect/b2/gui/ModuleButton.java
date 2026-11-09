package kat.respect.b2.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import kat.respect.b2.bridge.ClickGuiSettings;
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

public class ModuleButton {
   private final Module module;
   private boolean expanded = false;
   private final List<SettingComponent> settingComponents = new ArrayList<>();
   private final KeySetting moduleKeySetting;
   private final KeyComponent moduleKeybindComponent;
   private float expandAnim = 0.0F;
   private float enableAnim = 0.0F;
   private long lastAnimTime = -1L;

   public ModuleButton(final Module module) {
      this.module = module;
      this.enableAnim = module.isEnabled() ? 1.0F : 0.0F;
      int rh = this.getRowHeight();

      for (Setting<?> setting : module.getSettings()) {
         if (setting instanceof BooleanSetting bs) {
            this.settingComponents.add(new BooleanComponent(bs, 0, 0, 0, rh));
         } else if (setting instanceof NumberSetting ns) {
            this.settingComponents.add(new NumberComponent(ns, 0, 0, 0, rh));
         } else if (setting instanceof ModeSetting ms) {
            this.settingComponents.add(new ModeComponent(ms, 0, 0, 0, rh));
         } else if (setting instanceof EnumSetting<?> es) {
            this.settingComponents.add(new ModeComponent(es, 0, 0, 0, rh));
         } else if (setting instanceof ColorSetting cs) {
            this.settingComponents.add(new ColorPickerComponent(cs, 0, 0, 0, rh));
         } else if (setting instanceof RangeSetting rs) {
            this.settingComponents.add(new RangeComponent(rs, 0, 0, 0, rh));
         } else if (setting instanceof StringSetting ss) {
            this.settingComponents.add(new StringComponent(ss, 0, 0, 0, rh));
         } else if (setting instanceof KeySetting ks) {
            this.settingComponents.add(new KeyComponent(ks, 0, 0, 0, rh));
         }
      }

      this.moduleKeySetting = new KeySetting("Keybind", module.getKey()) {
         {
            Objects.requireNonNull(ModuleButton.this);
         }

         @Override
         public Integer getValue() {
            return module.getKey();
         }

         @Override
         public void setValue(Integer value) {
            super.setValue(value);
            module.setKey(value != null ? value : -1);
         }
      };
      this.moduleKeySetting.onChange(k -> module.setKey(this.moduleKeySetting.getValue()));
      this.moduleKeybindComponent = new KeyComponent(this.moduleKeySetting, 0, 0, 0, rh);
   }

   public Module getModule() {
      return this.module;
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   public void setExpanded(boolean expanded) {
      this.expanded = expanded;
   }

   private int getRowHeight() {
      if (ClickGuiSettings.moduleHeight != null) {
         String mMode = ClickGuiSettings.moduleHeight.getValue();
         if ("Slim (14px)".equals(mMode)) {
            return 14;
         } else {
            return "Tall (18px)".equals(mMode) ? 18 : 16;
         }
      } else {
         return 16;
      }
   }

   public int fullHeight() {
      int h = this.getRowHeight();
      if (this.expanded) {
         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible()) {
               h += sc.getHeight();
            }
         }

         h += this.moduleKeybindComponent.getHeight();
      }

      return h;
   }

   public int totalContentHeight() {
      int h = this.getRowHeight();

      for (SettingComponent sc : this.settingComponents) {
         if (sc.getSetting().isVisible()) {
            h += sc.getHeight();
         }
      }

      return h + this.moduleKeybindComponent.getHeight();
   }

   public int animatedHeight() {
      int baseH = this.getRowHeight();
      if (!this.expanded && this.expandAnim <= 0.001F) {
         return baseH;
      } else {
         int fullContentH = this.totalContentHeight();
         int dropTotal = fullContentH - baseH;
         return baseH + Math.round(dropTotal * this.expandAnim);
      }
   }

   public int getHeight() {
      int rowH = this.getRowHeight();
      if (!this.expanded && this.expandAnim <= 0.0F) {
         return rowH;
      } else {
         int full = this.fullHeight();
         return Math.round(rowH + (full - rowH) * this.expandAnim);
      }
   }

   public void updateAnimations() {
      long now = System.currentTimeMillis();
      if (this.lastAnimTime < 0L) {
         this.lastAnimTime = now;
      }

      float dt = Math.min(0.1F, (float)(now - this.lastAnimTime) / 1000.0F);
      this.lastAnimTime = now;
      float rate = 1.0F - (float)Math.exp(-22.0 * dt);
      float target = this.expanded ? 1.0F : 0.0F;
      this.expandAnim = this.expandAnim + (target - this.expandAnim) * rate;
      if (this.expanded || !(this.expandAnim < 0.005F) && !((this.fullHeight() - this.getRowHeight()) * this.expandAnim < 1.0F)) {
         if (this.expanded && 1.0F - this.expandAnim < 0.002F) {
            this.expandAnim = 1.0F;
         }
      } else {
         this.expandAnim = 0.0F;
      }

      float eTarget = this.module.isEnabled() ? 1.0F : 0.0F;
      this.enableAnim = this.enableAnim + (eTarget - this.enableAnim) * (1.0F - (float)Math.exp(-24.0 * dt));
      if (Math.abs(eTarget - this.enableAnim) < 0.003F) {
         this.enableAnim = eTarget;
      }
   }

   public boolean isHeaderHovered(int mouseX, int mouseY, int x, int y, int width) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + this.getRowHeight();
   }

   public void render(float mouseX, float mouseY, int x, int y, int width, int fontColor, int accentColor) {
      this.updateAnimations();
      int rowH = this.getRowHeight();
      boolean hovered = this.isHeaderHovered((int)mouseX, (int)mouseY, x, y, width);
      boolean enabled = this.module.isEnabled();
      GuiTheme theme = GuiTheme.current();
      float radius = GuiTheme.radius();
      if (hovered) {
         NvgUtil.fillRounded(x + 3.0F, y + 0.5F, width - 6.0F, rowH - 1.0F, radius, theme.rowHover);
      }

      if (enabled) {
         NvgUtil.fillRect(x + 3.0F, y + 2.5F, 2.0F, rowH - 5.0F, accentColor | 0xFF000000);
      }

      float fontSize = rowH >= 18 ? 10.5F : (rowH >= 16 ? 10.0F : 9.5F);
      float textOffsetY = (rowH - fontSize) / 2.0F - 0.5F;
      int idleText = hovered ? -1 : theme.dimText;
      int activeText = NvgUtil.brighten(accentColor | 0xFF000000, 0.2F);
      int textCol = NvgUtil.lerpColor(idleText, activeText, this.enableAnim);
      NvgUtil.drawText(this.module.getName(), x + 9.0F, y + textOffsetY, fontSize, textCol, 9);
      if (!this.settingComponents.isEmpty()) {
         int chevColor = enabled ? accentColor | 0xFF000000 : 1895825407;
         if (this.expanded) {
            NvgUtil.drawChevronDown(x + width - 8.0F, y + rowH * 0.5F, 2.5F, chevColor);
         } else {
            NvgUtil.drawChevronRight(x + width - 8.0F, y + rowH * 0.5F, 2.5F, chevColor);
         }
      }

      float dropH = (this.fullHeight() - rowH) * this.expandAnim;
      if ((this.expanded || this.expandAnim > 0.001F) && dropH > 0.5F) {
         int childIndent = 6;
         int childWidth = width - childIndent;
         NvgUtil.save();
         NvgUtil.intersectScissor(x, y + rowH, width, dropH);
         NvgUtil.fillRect(x + 1.0F, y + rowH + 1.0F, 1.5F, Math.max(0.0F, dropH - 2.0F), NvgUtil.withAlpha(accentColor | 0xFF000000, 85));
         int currentY = y + rowH;

         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible()) {
               int childH = sc.getHeight();
               sc.setPosition(x + childIndent, currentY, childWidth);
               sc.render(mouseX, mouseY, fontColor, accentColor);
               currentY += childH;
            }
         }

         this.moduleKeybindComponent.setPosition(x + childIndent, currentY, childWidth);
         this.moduleKeybindComponent.render(mouseX, mouseY, fontColor, accentColor);
         NvgUtil.restore();
      }
   }

   public boolean mouseClicked(MouseButtonEvent event, int x, int y, int width, float visibleBottom) {
      int mouseX = (int)event.x();
      int mouseY = (int)event.y();
      int btn = event.buttonInfo().button();
      if (this.isHeaderHovered(mouseX, mouseY, x, y, width)) {
         if (btn == 0) {
            if ("ClickGUI".equalsIgnoreCase(this.module.getName())) {
               this.expanded = !this.expanded;
               return true;
            }

            this.module.toggle();
            return true;
         }

         if (btn == 1) {
            this.expanded = !this.expanded;
            return true;
         }
      }

      if (this.expanded) {
         int currentY = y + this.getRowHeight();
         int childIndent = 6;
         int childWidth = width - childIndent;

         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible()) {
               sc.setPosition(x + childIndent, currentY, childWidth);
               if (currentY < visibleBottom && sc.mouseClicked(event, false)) {
                  return true;
               }

               currentY += sc.getHeight();
            }
         }

         this.moduleKeybindComponent.setPosition(x + childIndent, currentY, childWidth);
         if (currentY < visibleBottom && this.moduleKeybindComponent.mouseClicked(event, false)) {
            return true;
         }
      }

      return false;
   }

   public void mouseReleased(MouseButtonEvent event, int x, int y, int width) {
      if (this.expanded) {
         int currentY = y + this.getRowHeight();
         int childIndent = 6;
         int childWidth = width - childIndent;

         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible()) {
               sc.setPosition(x + childIndent, currentY, childWidth);
               sc.mouseReleased(event);
               currentY += sc.getHeight();
            }
         }

         this.moduleKeybindComponent.setPosition(x + childIndent, currentY, childWidth);
         this.moduleKeybindComponent.mouseReleased(event);
      }
   }

   public boolean mouseDragged(MouseButtonEvent event, int x, int y, int width, double dragX, double dragY) {
      if (this.expanded) {
         int currentY = y + this.getRowHeight();
         int childIndent = 6;
         int childWidth = width - childIndent;

         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible()) {
               sc.setPosition(x + childIndent, currentY, childWidth);
               if (sc.mouseDragged(event, dragX, dragY)) {
                  return true;
               }

               currentY += sc.getHeight();
            }
         }

         this.moduleKeybindComponent.setPosition(x + childIndent, currentY, childWidth);
         if (this.moduleKeybindComponent.mouseDragged(event, dragX, dragY)) {
            return true;
         }
      }

      return false;
   }

   public boolean keyPressed(KeyEvent event) {
      if (this.expanded) {
         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible() && sc.keyPressed(event)) {
               return true;
            }
         }

         if (this.moduleKeybindComponent.keyPressed(event)) {
            return true;
         }
      }

      return false;
   }

   public boolean charTyped(CharacterEvent event) {
      if (this.expanded) {
         for (SettingComponent sc : this.settingComponents) {
            if (sc.getSetting().isVisible() && sc.charTyped(event)) {
               return true;
            }
         }

         if (this.moduleKeybindComponent.charTyped(event)) {
            return true;
         }
      }

      return false;
   }
}
