package kat.respect.b2.render.mobile;

import java.nio.ByteBuffer;
import java.util.Stack;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;

public class MobileDraw2D {
   private static MobileDraw2D INSTANCE;
   public static final int CAMERA_UBO_BINDING = 7;
   private static float z = 0.001F;
   private static final float D_Z = 0.001F;
   private final int ubo;
   private final MobileRoundedRect roundedRect;
   private final MobileRoundedOutline roundedOutline;
   private final MobileShadow roundedShadow;
   private final MobileFont mobileFont;
   private final MobileLiquidGlass liquidGlass;
   private final MobileSBPicker sbPicker;
   private final MobileHueSlider hueSlider;
   private final MobileAlphaSlider alphaSlider;
   private final MobileBlur mobileBlur;
   private final MobileRoundedImage roundedImage;
   private final MobileCrossTexture blurResult = new MobileCrossTexture();
   private final Stack<Vector4i> scissorStack = new Stack<>();
   private MobileGlState state;
   private boolean inFrame = false;
   private boolean blurRequested = false;

   public static float nextZ() {
      return z += 0.001F;
   }

   private MobileDraw2D() {
      this.roundedRect = new MobileRoundedRect();
      this.roundedOutline = new MobileRoundedOutline();
      this.roundedShadow = new MobileShadow();
      this.mobileFont = new MobileFont();
      this.liquidGlass = new MobileLiquidGlass();
      this.sbPicker = new MobileSBPicker();
      this.hueSlider = new MobileHueSlider();
      this.alphaSlider = new MobileAlphaSlider();
      this.mobileBlur = new MobileBlur();
      this.roundedImage = new MobileRoundedImage();
      this.ubo = GL33.glGenBuffers();
      GL33.glBindBuffer(35345, this.ubo);
      GL33.glBufferData(35345, 64L, 35048);
      GL33.glBindBufferBase(35345, 7, this.ubo);
      GL33.glBindBuffer(35345, 0);
   }

