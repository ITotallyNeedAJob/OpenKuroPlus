package kat.respect.b2.gui.entity;

import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.module.setting.ColorSetting;
import kat.respect.b2.util.ColorUtil;
import net.minecraft.client.input.MouseButtonEvent;

public class UIColorPicker extends UIEntity {
   private final ColorSetting setting;
   private boolean expanded = false;
   private boolean draggingPicker = false;
   private boolean draggingHue = false;
   private boolean draggingAlpha = false;

   public UIColorPicker(ColorSetting setting) {
      this.setting = setting;
      this.height = 22.0F;
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   @Override
   public float getHeight() {
      return this.expanded ? 116.0F : 22.0F;
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      this.height = this.expanded ? 116.0F : 22.0F;
      GuiTheme theme = GuiTheme.current();
      int fontColor = theme.fontColor | 0xFF000000;
      int argb = this.setting.getValue();
      int a = argb >> 24 & 0xFF;
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      float[] hsb = ColorUtil.rgbToHsb(r, g, b, null);
      float hue = hsb[0];
      float sat = hsb[1];
      float bright = hsb[2];
      NvgUtil.drawText(this.setting.getName(), this.x, this.y + 4.0F, 9.5F, fontColor, 9);
      float swatchW = 28.0F;
      float swatchH = 14.0F;
      float swatchX = this.x + this.width - swatchW;
      float swatchY = this.y + 3.0F;
      NvgUtil.fillRounded(swatchX, swatchY, swatchW, swatchH, 3.0F, argb);
      NvgUtil.strokeRounded(swatchX - 0.5F, swatchY - 0.5F, swatchW + 1.0F, swatchH + 1.0F, 3.0F, 1.0F, 822083583);
      if (this.expanded) {
         float boxW = this.width;
         float sbY = this.y + 24.0F;
         float sbH = 54.0F;
         int solidRGB = ColorUtil.hsbToRgb(hue, 1.0F, 1.0F) & 16777215;
         NvgUtil.fillRounded(this.x, sbY, boxW, sbH, 3.0F, 0xFF000000 | solidRGB);
         NvgUtil.fillGradient(this.x, sbY, boxW, sbH, 3.0F, -1, 16777215, false);
         NvgUtil.fillGradient(this.x, sbY, boxW, sbH, 3.0F, 0, -16777216, true);
         float curX = this.x + sat * boxW;
         float curY = sbY + (1.0F - bright) * sbH;
         NvgUtil.strokeCircle(curX, curY, 3.5F, 1.5F, -16777216);
         NvgUtil.strokeCircle(curX, curY, 3.0F, 1.0F, -1);
         float hueY = sbY + sbH + 6.0F;
         float barH = 8.0F;
         float hueSegmentW = boxW / 6.0F;
         int[] rainbow = new int[]{-65536, -256, -16711936, -16711681, -16776961, -65281, -65536};

         for (int i = 0; i < 6; i++) {
            NvgUtil.fillGradient(this.x + i * hueSegmentW, hueY, hueSegmentW + 0.5F, barH, 0.0F, rainbow[i], rainbow[i + 1], false);
         }

         float hueCursorX = this.x + hue * boxW;
         NvgUtil.strokeRounded(hueCursorX - 2.0F, hueY - 1.0F, 4.0F, barH + 2.0F, 2.0F, 1.2F, -1);
         float alphaY = hueY + barH + 4.0F;
         NvgUtil.fillRounded(this.x, alphaY, boxW, barH, 2.0F, -15198680);
         NvgUtil.fillGradient(this.x, alphaY, boxW, barH, 2.0F, 16777215 & argb, 0xFF000000 | argb & 16777215, false);
         float alphaCursorX = this.x + a / 255.0F * boxW;
         NvgUtil.strokeRounded(alphaCursorX - 2.0F, alphaY - 1.0F, 4.0F, barH + 2.0F, 2.0F, 1.2F, -1);
      }
   }

   private void updatePicker(float mouseX, float mouseY) {
      float boxW = this.width;
      float sbY = this.y + 24.0F;
      float sbH = 54.0F;
      int argb = this.setting.getValue();
      int a = argb >> 24 & 0xFF;
      float[] hsb = ColorUtil.rgbToHsb(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, null);
      float sat = Math.max(0.0F, Math.min(1.0F, (mouseX - this.x) / boxW));
      float bright = Math.max(0.0F, Math.min(1.0F, 1.0F - (mouseY - sbY) / sbH));
      int newRgb = ColorUtil.hsbToRgb(hsb[0], sat, bright) & 16777215;
      this.setting.setValue(a << 24 | newRgb);
   }

   private void updateHue(float mouseX) {
      float boxW = this.width;
      int argb = this.setting.getValue();
      int a = argb >> 24 & 0xFF;
      float[] hsb = ColorUtil.rgbToHsb(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, null);
      float newHue = Math.max(0.0F, Math.min(1.0F, (mouseX - this.x) / boxW));
      int newRgb = ColorUtil.hsbToRgb(newHue, hsb[1], hsb[2]) & 16777215;
      this.setting.setValue(a << 24 | newRgb);
   }

   private void updateAlpha(float mouseX) {
      float boxW = this.width;
      int argb = this.setting.getValue();
      float newA = Math.max(0.0F, Math.min(1.0F, (mouseX - this.x) / boxW));
      int aInt = Math.round(newA * 255.0F);
      this.setting.setValue(aInt << 24 | argb & 16777215);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event) {
      if (this.visible && this.enabled) {
         float mx = (float)event.x();
         float my = (float)event.y();
         int btn = event.buttonInfo().button();
         if (my >= this.y && my <= this.y + 22.0F && mx >= this.x && mx <= this.x + this.width && btn == 0) {
            this.expanded = !this.expanded;
            return true;
         } else {
            if (this.expanded && btn == 0) {
               float boxW = this.width;
               float sbY = this.y + 24.0F;
               float sbH = 54.0F;
               float hueY = sbY + sbH + 6.0F;
               float alphaY = hueY + 8.0F + 4.0F;
               if (mx >= this.x && mx <= this.x + boxW && my >= sbY && my <= sbY + sbH) {
                  this.draggingPicker = true;
                  this.updatePicker(mx, my);
                  return true;
               }

               if (mx >= this.x && mx <= this.x + boxW && my >= hueY && my <= hueY + 8.0F) {
                  this.draggingHue = true;
                  this.updateHue(mx);
                  return true;
               }

               if (mx >= this.x && mx <= this.x + boxW && my >= alphaY && my <= alphaY + 8.0F) {
                  this.draggingAlpha = true;
                  this.updateAlpha(mx);
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.draggingPicker = this.draggingHue = this.draggingAlpha = false;
      return super.mouseReleased(event);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      float mx = (float)event.x();
      float my = (float)event.y();
      if (this.draggingPicker) {
         this.updatePicker(mx, my);
         return true;
      } else if (this.draggingHue) {
         this.updateHue(mx);
         return true;
      } else if (this.draggingAlpha) {
         this.updateAlpha(mx);
         return true;
      } else {
         return super.mouseDragged(event, dragX, dragY);
      }
   }
}
