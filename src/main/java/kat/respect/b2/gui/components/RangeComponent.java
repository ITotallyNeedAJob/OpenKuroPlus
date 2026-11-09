package kat.respect.b2.gui.components;

import kat.respect.b2.bridge.ClickGuiSettings;
import kat.respect.b2.gui.ClickGuiScreen;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import kat.respect.b2.gui.SettingComponent;
import kat.respect.b2.module.setting.RangeSetting;
import net.minecraft.client.input.MouseButtonEvent;

public class RangeComponent extends SettingComponent {
   private static final float PAD = 10.0F;
   private static final float TRACK_H = 3.5F;
   private boolean draggingMin = false;
   private boolean draggingMax = false;
   private float animMinPct = -1.0F;
   private float animMaxPct = -1.0F;
   private float hoverMinAnim = 0.0F;
   private float hoverMaxAnim = 0.0F;

   public RangeComponent(RangeSetting setting, int x, int y, int width, int height) {
      super(setting, x, y, width, 24);
   }

   public RangeSetting getSetting() {
      return (RangeSetting)super.getSetting();
   }

   @Override
   public int getHeight() {
      return 24;
   }

   @Override
   public void render(float mouseX, float mouseY, int fontColor, int accentColor) {
      boolean hovered = this.isHovered(mouseX, mouseY);
      RangeSetting rs = this.getSetting();
      float dt = ClickGuiScreen.lastDt;
      if (hovered || this.draggingMin || this.draggingMax) {
         NvgUtil.fillRounded(this.x, this.y, this.width, this.height, GuiTheme.radius(), GuiTheme.current().subHover);
      }

      NvgUtil.drawText(rs.getName(), this.x + 10.0F, this.y + 2.0F, 9.0F, fontColor | 0xFF000000, 9);
      String valStr = String.format("%.1f - %.1f", rs.getMinValue(), rs.getMaxValue());
      NvgUtil.drawText(valStr, this.x + this.width - 10.0F, this.y + 2.0F, 9.0F, accentColor | 0xFF000000, 12);
      float trackX = this.x + 10.0F;
      float trackW = this.width - 20.0F;
      float trackY = this.y + 16.0F;
      double min = rs.getMin();
      double max = rs.getMax();
      double totalRange = max - min;
      if (totalRange <= 0.0) {
         totalRange = 1.0;
      }

      float targetMinPct = (float)Math.max(0.0, Math.min(1.0, (rs.getMinValue() - min) / totalRange));
      float targetMaxPct = (float)Math.max(0.0, Math.min(1.0, (rs.getMaxValue() - min) / totalRange));
      if (this.animMinPct < 0.0F) {
         this.animMinPct = targetMinPct;
         this.animMaxPct = targetMaxPct;
      } else {
         if (this.draggingMin) {
            this.animMinPct = targetMinPct;
         } else {
            this.animMinPct = this.animMinPct + (targetMinPct - this.animMinPct) * (1.0F - (float)Math.exp(-28.0 * dt));
            if (Math.abs(targetMinPct - this.animMinPct) < 0.001F) {
               this.animMinPct = targetMinPct;
            }
         }

         if (this.draggingMax) {
            this.animMaxPct = targetMaxPct;
         } else {
            this.animMaxPct = this.animMaxPct + (targetMaxPct - this.animMaxPct) * (1.0F - (float)Math.exp(-28.0 * dt));
            if (Math.abs(targetMaxPct - this.animMaxPct) < 0.001F) {
               this.animMaxPct = targetMaxPct;
            }
         }
      }

      float fillX = trackX + trackW * this.animMinPct;
      float fillW = Math.max(1.0F, trackW * (this.animMaxPct - this.animMinPct));
      float minHandleX = trackX + trackW * this.animMinPct;
      float maxHandleX = trackX + trackW * this.animMaxPct;
      float handleY = trackY + 1.75F;
      boolean nearMin = Math.abs(mouseX - minHandleX) <= 8.0F && Math.abs(mouseY - handleY) <= 8.0F;
      boolean nearMax = Math.abs(mouseX - maxHandleX) <= 8.0F && Math.abs(mouseY - handleY) <= 8.0F;
      float targetHoverMin = !nearMin && !this.draggingMin ? 0.0F : 1.0F;
      this.hoverMinAnim = this.hoverMinAnim + (targetHoverMin - this.hoverMinAnim) * (1.0F - (float)Math.exp(-20.0 * dt));
      if (Math.abs(targetHoverMin - this.hoverMinAnim) < 0.002F) {
         this.hoverMinAnim = targetHoverMin;
      }

      float targetHoverMax = !nearMax && !this.draggingMax ? 0.0F : 1.0F;
      this.hoverMaxAnim = this.hoverMaxAnim + (targetHoverMax - this.hoverMaxAnim) * (1.0F - (float)Math.exp(-20.0 * dt));
      if (Math.abs(targetHoverMax - this.hoverMaxAnim) < 0.002F) {
         this.hoverMaxAnim = targetHoverMax;
      }

      NvgUtil.fillRounded(trackX, trackY, trackW, 3.5F, 1.75F, -14540749);
      NvgUtil.fillRounded(fillX, trackY, fillW, 3.5F, 1.75F, accentColor | 0xFF000000);
      float minR = 4.0F + 1.2F * this.hoverMinAnim;
      if (this.hoverMinAnim > 0.01F) {
         NvgUtil.fillCircle(minHandleX, handleY, minR + 2.0F, NvgUtil.withAlpha(accentColor | 0xFF000000, Math.round(50.0F * this.hoverMinAnim)));
      }

      NvgUtil.fillCircle(minHandleX, handleY + 0.5F, minR, 1073741824);
      NvgUtil.fillCircle(minHandleX, handleY, minR, -1);
      NvgUtil.fillCircle(minHandleX, handleY, minR * 0.45F, accentColor | 0xFF000000);
      float maxR = 4.0F + 1.2F * this.hoverMaxAnim;
      if (this.hoverMaxAnim > 0.01F) {
         NvgUtil.fillCircle(maxHandleX, handleY, maxR + 2.0F, NvgUtil.withAlpha(accentColor | 0xFF000000, Math.round(50.0F * this.hoverMaxAnim)));
      }

      NvgUtil.fillCircle(maxHandleX, handleY + 0.5F, maxR, 1073741824);
      NvgUtil.fillCircle(maxHandleX, handleY, maxR, -1);
      NvgUtil.fillCircle(maxHandleX, handleY, maxR * 0.45F, accentColor | 0xFF000000);
   }

