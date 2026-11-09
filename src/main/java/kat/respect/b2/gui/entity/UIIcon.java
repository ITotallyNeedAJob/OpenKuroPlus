package kat.respect.b2.gui.entity;

import kat.respect.b2.gui.NvgUtil;

public final class UIIcon {
   private UIIcon() {
   }

   public static void drawSearch(float cx, float cy, float size, int argb) {
      float r = size * 0.32F;
      float circleCx = cx - size * 0.08F;
      float circleCy = cy - size * 0.08F;
      NvgUtil.strokeCircle(circleCx, circleCy, r, 1.5F, argb);
      NvgUtil.drawLine(circleCx + r * 0.707F, circleCy + r * 0.707F, cx + size * 0.42F, cy + size * 0.42F, 1.8F, argb);
   }

   public static void drawSearch(long dummyVg, float cx, float cy, float size, int argb) {
      drawSearch(cx, cy, size, argb);
   }

   public static void drawClose(float cx, float cy, float size, int argb) {
      float hs = size * 0.34F;
      NvgUtil.drawLine(cx - hs, cy - hs, cx + hs, cy + hs, 1.6F, argb);
      NvgUtil.drawLine(cx + hs, cy - hs, cx - hs, cy + hs, 1.6F, argb);
   }

   public static void drawClose(long dummyVg, float cx, float cy, float size, int argb) {
      drawClose(cx, cy, size, argb);
   }

   public static void drawCheck(float cx, float cy, float size, int argb) {
      float s = size * 0.38F;
      NvgUtil.drawLine(cx - s * 0.85F, cy + s * 0.05F, cx - s * 0.15F, cy + s * 0.7F, 1.7F, argb);
      NvgUtil.drawLine(cx - s * 0.15F, cy + s * 0.7F, cx + s * 0.9F, cy - s * 0.65F, 1.7F, argb);
   }

   public static void drawCheck(long dummyVg, float cx, float cy, float size, int argb) {
      drawCheck(cx, cy, size, argb);
   }

   public static void drawSword(float cx, float cy, float size, int argb) {
      float s = size * 0.44F;
      float tipX = cx + s * 0.85F;
      float tipY = cy - s * 0.85F;
      float guardCenterX = cx - s * 0.15F;
      float guardCenterY = cy + s * 0.15F;
      float hiltEndX = cx - s * 0.6F;
      float hiltEndY = cy + s * 0.6F;
      float guardSpread = s * 0.35F;
      float g1X = guardCenterX - guardSpread;
      float g1Y = guardCenterY - guardSpread;
      float g2X = guardCenterX + guardSpread;
      float g2Y = guardCenterY + guardSpread;
      NvgUtil.drawLine(guardCenterX, guardCenterY, tipX, tipY, 2.0F, argb);
      NvgUtil.drawLine(g1X, g1Y, g2X, g2Y, 1.8F, argb);
      NvgUtil.drawLine(guardCenterX, guardCenterY, hiltEndX, hiltEndY, 1.6F, argb);
      NvgUtil.fillCircle(hiltEndX - s * 0.08F, hiltEndY + s * 0.08F, 1.3F, argb);
   }

   public static void drawSword(long dummyVg, float cx, float cy, float size, int argb) {
      drawSword(cx, cy, size, argb);
   }

   public static void drawMovement(float cx, float cy, float size, int argb) {
      float s = size * 0.44F;
      float y1 = cy - s * 0.52F;
      float y3 = cy + s * 0.52F;
      NvgUtil.drawLine(cx - s * 0.45F, y1, cx + s * 0.85F, y1, 1.6F, argb);
      NvgUtil.drawLine(cx - s * 0.85F, cy, cx + s * 0.45F, cy, 1.6F, argb);
      NvgUtil.drawLine(cx - s * 0.65F, y3, cx + s * 0.7F, y3, 1.6F, argb);
   }

   public static void drawMovement(long dummyVg, float cx, float cy, float size, int argb) {
      drawMovement(cx, cy, size, argb);
   }

