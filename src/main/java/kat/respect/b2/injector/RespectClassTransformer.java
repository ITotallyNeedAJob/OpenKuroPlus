package kat.respect.b2.injector;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class RespectClassTransformer implements ClassFileTransformer {
   private static final String BRIDGE_CLASS = "kat/respect/b2/injector/RespectHookBridge";

   @Override
   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
      if (className == null) {
         return null;
      } else {
         String name = className.replace('.', '/');
         if (!isTargetClass(name)) {
            return null;
         } else {
            try {
               ClassReader cr = new ClassReader(classfileBuffer);
               ClassNode cn = new ClassNode();
               cr.accept(cn, 0);
               boolean modified = false;
               if (name.equals("net/minecraft/client/Minecraft") || name.equals("net/minecraft/class_310")) {
                  modified = this.transformMinecraft(cn);
               } else if (name.equals("net/minecraft/client/player/LocalPlayer")
                  || name.equals("net/minecraft/client/network/ClientPlayerEntity")
                  || name.equals("net/minecraft/class_746")) {
                  modified = this.transformLocalPlayer(cn);
               }

               if (!modified) {
                  return null;
               } else {
                  ClassWriter cw = new ClassWriter(1);
                  cn.accept(cw);
                  return cw.toByteArray();
               }
            } catch (Throwable var11) {
               System.err.println("[Kuro+][injector] Failed to transform " + className + ": " + var11);
               return null;
            }
         }
      }
   }

   public static boolean isTargetClass(String name) {
      return name.equals("net/minecraft/client/Minecraft")
         || name.equals("net/minecraft/class_310")
         || name.equals("net/minecraft/client/player/LocalPlayer")
         || name.equals("net/minecraft/client/network/ClientPlayerEntity")
         || name.equals("net/minecraft/class_746");
   }

   private boolean transformMinecraft(ClassNode cn) {
      boolean modified = false;

      for (MethodNode mn : cn.methods) {
         if (matches(mn.name, "tick", "method_1574", "m_91398_") && mn.desc.equals("()V") && !hasCall(mn, "onClientTick")) {
            for (AbstractInsnNode insn : mn.instructions.toArray()) {
               if (isReturn(insn)) {
                  InsnList hook = new InsnList();
                  hook.add(new MethodInsnNode(184, "kat/respect/b2/injector/RespectHookBridge", "onClientTick", "()V", false));
                  mn.instructions.insertBefore(insn, hook);
                  modified = true;
               }
            }
         }
      }

      return modified;
   }

   private boolean transformLocalPlayer(ClassNode cn) {
      boolean modified = false;

      for (MethodNode mn : cn.methods) {
         if (matches(mn.name, "sendPosition", "sendMovementPackets", "method_3136", "m_108705_")
            && mn.desc.equals("()V")
            && !hasCall(mn, "onSendMovementPacketsHead")) {
            InsnList head = new InsnList();
            head.add(new VarInsnNode(25, 0));
            head.add(new MethodInsnNode(184, "kat/respect/b2/injector/RespectHookBridge", "onSendMovementPacketsHead", "(Ljava/lang/Object;)V", false));
            mn.instructions.insert(head);

            for (AbstractInsnNode insn : mn.instructions.toArray()) {
               if (isReturn(insn)) {
                  InsnList tail = new InsnList();
                  tail.add(new VarInsnNode(25, 0));
                  tail.add(new MethodInsnNode(184, "kat/respect/b2/injector/RespectHookBridge", "onSendMovementPacketsTail", "(Ljava/lang/Object;)V", false));
                  mn.instructions.insertBefore(insn, tail);
               }
            }

            modified = true;
         }
      }

      return modified;
   }

   private static boolean matches(String name, String... targets) {
      for (String t : targets) {
         if (name.equals(t)) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasCall(MethodNode mn, String methodName) {
      for (AbstractInsnNode insn : mn.instructions.toArray()) {
         if (insn instanceof MethodInsnNode min && min.name.equals(methodName)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isReturn(AbstractInsnNode insn) {
      int op = insn.getOpcode();
      return op >= 172 && op <= 177;
   }
}
