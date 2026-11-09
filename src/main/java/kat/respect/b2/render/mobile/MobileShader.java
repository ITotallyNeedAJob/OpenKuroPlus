package kat.respect.b2.render.mobile;

import org.lwjgl.opengl.GL33;

public class MobileShader {
   public static final int CAMERA_UBO_BINDING = 7;
   protected final int vao;
   protected final int program;

   public MobileShader(String fragSource, String vertSource) {
      this.program = MobileShaderUtil.createShader(fragSource, vertSource);
      if (this.program != -1) {
         int index = GL33.glGetUniformBlockIndex(this.program, "Camera");
         if (index != -1) {
            GL33.glUniformBlockBinding(this.program, index, 7);
         }
      }

      this.vao = GL33.glGenVertexArrays();
   }

   public int getProgram() {
      return this.program;
   }

   public int getVao() {
      return this.vao;
   }

   public void cleanup() {
      if (this.vao != 0) {
         GL33.glDeleteVertexArrays(this.vao);
      }

      if (this.program != -1 && this.program != 0) {
         GL33.glDeleteProgram(this.program);
      }
   }
}