   public static void drawEye(float cx, float cy, float size, int argb) {
      float rx = size * 0.46F;
      float ry = size * 0.28F;
      NvgUtil.strokeCircle(cx, cy, ry * 1.1F, 1.4F, argb);
      NvgUtil.fillCircle(cx, cy, ry * 0.55F, argb);
   }

   public static void drawEye(long dummyVg, float cx, float cy, float size, int argb) {
      drawEye(cx, cy, size, argb);
   }

   public static void drawCube(float cx, float cy, float size, int argb) {
      float s = size * 0.42F;
      NvgUtil.strokeRect(cx - s * 0.7F, cy - s * 0.7F, s * 1.4F, s * 1.4F, 1.3F, argb);
      NvgUtil.drawLine(cx, cy - s * 0.7F, cx, cy + s * 0.7F, 1.2F, argb);
      NvgUtil.drawLine(cx - s * 0.7F, cy, cx + s * 0.7F, cy, 1.2F, argb);
   }

   public static void drawCube(long dummyVg, float cx, float cy, float size, int argb) {
      drawCube(cx, cy, size, argb);
   }

   public static void drawSliders(float cx, float cy, float size, int argb) {
      float w = size * 0.44F;
      float dy = size * 0.28F;
      float knobR = 2.0F;
      float y1 = cy - dy;
      float y2 = cy + dy;
      float k1 = cx - w * 0.3F;
      float k2 = cx + w * 0.3F;
      NvgUtil.drawLine(cx - w, y1, cx + w, y1, 1.4F, argb);
      NvgUtil.fillCircle(k1, y1, knobR, argb);
      NvgUtil.drawLine(cx - w, y2, cx + w, y2, 1.4F, argb);
      NvgUtil.fillCircle(k2, y2, knobR, argb);
   }

   public static void drawSliders(long dummyVg, float cx, float cy, float size, int argb) {
      drawSliders(cx, cy, size, argb);
   }

   public static void drawInterface(float cx, float cy, float size, int argb) {
      float w = size * 0.44F;
      float h = size * 0.34F;
      float headerY = cy - h + size * 0.26F;
      NvgUtil.strokeRounded(cx - w, cy - h, w * 2.0F, h * 2.0F, 2.0F, 1.3F, argb);
      NvgUtil.drawLine(cx - w, headerY, cx + w, headerY, 1.1F, argb);
   }

   public static void drawInterface(long dummyVg, float cx, float cy, float size, int argb) {
      drawInterface(cx, cy, size, argb);
   }

   public static void drawPerson(float cx, float cy, float size, int argb) {
      float headR = size * 0.2F;
      float headY = cy - size * 0.22F;
      NvgUtil.fillCircle(cx, headY, headR, argb);
      float shoulderW = size * 0.34F;
      float shoulderY = cy + size * 0.16F;
      NvgUtil.strokeRounded(cx - shoulderW, shoulderY - size * 0.06F, shoulderW * 2.0F, size * 0.34F, size * 0.16F, 1.5F, argb);
   }

   public static void drawPerson(long dummyVg, float cx, float cy, float size, int argb) {
      drawPerson(cx, cy, size, argb);
   }

   public static void drawFolder(float cx, float cy, float size, int argb) {
      float w = size * 0.44F;
      float h = size * 0.32F;
      NvgUtil.strokeRounded(cx - w, cy - h + 2.0F, w * 2.0F, h * 2.0F - 2.0F, 2.0F, 1.3F, argb);
   }

   public static void drawFolder(long dummyVg, float cx, float cy, float size, int argb) {
      drawFolder(cx, cy, size, argb);
   }

   public static void drawInfo(float cx, float cy, float size, int argb) {
      float r = size * 0.4F;
      NvgUtil.strokeCircle(cx, cy, r, 1.4F, argb);
      NvgUtil.fillCircle(cx, cy - r * 0.4F, 1.1F, argb);
      NvgUtil.drawLine(cx, cy - r * 0.05F, cx, cy + r * 0.45F, 1.5F, argb);
   }

