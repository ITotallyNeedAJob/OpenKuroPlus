package kat.respect.b2.ct;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.additionalclassprovider.DelegatingClassProvider;
import net.lenni0451.classtransform.additionalclassprovider.InstrumentationClassProvider;
import net.lenni0451.classtransform.mixinstranslator.MixinsTranslator;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.classtransform.utils.FailStrategy;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class RespectCTBootstrap {
   public static final List<String> COMMON = List.of(
      "kat.respect.b2.mixin.MinecraftMixin",
      "kat.respect.b2.mixin.MultiPlayerGameModeMixin",
      "kat.respect.b2.mixin.KeyboardMixin",
      "kat.respect.b2.mixin.MouseMixin",
      "kat.respect.b2.mixin.ClientPacketListenerMixin",
      "kat.respect.b2.mixin.ConnectionMixin",
      "kat.respect.b2.mixin.ClientInputMixin",
      "kat.respect.b2.mixin.KeyboardInputMixin",
      "kat.respect.b2.mixin.EntityMixin",
      "kat.respect.b2.mixin.LivingEntityMixin",
      "kat.respect.b2.mixin.CameraMixin",
      "kat.respect.b2.mixin.ReachMixin",
      "kat.respect.b2.mixin.ModelBlockRendererMixin",
      "kat.respect.b2.mixin.LocalPlayerMixin"
   );
   public static final List<String> NATIVE_COMMON = List.of("kat.respect.b2.ct.HitboxCT");
   public static final List<String> RENDER_26 = List.of(
      "kat.respect.b2.mixin.WindowMixin",
      "kat.respect.b2.mixin.GameRendererMixin",
      "kat.respect.b2.mixin.EntityRendererMixin",
      "kat.respect.b2.mixin.ItemInHandRendererMixin"
   );
   public static final List<String> NATIVE_RENDER_26 = List.of("kat.respect.b2.ct.LevelRendererCT");
   public static final List<String> SKIPPED_NO_TRANSLATOR = List.of(
      "kat.respect.b2.mixin.IMinecraftAccessor",
      "kat.respect.b2.mixin.IMultiPlayerGameModeAccessor",
      "kat.respect.b2.mixin.IAbstractContainerScreenAccessor",
      "kat.respect.b2.mixin.IInventoryAccessor",
      "kat.respect.b2.mixin.FontMixin",
      "kat.respect.b2.mixin.FreeLookMixin"
   );
   private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean(false);
   private static volatile TransformerManager manager;

   private RespectCTBootstrap() {
   }

   public static synchronized TransformerManager bootstrap(Instrumentation inst) {
      return bootstrap(inst, false);
   }

   public static synchronized TransformerManager bootstrap(Instrumentation inst, boolean hotswap) {
      if (inst == null) {
         System.err.println("[Kuro+][CT] no Instrumentation — skipping ClassTransform bootstrap");
         return null;
      } else if (AgentRuntime.isRespectModLoaded()) {
         System.out.println("[Kuro+][CT] Fabric mod loaded — skipping ClassTransform to avoid double injection");
         return null;
      } else if (!BOOTSTRAPPED.compareAndSet(false, true)) {
         System.out.println("[Kuro+][CT] already bootstrapped — skipping");
         return manager;
      } else {
         try {
            GameVersion version = VersionDetector.detect();
            TransformerManager tm = new TransformerManager(new DelegatingClassProvider(new InstrumentationClassProvider(inst), new BasicClassProvider()));
            tm.setFailStrategy(FailStrategy.CONTINUE);
            tm.addTransformerPreprocessor(new MixinsTranslator());
            List<String> plan = planFor(version);
            int ok = 0;

            for (String name : plan) {
               try {
                  tm.addTransformer(name);
                  ok++;
               } catch (Throwable var11) {
                  System.err.println("[Kuro+][CT] skipping transformer " + name + ": " + var11);
               }
            }

            System.out.println("[Kuro+][CT] registered " + ok + "/" + plan.size() + " transformers");

            try {
               tm.addTransformer("kat.respect.b2.ct.AgentTickTransformer");
               AgentTickHook.activate();
               System.out.println("[Kuro+][CT] tick transformer registered (Minecraft.tick HEAD+RETURN)");
            } catch (Throwable var10) {
               AgentTickHook.deactivate();
               System.err.println("[Kuro+][CT] tick transformer failed to register: " + var10);
            }

            if (hotswap) {
               probeRenderFrameCalls(inst);
            }

            if (!hotswap) {
               try {
                  tm.addTransformer("kat.respect.b2.ct.AgentEarlyTransformer");
                  System.out.println("[Kuro+][CT] early-init transformer registered (Minecraft <init>)");
               } catch (Throwable var9) {
                  System.err.println("[Kuro+][CT] early-init transformer failed to register: " + var9);
               }
            }

            tm.hookInstrumentation(inst, false);
            if (hotswap) {
               System.out.println("[Kuro+][CT] executing isolated per-class retransforms...");
               isolateRetransforms(inst, tm);
            }

            manager = tm;
            System.out
               .println("[Kuro+][CT] bootstrap complete (version=" + version + ", hotswap=" + hotswap + ", live=" + tm.getRegisteredTransformer().size() + ")");
            System.out
               .println("[Kuro+][CT] " + SKIPPED_NO_TRANSLATOR.size() + " mixins stay Fabric-only (no MixinsTranslator mapping): " + SKIPPED_NO_TRANSLATOR);
            return tm;
         } catch (Throwable var12) {
            System.err.println("[Kuro+][CT] bootstrap FAILED (game continues without CT injection): " + var12);
            var12.printStackTrace();
            BOOTSTRAPPED.set(false);
            return null;
         }
      }
   }

   public static List<String> planFor(GameVersion version) {
      List<String> plan = new ArrayList<>(COMMON);
      plan.addAll(NATIVE_COMMON);
      if (version == GameVersion.V26_X) {
         if (VersionDetector.hasTracyFrameCapture()) {
            plan.add("kat.respect.b2.mixin.WindowMixin");
         }

         if (VersionDetector.hasCameraRenderState()) {
            plan.add("kat.respect.b2.mixin.GameRendererMixin");
         }

         if (VersionDetector.hasSubmitNodeCollector()) {
            plan.add("kat.respect.b2.mixin.EntityRendererMixin");
            plan.add("kat.respect.b2.mixin.ItemInHandRendererMixin");
         }

         if (VersionDetector.hasTracyFrameCapture() && VersionDetector.hasSubmitNodeCollector()) {
            plan.addAll(NATIVE_RENDER_26);
         }
      } else if (version == GameVersion.V1_21_X) {
         System.out
            .println("[Kuro+][CT] 1.21.x: skipping 26.x render mixins (their 26.1.2 descriptors would fail) — add 1.21 variants as native CTransformers");
      } else {
         System.out.println("[Kuro+][CT] unknown version: COMMON only (safe subset)");
      }

      return plan;
   }

   static void resetForTests() {
      BOOTSTRAPPED.set(false);
      manager = null;
   }

   private static void probeRenderFrameCalls(Instrumentation inst) {
      try {
         Class<?> target = null;

         for (Class<?> c : inst.getAllLoadedClasses()) {
            if (c != null && "net.minecraft.client.Minecraft".equals(c.getName())) {
               target = c;
               break;
            }
         }

         if (target == null) {
            System.out.println("[Kuro+][probe] Minecraft not loaded yet — skipping");
            return;
         }

         Class<?> t = target;
         ClassFileTransformer probe = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String name, Class<?> cls, ProtectionDomain pd, byte[] buf) {
               if ("net/minecraft/client/Minecraft".equals(name) && buf != null) {
                  try {
                     ClassNode cn = ASMUtils.fromBytes(buf);
                     int found = 0;

                     for (Object mo : cn.methods) {
                        MethodNode m = (MethodNode)mo;
                        if ("renderFrame".equals(m.name)) {
                           found++;
                           StringBuilder sb = new StringBuilder("[Kuro+][probe] renderFrame").append(m.desc).append(" calls:");

                           for (Object io : m.instructions.toArray()) {
                              if (io instanceof MethodInsnNode mi) {
                                 sb.append("\n    ").append(mi.owner).append('.').append(mi.name).append(mi.desc);
                              }
                           }

                           System.out.println(sb);
                        }
                     }

                     if (found == 0) {
                        System.out.println("[Kuro+][probe] no renderFrame method present?!");
                     }
                  } catch (Throwable var17) {
                     System.out.println("[Kuro+][probe] dump failed: " + var17);
                  }

                  return null;
               } else {
                  return null;
               }
            }
         };
         inst.addTransformer(probe, true);

         try {
            inst.retransformClasses(t);
         } finally {
            try {
               inst.removeTransformer(probe);
            } catch (Throwable var12) {
            }
         }

         System.out.println("[Kuro+][probe] done");
      } catch (Throwable var14) {
         System.out.println("[Kuro+][probe] failed: " + var14);
      }
   }

   private static void isolateRetransforms(Instrumentation inst, TransformerManager tm) {
      Set<String> targets = new HashSet<>(tm.getTransformedClasses());
      if (!targets.isEmpty()) {
         int ok = 0;
         int failed = 0;

         for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (clazz != null && targets.contains(clazz.getName())) {
               try {
                  inst.retransformClasses(clazz);
                  ok++;
               } catch (Throwable var10) {
                  failed++;
                  System.err.println("[Kuro+][CT] retransform FAILED: " + clazz.getName() + " -> " + var10);
                  diagnoseMethodAdditions(tm, clazz);
               }
            }
         }

         System.out.println("[Kuro+][CT] isolated retransform: " + ok + " ok, " + failed + " failed");
      }
   }

   private static void diagnoseMethodAdditions(TransformerManager tm, Class<?> clazz) {
      try {
         String name = clazz.getName();
         ClassLoader loader = clazz.getClassLoader();
         InputStream in = (loader != null ? loader : ClassLoader.getSystemClassLoader()).getResourceAsStream(name.replace('.', '/') + ".class");
         if (in == null) {
            System.err.println("[Kuro+][CT]   (no original bytes available for diagnostics)");
            return;
         }

         InputStream transformed = in;

         byte[] original;
         try {
            original = in.readAllBytes();
         } catch (Throwable var16) {
            if (in != null) {
               try {
                  transformed.close();
               } catch (Throwable var15) {
                  var16.addSuppressed(var15);
               }
            }

            throw var16;
         }

         if (in != null) {
            in.close();
         }

         byte[] transformedx = tm.transform(name, original, false);
         if (transformedx == null) {
            return;
         }

         ClassNode before = ASMUtils.fromBytes(original);
         ClassNode after = ASMUtils.fromBytes(transformedx);
         Set<String> existingMethods = new HashSet<>();

         for (MethodNode m : before.methods) {
            existingMethods.add(m.name + m.desc);
         }

         StringBuilder addedMethods = new StringBuilder();

         for (MethodNode m : after.methods) {
            if (!existingMethods.contains(m.name + m.desc)) {
               if (addedMethods.length() > 0) {
                  addedMethods.append(", ");
               }

               addedMethods.append(m.name).append(m.desc);
            }
         }

         Set<String> existingFields = new HashSet<>();

         for (FieldNode f : before.fields) {
            existingFields.add(f.name + f.desc);
         }

         StringBuilder addedFields = new StringBuilder();

         for (FieldNode f : after.fields) {
            if (!existingFields.contains(f.name + f.desc)) {
               if (addedFields.length() > 0) {
                  addedFields.append(", ");
               }

               addedFields.append(f.name).append(f.desc);
            }
         }

         System.err.println("[Kuro+][CT]   extra methods CT would add: " + (addedMethods.length() == 0 ? "<none>" : addedMethods));
         System.err.println("[Kuro+][CT]   extra fields CT would add: " + (addedFields.length() == 0 ? "<none>" : addedFields));
      } catch (Throwable var17) {
         System.err.println("[Kuro+][CT]   diagnostic failed: " + var17);
      }
   }
}
