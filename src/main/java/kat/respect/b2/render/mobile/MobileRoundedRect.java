package kat.respect.b2.render.mobile;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

public class MobileRoundedRect extends MobileInstancedShader {
   public MobileRoundedRect() {
      super("gl/2d/shapes/rounded_rect.frag", "gl/2d/shapes/rounded_rect.vert", 116);
      new MobileAttributeBuilder().stride(116).mat4Attrib(0L).floatAttrib(4, 64L).floatAttrib(4, 80L).intAttrib(4, 96L).floatAttrib(1, 112L);
   }

   public void addRect(Matrix4f model, float x, float y, float w, float h, float radius, int color, float z) {
      this.addRect(model, x, y, w, h, radius, radius, radius, radius, color, color, color, color, z);
   }

   public void addRect(Matrix4f model, float x, float y, float w, float h, float r1, float r2, float r3, float r4, int color, float z) {
      this.addRect(model, x, y, w, h, r1, r2, r3, r4, color, color, color, color, z);
   }

   public void addRect(Matrix4f model, float x, float y, float w, float h, float r1, float r2, float r3, float r4, int c1, int c2, int c3, int c4, float z) {
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
      this.buffer.putInt(pos + 96, c1);
      this.buffer.putInt(pos + 100, c2);
      this.buffer.putInt(pos + 104, c3);
      this.buffer.putInt(pos + 108, c4);
      this.buffer.putFloat(pos + 112, z);
      this.buffer.position(pos + 116);
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
