package kat.respect.b2.render.mobile;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

public class MobileRoundedImage extends MobileShader {
   public MobileRoundedImage() {
      super("gl/2d/image/rounded_image.frag", "gl/2d/image/rounded_image.vert");
   }

   public void draw(Matrix4f model, int textureId, float x, float y, float w, float h, float radius, float alpha, float z) {
      this.draw(model, textureId, x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F, radius, alpha, z);
   }

   public void draw(Matrix4f model, int textureId, float x, float y, float w, float h, float u, float v, float uw, float vh, float radius, float alpha, float z) {
      if (textureId > 0 && !(alpha <= 0.001F) && this.program != -1) {
         GL33.glUseProgram(this.program);
         MobileGLUniforms.uniformMatrix4f(this.program, "modelMat", model);
         MobileGLUniforms.uniform4f(this.program, "rect", x, y, w, h);
         MobileGLUniforms.uniform4f(this.program, "radius", radius, radius, radius, radius);
         MobileGLUniforms.uniform4f(this.program, "uvRect", u, v, uw, vh);
         MobileGLUniforms.uniform1f(this.program, "z", z);
         MobileGLUniforms.uniform1f(this.program, "uAlpha", alpha);
         MobileGLUniforms.uniformTexture(this.program, "Sampler0", textureId);
         GL33.glBindVertexArray(this.vao);
         GL33.glDrawArrays(5, 0, 4);
         GL33.glBindVertexArray(0);
         GL33.glUseProgram(0);
      }
   }
}
