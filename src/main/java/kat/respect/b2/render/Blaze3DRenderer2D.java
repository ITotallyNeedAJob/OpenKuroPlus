package kat.respect.b2.render;

import java.util.Stack;
import kat.respect.b2.gui.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Blaze3DRenderer2D implements IRenderer2D {
   private GuiGraphicsExtractor graphics;
   private float currentScale = 1.0F;
   private float currentAlpha = 1.0F;
   private int scissorDepth = 0;
   private final Stack<Integer> saveStack = new Stack<>();

   public void setGraphics(GuiGraphicsExtractor graphics) {
      this.graphics = graphics;
   }

   public GuiGraphicsExtractor getGraphics() {
      return this.graphics;
   }

   private int applyAlpha(int argb) {
      if (this.currentAlpha >= 1.0F) {
         return argb;
      } else {
         int a = argb >> 24 & 0xFF;
         int scaledA = (int)(a * this.currentAlpha);
         return scaledA << 24 | argb & 16777215;
      }
   }

   @Override
   public void begin(float screenW, float screenH, float scale, float alpha) {
      this.currentScale = scale;
      this.currentAlpha = alpha;
      this.scissorDepth = 0;
      this.saveStack.clear();
      if (this.graphics != null) {
         this.graphics.pose().pushMatrix();
         this.graphics.pose().scale(scale, scale);
      }
   }

   @Override
   public void end() {
      while (this.scissorDepth > 0) {
         this.popScissor();
      }

      this.saveStack.clear();
      if (this.graphics != null) {
         this.graphics.pose().popMatrix();
      }

      this.currentScale = 1.0F;
      this.currentAlpha = 1.0F;
   }

   @Override
   public void save() {
      this.saveStack.push(this.scissorDepth);
   }

   @Override
   public void restore() {
      if (!this.saveStack.isEmpty()) {
         int targetDepth = this.saveStack.pop();

         while (this.scissorDepth > targetDepth) {
            this.popScissor();
         }
      }
   }

   @Override
   public void pushScissor(float x, float y, float w, float h) {
      if (this.graphics != null && !(w <= 0.0F) && !(h <= 0.0F)) {
         int x1 = (int)Math.floor(x);
         int y1 = (int)Math.floor(y);
         int x2 = (int)Math.ceil(x + w);
         int y2 = (int)Math.ceil(y + h);
         this.scissorDepth++;
         this.graphics.enableScissor(x1, y1, x2, y2);
      }
   }

   @Override
   public void popScissor() {
      if (this.graphics != null && this.scissorDepth > 0) {
         this.scissorDepth--;
         this.graphics.disableScissor();
      }
   }

   @Override
   public void fillRect(float x, float y, float w, float h, int argb) {
      if (this.graphics != null && !(w <= 0.0F) && !(h <= 0.0F)) {
         RenderUtil.fillRect(this.graphics, (int)x, (int)y, (int)w, (int)h, this.applyAlpha(argb));
      }
   }

   @Override
   public void fillRounded(float x, float y, float w, float h, float radius, int argb) {
      if (this.graphics != null && !(w <= 0.0F) && !(h <= 0.0F)) {
         RenderUtil.fillRounded(this.graphics, (int)x, (int)y, (int)w, (int)h, (int)radius, this.applyAlpha(argb));
      }
   }

   @Override
   public void fillRoundedTop(float x, float y, float w, float h, float radius, int argb) {
      if (this.graphics != null && !(w <= 0.0F) && !(h <= 0.0F)) {
         RenderUtil.fillRoundedTop(this.graphics, (int)x, (int)y, (int)w, (int)h, (int)radius, this.applyAlpha(argb));
      }
   }

   @Override
   public void fillRoundedVarying(float x, float y, float w, float h, float tl, float tr, float br, float bl, int argb) {
      if (this.graphics != null && !(w <= 0.0F) && !(h <= 0.0F)) {
         RenderUtil.fillRounded(this.graphics, (int)x, (int)y, (int)w, (int)h, (int)Math.max(tl, tr), this.applyAlpha(argb));
      }
   }

   @Override
   public void strokeRect(float x, float y, float w, float h, float lineW, int argb) {
      if (this.graphics != null && !(w <= 0.0F) && !(h <= 0.0F)) {
         int c = this.applyAlpha(argb);
         int lw = Math.max(1, (int)lineW);
         RenderUtil.fillRect(this.graphics, (int)x, (int)y, (int)w, lw, c);
         RenderUtil.fillRect(this.graphics, (int)x, (int)(y + h - lw), (int)w, lw, c);
         RenderUtil.fillRect(this.graphics, (int)x, (int)(y + lw), lw, (int)(h - lw * 2), c);
         RenderUtil.fillRect(this.graphics, (int)(x + w - lw), (int)(y + lw), lw, (int)(h - lw * 2), c);
      }
   }

   @Override
   public void strokeRounded(float x, float y, float w, float h, float radius, float lineW, int argb) {
      if (this.graphics != null && !(w <= 0.0F) && !(h <= 0.0F)) {
         int c = this.applyAlpha(argb);
         int lw = Math.max(1, (int)lineW);
         RenderUtil.fillRect(this.graphics, (int)(x + radius), (int)y, (int)(w - radius * 2.0F), lw, c);
         RenderUtil.fillRect(this.graphics, (int)(x + radius), (int)(y + h - lw), (int)(w - radius * 2.0F), lw, c);
         RenderUtil.fillRect(this.graphics, (int)x, (int)(y + radius), lw, (int)(h - radius * 2.0F), c);
         RenderUtil.fillRect(this.graphics, (int)(x + w - lw), (int)(y + radius), lw, (int)(h - radius * 2.0F), c);
      }
   }

   @Override
   public void fillCircle(float cx, float cy, float radius, int argb) {
      if (this.graphics != null && !(radius <= 0.0F)) {
         int r = (int)radius;
         RenderUtil.fillRounded(this.graphics, (int)(cx - r), (int)(cy - r), r * 2, r * 2, r, this.applyAlpha(argb));
      }
   }

   @Override
   public void fillGradient(float x, float y, float w, float h, float radius, int topArgb, int bottomArgb, boolean vertical) {
      if (this.graphics != null && !(w <= 0.0F) && !(h <= 0.0F)) {
         int c1 = this.applyAlpha(topArgb);
         int c2 = this.applyAlpha(bottomArgb);
         if (vertical) {
            RenderUtil.fillGradientVertical(this.graphics, (int)x, (int)y, (int)w, (int)h, c1, c2);
         } else {
            RenderUtil.fillGradientHorizontal(this.graphics, (int)x, (int)y, (int)w, (int)h, c1, c2);
         }
      }
   }

   @Override
   public void drawShadow(float x, float y, float w, float h, float radius, int shadowArgb) {
      if (this.graphics != null && !(w <= 0.0F) && !(h <= 0.0F)) {
         RenderUtil.drawShadow(this.graphics, (int)x, (int)y, (int)w, (int)h, (int)radius, this.applyAlpha(shadowArgb));
      }
   }

   @Override
   public void drawText(String text, float x, float y, float size, int argb, int align) {
      if (this.graphics != null && text != null && !text.isEmpty()) {
         Font font = Minecraft.getInstance().font;
         int tw = font.width(text);
         float drawX = x;
         if ((align & 2) != 0) {
            drawX = x - tw / 2.0F;
         } else if ((align & 4) != 0) {
            drawX = x - tw;
         }

         float drawY = y;
         if ((align & 16) != 0) {
            drawY = y - 9.0F / 2.0F;
         } else if ((align & 32) != 0) {
            drawY = y - 9.0F;
         }

         this.graphics.text(font, text, (int)drawX, (int)drawY, this.applyAlpha(argb), false);
      }
   }

   @Override
   public void drawTextShadow(String text, float x, float y, float size, int argb, int shadowArgb, int align) {
      if (this.graphics != null && text != null && !text.isEmpty()) {
         this.drawText(text, x + 1.0F, y + 1.0F, size, shadowArgb, align);
         this.drawText(text, x, y, size, argb, align);
      }
   }

   @Override
   public float textWidth(String text, float size) {
      if (text != null && !text.isEmpty()) {
         Font font = Minecraft.getInstance().font;
         return font.width(text);
      } else {
         return 0.0F;
      }
   }

   @Override
   public void drawChevronDown(float cx, float cy, float size, int argb) {
      if (this.graphics != null) {
         int c = this.applyAlpha(argb);
         int s = (int)size;

         for (int i = 0; i < s; i++) {
            RenderUtil.fillRect(this.graphics, (int)(cx - s + i), (int)(cy - s / 2 + i), (s - i) * 2, 1, c);
         }
      }
   }

   @Override
   public void drawChevronRight(float cx, float cy, float size, int argb) {
      if (this.graphics != null) {
         int c = this.applyAlpha(argb);
         int s = (int)size;

         for (int i = 0; i < s; i++) {
            RenderUtil.fillRect(this.graphics, (int)(cx - s / 2 + i), (int)(cy - s + i), 1, (s - i) * 2, c);
         }
      }
   }

   @Override
   public void drawLine(float x1, float y1, float x2, float y2, float lineW, int argb) {
      if (this.graphics != null) {
         float dx = x2 - x1;
         float dy = y2 - y1;
         float len = (float)Math.hypot(dx, dy);
         if (!(len <= 0.0F)) {
            float minX = Math.min(x1, x2);
            float minY = Math.min(y1, y2);
            float w = Math.max(Math.abs(dx), lineW);
            float h = Math.max(Math.abs(dy), lineW);
            this.fillRect(minX, minY, w, h, argb);
         }
      }
   }

   @Override
   public int loadTexture(String resourcePath) {
      return 0;
   }

   @Override
   public void drawImage(int imgId, float x, float y, float w, float h, float alpha) {
   }
}
