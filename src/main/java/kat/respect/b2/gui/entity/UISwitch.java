package kat.respect.b2.gui.entity;

import java.util.function.Consumer;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import net.minecraft.client.input.MouseButtonEvent;

public class UISwitch extends UIEntity {
   private boolean checked = false;
   private Consumer<Boolean> toggleListener;

   public UISwitch(boolean checked) {
      this.checked = checked;
      this.width = 26.0F;
      this.height = 14.0F;
      this.radius = 7.0F;
   }

   public UISwitch checked(boolean checked) {
      this.checked = checked;
      return this;
   }

   public boolean isChecked() {
      return this.checked;
   }

   public void setChecked(boolean checked) {
      this.checked = checked;
   }

   public UISwitch onToggle(Consumer<Boolean> listener) {
      this.toggleListener = listener;
      return this;
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      float target = this.checked ? 1.0F : 0.0F;
      float progress = this.anim("toggle", target, 24.0F);
      GuiTheme theme = GuiTheme.current();
      int accentColor = theme.accentColor | 0xFF000000;
      int idleTrack = -13751484;
      int trackColor = NvgUtil.lerpColor(idleTrack, accentColor, progress);
      if (this.hovered) {
         trackColor = NvgUtil.brighten(trackColor, 0.12F);
      }

      float r = this.height * 0.5F;
      NvgUtil.fillRounded(this.x, this.y, this.width, this.height, r, trackColor);
      if (progress < 0.5F) {
         NvgUtil.strokeRounded(this.x - 0.5F, this.y - 0.5F, this.width + 1.0F, this.height + 1.0F, r, 1.0F, 352321535);
      }

      float knobPad = 2.0F;
      float knobDiameter = this.height - knobPad * 2.0F;
      float minKnobX = this.x + knobPad;
      float maxKnobX = this.x + this.width - knobDiameter - knobPad;
      float knobX = minKnobX + (maxKnobX - minKnobX) * progress;
      float knobY = this.y + knobPad;
      float knobR = knobDiameter * 0.5F;
      NvgUtil.fillCircle(knobX + knobR + 0.25F, knobY + knobR + 0.75F, knobR, 704643072);
      NvgUtil.fillCircle(knobX + knobR, knobY + knobR, knobR, -1);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event) {
      if (this.visible && this.enabled) {
         if (this.contains((float)event.x(), (float)event.y()) && event.buttonInfo().button() == 0) {
            this.checked = !this.checked;
            if (this.toggleListener != null) {
               this.toggleListener.accept(this.checked);
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