   public static MobileDraw2D getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new MobileDraw2D();
      }

      return INSTANCE;
   }

   public MobileCrossTexture getBlurResult() {
      return this.blurResult;
   }

   public void cleanup() {
      this.roundedRect.cleanup();
      this.roundedOutline.cleanup();
      this.roundedShadow.cleanup();
      this.mobileFont.cleanup();
      this.liquidGlass.cleanup();
      this.sbPicker.cleanup();
      this.hueSlider.cleanup();
      this.alphaSlider.cleanup();
      this.mobileBlur.cleanup();
      this.roundedImage.cleanup();
      if (this.ubo != 0) {
         GL33.glDeleteBuffers(this.ubo);
      }
   }

   public void computeBlur() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getWindow() != null) {
         int width = mc.getWindow().getWidth();
         int height = mc.getWindow().getHeight();
         if (width > 0 && height > 0) {
            MobileCrossTexture src = MobileCrossTexture.fromMainRenderTarget();
            if (src.glId == -1) {
               this.blurRequested = false;
            } else {
               this.blurResult.glId = this.mobileBlur.blur(src, width, height, 3, 1.0F);
               this.blurRequested = false;
            }
         }
      }
   }

   public void requestBlur() {
      this.blurRequested = true;
   }

   public boolean needsBlur() {
      return this.blurRequested;
   }

   public void beginFrame(Matrix4f proj) {
      if (this.inFrame) {
         this.flush();
      } else {
         this.state = new MobileGlState();
         this.inFrame = true;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getWindow() != null) {
         GL33.glBindFramebuffer(36160, 0);
         GL33.glViewport(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight());
      }

      GL33.glBindSampler(0, 0);
      GL33.glBindSampler(8, 0);
      GL33.glBindSampler(9, 0);
      this.updateProjection(proj);
      z = 0.0F;
      GL33.glDisable(2884);
      GL33.glEnable(3042);
      GL33.glBlendFuncSeparate(770, 771, 1, 771);
      GL33.glDisable(2929);
      GL33.glDepthMask(false);
      GL33.glDisable(3089);
      GL33.glColorMask(true, true, true, true);
   }

   public void updateProjection(Matrix4f proj) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         ByteBuffer buf = stack.malloc(64);
         proj.get(buf);
         GL33.glBindBuffer(35345, this.ubo);
         GL33.glBufferSubData(35345, 0L, buf);
         GL33.glBindBufferBase(35345, 7, this.ubo);
         GL33.glBindBuffer(35345, 0);
      } catch (Throwable var6) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public void endFrame() {
      if (this.inFrame) {
         this.flush();
         if (!this.scissorStack.isEmpty()) {
            this.scissorStack.clear();
            GL33.glDisable(3089);
         }

         GL33.glBindVertexArray(0);
         GL33.glUseProgram(0);
         GL33.glBindBuffer(34962, 0);
         GL33.glBindBuffer(35345, 0);
         GL33.glBindTexture(3553, 0);
         if (this.state != null) {
            this.state.restore();
            this.state = null;
         }

         this.inFrame = false;
      }
   }

   public void flush() {
      this.roundedShadow.flush();
      this.roundedRect.flush();
      if (this.blurResult.glId > 0) {
         this.liquidGlass.flush(this.blurResult.glId);
      } else {
         this.liquidGlass.flush();
      }

      this.sbPicker.flush();
      this.hueSlider.flush();
      this.alphaSlider.flush();
      this.roundedOutline.flush();
      this.mobileFont.flush();
   }

   public void rect(Matrix4f model, float x, float y, float w, float h, float radius, int color) {
      this.roundedRect.addRect(model, x, y, w, h, radius, color, nextZ());
   }

   public void rect(Matrix4f model, float x, float y, float w, float h, float r1, float r2, float r3, float r4, int color) {
      this.roundedRect.addRect(model, x, y, w, h, r1, r2, r3, r4, color, nextZ());
   }

   public void rect(Matrix4f model, float x, float y, float w, float h, float r1, float r2, float r3, float r4, int c1, int c2, int c3, int c4) {
      this.roundedRect.addRect(model, x, y, w, h, r1, r2, r3, r4, c1, c2, c3, c4, nextZ());
   }

   public void liquidGlass(Matrix4f model, float x, float y, float w, float h, float radius, int tint, float refractionStrength) {
      this.liquidGlass.add(model, x, y, w, h, radius, radius, radius, radius, tint, refractionStrength, nextZ());
   }

   public void sbPicker(Matrix4f model, float x, float y, float w, float h, float hue) {
      this.sbPicker.picker(model, x, y, w, h, hue, nextZ());
   }

   public void hueSlider(Matrix4f model, float x, float y, float w, float h) {
      this.hueSlider.slider(model, x, y, w, h, nextZ());
   }

   public void alphaSlider(Matrix4f model, float x, float y, float w, float h, float r, float g, float b) {
      this.alphaSlider.slider(model, x, y, w, h, r, g, b, nextZ());
   }

   public void outline(Matrix4f model, float x, float y, float w, float h, float radius, float thickness, int color) {
      this.roundedOutline.addOutline(model, x, y, w, h, radius, thickness, color, nextZ());
   }

   public void shadow(Matrix4f model, float x, float y, float w, float h, float radius, float shadowRadius, int color) {
      this.roundedShadow.addShadow(model, x, y, w, h, radius, shadowRadius, color, nextZ());
   }

   public void image(Matrix4f model, int textureId, float x, float y, float w, float h, float radius, float alpha) {
      this.flush();
      this.roundedImage.draw(model, textureId, x, y, w, h, radius, alpha, nextZ());
   }

   public void image(Matrix4f model, int textureId, float x, float y, float w, float h, float u, float v, float uw, float vh, float radius, float alpha) {
      this.flush();
      this.roundedImage.draw(model, textureId, x, y, w, h, u, v, uw, vh, radius, alpha, nextZ());
   }

   public void text(Matrix4f model, String text, float x, float y, float size, int color, int align) {
      this.mobileFont.drawString(model, text, x, y, size, color, align, nextZ());
   }

   public MobileFont getFont() {
      return this.mobileFont;
   }

   public void pushScissor(float x, float y, float width, float height) {
      this.flush();
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getWindow() != null) {
         double scale = mc.getWindow().getGuiScale();
         int sx = (int)Math.floor(x * scale);
         int sy = (int)Math.floor((mc.getWindow().getGuiScaledHeight() - (y + height)) * scale);
         int sw = (int)Math.ceil(width * scale);
         int sh = (int)Math.ceil(height * scale);
         Vector4i current = new Vector4i(sx, sy, sw, sh);
         if (!this.scissorStack.isEmpty()) {
            current = intersect(this.scissorStack.peek(), current);
         }

         this.scissorStack.push(current);
         GL33.glEnable(3089);
         GL33.glScissor(current.x, current.y, current.z, current.w);
      }
   }

   public void popScissor() {
      this.flush();
      if (!this.scissorStack.isEmpty()) {
         this.scissorStack.pop();
         if (!this.scissorStack.isEmpty()) {
            Vector4i top = this.scissorStack.peek();
            GL33.glScissor(top.x, top.y, top.z, top.w);
         } else {
            GL33.glDisable(3089);
         }
      }
   }

   private static Vector4i intersect(Vector4i a, Vector4i b) {
      int ax1 = a.x;
      int ay1 = a.y;
      int ax2 = a.x + a.z;
      int ay2 = a.y + a.w;
      int bx1 = b.x;
      int by1 = b.y;
      int bx2 = b.x + b.z;
      int by2 = b.y + b.w;
      int ix1 = Math.max(ax1, bx1);
      int iy1 = Math.max(ay1, by1);
      int ix2 = Math.min(ax2, bx2);
      int iy2 = Math.min(ay2, by2);
      int iw = Math.max(0, ix2 - ix1);
      int ih = Math.max(0, iy2 - iy1);
      return new Vector4i(ix1, iy1, iw, ih);
   }
}
