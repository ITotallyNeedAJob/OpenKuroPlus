package kat.respect.b2.render.mobile;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

public class MobileAlphaSlider extends MobileInstancedShader {
   public MobileAlphaSlider() {
      super("gl/2d/color/alpha_slider.frag", "gl/2d/color/alpha_slider.vert", 96);
      new MobileAttributeBuilder().stride(96).mat4Attrib(0L).floatAttrib(4, 64L).floatAttrib(3, 80L).floatAttrib(1, 92L);
   }

   public void slider(Matrix4f model, float x, float y, float w, float h, float r, float g, float b, float z) {
      this.checkFlush();
      int pos = this.buffer.position();
      model.get(pos, this.buffer);
      this.buffer.putFloat(pos + 64, x);
      this.buffer.putFloat(pos + 68, y);
      this.buffer.putFloat(pos + 72, w);
      this.buffer.putFloat(pos + 76, h);
      this.buffer.putFloat(pos + 80, r);
      this.buffer.putFloat(pos + 84, g);
      this.buffer.putFloat(pos + 88, b);
      this.buffer.putFloat(pos + 92, z);
      this.buffer.position(pos + 96);
      this.count++;
   }

   @Override
   public void flush() {
      if (this.count != 0) {
         GL33.glUseProgram(this.program);
         GL33.glBindVertexArray(this.vao);
         GL33.glDrawArraysInstanced(5, 0, 4, this.flushBuffer());
         GL33.glBindVertexArray(0);
         GL33.glUseProgram(0);
      }
   }
}
