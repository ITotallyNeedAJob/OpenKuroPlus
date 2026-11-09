package kat.respect.b2.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RenderUtil {
   private RenderUtil() {
   }

   public static int alpha(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }

   public static int brighten(int color, float amount) {
      int a = color >> 24 & 0xFF;
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      r = Math.min(255, (int)(r + (255 - r) * amount));
      g = Math.min(255, (int)(g + (255 - g) * amount));
      b = Math.min(255, (int)(b + (255 - b) * amount));
      return a << 24 | r << 16 | g << 8 | b;
   }

   public static int darken(int color, float amount) {
      int a = color >> 24 & 0xFF;
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      r = Math.max(0, (int)(r * (1.0F - amount)));
      g = Math.max(0, (int)(g * (1.0F - amount)));
      b = Math.max(0, (int)(b * (1.0F - amount)));
      return a << 24 | r << 16 | g << 8 | b;
   }

   public static void fillRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
      g.fill(x, y, x + w, y + h, color);
   }

   public static void drawHLine(GuiGraphicsExtractor g, int x, int y, int w, int color) {
      if (w > 0) {
         g.fill(x, y, x + w, y + 1, color);
      }
   }

   public static void drawVLine(GuiGraphicsExtractor g, int x, int y, int h, int color) {
      if (h > 0) {
         g.fill(x, y, x + 1, y + h, color);
      }
   }

   public static void fillRounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
      if (r > 0 && w >= r * 2 && h >= r * 2) {
         g.fill(x + r, y, x + w - r, y + r, color);
         g.fill(x, y + r, x + w, y + h - r, color);
         g.fill(x + r, y + h - r, x + w - r, y + h, color);

         for (int i = 0; i < r; i++) {
            int inset = cornerInset(r, i);
            int yTop = y + i;
            int yBot = y + h - i - 1;
            g.fill(x + inset, yTop, x + r, yTop + 1, color);
            g.fill(x + w - r, yTop, x + w - inset, yTop + 1, color);
            g.fill(x + inset, yBot, x + r, yBot + 1, color);
            g.fill(x + w - r, yBot, x + w - inset, yBot + 1, color);
         }
      } else {
         fillRect(g, x, y, w, h, color);
      }
   }

   public static void fillRoundedTop(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
      if (r > 0 && w >= r * 2) {
         g.fill(x + r, y, x + w - r, y + r, color);
         g.fill(x, y + r, x + w, y + h, color);

         for (int i = 0; i < r; i++) {
            int inset = cornerInset(r, i);
            int yTop = y + i;
            g.fill(x + inset, yTop, x + r, yTop + 1, color);
            g.fill(x + w - r, yTop, x + w - inset, yTop + 1, color);
         }
      } else {
         fillRect(g, x, y, w, h, color);
      }
   }

   private static int cornerInset(int r, int row) {
      double dy = r - row - 0.5;
      double dx = Math.sqrt(Math.max(0.0, r * r - dy * dy));
      return (int)Math.ceil(r - dx);
   }

   public static void drawShadow(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int shadowColor) {
      for (int i = radius; i > 0; i--) {
         int alpha = (shadowColor >> 24 & 0xFF) / (radius + 1);
         int c = alpha(shadowColor, alpha);
         g.fill(x - i, y - i, x + w + i, y - i + 1, c);
         g.fill(x - i, y + h + i - 1, x + w + i, y + h + i, c);
         g.fill(x - i, y - i, x - i + 1, y + h + i, c);
         g.fill(x + w + i - 1, y - i, x + w + i, y + h + i, c);
      }
   }

   public static void fillGradientVertical(GuiGraphicsExtractor g, int x, int y, int w, int h, int top, int bottom) {
      for (int i = 0; i < h; i++) {
         float t = (float)i / h;
         int c = lerpColor(top, bottom, t);
         g.fill(x, y + i, x + w, y + i + 1, c);
      }
   }

   public static void fillGradientHorizontal(GuiGraphicsExtractor g, int x, int y, int w, int h, int left, int right) {
      for (int i = 0; i < w; i++) {
         float t = (float)i / w;
         int c = lerpColor(left, right, t);
         g.fill(x + i, y, x + i + 1, y + h, c);
      }
   }

   private static int lerpColor(int a, int b, float t) {
      int aa = a >> 24 & 0xFF;
      int ab = b >> 24 & 0xFF;
      int ar = a >> 16 & 0xFF;
      int br = b >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int abl = a & 0xFF;
      int bbl = b & 0xFF;
      int ra = (int)(aa + (ab - aa) * t);
      int rr = (int)(ar + (br - ar) * t);
      int rg = (int)(ag + (bg - ag) * t);
      int rb = (int)(abl + (bbl - abl) * t);
      return ra << 24 | rr << 16 | rg << 8 | rb;
   }

   public static void drawString(GuiGraphicsExtractor g, String text, int x, int y, int color, boolean shadow) {
      Font font = Minecraft.getInstance().font;
      g.text(font, text, x, y, color, shadow);
   }

   public static void drawCenteredString(GuiGraphicsExtractor g, String text, int x, int y, int w, int color, boolean shadow) {
      Font font = Minecraft.getInstance().font;
      int tw = font.width(text);
      g.text(font, text, x + (w - tw) / 2, y, color, shadow);
   }

   public static void rowBackground(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean hovered) {
      fillRounded(g, x, y, w, h, 4, hovered ? -266460878 : -267053278);
   }

   public static void rowLabel(GuiGraphicsExtractor g, String text, int x, int y, int color) {
      drawString(g, text, x, y, color, true);
   }

   public static void rowValue(GuiGraphicsExtractor g, String text, int x, int y, int w, int color) {
      Font font = Minecraft.getInstance().font;
      int tw = font.width(text);
      g.text(font, text, x + w - tw - 6, y, color, true);
   }
}
