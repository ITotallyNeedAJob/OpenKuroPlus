package kat.respect.b2.render.mobile;

import java.util.Arrays;
import org.lwjgl.opengl.GL33;

public class MobileGlState implements AutoCloseable {
   private static final int[] PIPELINE_UNITS = new int[]{0, 8, 9};
   private final boolean blend;
   private final boolean depthTest;
   private final boolean cullFace;
   private final boolean scissorTest;
   private final boolean stencilTest;
   private final int blendSrcRGB;
   private final int blendDstRGB;
   private final int blendSrcAlpha;
   private final int blendDstAlpha;
   private final int blendEquationRGB;
   private final int blendEquationAlpha;
   private final int depthFunc;
   private final boolean depthMask;
   private final int cullFaceMode;
   private final int frontFace;
   private final int stencilFunc;
   private final int stencilRef;
   private final int stencilValueMask;
   private final int stencilWriteMask;
   private final int stencilFail;
   private final int stencilPassDepthFail;
   private final int stencilPassDepthPass;
   private final boolean[] colorMask = new boolean[4];
   private final int[] scissorBox = new int[4];
   private final int unpackAlignment;
   private final int unpackRowLength;
   private final int unpackSkipPixels;
   private final int unpackSkipRows;
   private final int pixelUnpackBuffer;
   private final int ubo7;
   private final int[] viewport = new int[4];
   private final int framebuffer;
   private final int program;
   private final int vao;
   private final int arrayBuffer;
   private final int elementArrayBuffer;
   private final int uniformBuffer;
   private final int activeTexture;
   private final int[] trackedUnits;
   private final int[] boundTextures;
   private final int[] boundSamplers;

   public MobileGlState() {
      this.blend = GL33.glIsEnabled(3042);
      this.depthTest = GL33.glIsEnabled(2929);
      this.cullFace = GL33.glIsEnabled(2884);
      this.scissorTest = GL33.glIsEnabled(3089);
      this.stencilTest = GL33.glIsEnabled(2960);
      this.blendSrcRGB = GL33.glGetInteger(32969);
      this.blendDstRGB = GL33.glGetInteger(32968);
      this.blendSrcAlpha = GL33.glGetInteger(32971);
      this.blendDstAlpha = GL33.glGetInteger(32970);
      this.blendEquationRGB = GL33.glGetInteger(32777);
      this.blendEquationAlpha = GL33.glGetInteger(34877);
      this.depthFunc = GL33.glGetInteger(2932);
      this.depthMask = GL33.glGetBoolean(2930);
      this.cullFaceMode = GL33.glGetInteger(2885);
      this.frontFace = GL33.glGetInteger(2886);
      this.stencilFunc = GL33.glGetInteger(2962);
      this.stencilRef = GL33.glGetInteger(2967);
      this.stencilValueMask = GL33.glGetInteger(2963);
      this.stencilWriteMask = GL33.glGetInteger(2968);
      this.stencilFail = GL33.glGetInteger(2964);
      this.stencilPassDepthFail = GL33.glGetInteger(2965);
      this.stencilPassDepthPass = GL33.glGetInteger(2966);
      int[] mask = new int[4];
      GL33.glGetIntegerv(3107, mask);

      for (int i = 0; i < 4; i++) {
         this.colorMask[i] = mask[i] != 0;
      }

      GL33.glGetIntegerv(3088, this.scissorBox);
      this.unpackAlignment = GL33.glGetInteger(3317);
      this.unpackRowLength = GL33.glGetInteger(3314);
      this.unpackSkipPixels = GL33.glGetInteger(3316);
      this.unpackSkipRows = GL33.glGetInteger(3315);
      this.pixelUnpackBuffer = GL33.glGetInteger(35055);
      GL33.glGetIntegerv(2978, this.viewport);
      this.framebuffer = GL33.glGetInteger(36006);
      this.program = GL33.glGetInteger(35725);
      this.vao = GL33.glGetInteger(34229);
      this.arrayBuffer = GL33.glGetInteger(34964);
      this.elementArrayBuffer = GL33.glGetInteger(34965);
      this.uniformBuffer = GL33.glGetInteger(35368);
      this.activeTexture = GL33.glGetInteger(34016);
      int activeUnit = this.activeTexture - 33984;
      int[] units = new int[1 + PIPELINE_UNITS.length];
      units[0] = activeUnit;
      int n = 1;

      for (int unit : PIPELINE_UNITS) {
         if (unit != activeUnit) {
            units[n++] = unit;
         }
      }

      this.trackedUnits = Arrays.copyOf(units, n);
      this.boundTextures = new int[n];
      this.boundSamplers = new int[n];
      this.ubo7 = GL33.glGetIntegeri(35368, 7);

      for (int i = 0; i < n; i++) {
         GL33.glActiveTexture(33984 + this.trackedUnits[i]);
         this.boundTextures[i] = GL33.glGetInteger(32873);
         this.boundSamplers[i] = GL33.glGetIntegeri(35097, this.trackedUnits[i]);
      }

      GL33.glActiveTexture(this.activeTexture);
   }