   public static void drawInfo(long dummyVg, float cx, float cy, float size, int argb) {
      drawInfo(cx, cy, size, argb);
   }

   public static void drawWarning(float cx, float cy, float size, int argb) {
      float s = size * 0.42F;
      NvgUtil.strokeRounded(cx - s, cy - s, s * 2.0F, s * 2.0F, 2.0F, 1.5F, argb);
      NvgUtil.drawLine(cx, cy - s * 0.35F, cx, cy + s * 0.15F, 1.5F, argb);
      NvgUtil.fillCircle(cx, cy + s * 0.48F, 1.1F, argb);
   }

   public static void drawWarning(long dummyVg, float cx, float cy, float size, int argb) {
      drawWarning(cx, cy, size, argb);
   }

   public static void drawGear(float cx, float cy, float size, int argb) {
      float r = size * 0.22F;
      float spokeIn = size * 0.22F;
      float spokeOut = size * 0.44F;
      NvgUtil.strokeCircle(cx, cy, r, 1.4F, argb);

      for (int i = 0; i < 6; i++) {
         double angle = i * Math.PI / 3.0;
         float cos = (float)Math.cos(angle);
         float sin = (float)Math.sin(angle);
         NvgUtil.drawLine(cx + spokeIn * cos, cy + spokeIn * sin, cx + spokeOut * cos, cy + spokeOut * sin, 2.0F, argb);
      }
   }

   public static void drawGear(long dummyVg, float cx, float cy, float size, int argb) {
      drawGear(cx, cy, size, argb);
   }

   public static void drawHome(float cx, float cy, float size, int argb) {
      float w = size * 0.44F;
      float stroke = Math.max(1.1F, size * 0.09F);
      float roofY = cy - size * 0.06F;
      NvgUtil.drawLine(cx - w, roofY, cx, cy - size * 0.44F, stroke, argb);
      NvgUtil.drawLine(cx, cy - size * 0.44F, cx + w, roofY, stroke, argb);
      NvgUtil.drawLine(cx - w * 0.72F, roofY, cx - w * 0.72F, cy + size * 0.42F, stroke, argb);
      NvgUtil.drawLine(cx + w * 0.72F, roofY, cx + w * 0.72F, cy + size * 0.42F, stroke, argb);
      NvgUtil.drawLine(cx - w * 0.72F, cy + size * 0.42F, cx + w * 0.72F, cy + size * 0.42F, stroke, argb);
   }

   public static void drawGrid(float cx, float cy, float size, int argb) {
      float cell = size * 0.3F;
      float gap = size * 0.1F;
      float r = cell * 0.28F;

      for (int gx = 0; gx < 2; gx++) {
         for (int gy = 0; gy < 2; gy++) {
            NvgUtil.fillRounded(cx - cell - gap * 0.5F + gx * (cell + gap), cy - cell - gap * 0.5F + gy * (cell + gap), cell, cell, r, argb);
         }
      }
   }

   public static void drawLayers(float cx, float cy, float size, int argb) {
      float w = size * 0.46F;
      float stroke = Math.max(1.1F, size * 0.09F);
      NvgUtil.drawLine(cx - w, cy - size * 0.3F, cx + w, cy - size * 0.3F, stroke, argb);
      NvgUtil.drawLine(cx - w, cy, cx + w, cy, stroke, argb);
      NvgUtil.drawLine(cx - w, cy + size * 0.3F, cx + w, cy + size * 0.3F, stroke, argb);
   }

   public static void drawRefresh(float cx, float cy, float size, int argb) {
      float r = size * 0.34F;
      float stroke = Math.max(1.1F, size * 0.1F);
      NvgUtil.strokeCircle(cx, cy, r, stroke, argb);
      NvgUtil.drawLine(cx + r * 0.25F, cy - r, cx + r + stroke, cy - r - stroke, stroke, argb);
      NvgUtil.drawLine(cx + r + stroke, cy - r - stroke, cx + r * 0.95F, cy - r * 0.29F, stroke, argb);
   }

