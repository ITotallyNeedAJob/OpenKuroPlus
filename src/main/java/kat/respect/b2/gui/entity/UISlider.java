package kat.respect.b2.gui.entity;

import java.util.function.Consumer;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import net.minecraft.client.input.MouseButtonEvent;

public class UISlider extends UIEntity {
   private String label = "";
   private double min = 0.0;
   private double max = 100.0;
   private double value = 50.0;
   private double step = 1.0;
   private boolean dragging = false;
   private Consumer<Double> changeListener;

   public UISlider(String label, double min, double max, double value, double step) {
      this.label = label != null ? label : "";
      this.min = min;
      this.max = max;
      this.value = value;
      this.step = step;
      this.height = 24.0F;
   }

   public UISlider onChange(Consumer<Double> listener) {
      this.changeListener = listener;
      return this;
   }

   public double getValue() {
      return this.value;
   }

   public void setValue(double val) {
      this.value = Math.max(this.min, Math.min(this.max, val));
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      GuiTheme theme = GuiTheme.current();
      int accentColor = theme.accentColor | 0xFF000000;
      int fontColor = theme.fontColor | 0xFF000000;
      int dimColor = theme.dimText;
      if (!this.label.isEmpty()) {
         NvgUtil.drawText(this.label, this.x, this.y + 1.0F, 9.5F, fontColor, 9);
      }

      String valStr = this.step % 1.0 == 0.0 ? String.valueOf(Math.round(this.value)) : String.format("%.2f", this.value);
      NvgUtil.drawText(valStr, this.x + this.width, this.y + 1.0F, 9.5F, accentColor, 12);
      float trackY = this.y + 16.0F;
      float trackH = 4.0F;
      float trackR = 2.0F;
      float trackW = this.width;
      NvgUtil.fillRounded(this.x, trackY, trackW, trackH, trackR, -14540749);
      double pct = this.max > this.min ? Math.max(0.0, Math.min(1.0, (this.value - this.min) / (this.max - this.min))) : 0.0;
      float fillTarget = (float)(trackW * pct);
      float fillW;
      if (this.dragging) {
         fillW = fillTarget;
         this.setAnim("fill", fillTarget);
      } else {
         fillW = this.anim("fill", fillTarget, 28.0F);
      }

      fillW = Math.max(0.0F, Math.min(trackW, fillW));
      if (fillW > 0.0F) {
         NvgUtil.fillRounded(this.x, trackY, fillW, trackH, trackR, accentColor);
      }

      float knobRadius = !this.hovered && !this.dragging ? 5.0F : 6.0F;
      float knobAnimR = this.anim("knobR", knobRadius, 20.0F);
      float knobCx = this.x + fillW;
      float knobCy = trackY + trackH * 0.5F;
      NvgUtil.fillCircle(knobCx, knobCy + 1.0F, knobAnimR, 1073741824);
      NvgUtil.fillCircle(knobCx, knobCy, knobAnimR, -1);
      NvgUtil.fillCircle(knobCx, knobCy, knobAnimR * 0.45F, accentColor);
   }

   private void updateValueFromMouse(float mouseX) {
      if (!(this.width <= 0.0F)) {
         double pct = Math.max(0.0, Math.min(1.0, (double)((mouseX - this.x) / this.width)));
         double rawVal = this.min + pct * (this.max - this.min);
         if (this.step > 0.0) {
            rawVal = Math.round(rawVal / this.step) * this.step;
         }

         rawVal = Math.max(this.min, Math.min(this.max, rawVal));
         if (Math.abs(rawVal - this.value) > 1.0E-5) {
            this.value = rawVal;
            if (this.changeListener != null) {
               this.changeListener.accept(this.value);
            }
         }
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event) {
      if (this.visible && this.enabled) {
         if (this.contains((float)event.x(), (float)event.y()) && event.buttonInfo().button() == 0) {
            this.dragging = true;
            this.updateValueFromMouse((float)event.x());
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (event.buttonInfo().button() == 0) {
         this.dragging = false;
      }

      return false;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      if (this.dragging) {
         this.updateValueFromMouse((float)event.x());
         return true;
      } else {
         return false;
      }
   }
}
