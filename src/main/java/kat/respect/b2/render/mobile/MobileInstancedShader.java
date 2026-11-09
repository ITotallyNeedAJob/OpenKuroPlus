package kat.respect.b2.render.mobile;

import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

public abstract class MobileInstancedShader extends MobileShader {
   private static final float GROW_FACTOR = 2.0F;
   protected final int stride;
   protected final int vbo;
   protected ByteBuffer buffer;
   protected int max;
   protected int count = 0;

   protected MobileInstancedShader(String fragResource, String vertResource, int bytes) {
      super(fragResource, vertResource);
      this.stride = bytes;
      int initialSize = Math.max(65536, bytes * 256);
      this.max = initialSize / bytes;
      this.buffer = BufferUtils.createByteBuffer(initialSize);
      this.vbo = GL33.glGenBuffers();
      GL33.glBindVertexArray(this.vao);
      GL33.glBindBuffer(34962, this.vbo);
      GL33.glBufferData(34962, initialSize, 35048);
   }

   public abstract void flush();

   @Override
   public void cleanup() {
      super.cleanup();
      if (this.vbo != 0) {
         GL33.glDeleteBuffers(this.vbo);
      }

      this.buffer.clear();
   }

   protected void checkFlush() {
      if (this.count >= this.max) {
         this.grow();
      }
   }

   private void grow() {
      int newMax = (int)(this.max * 2.0F);
      ByteBuffer newBuffer = BufferUtils.createByteBuffer(newMax * this.stride);
      this.buffer.flip();
      newBuffer.put(this.buffer);
      this.buffer = newBuffer;
      this.max = newMax;
      GL33.glBindBuffer(34962, this.vbo);
      GL33.glBufferData(34962, (long)newMax * this.stride, 35048);
   }

   protected int flushBuffer() {
      if (this.count == 0) {
         this.buffer.clear();
         return 0;
      } else {
         this.buffer.flip();
         GL33.glBindBuffer(34962, this.vbo);
         GL33.glBufferData(34962, this.buffer.remaining(), 35040);
         GL33.glBufferSubData(34962, 0L, this.buffer);
         this.buffer.clear();
         int c = this.count;
         this.count = 0;
         return c;
      }
   }
}
