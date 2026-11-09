package kat.respect.b2.gui.entity;

import java.util.List;
import java.util.function.Consumer;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import net.minecraft.client.input.MouseButtonEvent;

public class UIDropdown extends UIEntity {
   private String label = "";
   private List<String> modes;
   private int selectedIndex = 0;
   private Consumer<String> selectListener;

   public UIDropdown(String label, List<String> modes, String current) {
      this.label = label != null ? label : "";
      this.modes = modes;
      if (modes != null && current != null) {
         int idx = modes.indexOf(current);
         if (idx >= 0) {
            this.selectedIndex = idx;
         }
      }

      this.height = 20.0F;
   }

   public UIDropdown onSelect(Consumer<String> listener) {
      this.selectListener = listener;
      return this;
   }

   public String getSelected() {
      return this.modes != null && !this.modes.isEmpty() ? this.modes.get(Math.max(0, Math.min(this.modes.size() - 1, this.selectedIndex))) : "";
   }

   public void setSelected(String val) {
      if (this.modes != null && val != null) {
         int idx = this.modes.indexOf(val);
         if (idx >= 0) {
            this.selectedIndex = idx;
         }
      }
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      GuiTheme theme = GuiTheme.current();
      int fontColor = theme.fontColor | 0xFF000000;
      int accentColor = theme.accentColor | 0xFF000000;
      if (!this.label.isEmpty()) {
         NvgUtil.drawText(this.label, this.x, this.y + 4.0F, 9.5F, fontColor, 9);
      }

      String current = this.getSelected();
      float textW = NvgUtil.textWidth(current, 9.0F);
      float pillW = Math.max(50.0F, textW + 20.0F);
      float pillH = 16.0F;
      float pillX = this.x + this.width - pillW;
      float pillY = this.y + 2.0F;
      float pillR = 4.0F;
      boolean pillHovered = mouseX >= pillX && mouseX <= pillX + pillW && mouseY >= pillY && mouseY <= pillY + pillH;
      float hoverP = this.anim("hover", pillHovered ? 1.0F : 0.0F, 20.0F);
      int pillBg = NvgUtil.lerpColor(-14540749, -14014400, hoverP);
      NvgUtil.fillRounded(pillX, pillY, pillW, pillH, pillR, pillBg);
      int pillBorder = NvgUtil.lerpColor(553648127, NvgUtil.withAlpha(accentColor, 136), hoverP);
      NvgUtil.strokeRounded(pillX - 0.5F, pillY - 0.5F, pillW + 1.0F, pillH + 1.0F, pillR, 1.0F, pillBorder);
      NvgUtil.drawText(current, pillX + 6.0F, pillY + 3.5F, 9.0F, accentColor, 9);
      NvgUtil.drawChevronDown(pillX + pillW - 6.0F, pillY + pillH * 0.5F, 2.5F, -2130706433);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event) {
      if (this.visible && this.enabled && this.modes != null && !this.modes.isEmpty()) {
         if (this.contains((float)event.x(), (float)event.y())) {
            int btn = event.buttonInfo().button();
            if (btn == 0) {
               this.selectedIndex = (this.selectedIndex + 1) % this.modes.size();
               if (this.selectListener != null) {
                  this.selectListener.accept(this.getSelected());
               }

               return true;
            }

            if (btn == 1) {
               this.selectedIndex = (this.selectedIndex - 1 + this.modes.size()) % this.modes.size();
               if (this.selectListener != null) {
                  this.selectListener.accept(this.getSelected());
               }

               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
