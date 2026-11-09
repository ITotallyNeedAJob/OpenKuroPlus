package kat.respect.b2.render.mobile;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

public class MobileHueSlider extends MobileInstancedShader {
   public MobileHueSlider() {
      super("gl/2d/color/hue_slider.frag", "gl/2d/color/hue_slider.vert", 84);
      new MobileAttributeBuilder().stride(84).mat4Attrib(0L).floatAttrib(4, 64L).floatAttrib(1, 80L);
   }

   public void slider(Matrix4f model, float x, float y, float w, float h, float z) {
      this.checkFlush();
      int pos = this.buffer.position();
      model.get(pos, this.buffer);
      this.buffer.putFloat(pos + 64, x);
      this.buffer.putFloat(pos + 68, y);
      this.buffer.putFloat(pos + 72, w);
      this.buffer.putFloat(pos + 76, h);
      this.buffer.putFloat(pos + 80, z);
      this.buffer.position(pos + 84);
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
