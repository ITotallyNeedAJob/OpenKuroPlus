package kat.respect.b2.gui.entity;

import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.gui.NvgUtil;
import net.minecraft.client.input.MouseButtonEvent;

public class UIButton extends UIEntity {
   private String text = "";
   private UIButton.IconRenderer iconRenderer;
   private float iconSize = 14.0F;
   private int textColor = -1;
   private float fontSize = 10.0F;
   private boolean pressed = false;

   public UIButton(String text) {
      this.text = text != null ? text : "";
      this.height = 24.0F;
      this.radius = 6.0F;
      this.bg = -14672336;
      this.border = 553648127;
      this.borderWidth = 1.0F;
   }

   public UIButton text(String text) {
      this.text = text;
      return this;
   }

   public UIButton icon(UIButton.IconRenderer iconRenderer, float size) {
      this.iconRenderer = iconRenderer;
      this.iconSize = size;
      return this;
   }

   public UIButton textColor(int argb) {
      this.textColor = argb;
      return this;
   }

   public UIButton fontSize(float size) {
      this.fontSize = size;
      return this;
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      float hoverProgress = this.anim("hover", this.hovered ? 1.0F : 0.0F, 20.0F);
      float pressProgress = this.anim("press", this.pressed ? 1.0F : 0.0F, 24.0F);
      GuiTheme theme = GuiTheme.current();
      int baseBg = this.bg != 0 ? this.bg : -14672336;
      int hoverBg = NvgUtil.brighten(baseBg, 0.18F);
      int curBg = NvgUtil.lerpColor(baseBg, hoverBg, hoverProgress);
      NvgUtil.fillRounded(this.x, this.y, this.width, this.height, this.radius, curBg);
      int curBorder = NvgUtil.lerpColor(this.border != 0 ? this.border : 419430399, 1358954495, hoverProgress);
      NvgUtil.strokeRounded(this.x - 0.5F, this.y - 0.5F, this.width + 1.0F, this.height + 1.0F, this.radius, 1.0F, curBorder);
      int drawTextColor = NvgUtil.lerpColor(this.textColor, -1, hoverProgress);
      float textStartX = this.x + this.width * 0.5F;
      if (this.iconRenderer != null) {
         float iconCx = this.text != null && !this.text.isEmpty() ? this.x + 12.0F + this.iconSize * 0.5F : this.x + this.width * 0.5F;
         float iconCy = this.y + this.height * 0.5F;
         this.iconRenderer.render(iconCx, iconCy, this.iconSize, drawTextColor);
         if (this.text != null && !this.text.isEmpty()) {
            textStartX = iconCx + this.iconSize * 0.5F + 8.0F;
            NvgUtil.drawText(this.text, textStartX, this.y + (this.height - this.fontSize) * 0.5F - 0.5F, this.fontSize, drawTextColor, 9);
         }
      } else if (this.text != null && !this.text.isEmpty()) {
         NvgUtil.drawText(this.text, textStartX, this.y + (this.height - this.fontSize) * 0.5F - 0.5F, this.fontSize, drawTextColor, 10);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event) {
      if (this.visible && this.enabled) {
         if (this.contains((float)event.x(), (float)event.y())) {
            int btn = event.buttonInfo().button();
            if (btn == 0) {
               this.pressed = true;
               if (this.clickListener != null) {
                  this.clickListener.accept(event);
                  return true;
               }
            } else if (btn == 1 && this.rightClickListener != null) {
               this.rightClickListener.accept(event);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.pressed = false;
      return super.mouseReleased(event);
   }

   @FunctionalInterface
   public interface IconRenderer {
      void render(float var1, float var2, float var3, int var4);
   }
}
