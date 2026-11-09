package kat.respect.b2.render.mobile;

import org.lwjgl.opengl.GL33;

public class MobileAttributeBuilder {
   private int stride;
   private int index = 0;

   public MobileAttributeBuilder stride(int stride) {
      this.stride = stride;
      return this;
   }

   public MobileAttributeBuilder floatAttrib(int size, long pointer) {
      GL33.glEnableVertexAttribArray(this.index);
      GL33.glVertexAttribPointer(this.index, size, 5126, false, this.stride, pointer);
      GL33.glVertexAttribDivisor(this.index, 1);
      this.index++;
      return this;
   }

   public MobileAttributeBuilder intAttrib(int size, long pointer) {
      GL33.glEnableVertexAttribArray(this.index);
      GL33.glVertexAttribIPointer(this.index, size, 5124, this.stride, pointer);
      GL33.glVertexAttribDivisor(this.index, 1);
      this.index++;
      return this;
   }

   public MobileAttributeBuilder mat4Attrib(long offset) {
      for (int i = 0; i < 4; i++) {
         GL33.glEnableVertexAttribArray(this.index);
         GL33.glVertexAttribPointer(this.index, 4, 5126, false, this.stride, offset + i * 16L);
         GL33.glVertexAttribDivisor(this.index, 1);
         this.index++;
      }

      return this;
   }
}
