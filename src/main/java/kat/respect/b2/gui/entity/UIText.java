package kat.respect.b2.gui.entity;

import kat.respect.b2.gui.NvgUtil;

public class UIText extends UIEntity {
   private String text = "";
   private float fontSize = 10.0F;
   private int color = -1;
   private int align = 9;
   private boolean shadow = false;
   private boolean truncate = true;
   private String cachedDisplay = "";
   private String lastInput = "";
   private float lastCheckW = -1.0F;
   private boolean centerVertically = false;

   public UIText(String text) {
      this.text = text != null ? text : "";
   }

   public UIText text(String text) {
      this.text = text != null ? text : "";
      return this;
   }

   public String getText() {
      return this.text;
   }

   public UIText size(float fontSize) {
      this.fontSize = fontSize;
      return this;
   }

   public UIText color(int argb) {
      this.color = argb;
      return this;
   }

   public UIText align(int align) {
      this.align = align;
      return this;
   }

   public UIText shadow(boolean shadow) {
      this.shadow = shadow;
      return this;
   }

   public UIText truncate(boolean truncate) {
      this.truncate = truncate;
      return this;
   }

   public UIText centerY() {
      this.centerVertically = true;
      return this;
   }

   @Override
   protected void renderSelf(float mouseX, float mouseY) {
      if (this.text != null && !this.text.isEmpty()) {
         float drawY = this.y;
         if (this.centerVertically && this.height > 0.0F) {
            drawY = this.y + Math.max(0.0F, (this.height - this.fontSize) * 0.5F);
         }

         String toDraw = this.text;
         if (this.truncate && this.width > 10.0F) {
            if (!this.text.equals(this.lastInput) || Math.abs(this.width - this.lastCheckW) > 1.0F) {
               this.lastInput = this.text;
               this.lastCheckW = this.width;
               float currentW = NvgUtil.textWidth(this.text, this.fontSize);
               if (!(currentW > this.width)) {
                  this.cachedDisplay = this.text;
               } else {
                  String el = "...";
                  float elW = NvgUtil.textWidth(el, this.fontSize);
                  StringBuilder sb = new StringBuilder();

                  for (int i = 0; i < this.text.length(); i++) {
                     sb.append(this.text.charAt(i));
                     if (NvgUtil.textWidth(sb.toString(), this.fontSize) + elW > this.width) {
                        if (sb.length() > 1) {
                           sb.setLength(sb.length() - 1);
                        }

                        sb.append(el);
                        break;
                     }
                  }

                  this.cachedDisplay = sb.toString();
               }
            }

            toDraw = this.cachedDisplay;
         }

         if (this.shadow) {
            NvgUtil.drawTextShadow(toDraw, this.x, drawY, this.fontSize, this.color, Integer.MIN_VALUE, this.align);
         } else {
            NvgUtil.drawText(toDraw, this.x, drawY, this.fontSize, this.color, this.align);
         }
      }
   }
}
