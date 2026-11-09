package kat.respect.b2.render.mobile;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import kat.respect.b2.render.IRenderer2D;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

public class MobileRenderer2D implements IRenderer2D {
   private final MobileDraw2D draw2D = MobileDraw2D.getInstance();
   private final Deque<Matrix4f> matrixStack = new ArrayDeque<>();
   private final Deque<Integer> scissorSaveStack = new ArrayDeque<>();
   private Matrix4f currentModel = new Matrix4f();
   private float globalAlpha = 1.0F;
   private float globalScale = 1.0F;
   private int scissorDepth = 0;
   private static final Map<String, Integer> textureCache = new ConcurrentHashMap<>();

   public MobileDraw2D getDraw2D() {
      return this.draw2D;
   }

   private int applyAlpha(int argb) {
      if (this.globalAlpha >= 0.999F) {
         return argb;
      } else {
         int a = argb >> 24 & 0xFF;
         int r = argb >> 16 & 0xFF;
         int g = argb >> 8 & 0xFF;
         int b = argb & 0xFF;
         a = Math.max(0, Math.min(255, (int)(a * this.globalAlpha)));
         return a << 24 | r << 16 | g << 8 | b;
      }
   }

   @Override
   public void begin(float screenW, float screenH, float scale, float alpha) {
      this.globalAlpha = Math.max(0.0F, Math.min(1.0F, alpha));
      this.globalScale = scale;
      this.currentModel = new Matrix4f().scale(scale, scale, 1.0F);
      this.matrixStack.clear();
      this.scissorSaveStack.clear();
      this.scissorDepth = 0;
      Matrix4f proj = new Matrix4f().ortho(0.0F, screenW, screenH, 0.0F, -1000.0F, 1000.0F);
      this.draw2D.beginFrame(proj);
   }

   @Override
   public void end() {
      this.draw2D.endFrame();
   }

   @Override
   public void save() {
      this.matrixStack.push(new Matrix4f(this.currentModel));
      this.scissorSaveStack.push(this.scissorDepth);
   }

   @Override
   public void restore() {
      if (!this.matrixStack.isEmpty()) {
         this.currentModel = this.matrixStack.pop();
      }

      if (!this.scissorSaveStack.isEmpty()) {
         for (int targetDepth = this.scissorSaveStack.pop(); this.scissorDepth > targetDepth; this.scissorDepth--) {
            this.draw2D.popScissor();
         }
      }
   }

   @Override
   public void pushScissor(float x, float y, float w, float h) {
      if (!(w <= 0.0F) && !(h <= 0.0F)) {
         this.draw2D.pushScissor(x * this.globalScale, y * this.globalScale, w * this.globalScale, h * this.globalScale);
         this.scissorDepth++;
      }
   }

   @Override
   public void popScissor() {
      if (this.scissorDepth > 0) {
         this.scissorDepth--;
         this.draw2D.popScissor();
      }
   }

   @Override
   public void fillRect(float x, float y, float w, float h, int argb) {
      this.draw2D.rect(this.currentModel, x, y, w, h, 0.0F, this.applyAlpha(argb));
   }

   @Override
   public void fillRounded(float x, float y, float w, float h, float radius, int argb) {
      this.draw2D.rect(this.currentModel, x, y, w, h, radius, this.applyAlpha(argb));
   }

   @Override
   public void fillRoundedTop(float x, float y, float w, float h, float radius, int argb) {
      this.draw2D.rect(this.currentModel, x, y, w, h, radius, radius, 0.0F, 0.0F, this.applyAlpha(argb));
   }

   @Override
   public void fillRoundedVarying(float x, float y, float w, float h, float tl, float tr, float br, float bl, int argb) {
      this.draw2D.rect(this.currentModel, x, y, w, h, tl, tr, br, bl, this.applyAlpha(argb));
   }

   @Override
   public void strokeRect(float x, float y, float w, float h, float lineW, int argb) {
      int c = this.applyAlpha(argb);
      this.fillRect(x, y, w, lineW, c);
      this.fillRect(x, y + h - lineW, w, lineW, c);
      this.fillRect(x, y + lineW, lineW, h - lineW * 2.0F, c);
      this.fillRect(x + w - lineW, y + lineW, lineW, h - lineW * 2.0F, c);
   }

   @Override
   public void strokeRounded(float x, float y, float w, float h, float radius, float lineW, int argb) {
      this.draw2D.outline(this.currentModel, x, y, w, h, radius, lineW, this.applyAlpha(argb));
   }

   @Override
   public void fillCircle(float cx, float cy, float radius, int argb) {
      this.draw2D.rect(this.currentModel, cx - radius, cy - radius, radius * 2.0F, radius * 2.0F, radius, this.applyAlpha(argb));
   }

   @Override
   public void fillGradient(float x, float y, float w, float h, float radius, int topArgb, int bottomArgb, boolean vertical) {
      int cTop = this.applyAlpha(topArgb);
      int cBottom = this.applyAlpha(bottomArgb);
      if (vertical) {
         this.draw2D.rect(this.currentModel, x, y, w, h, radius, radius, radius, radius, cTop, cTop, cBottom, cBottom);
      } else {
         this.draw2D.rect(this.currentModel, x, y, w, h, radius, radius, radius, radius, cTop, cBottom, cBottom, cTop);
      }
   }

