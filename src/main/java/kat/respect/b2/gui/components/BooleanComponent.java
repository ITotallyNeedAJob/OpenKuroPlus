package kat.respect.b2.gui.components;

import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.gui.SettingComponent;
import kat.respect.b2.module.setting.BooleanSetting;
import net.minecraft.client.input.MouseButtonEvent;

public class BooleanComponent extends SettingComponent {
   private float animProgress = 0.0F;

   public BooleanComponent(BooleanSetting setting, int x, int y, int width, int height) {
      super(setting, x, y, width, 15);
   }

   public BooleanSetting getSetting() {
      return (BooleanSetting)super.getSetting();
   }

   @Override
   public int getHeight() {
      return 15;
   }

   @Override
   public void render(float mouseX, float mouseY, int fontColor, int accentColor) {
      boolean hovered = this.isHovered(mouseX, mouseY);
      BooleanSetting bs = this.getSetting();
      boolean enabled = bs.getValue();
      if (hovered) {
         NvgUtil.fillRounded(this.x, this.y, this.width, this.height, GuiTheme.radius(), GuiTheme.current().subHover);
      }

      int textColor = enabled ? accentColor | 0xFF000000 : fontColor | 0xFF000000;
      NvgUtil.drawText(bs.getName(), this.x + 10.0F, this.y + 2.5F, 9.0F, textColor, 9);
      float swW = 20.0F;
      float swH = 11.0F;
      float swX = this.x + this.width - swW - 8.0F;
      float swY = this.y + (this.height - swH) * 0.5F;
      float swR = swH * 0.5F;
      float dt = ClickGuiScreen.lastDt;
      float target = enabled ? 1.0F : 0.0F;
      this.animProgress = this.animProgress + (target - this.animProgress) * (1.0F - (float)Math.exp(-24.0 * dt));
      if (Math.abs(target - this.animProgress) < 0.002F) {
         this.animProgress = target;
      }

      int trackColor = NvgUtil.lerpColor(-14145992, accentColor | 0xFF000000, this.animProgress);
      NvgUtil.fillRounded(swX, swY, swW, swH, swR, trackColor);
      int borderCol = NvgUtil.lerpColor(553648127, NvgUtil.withAlpha(accentColor | 0xFF000000, 136), this.animProgress);
      NvgUtil.strokeRounded(swX - 0.5F, swY - 0.5F, swW + 1.0F, swH + 1.0F, swR, 1.0F, borderCol);
      float knobPad = 1.5F;
      float knobD = swH - knobPad * 2.0F;
      float minKnobX = swX + knobPad;
      float maxKnobX = swX + swW - knobD - knobPad;
      float knobX = minKnobX + (maxKnobX - minKnobX) * this.animProgress;
      float knobY = swY + knobPad;
      float knobR = knobD * 0.5F;
      NvgUtil.fillCircle(knobX + knobR, knobY + knobR, knobR, -1);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.isHovered((float)event.x(), (float)event.y()) && event.buttonInfo().button() == 0) {
         this.getSetting().toggle();
         return true;
      } else {
         return false;
      }
   }
}
