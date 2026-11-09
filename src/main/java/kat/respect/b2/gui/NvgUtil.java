package kat.respect.b2.gui;

import kat.respect.b2.render.Renderer2D;

public final class NvgUtil {
   public static final int ALIGN_LEFT = 1;
   public static final int ALIGN_CENTER = 2;
   public static final int ALIGN_RIGHT = 4;
   public static final int ALIGN_TOP = 8;
   public static final int ALIGN_MIDDLE = 16;
   public static final int ALIGN_BOTTOM = 32;
   public static final int ALIGN_LEFT_TOP = 9;
   public static final int ALIGN_CENTER_TOP = 10;
   public static final int ALIGN_RIGHT_TOP = 12;
   public static final int ALIGN_LEFT_MIDDLE = 17;
   public static final int ALIGN_CENTER_MIDDLE = 18;
   public static final int ALIGN_RIGHT_MIDDLE = 20;

   private NvgUtil() {
   }

   public static int toArgb(int argb) {
      return argb;
   }

   public static int withAlpha(int argb, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | argb & 16777215;
   }

   public static int withAlpha(int argb, float alphaMul) {
      int a = argb >> 24 & 0xFF;
      int newA = Math.round(a * Math.max(0.0F, Math.min(1.0F, alphaMul)));
      return newA << 24 | argb & 16777215;
   }

   public static int brighten(int argb, float amount) {
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      r = Math.min(255, (int)(r + (255 - r) * amount));
      g = Math.min(255, (int)(g + (255 - g) * amount));
      b = Math.min(255, (int)(b + (255 - b) * amount));
      return (argb >> 24 & 0xFF) << 24 | r << 16 | g << 8 | b;
   }

   public static int darken(int argb, float amount) {
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      r = Math.max(0, (int)(r * (1.0F - amount)));
      g = Math.max(0, (int)(g * (1.0F - amount)));
      b = Math.max(0, (int)(b * (1.0F - amount)));
      return (argb >> 24 & 0xFF) << 24 | r << 16 | g << 8 | b;
   }

   public static int lerpColor(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int aa = a >> 24 & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      int ba = b >> 24 & 0xFF;
      return (int)(aa + (ba - aa) * t) << 24 | (int)(ar + (br - ar) * t) << 16 | (int)(ag + (bg - ag) * t) << 8 | (int)(ab + (bb - ab) * t);
   }

   public static void save() {
      Renderer2D.get().save();
   }

   public static void save(long dummyVg) {
      save();
   }

   public static void restore() {
      Renderer2D.get().restore();
   }

   public static void restore(long dummyVg) {
      restore();
   }

   public static void intersectScissor(float x, float y, float w, float h) {
      Renderer2D.get().pushScissor(x, y, w, h);
   }

   public static void intersectScissor(long dummyVg, float x, float y, float w, float h) {
      intersectScissor(x, y, w, h);
   }

   public static void scissor(float x, float y, float w, float h) {
      intersectScissor(x, y, w, h);
   }

   public static void scissor(long dummyVg, float x, float y, float w, float h) {
      intersectScissor(x, y, w, h);
   }

   public static void resetScissor() {
      Renderer2D.get().popScissor();
   }

   public static void resetScissor(long dummyVg) {
      resetScissor();
   }

   public static void fillRect(float x, float y, float w, float h, int argb) {
      Renderer2D.get().fillRect(x, y, w, h, argb);
   }

   public static void fillRect(long dummyVg, float x, float y, float w, float h, int argb) {
      fillRect(x, y, w, h, argb);
   }

   public static void fillRounded(float x, float y, float w, float h, float r, int argb) {
      Renderer2D.get().fillRounded(x, y, w, h, r, argb);
   }

   public static void fillRounded(long dummyVg, float x, float y, float w, float h, float r, int argb) {
      fillRounded(x, y, w, h, r, argb);
   }

   public static void fillRoundedTop(float x, float y, float w, float h, float r, int argb) {
      Renderer2D.get().fillRoundedTop(x, y, w, h, r, argb);
   }

   public static void fillRoundedTop(long dummyVg, float x, float y, float w, float h, float r, int argb) {
      fillRoundedTop(x, y, w, h, r, argb);
   }

   public static void fillRoundedBottom(float x, float y, float w, float h, float r, int argb) {
      Renderer2D.get().fillRoundedVarying(x, y, w, h, 0.0F, 0.0F, r, r, argb);
   }

   public static void fillRoundedBottom(long dummyVg, float x, float y, float w, float h, float r, int argb) {
      fillRoundedBottom(x, y, w, h, r, argb);
   }

   public static void fillRoundedVarying(float x, float y, float w, float h, float tl, float tr, float br, float bl, int argb) {
      Renderer2D.get().fillRoundedVarying(x, y, w, h, tl, tr, br, bl, argb);
   }

   public static void fillRoundedVarying(long dummyVg, float x, float y, float w, float h, float tl, float tr, float br, float bl, int argb) {
      fillRoundedVarying(x, y, w, h, tl, tr, br, bl, argb);
   }

   public static void strokeRect(float x, float y, float w, float h, float lineW, int argb) {
      Renderer2D.get().strokeRect(x, y, w, h, lineW, argb);
   }

   public static void strokeRect(long dummyVg, float x, float y, float w, float h, float lineW, int argb) {
      strokeRect(x, y, w, h, lineW, argb);
   }

   public static void strokeRounded(float x, float y, float w, float h, float r, float lineW, int argb) {
      Renderer2D.get().strokeRounded(x, y, w, h, r, lineW, argb);
   }

   public static void strokeRounded(long dummyVg, float x, float y, float w, float h, float r, float lineW, int argb) {
      strokeRounded(x, y, w, h, r, lineW, argb);
   }

