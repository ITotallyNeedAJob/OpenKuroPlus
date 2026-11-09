package kat.respect.b2.render;

import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.client.renderer.rendertype.RenderSetup.RenderSetupBuilder;
import net.minecraft.resources.Identifier;

public final class NoDepthRenderTypes {
   private static boolean probed;
   private static RenderType quads;
   private static RenderType lines;

   private NoDepthRenderTypes() {
   }

   public static RenderType quads() {
      probe();
      return quads != null ? quads : RenderTypes.debugQuads();
   }

   public static RenderType lines() {
      probe();
      return lines != null ? lines : RenderTypes.linesTranslucent();
   }

   private static synchronized void probe() {
      if (!probed) {
         probed = true;

         try {
            Method create = RenderType.class.getDeclaredMethod("create", String.class, RenderSetup.class);
            create.setAccessible(true);
            quads = build(create, "respect_no_depth_quads", "respect:no_depth_quads", RenderPipelines.DEBUG_QUADS, true);
            lines = build(create, "respect_no_depth_lines", "respect:no_depth_lines", RenderPipelines.LINES_TRANSLUCENT, false);
         } catch (Throwable var1) {
            quads = null;
            lines = null;
         }
      }
   }

   private static RenderType build(Method create, String name, String location, RenderPipeline base, boolean sort) throws Exception {
      RenderPipeline pipeline = new NoDepthRenderTypes.NoDepthPipeline(base, Identifier.parse(location));
      GpuDevice device = RenderSystem.getDevice();
      if (device == null) {
         return null;
      } else {
         try {
            CompiledRenderPipeline compiled = device.precompilePipeline(pipeline);
            if (compiled == null || !compiled.isValid()) {
               return null;
            }
         } catch (Throwable var8) {
            return null;
         }

         RenderSetupBuilder builder = RenderSetup.builder(pipeline)
            .setLayeringTransform(LayeringTransform.NO_LAYERING)
            .setOutputTarget(OutputTarget.MAIN_TARGET)
            .setOutline(OutlineProperty.NONE);
         if (sort) {
            builder = builder.sortOnUpload();
         }

         return (RenderType)create.invoke(null, name, builder.createRenderSetup());
      }
   }

   private static final class NoDepthPipeline extends RenderPipeline {
      private NoDepthPipeline(RenderPipeline base, Identifier location) {
         super(
            location,
            base.getVertexShader(),
            base.getFragmentShader(),
            base.getShaderDefines(),
            base.getSamplers(),
            base.getUniforms(),
            base.getColorTargetState(),
            null,
            base.getPolygonMode(),
            base.isCull(),
            base.getVertexFormat(),
            base.getVertexFormatMode(),
            base.getSortKey()
         );
      }
   }
}
