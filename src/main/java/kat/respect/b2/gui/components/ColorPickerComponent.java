package kat.respect.b2.gui.components;

import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.gui.SettingComponent;
import kat.respect.b2.module.setting.ColorSetting;
import kat.respect.b2.util.ColorUtil;
import net.minecraft.client.input.MouseButtonEvent;

public class ColorPickerComponent extends SettingComponent {
   private static final float BOX_MARGIN = 6.0F;
   private boolean expanded = false;
   private boolean draggingPicker = false;
   private boolean draggingHue = false;
   private boolean draggingAlpha = false;

   public ColorPickerComponent(ColorSetting setting, int x, int y, int width, int height) {
      super(setting, x, y, width, 18);
   }

   public ColorSetting getSetting() {
      return (ColorSetting)super.getSetting();
   }

   @Override
   public int getHeight() {
      return this.expanded ? 108 : 18;
   }

   @Override
   public void render(float mouseX, float mouseY, int fontColor, int accentColor) {
      boolean hovered = this.isHovered(mouseX, mouseY);
      ColorSetting cs = this.getSetting();
      int argb = cs.getValue();
      int a = argb >> 24 & 0xFF;
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      float[] hsb = ColorUtil.rgbToHsb(r, g, b, null);
      float hue = hsb[0];
      float sat = hsb[1];
      float bright = hsb[2];
      if (hovered) {
         NvgUtil.fillRounded(this.x, this.y, this.width, this.getHeight(), GuiTheme.radius(), GuiTheme.current().pickerBg);
      }

      NvgUtil.drawText(cs.getName(), this.x + 10.0F, this.y + 4.5F, 9.0F, fontColor | 0xFF000000, 9);
      float swatchX = this.x + this.width - 24.0F;
      float swatchY = this.y + 4.0F;
      if (this.expanded) {
         NvgUtil.drawChevronDown(swatchX - 7.0F, this.y + 9.0F, 2.5F, -1862270977);
      } else {
         NvgUtil.drawChevronRight(swatchX - 7.0F, this.y + 9.0F, 2.5F, 1627389951);
      }

      NvgUtil.strokeRect(swatchX - 1.0F, swatchY - 1.0F, 18.0F, 11.0F, 1.0F, GuiTheme.current().pickerBorder);
      NvgUtil.fillRect(swatchX, swatchY, 16.0F, 9.0F, argb);
      if (this.expanded) {
         float boxW = this.width - 12.0F;
         float sbY = this.y + 18.0F;
         float sbH = 50.0F;
         float sx = this.x + 6.0F;
         int solidRGB = ColorUtil.hsbToRgb(hue, 1.0F, 1.0F) & 16777215;
         int pickRGB = ColorUtil.hsbToRgb(hue, sat, bright) & 16777215;
         NvgUtil.save();
         NvgUtil.intersectScissor(sx, sbY, boxW, sbH);
         NvgUtil.fillRect(sx, sbY, boxW, sbH, 0xFF000000 | solidRGB);
         NvgUtil.fillGradient(sx, sbY, boxW, sbH, 0.0F, -1, 16777215, false);
         NvgUtil.fillGradient(sx, sbY, boxW, sbH, 0.0F, 0, -16777216, true);
         NvgUtil.restore();
         NvgUtil.strokeRect(sx - 0.5F, sbY - 0.5F, boxW + 1.0F, sbH + 1.0F, 1.0F, GuiTheme.current().pickerBorder);
         float cursorX = sx + sat * boxW;
         float cursorY = sbY + (1.0F - bright) * sbH;
         NvgUtil.strokeRect(cursorX - 2.5F, cursorY - 2.5F, 5.0F, 5.0F, 1.2F, -1);
         float hueY = sbY + sbH + 6.0F;
         float hueH = 10.0F;
         int segments = 6;
         float segW = boxW / segments;

         for (int i = 0; i < segments; i++) {
            float hue0 = (float)i / segments;
            float hue1 = (float)(i + 1) / segments;
            int h1 = 0xFF000000 | ColorUtil.hsbToRgb(hue0, 1.0F, 1.0F) & 16777215;
            int h2 = 0xFF000000 | ColorUtil.hsbToRgb(hue1, 1.0F, 1.0F) & 16777215;
            NvgUtil.fillGradient(sx + i * segW, hueY, segW, hueH, 0.0F, h1, h2, false);
         }

         NvgUtil.strokeRect(sx - 0.5F, hueY - 0.5F, boxW + 1.0F, hueH + 1.0F, 1.0F, GuiTheme.current().pickerBorder);
         float hueIndicatorX = sx + hue * boxW;
         NvgUtil.fillRect(hueIndicatorX - 1.0F, hueY - 1.0F, 2.0F, hueH + 2.0F, -1);
         float alphaY = hueY + hueH + 6.0F;
         float alphaH = 10.0F;
         int strips = 10;
         float stripW = boxW / strips;

         for (int i = 0; i < strips; i++) {
            NvgUtil.fillRect(sx + i * stripW, alphaY, stripW, alphaH, i % 2 == 0 ? -7829351 : -4473908);
         }

         NvgUtil.fillRect(sx, alphaY, boxW, alphaH, NvgUtil.withAlpha(0xFF000000 | pickRGB, a));
         NvgUtil.strokeRect(sx - 0.5F, alphaY - 0.5F, boxW + 1.0F, alphaH + 1.0F, 1.0F, GuiTheme.current().pickerBorder);
         float alphaIndicatorX = sx + a / 255.0F * boxW;
         NvgUtil.fillRect(alphaIndicatorX - 1.0F, alphaY - 1.0F, 2.0F, alphaH + 2.0F, -1);
      }
   }

