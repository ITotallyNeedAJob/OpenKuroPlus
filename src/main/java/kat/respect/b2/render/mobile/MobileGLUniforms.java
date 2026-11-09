package kat.respect.b2.render.mobile;

import java.nio.FloatBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public final class MobileGLUniforms {
   private static final Map<String, Integer> LOCATION_CACHE = new ConcurrentHashMap<>();

   private MobileGLUniforms() {
   }

   private static int getLocation(int programId, String name) {
      String key = programId + ":" + name;
      return LOCATION_CACHE.computeIfAbsent(key, k -> GL20.glGetUniformLocation(programId, name));
   }

   public static void uniform1i(int programId, String name, int i) {
      GL20.glUniform1i(getLocation(programId, name), i);
   }

   public static void uniformTexture(int programId, String name, int texture) {
      GL20.glActiveTexture(33984);
      GL20.glBindTexture(3553, texture);
      uniform1i(programId, name, 0);
   }

   public static void uniform1ui(int programId, String name, int i) {
      GL30.glUniform1ui(getLocation(programId, name), i);
   }

   public static void uniform2i(int programId, String name, int i, int j) {
      GL20.glUniform2i(getLocation(programId, name), i, j);
   }

   public static void uniform1f(int programId, String name, float f) {
      GL20.glUniform1f(getLocation(programId, name), f);
   }

   public static void uniform2f(int programId, String name, float f, float g) {
      GL20.glUniform2f(getLocation(programId, name), f, g);
   }

   public static void uniform3f(int programId, String name, float f, float g, float h) {
      GL20.glUniform3f(getLocation(programId, name), f, g, h);
   }

   public static void uniform3i(int programId, String name, int f, int g, int h) {
      GL20.glUniform3i(getLocation(programId, name), f, g, h);
   }

   public static void uniform4f(int programId, String name, float f, float g, float h, float i) {
      GL20.glUniform4f(getLocation(programId, name), f, g, h, i);
   }

   public static void uniformMatrix4f(int programId, String name, Matrix4f matrix) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         FloatBuffer buffer = stack.mallocFloat(16);
         matrix.get(buffer);
         GL20.glUniformMatrix4fv(getLocation(programId, name), false, buffer);
      } catch (Throwable var7) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (stack != null) {
         stack.close();
      }
   }
}