   public static void drawStar(float cx, float cy, float size, int argb) {
      float r = size * 0.46F;
      float inner = r * 0.24F;
      NvgUtil.drawLine(cx, cy - r, cx, cy + r, 1.8F, argb);
      NvgUtil.drawLine(cx - r, cy, cx + r, cy, 1.8F, argb);
      NvgUtil.drawLine(cx - inner, cy - inner, cx + inner, cy + inner, 1.2F, argb);
      NvgUtil.drawLine(cx - inner, cy + inner, cx + inner, cy - inner, 1.2F, argb);
      NvgUtil.fillCircle(cx, cy, inner * 0.8F, argb);
   }

   public static void drawGlobe(float cx, float cy, float size, int argb) {
      float r = size * 0.4F;
      NvgUtil.strokeCircle(cx, cy, r, 1.3F, argb);
      NvgUtil.drawLine(cx - r, cy, cx + r, cy, 1.2F, argb);
      NvgUtil.strokeCircle(cx, cy, r * 0.48F, 1.1F, argb);
   }

   public static void drawMonitor(float cx, float cy, float size, int argb) {
      float w = size * 0.44F;
      float h = size * 0.3F;
      float screenY = cy - size * 0.08F;
      NvgUtil.strokeRounded(cx - w, screenY - h, w * 2.0F, h * 2.0F, 2.0F, 1.3F, argb);
      NvgUtil.drawLine(cx, screenY + h, cx, cy + size * 0.35F, 1.3F, argb);
      NvgUtil.drawLine(cx - size * 0.22F, cy + size * 0.35F, cx + size * 0.22F, cy + size * 0.35F, 1.3F, argb);
   }

   public static void drawBarChart(float cx, float cy, float size, int argb) {
      float barW = size * 0.18F;
      float r = 1.0F;
      float bY = cy + size * 0.35F;
      NvgUtil.fillRounded(cx - size * 0.35F, bY - size * 0.45F, barW, size * 0.45F, r, argb);
      NvgUtil.fillRounded(cx - barW * 0.5F, bY - size * 0.75F, barW, size * 0.75F, r, argb);
      NvgUtil.fillRounded(cx + size * 0.35F - barW, bY - size * 0.55F, barW, size * 0.55F, r, argb);
   }

   public static void drawExit(float cx, float cy, float size, int argb) {
      float w = size * 0.4F;
      float h = size * 0.42F;
      float stroke = 1.3F;
      NvgUtil.drawLine(cx + w * 0.2F, cy - h, cx - w, cy - h, stroke, argb);
      NvgUtil.drawLine(cx - w, cy - h, cx - w, cy + h, stroke, argb);
      NvgUtil.drawLine(cx - w, cy + h, cx + w * 0.2F, cy + h, stroke, argb);
      NvgUtil.drawLine(cx - w * 0.3F, cy, cx + w, cy, stroke, argb);
      NvgUtil.drawLine(cx + w * 0.6F, cy - h * 0.35F, cx + w, cy, stroke, argb);
      NvgUtil.drawLine(cx + w * 0.6F, cy + h * 0.35F, cx + w, cy, stroke, argb);
   }

   public static void drawPlus(float cx, float cy, float size, int argb) {
      float s = size * 0.35F;
      NvgUtil.drawLine(cx - s, cy, cx + s, cy, 1.4F, argb);
      NvgUtil.drawLine(cx, cy - s, cx, cy + s, 1.4F, argb);
   }

   public static void drawMinus(float cx, float cy, float size, int argb) {
      float s = size * 0.35F;
      NvgUtil.drawLine(cx - s, cy, cx + s, cy, 1.4F, argb);
   }

   public static void drawMaximize(float cx, float cy, float size, int argb) {
      float s = size * 0.35F;
      NvgUtil.strokeRounded(cx - s, cy - s, s * 2.0F, s * 2.0F, 1.5F, 1.3F, argb);
   }
}
