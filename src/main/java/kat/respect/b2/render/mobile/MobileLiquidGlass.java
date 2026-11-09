package kat.respect.b2.render.mobile;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

public class MobileLiquidGlass extends MobileInstancedShader {
   private static final int STRIDE = 108;

   public MobileLiquidGlass() {
      super("gl/2d/glass/liquid_glass.frag", "gl/2d/glass/liquid_glass.vert", 108);
      new MobileAttributeBuilder()
         .stride(108)
         .mat4Attrib(0L)
         .floatAttrib(4, 64L)
         .floatAttrib(4, 80L)
         .intAttrib(1, 96L)
         .floatAttrib(1, 100L)
         .floatAttrib(1, 104L);
   }

   public void add(Matrix4f model, float x, float y, float w, float h, float r1, float r2, float r3, float r4, int tint, float refractionStrength, float z) {
      this.checkFlush();
      int pos = this.buffer.position();
      model.get(pos, this.buffer);
      this.buffer.putFloat(pos + 64, x);
      this.buffer.putFloat(pos + 68, y);
      this.buffer.putFloat(pos + 72, w);
      this.buffer.putFloat(pos + 76, h);
      this.buffer.putFloat(pos + 80, r1);
      this.buffer.putFloat(pos + 84, r2);
      this.buffer.putFloat(pos + 88, r3);
      this.buffer.putFloat(pos + 92, r4);
      this.buffer.putInt(pos + 96, tint);
      this.buffer.putFloat(pos + 100, refractionStrength);
      this.buffer.putFloat(pos + 104, z);
      this.buffer.position(pos + 108);
      this.count++;
   }

   public void flush(int blurTextureId) {
      if (this.count == 0) {
         this.buffer.clear();
      } else if (blurTextureId <= 0) {
         this.count = 0;
         this.buffer.clear();
         MobileDraw2D.getInstance().requestBlur();
      } else {
         MobileDraw2D.getInstance().requestBlur();
         GL33.glUseProgram(this.program);
         MobileGLUniforms.uniformTexture(this.program, "blurTex", blurTextureId);
         GL33.glBindVertexArray(this.vao);
         GL33.glDrawArraysInstanced(5, 0, 4, this.flushBuffer());
         GL33.glBindVertexArray(0);
         GL33.glUseProgram(0);
      }
   }

   @Override
   public void flush() {
      this.flush(0);
   }
}
