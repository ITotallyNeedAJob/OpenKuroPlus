package kat.respect.b2.render.mobile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import kat.respect.b2.KuroPlusMod;
import org.lwjgl.opengl.GL20;

public final class MobileShaderUtil {
   private MobileShaderUtil() {
   }

   public static int createShader(String fragmentResource, String vertexResource) {
      String fragmentSource = getShaderResource(fragmentResource);
      String vertexSource = getShaderResource(vertexResource);
      if (fragmentSource != null && vertexSource != null) {
         int fragmentId = GL20.glCreateShader(35632);
         int vertexId = GL20.glCreateShader(35633);
         GL20.glShaderSource(fragmentId, fragmentSource);
         GL20.glShaderSource(vertexId, vertexSource);
         GL20.glCompileShader(fragmentId);
         GL20.glCompileShader(vertexId);
         if (isShaderInvalid(fragmentId, fragmentResource)) {
            GL20.glDeleteShader(fragmentId);
            GL20.glDeleteShader(vertexId);
            return -1;
         } else if (isShaderInvalid(vertexId, vertexResource)) {
            GL20.glDeleteShader(fragmentId);
            GL20.glDeleteShader(vertexId);
            return -1;
         } else {
            int programId = GL20.glCreateProgram();
            GL20.glAttachShader(programId, fragmentId);
            GL20.glAttachShader(programId, vertexId);
            GL20.glLinkProgram(programId);
            int linkStatus = GL20.glGetProgrami(programId, 35714);
            if (linkStatus == 0) {
               String log = GL20.glGetProgramInfoLog(programId, 8192);
               System.err.println("[Kuro+][mobile] Program link failed (" + fragmentResource + " + " + vertexResource + "): " + log);
               GL20.glDeleteShader(fragmentId);
               GL20.glDeleteShader(vertexId);
               GL20.glDeleteProgram(programId);
               return -1;
            } else {
               GL20.glDeleteShader(fragmentId);
               GL20.glDeleteShader(vertexId);
               return programId;
            }
         }
      } else {
         System.err.println("[Kuro+][mobile] Failed to find shader sources: " + fragmentResource + " / " + vertexResource);
         return -1;
      }
   }

   private static boolean isShaderInvalid(int shaderId, String source) {
      boolean compiled = GL20.glGetShaderi(shaderId, 35713) == 1;
      if (compiled) {
         return false;
      } else {
         String shaderLog = GL20.glGetShaderInfoLog(shaderId, 8192);
         System.err.println("[Kuro+][mobile] Error compiling shader: " + source);
         System.err.println("----------------------------------------");
         System.err.println(shaderLog);
         return true;
      }
   }

   public static InputStream getResourceStream(String path) {
      String clean = path.startsWith("/") ? path.substring(1) : path;
      String slash = "/" + clean;
      InputStream is = MobileShaderUtil.class.getClassLoader().getResourceAsStream(clean);
      if (is != null) {
         return is;
      } else {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         if (cl != null) {
            is = cl.getResourceAsStream(clean);
            if (is != null) {
               return is;
            }
         }

         is = MobileShaderUtil.class.getResourceAsStream(slash);
         if (is != null) {
            return is;
         } else {
            try {
               is = KuroPlusMod.class.getClassLoader().getResourceAsStream(clean);
               return is != null ? is : KuroPlusMod.class.getResourceAsStream(slash);
            } catch (Throwable var6) {
               return null;
            }
         }
      }
   }

   public static String getShaderResource(String resource) {
      String cleanResource = resource.startsWith("/") ? resource.substring(1) : resource;
      String[] candidatePaths = new String[]{"assets/kuroplus/mobile/shaders/" + cleanResource, cleanResource};

      for (String candidate : candidatePaths) {
         try (InputStream in = getResourceStream(candidate)) {
            if (in != null) {
               return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
         } catch (Exception var12) {
         }
      }

      System.err.println("[kuroplus-mobile] Could not find shader resource: " + resource);
      return null;
   }
}