   private void updateMinValue(float mouseX) {
      RangeSetting rs = this.getSetting();
      double min = rs.getMin();
      double max = rs.getMax();
      double totalRange = max - min;
      float trackX = this.x + 10.0F;
      float trackW = this.width - 20.0F;
      double pct = Math.max(0.0, Math.min(1.0, (double)((mouseX - trackX) / trackW)));
      double newVal = min + pct * totalRange;
      if (newVal > rs.getMaxValue()) {
         newVal = rs.getMaxValue();
      }

      rs.setMinValue(newVal);
   }

   private void updateMaxValue(float mouseX) {
      RangeSetting rs = this.getSetting();
      double min = rs.getMin();
      double max = rs.getMax();
      double totalRange = max - min;
      float trackX = this.x + 10.0F;
      float trackW = this.width - 20.0F;
      double pct = Math.max(0.0, Math.min(1.0, (double)((mouseX - trackX) / trackW)));
      double newVal = min + pct * totalRange;
      if (newVal < rs.getMinValue()) {
         newVal = rs.getMinValue();
      }

      rs.setMaxValue(newVal);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.isHovered((float)event.x(), (float)event.y())) {
         float clickX = (float)event.x();
         int button = event.buttonInfo().button();
         boolean split = ClickGuiSettings.rangeSplitClick.getValue();
         if (split) {
            if (button == 0) {
               this.draggingMin = true;
               this.draggingMax = false;
               this.updateMinValue(clickX);
               return true;
            }

            if (button == 1) {
               this.draggingMax = true;
               this.draggingMin = false;
               this.updateMaxValue(clickX);
               return true;
            }
         } else {
            if (button == 0) {
               float trackX = this.x + 10.0F;
               float trackW = this.width - 20.0F;
               double min = this.getSetting().getMin();
               double totalRange = this.getSetting().getMax() - min;
               if (totalRange <= 0.0) {
                  totalRange = 1.0;
               }

               float minX = trackX + (float)((this.getSetting().getMinValue() - min) / totalRange) * trackW;
               float maxX = trackX + (float)((this.getSetting().getMaxValue() - min) / totalRange) * trackW;
               if (Math.abs(clickX - minX) <= Math.abs(clickX - maxX)) {
                  this.draggingMin = true;
                  this.draggingMax = false;
                  this.updateMinValue(clickX);
               } else {
                  this.draggingMax = true;
                  this.draggingMin = false;
                  this.updateMaxValue(clickX);
               }

               return true;
            }

            if (button == 1) {
               this.draggingMax = true;
               this.draggingMin = false;
               this.updateMaxValue(clickX);
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.draggingMin = false;
      this.draggingMax = false;
      return false;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      if (this.draggingMin) {
         this.updateMinValue((float)event.x());
         return true;
      } else if (this.draggingMax) {
         this.updateMaxValue((float)event.x());
         return true;
      } else {
         return false;
      }
   }
}