   private void updateColor(ColorSetting cs, float h, float s, float b, int a) {
      int rgb = ColorUtil.hsbToRgb(h, s, b) & 16777215;
      cs.setValue((a & 0xFF) << 24 | rgb);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      float clickX = (float)event.x();
      float clickY = (float)event.y();
      int btn = event.buttonInfo().button();
      if (!(clickX >= this.x) || !(clickX <= this.x + this.width) || !(clickY >= this.y) || !(clickY <= this.y + 18) || btn != 0 && btn != 1) {
         if (!this.expanded) {
            return false;
         } else {
            float boxW = this.width - 12.0F;
            float sbY = this.y + 18.0F;
            float sbH = 50.0F;
            float hueY = sbY + sbH + 6.0F;
            float hueH = 10.0F;
            float alphaY = hueY + hueH + 6.0F;
            float alphaH = 10.0F;
            float sx = this.x + 6.0F;
            if (clickX >= sx && clickX <= sx + boxW) {
               if (clickY >= sbY && clickY <= sbY + sbH) {
                  this.draggingPicker = true;
                  this.applyClickPosition(clickX, clickY);
                  return true;
               }

               if (clickY >= hueY && clickY <= hueY + hueH) {
                  this.draggingHue = true;
                  this.applyClickPosition(clickX, clickY);
                  return true;
               }

               if (clickY >= alphaY && clickY <= alphaY + alphaH) {
                  this.draggingAlpha = true;
                  this.applyClickPosition(clickX, clickY);
                  return true;
               }
            }

            return false;
         }
      } else {
         this.expanded = !this.expanded;
         return true;
      }
   }

   private void applyClickPosition(float mx, float my) {
      ColorSetting cs = this.getSetting();
      int argb = cs.getValue();
      int a = argb >> 24 & 0xFF;
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      float[] hsb = ColorUtil.rgbToHsb(r, g, b, null);
      float hue = hsb[0];
      float sat = hsb[1];
      float bright = hsb[2];
      float boxW = Math.max(1.0F, this.width - 12.0F);
      float sbY = this.y + 18.0F;
      float sbH = 50.0F;
      float sx = this.x + 6.0F;
      if (this.draggingPicker) {
         sat = Math.max(0.0F, Math.min(1.0F, (mx - sx) / boxW));
         bright = Math.max(0.0F, Math.min(1.0F, 1.0F - (my - sbY) / sbH));
      } else if (this.draggingHue) {
         hue = Math.max(0.0F, Math.min(1.0F, (mx - sx) / boxW));
      } else if (this.draggingAlpha) {
         a = Math.max(0, Math.min(255, Math.round((mx - sx) / boxW * 255.0F)));
      }

      this.updateColor(cs, hue, sat, bright, a);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.draggingPicker = false;
      this.draggingHue = false;
      this.draggingAlpha = false;
      return false;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      if (!this.draggingPicker && !this.draggingHue && !this.draggingAlpha) {
         return false;
      } else {
         ColorSetting cs = this.getSetting();
         int argb = cs.getValue();
         int a = argb >> 24 & 0xFF;
         int r = argb >> 16 & 0xFF;
         int g = argb >> 8 & 0xFF;
         int b = argb & 0xFF;
         float[] hsb = ColorUtil.rgbToHsb(r, g, b, null);
         float hue = hsb[0];
         float sat = hsb[1];
         float bright = hsb[2];
         float boxW = Math.max(1.0F, this.width - 12.0F);
         float sbY = this.y + 18.0F;
         float sbH = 50.0F;
         float hueY = sbY + sbH + 6.0F;
         float hueH = 10.0F;
         float alphaY = hueY + hueH + 6.0F;
         float alphaH = 10.0F;
         float sx = this.x + 6.0F;
         float mx = (float)event.x();
         float my = (float)event.y();
         if (this.draggingPicker) {
            sat = Math.max(0.0F, Math.min(1.0F, (mx - sx) / boxW));
            bright = Math.max(0.0F, Math.min(1.0F, 1.0F - (my - sbY) / sbH));
         } else if (this.draggingHue) {
            hue = Math.max(0.0F, Math.min(1.0F, (mx - sx) / boxW));
         } else {
            a = Math.max(0, Math.min(255, Math.round((mx - sx) / boxW * 255.0F)));
         }

         this.updateColor(cs, hue, sat, bright, a);
         return true;
      }
   }
}
