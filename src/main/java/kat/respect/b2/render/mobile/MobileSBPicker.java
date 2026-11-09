package kat.respect.b2.render.mobile;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

public class MobileSBPicker extends MobileInstancedShader {
   public MobileSBPicker() {
      super("gl/2d/color/sb_picker.frag", "gl/2d/color/sb_picker.vert", 88);
      new MobileAttributeBuilder().stride(88).mat4Attrib(0L).floatAttrib(4, 64L).floatAttrib(1, 80L).floatAttrib(1, 84L);
   }

   public void picker(Matrix4f model, float x, float y, float w, float h, float hue, float z) {
      this.checkFlush();
      int pos = this.buffer.position();
      model.get(pos, this.buffer);
      this.buffer.putFloat(pos + 64, x);
      this.buffer.putFloat(pos + 68, y);
      this.buffer.putFloat(pos + 72, w);
      this.buffer.putFloat(pos + 76, h);
      this.buffer.putFloat(pos + 80, hue);
      this.buffer.putFloat(pos + 84, z);
      this.buffer.position(pos + 88);
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
