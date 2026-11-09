package kat.respect.b2.render.mobile;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

public class MobileShadow extends MobileInstancedShader {
   public MobileShadow() {
      super("gl/2d/shapes/rounded_shadow.frag", "gl/2d/shapes/rounded_shadow.vert", 120);
      new MobileAttributeBuilder()
         .stride(120)
         .mat4Attrib(0L)
         .floatAttrib(4, 64L)
         .floatAttrib(4, 80L)
         .intAttrib(4, 96L)
         .floatAttrib(1, 112L)
         .floatAttrib(1, 116L);
   }

   public void addShadow(Matrix4f model, float x, float y, float w, float h, float radius, float shadowRadius, int color, float z) {
      this.checkFlush();
      int pos = this.buffer.position();
      model.get(pos, this.buffer);
      this.buffer.putFloat(pos + 64, x);
      this.buffer.putFloat(pos + 68, y);
      this.buffer.putFloat(pos + 72, w);
      this.buffer.putFloat(pos + 76, h);
      this.buffer.putFloat(pos + 80, radius);
      this.buffer.putFloat(pos + 84, radius);
      this.buffer.putFloat(pos + 88, radius);
      this.buffer.putFloat(pos + 92, radius);
      this.buffer.putInt(pos + 96, color);
      this.buffer.putInt(pos + 100, color);
      this.buffer.putInt(pos + 104, color);
      this.buffer.putInt(pos + 108, color);
      this.buffer.putFloat(pos + 112, z);
      this.buffer.putFloat(pos + 116, shadowRadius);
      this.buffer.position(pos + 120);
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
