package kat.respect.b2.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import kat.respect.b2.render.NoDepthRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionfc;

public final class RenderCompat {
   private static volatile Constructor<?> builderCtor = null;
   private static volatile Method topoMethod = null;
   private static volatile Method formatMethod = null;
   private static volatile boolean builderProbed = false;

   private RenderCompat() {
   }

   public static RenderType entityTranslucentCull(Identifier texture) {
      if (texture == null) {
         return null;
      } else {
         try {
            if (RenderTypes.class.getMethod("entityTranslucentCull", Identifier.class).invoke(null, texture) instanceof RenderType type) {
               return type;
            }
         } catch (Throwable var4) {
         }

         try {
            return RenderTypes.entityTranslucent(texture);
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   public static RenderType linesNoDepth() {
      try {
         RenderType type = NoDepthRenderTypes.lines();
         if (type != null) {
            return type;
         }
      } catch (Throwable var2) {
      }

      try {
         return RenderTypes.linesTranslucent();
      } catch (Throwable var1) {
         return null;
      }
   }

   public static BufferBuilder newBuilder(ByteBufferBuilder buffer, RenderType type) {
      if (buffer != null && type != null) {
         probeBuilder(type);

         try {
            Constructor<?> ctor = builderCtor;
            Method topo = topoMethod;
            Method format = formatMethod;
            if (ctor != null
               && topo != null
               && format != null
               && ctor.newInstance(buffer, topo.invoke(type), format.invoke(type)) instanceof BufferBuilder builder) {
               return builder;
            }
         } catch (Throwable var7) {
         }

         throw new IllegalStateException("no linkable BufferBuilder shape for " + type.getClass());
      } else {
         throw new IllegalArgumentException("null buffer/type");
      }
   }

   private static synchronized void probeBuilder(RenderType type) {
      if (!builderProbed) {
         builderProbed = true;

         try {
            Method topo = RenderType.class.getMethod("primitiveTopology");
            Method format = RenderType.class.getMethod("format");
            Constructor<?> ctor = BufferBuilder.class.getConstructor(ByteBufferBuilder.class, topo.getReturnType(), format.getReturnType());
            topoMethod = topo;
            formatMethod = format;
            builderCtor = ctor;
         } catch (Throwable var5) {
            try {
               Method mode = RenderType.class.getMethod("mode");
               Method formatx = RenderType.class.getMethod("format");
               Constructor<?> ctorx = BufferBuilder.class.getConstructor(ByteBufferBuilder.class, mode.getReturnType(), formatx.getReturnType());
               topoMethod = mode;
               formatMethod = formatx;
               builderCtor = ctorx;
            } catch (Throwable var4) {
            }
         }
      }
   }

   public static void reloadChunks() {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.levelRenderer != null && mc.gameRenderer != null) {
            Object renderer = mc.levelRenderer;
            boolean done = false;

            for (Method m : renderer.getClass().getMethods()) {
               if (m.getName().equals("invalidateCompiledGeometry") && m.getParameterCount() == 4) {
                  m.invoke(renderer, mc.level, mc.options, CameraCompat.mainCamera(mc.gameRenderer), mc.getBlockColors());
                  done = true;
                  break;
               }
            }

            for (Method mx : renderer.getClass().getMethods()) {
               if (mx.getName().equals("resetLevelRenderData") && mx.getParameterCount() == 0) {
                  mx.invoke(renderer);
                  done = true;
                  break;
               }
            }

            if (done) {
               return;
            }
         }
      } catch (Throwable var8) {
         return;
      }

      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.levelRenderer != null) {
            mc.levelRenderer.getClass().getMethod("allChanged").invoke(mc.levelRenderer);
         }
      } catch (Throwable var7) {
      }
   }

   public static void rotatePose(PoseStack pose, Quaternionfc rotation) {
      if (pose != null && rotation != null) {
         try {
            pose.getClass().getMethod("rotate", Quaternionfc.class).invoke(pose, rotation);
         } catch (Throwable var4) {
            try {
               pose.rotateAround(rotation, 0.0F, 0.0F, 0.0F);
            } catch (Throwable var3) {
            }
         }
      }
   }
}
