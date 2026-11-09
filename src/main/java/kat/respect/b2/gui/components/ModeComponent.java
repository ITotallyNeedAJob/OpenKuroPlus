package kat.respect.b2.gui.components;

import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.gui.SettingComponent;
import kat.respect.b2.module.setting.EnumSetting;
import kat.respect.b2.module.setting.ModeSetting;
import kat.respect.b2.module.setting.Setting;
import net.minecraft.client.input.MouseButtonEvent;

public class ModeComponent extends SettingComponent {
   public ModeComponent(Setting<?> setting, int x, int y, int width, int height) {
      super(setting, x, y, width, 15);
   }

   private String getModeValue() {
      if (this.setting instanceof ModeSetting ms) {
         return ms.getValue();
      } else {
         return this.setting instanceof EnumSetting<?> es ? es.getValue().name() : "";
      }
   }

   private boolean isMultiLine() {
      String name = this.setting.getName();
      String modeVal = this.getModeValue();
      float approxNameW = name.length() * 5.4F;
      float approxValW = modeVal.length() * 5.2F + 16.0F;
      return approxNameW + approxValW + 24.0F > this.width;
   }

   @Override
   public int getHeight() {
      return this.isMultiLine() ? 24 : 15;
   }

   @Override
   public void render(float mouseX, float mouseY, int fontColor, int accentColor) {
      boolean hovered = this.isHovered(mouseX, mouseY);
      int currentH = this.getHeight();
      if (hovered) {
         NvgUtil.fillRounded(this.x, this.y, this.width, currentH, GuiTheme.radius(), GuiTheme.current().subHover);
      }

      String name = this.setting.getName();
      String modeVal = this.getModeValue();
      float textW = NvgUtil.textWidth(modeVal, 8.5F);
      float pillW = Math.max(34.0F, textW + 14.0F);
      float pillH = 11.5F;
      if (currentH >= 22) {
         NvgUtil.drawText(name, this.x + 10.0F, this.y + 2.0F, 9.0F, fontColor | 0xFF000000, 9);
         float pillX = this.x + this.width - pillW - 8.0F;
         float pillY = this.y + 11.5F;
         NvgUtil.fillRounded(pillX, pillY, pillW, pillH, 3.0F, -14540749);
         NvgUtil.strokeRounded(pillX - 0.5F, pillY - 0.5F, pillW + 1.0F, pillH + 1.0F, 3.0F, 1.0F, 419430399);
         NvgUtil.drawText(modeVal, pillX + 4.0F, pillY + 1.5F, 8.5F, accentColor | 0xFF000000, 9);
         NvgUtil.drawChevronDown(pillX + pillW - 5.0F, pillY + pillH * 0.5F, 2.0F, 1627389951);
      } else {
         NvgUtil.drawText(name, this.x + 10.0F, this.y + 2.5F, 9.0F, fontColor | 0xFF000000, 9);
         float pillX = this.x + this.width - pillW - 8.0F;
         float pillY = this.y + (currentH - pillH) * 0.5F;
         NvgUtil.fillRounded(pillX, pillY, pillW, pillH, 3.0F, -14540749);
         NvgUtil.strokeRounded(pillX - 0.5F, pillY - 0.5F, pillW + 1.0F, pillH + 1.0F, 3.0F, 1.0F, 419430399);
         NvgUtil.drawText(modeVal, pillX + 4.0F, pillY + 1.5F, 8.5F, accentColor | 0xFF000000, 9);
         NvgUtil.drawChevronDown(pillX + pillW - 5.0F, pillY + pillH * 0.5F, 2.0F, 1627389951);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.isHovered((float)event.x(), (float)event.y())) {
         int btn = event.buttonInfo().button();
         if (this.setting instanceof ModeSetting ms) {
            if (btn == 0) {
               ms.cycle();
            } else if (btn == 1) {
               ms.cycleBack();
            }

            return true;
         }

         if (this.setting instanceof EnumSetting<?> es) {
            if (btn == 0) {
               es.cycle();
            }

            return true;
         }
      }

      return false;
   }
}