   @Override
   public void drawLine(float x1, float y1, float x2, float y2, float lineW, int argb) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      float len = (float)Math.hypot(dx, dy);
      if (!(len <= 0.001F)) {
         float angle = (float)Math.atan2(dy, dx);
         Matrix4f lineModel = new Matrix4f(this.currentModel).translate(x1, y1, 0.0F).rotateZ(angle);
         this.draw2D.rect(lineModel, 0.0F, -lineW * 0.5F, len, lineW, lineW * 0.5F, this.applyAlpha(argb));
      }
   }

   @Override
   public void liquidGlass(float x, float y, float w, float h, float radius, int tint, float refractionStrength) {
      this.draw2D.liquidGlass(this.currentModel, x, y, w, h, radius, this.applyAlpha(tint), refractionStrength);
   }

   @Override
   public void drawShadow(float x, float y, float w, float h, float radius, int shadowArgb) {
      int color = this.applyAlpha(shadowArgb);
      this.draw2D.shadow(this.currentModel, x, y, w, h, radius, 8.0F, color);
   }

   @Override
   public void drawText(String text, float x, float y, float size, int argb, int align) {
      if (text != null && !text.isEmpty()) {
         this.draw2D.text(this.currentModel, text, x, y, size, this.applyAlpha(argb), align);
      }
   }

   @Override
   public void drawTextShadow(String text, float x, float y, float size, int argb, int shadowArgb, int align) {
      if (text != null && !text.isEmpty()) {
         this.draw2D.text(this.currentModel, text, x + 0.75F, y + 0.75F, size, this.applyAlpha(shadowArgb), align);
         this.draw2D.text(this.currentModel, text, x, y, size, this.applyAlpha(argb), align);
      }
   }

   @Override
   public float textWidth(String text, float size) {
      return text != null && !text.isEmpty() ? this.draw2D.getFont().getStringWidth(text, size) : 0.0F;
   }

   @Override
   public void drawChevronDown(float cx, float cy, float size, int argb) {
      int c = this.applyAlpha(argb);
      float half = size * 0.5F;
      float height = size * 0.35F;
      this.drawLine(cx - half, cy - height, cx, cy + height, 1.5F, c);
      this.drawLine(cx, cy + height, cx + half, cy - height, 1.5F, c);
   }

   @Override
   public void drawChevronRight(float cx, float cy, float size, int argb) {
      int c = this.applyAlpha(argb);
      float half = size * 0.5F;
      float width = size * 0.35F;
      this.drawLine(cx - width, cy - half, cx + width, cy, 1.5F, c);
      this.drawLine(cx + width, cy, cx - width, cy + half, 1.5F, c);
   }

   @Override
   public int loadTexture(String resourcePath) {
      if (resourcePath != null && !resourcePath.isEmpty()) {
         Integer cached = textureCache.get(resourcePath);
         if (cached != null) {
            return cached;
         } else {
            try {
               InputStream is = MobileRenderer2D.class.getResourceAsStream(resourcePath);
               if (is == null && !resourcePath.startsWith("/")) {
                  is = MobileRenderer2D.class.getResourceAsStream("/" + resourcePath);
               }

               if (is == null) {
                  return 0;
               } else {
                  BufferedImage img = ImageIO.read(is);
                  is.close();
                  if (img == null) {
                     return 0;
                  } else {
                     int w = img.getWidth();
                     int h = img.getHeight();
                     int[] pixels = new int[w * h];
                     img.getRGB(0, 0, w, h, pixels, 0, w);
                     ByteBuffer buffer = MemoryUtil.memAlloc(w * h * 4);

                     for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                           int pixel = pixels[y * w + x];
                           buffer.put((byte)(pixel >> 16 & 0xFF));
                           buffer.put((byte)(pixel >> 8 & 0xFF));
                           buffer.put((byte)(pixel & 0xFF));
                           buffer.put((byte)(pixel >> 24 & 0xFF));
                        }
                     }

                     buffer.flip();
                     int texId = GL33.glGenTextures();
                     GL33.glBindTexture(3553, texId);
                     GL33.glTexParameteri(3553, 10241, 9729);
                     GL33.glTexParameteri(3553, 10240, 9729);
                     GL33.glTexParameteri(3553, 10242, 33071);
                     GL33.glTexParameteri(3553, 10243, 33071);
                     int prevSkipRows = GL33.glGetInteger(3315);
                     int prevSkipPixels = GL33.glGetInteger(3316);
                     int prevAlignment = GL33.glGetInteger(3317);
                     GL33.glPixelStorei(3315, 0);
                     GL33.glPixelStorei(3316, 0);
                     GL33.glPixelStorei(3317, 1);
                     GL33.glTexImage2D(3553, 0, 32856, w, h, 0, 6408, 5121, buffer);
                     GL33.glPixelStorei(3315, prevSkipRows);
                     GL33.glPixelStorei(3316, prevSkipPixels);
                     GL33.glPixelStorei(3317, prevAlignment);
                     GL33.glBindTexture(3553, 0);
                     MemoryUtil.memFree(buffer);
                     textureCache.put(resourcePath, texId);
                     return texId;
                  }
               }
            } catch (Throwable var13) {
               return 0;
            }
         }
      } else {
         return 0;
      }
   }

   @Override
   public void drawImage(int imgId, float x, float y, float w, float h, float alpha) {
      if (imgId > 0 && !(alpha <= 0.001F)) {
         this.draw2D.image(this.currentModel, imgId, x, y, w, h, 0.0F, alpha);
      }
   }

   @Override
   public void drawImage(int imgId, float x, float y, float w, float h, float u, float v, float uw, float vh, float radius, float alpha) {
      if (imgId > 0 && !(alpha <= 0.001F)) {
         this.draw2D.image(this.currentModel, imgId, x, y, w, h, u, v, uw, vh, radius, alpha);
      }
   }
}
