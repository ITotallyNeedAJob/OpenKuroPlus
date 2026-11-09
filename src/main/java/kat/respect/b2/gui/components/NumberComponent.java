package kat.respect.b2.gui.components;

import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.gui.SettingComponent;
import kat.respect.b2.module.setting.NumberSetting;
import net.minecraft.client.input.MouseButtonEvent;

public class NumberComponent extends SettingComponent {
   private boolean dragging = false;
   private float animatedPct = -1.0F;
   private float hoverAnim = 0.0F;

   public NumberComponent(NumberSetting setting, int x, int y, int width, int height) {
      super(setting, x, y, width, 18);
   }

   public NumberSetting getSetting() {
      return (NumberSetting)super.getSetting();
   }

   @Override
   public int getHeight() {
      return 18;
   }

   @Override
   public void render(float mouseX, float mouseY, int fontColor, int accentColor) {
      boolean hovered = this.isHovered(mouseX, mouseY);
      NumberSetting ns = this.getSetting();
      float dt = ClickGuiScreen.lastDt;
      if (hovered || this.dragging) {
         NvgUtil.fillRounded(this.x, this.y, this.width, this.height, GuiTheme.radius(), GuiTheme.current().subHover);
      }

      String valStr = ns.getIncrement() % 1.0 == 0.0 ? String.valueOf(ns.getValueInt()) : String.format("%.1f", ns.getValue());
      NvgUtil.drawText(ns.getName(), this.x + 10.0F, this.y + 2.0F, 9.0F, fontColor | 0xFF000000, 9);
      NvgUtil.drawText(valStr, this.x + this.width - 8.0F, this.y + 2.0F, 9.0F, accentColor | 0xFF000000, 12);
      float trackX = this.x + 10.0F;
      float trackW = this.width - 20.0F;
      float trackY = this.y + 12.5F;
      float trackH = 3.5F;
      float trackR = 1.75F;
      double min = ns.getMin();
      double max = ns.getMax();
      double val = ns.getValue();
      float targetPct = (float)Math.max(0.0, Math.min(1.0, (val - min) / (max - min)));
      if (this.animatedPct < 0.0F) {
         this.animatedPct = targetPct;
      } else if (this.dragging) {
         this.animatedPct = targetPct;
      } else {
         this.animatedPct = this.animatedPct + (targetPct - this.animatedPct) * (1.0F - (float)Math.exp(-28.0 * dt));
         if (Math.abs(targetPct - this.animatedPct) < 0.001F) {
            this.animatedPct = targetPct;
         }
      }

      float targetHover = !hovered && !this.dragging ? 0.0F : 1.0F;
      this.hoverAnim = this.hoverAnim + (targetHover - this.hoverAnim) * (1.0F - (float)Math.exp(-20.0 * dt));
      if (Math.abs(targetHover - this.hoverAnim) < 0.002F) {
         this.hoverAnim = targetHover;
      }

      float fillW = (float)Math.max(0.0, (double)(trackW * this.animatedPct));
      NvgUtil.fillRounded(trackX, trackY, trackW, trackH, trackR, -14540749);
      if (fillW > 0.0F) {
         NvgUtil.fillRounded(trackX, trackY, fillW, trackH, trackR, accentColor | 0xFF000000);
      }

      float knobCx = trackX + fillW;
      float knobCy = trackY + trackH * 0.5F;
      float knobR = 4.0F + 1.2F * this.hoverAnim;
      if (this.hoverAnim > 0.01F) {
         NvgUtil.fillCircle(knobCx, knobCy, knobR + 2.0F, NvgUtil.withAlpha(accentColor | 0xFF000000, Math.round(50.0F * this.hoverAnim)));
      }

      NvgUtil.fillCircle(knobCx, knobCy + 0.5F, knobR, 1073741824);
      NvgUtil.fillCircle(knobCx, knobCy, knobR, -1);
      NvgUtil.fillCircle(knobCx, knobCy, knobR * 0.45F, accentColor | 0xFF000000);
   }

   private void updateValue(float mouseX) {
      NumberSetting ns = this.getSetting();
      double pct = Math.max(0.0, Math.min(1.0, (double)((mouseX - (this.x + 10.0F)) / (this.width - 20.0F))));
      double newVal = ns.getMin() + pct * (ns.getMax() - ns.getMin());
      ns.setValue(newVal);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.isHovered((float)event.x(), (float)event.y()) && event.buttonInfo().button() == 0) {
         this.dragging = true;
         this.updateValue((float)event.x());
         return true;
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
         this.updateValue((float)event.x());
         return true;
      } else {
         return false;
      }
   }
}