   public static void fillCircle(float cx, float cy, float r, int argb) {
      Renderer2D.get().fillCircle(cx, cy, r, argb);
   }

   public static void fillCircle(long dummyVg, float cx, float cy, float r, int argb) {
      fillCircle(cx, cy, r, argb);
   }

   public static void strokeCircle(float cx, float cy, float r, float lineW, int argb) {
      Renderer2D.get().strokeRounded(cx - r, cy - r, r * 2.0F, r * 2.0F, r, lineW, argb);
   }

   public static void strokeCircle(long dummyVg, float cx, float cy, float r, float lineW, int argb) {
      strokeCircle(cx, cy, r, lineW, argb);
   }

   public static void drawLine(float x1, float y1, float x2, float y2, float lineW, int argb) {
      Renderer2D.get().drawLine(x1, y1, x2, y2, lineW, argb);
   }

   public static void drawLine(long dummyVg, float x1, float y1, float x2, float y2, float lineW, int argb) {
      drawLine(x1, y1, x2, y2, lineW, argb);
   }

   public static void fillGradient(float x, float y, float w, float h, float r, int c1, int c2, boolean vertical) {
      Renderer2D.get().fillGradient(x, y, w, h, r, c1, c2, vertical);
   }

   public static void fillGradient(float x, float y, float w, float h, int c1, int c2, boolean vertical) {
      fillGradient(x, y, w, h, 0.0F, c1, c2, vertical);
   }

   public static void fillGradient(long dummyVg, float x, float y, float w, float h, float r, int c1, int c2, boolean vertical) {
      fillGradient(x, y, w, h, r, c1, c2, vertical);
   }

   public static void fillGradient(long dummyVg, float x, float y, float w, float h, boolean vertical, int c1, int c2) {
      fillGradient(x, y, w, h, 0.0F, c1, c2, vertical);
   }

   public static void drawShadow(float x, float y, float w, float h, float radius, int shadowArgb) {
      Renderer2D.get().drawShadow(x, y, w, h, radius, shadowArgb);
   }

   public static void drawShadow(long dummyVg, float x, float y, float w, float h, float radius, int shadowArgb) {
      drawShadow(x, y, w, h, radius, shadowArgb);
   }

   public static void drawText(String text, float x, float y, float size, int argb, int align) {
      Renderer2D.get().drawText(text, x, y, size, argb, align);
   }

   public static void drawText(long dummyVg, String text, float x, float y, float size, int argb, int align) {
      drawText(text, x, y, size, argb, align);
   }

   public static void drawTextShadow(String text, float x, float y, float size, int argb, int shadowArgb, int align) {
      Renderer2D.get().drawTextShadow(text, x, y, size, argb, shadowArgb, align);
   }

   public static void drawTextShadow(long dummyVg, String text, float x, float y, float size, int argb, int shadowArgb, int align) {
      drawTextShadow(text, x, y, size, argb, shadowArgb, align);
   }

   public static float textWidth(String text, float size) {
      return Renderer2D.get().textWidth(text, size);
   }

   public static float textWidth(long dummyVg, String text, float size) {
      return textWidth(text, size);
   }

   public static void drawChevronDown(float cx, float cy, float size, int argb) {
      Renderer2D.get().drawChevronDown(cx, cy, size, argb);
   }

   public static void drawChevronDown(long dummyVg, float cx, float cy, float size, int argb) {
      drawChevronDown(cx, cy, size, argb);
   }

   public static void drawChevronRight(float cx, float cy, float size, int argb) {
      Renderer2D.get().drawChevronRight(cx, cy, size, argb);
   }

   public static void drawChevronRight(long dummyVg, float cx, float cy, float size, int argb) {
      drawChevronRight(cx, cy, size, argb);
   }

   public static void liquidGlass(float x, float y, float w, float h, float radius, int tint, float refractionStrength) {
      Renderer2D.get().liquidGlass(x, y, w, h, radius, tint, refractionStrength);
   }

   public static void liquidGlass(long dummyVg, float x, float y, float w, float h, float radius, int tint, float refractionStrength) {
      liquidGlass(x, y, w, h, radius, tint, refractionStrength);
   }

   public static int loadTexture(String resourcePath) {
      return Renderer2D.get().loadTexture(resourcePath);
   }

   public static int loadTexture(long dummyVg, String resourcePath) {
      return loadTexture(resourcePath);
   }

   public static void drawImage(int imgId, float x, float y, float w, float h, float alpha) {
      Renderer2D.get().drawImage(imgId, x, y, w, h, alpha);
   }

   public static void drawImage(int imgId, float x, float y, float w, float h, float u, float v, float uw, float vh, float radius, float alpha) {
      Renderer2D.get().drawImage(imgId, x, y, w, h, u, v, uw, vh, radius, alpha);
   }

   public static void drawImage(long dummyVg, int imgId, float x, float y, float w, float h, float alpha) {
      drawImage(imgId, x, y, w, h, alpha);
   }

   public static void drawImage(long dummyVg, int imgId, float x, float y, float w, float h, float u, float v, float uw, float vh, float radius, float alpha) {
      drawImage(imgId, x, y, w, h, u, v, uw, vh, radius, alpha);
   }

   public static void drawPlayerHead(int textureId, float x, float y, float size, float radius) {
      if (textureId > 0) {
         drawImage(textureId, x, y, size, size, 0.125F, 0.125F, 0.125F, 0.125F, radius, 1.0F);
         drawImage(textureId, x, y, size, size, 0.625F, 0.125F, 0.125F, 0.125F, radius, 1.0F);
      }
   }
}