   public void restore() {
      setEnabled(3042, this.blend);
      setEnabled(2929, this.depthTest);
      setEnabled(2884, this.cullFace);
      setEnabled(2960, this.stencilTest);
      GL33.glScissor(this.scissorBox[0], this.scissorBox[1], this.scissorBox[2], this.scissorBox[3]);
      setEnabled(3089, this.scissorTest);
      GL33.glBlendFuncSeparate(this.blendSrcRGB, this.blendDstRGB, this.blendSrcAlpha, this.blendDstAlpha);
      GL33.glBlendEquationSeparate(this.blendEquationRGB, this.blendEquationAlpha);
      GL33.glDepthFunc(this.depthFunc);
      GL33.glDepthMask(this.depthMask);
      GL33.glCullFace(this.cullFaceMode);
      GL33.glFrontFace(this.frontFace);
      GL33.glStencilFunc(this.stencilFunc, this.stencilRef, this.stencilValueMask);
      GL33.glStencilMask(this.stencilWriteMask);
      GL33.glStencilOp(this.stencilFail, this.stencilPassDepthFail, this.stencilPassDepthPass);
      GL33.glColorMask(this.colorMask[0], this.colorMask[1], this.colorMask[2], this.colorMask[3]);
      GL33.glPixelStorei(3317, this.unpackAlignment);
      GL33.glPixelStorei(3314, this.unpackRowLength);
      GL33.glPixelStorei(3316, this.unpackSkipPixels);
      GL33.glPixelStorei(3315, this.unpackSkipRows);
      GL33.glBindBuffer(35052, this.pixelUnpackBuffer);
      GL33.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
      GL33.glBindFramebuffer(36160, this.framebuffer);
      GL33.glUseProgram(this.program);
      GL33.glBindVertexArray(0);
      GL33.glBindBuffer(34962, this.arrayBuffer);
      GL33.glBindBuffer(34963, this.elementArrayBuffer);
      GL33.glBindBuffer(35345, this.uniformBuffer);
      GL33.glBindBufferBase(35345, 7, this.ubo7);
      GL33.glBindVertexArray(this.vao);

      for (int i = 0; i < this.trackedUnits.length; i++) {
         GL33.glActiveTexture(33984 + this.trackedUnits[i]);
         GL33.glBindTexture(3553, this.boundTextures[i]);
         GL33.glBindSampler(this.trackedUnits[i], this.boundSamplers[i]);
      }

      GL33.glActiveTexture(this.activeTexture);
   }

   private static void setEnabled(int cap, boolean enabled) {
      if (enabled) {
         GL33.glEnable(cap);
      } else {
         GL33.glDisable(cap);
      }
   }

   @Override
   public void close() {
      this.restore();
   }
}
