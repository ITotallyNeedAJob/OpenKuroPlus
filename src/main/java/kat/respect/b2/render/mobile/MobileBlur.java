package kat.respect.b2.render.mobile;

import org.lwjgl.opengl.GL33;

public class MobileBlur {
   private final int max = 8;
   private final int down;
   private final int up;
   private final int vao;
   private final int sampler;
   private final int[] fbo = new int[8];
   private final int[] texture = new int[8];
   private final int[] width = new int[8];
   private final int[] height = new int[8];
   private int lwidth = -1;
   private int lheight = -1;

   public MobileBlur() {
      this.vao = GL33.glGenVertexArrays();
      this.down = MobileShaderUtil.createShader("gl/post/kawase_down.frag", "gl/post/post.vert");
      this.up = MobileShaderUtil.createShader("gl/post/kawase_up.frag", "gl/post/post.vert");
      this.sampler = GL33.glGenSamplers();
      GL33.glSamplerParameteri(this.sampler, 10241, 9729);
      GL33.glSamplerParameteri(this.sampler, 10240, 9729);
      GL33.glSamplerParameteri(this.sampler, 10242, 33071);
      GL33.glSamplerParameteri(this.sampler, 10243, 33071);

      for (int i = 0; i < 8; i++) {
         this.fbo[i] = GL33.glGenFramebuffers();
         this.texture[i] = GL33.glGenTextures();
      }
   }

   public void cleanup() {
      GL33.glDeleteVertexArrays(this.vao);
      if (this.down != -1) {
         GL33.glDeleteProgram(this.down);
      }

      if (this.up != -1) {
         GL33.glDeleteProgram(this.up);
      }

      GL33.glDeleteSamplers(this.sampler);

      for (int i = 0; i < 8; i++) {
         GL33.glDeleteFramebuffers(this.fbo[i]);
         GL33.glDeleteTextures(this.texture[i]);
      }
   }

   private void resize(int screenWidth, int screenHeight) {
      if (this.lwidth != screenWidth || this.lheight != screenHeight) {
         this.lwidth = screenWidth;
         this.lheight = screenHeight;

         for (int i = 0; i < 8; i++) {
            int w = Math.max(1, screenWidth >> i + 1);
            int h = Math.max(1, screenHeight >> i + 1);
            this.width[i] = w;
            this.height[i] = h;
            this.resizeFbo(this.fbo[i], this.texture[i], w, h);
         }
      }
   }

   private void resizeFbo(int fbo, int tex, int w, int h) {
      MobileGlState state = new MobileGlState();
      GL33.glBindTexture(3553, tex);
      GL33.glTexImage2D(3553, 0, 32856, w, h, 0, 6408, 5121, 0L);
      GL33.glTexParameteri(3553, 10241, 9729);
      GL33.glTexParameteri(3553, 10240, 9729);
      GL33.glTexParameteri(3553, 10242, 33071);
      GL33.glTexParameteri(3553, 10243, 33071);
      GL33.glTexParameteri(3553, 33085, 0);
      GL33.glBindFramebuffer(36160, fbo);
      GL33.glFramebufferTexture2D(36160, 36064, 3553, tex, 0);
      GL33.glBindFramebuffer(36160, 0);
      GL33.glBindTexture(3553, 0);
      state.restore();
   }

   public int blur(MobileCrossTexture sourceTexture, int sourceWidth, int sourceHeight, int levels, float offsetMul) {
      if (sourceTexture != null && sourceTexture.glId > 0 && this.down != -1 && this.up != -1) {
         MobileGlState state = new MobileGlState();
         this.resize(sourceWidth, sourceHeight);
         levels = Math.min(levels, 8);
         GL33.glDisable(2929);
         GL33.glDisable(3042);
         GL33.glBindVertexArray(this.vao);
         GL33.glBindSampler(8, this.sampler);
         GL33.glBindSampler(9, this.sampler);
         GL33.glUseProgram(this.down);
         MobileGLUniforms.uniform1i(this.down, "tex", 8);

         for (int i = 0; i < levels; i++) {
            GL33.glBindFramebuffer(36160, this.fbo[i]);
            GL33.glViewport(0, 0, this.width[i], this.height[i]);
            int tex = i == 0 ? sourceTexture.glId : this.texture[i - 1];
            GL33.glActiveTexture(33992);
            GL33.glBindTexture(3553, tex);
            MobileGLUniforms.uniform1f(this.down, "offset", (i + 0.5F) * offsetMul);
            GL33.glDrawArrays(4, 0, 3);
         }

         GL33.glUseProgram(this.up);
         MobileGLUniforms.uniform1i(this.up, "tex", 9);

         for (int i = levels - 2; i >= 0; i--) {
            GL33.glBindFramebuffer(36160, this.fbo[i]);
            GL33.glViewport(0, 0, this.width[i], this.height[i]);
            GL33.glActiveTexture(33993);
            GL33.glBindTexture(3553, this.texture[i + 1]);
            MobileGLUniforms.uniform1f(this.up, "offset", (i + 0.5F) * offsetMul);
            GL33.glDrawArrays(4, 0, 3);
         }

         GL33.glBindSampler(8, 0);
         GL33.glBindSampler(9, 0);
         state.restore();
         return this.texture[0];
      } else {
         return -1;
      }
   }
}
