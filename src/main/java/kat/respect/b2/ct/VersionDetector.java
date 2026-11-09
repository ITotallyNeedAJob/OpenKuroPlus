package kat.respect.b2.ct;

import java.lang.reflect.Method;

public final class VersionDetector {
   private static volatile GameVersion cached;
   private static volatile String cachedId;

   private VersionDetector() {
   }

   public static GameVersion detect() {
      GameVersion hit = cached;
      if (hit != null) {
         return hit;
      } else {
         synchronized (VersionDetector.class) {
            if (cached != null) {
               return cached;
            } else {
               String id = detectVersionId();
               cachedId = id;
               cached = GameVersion.fromId(id);
               System.out.println("[Respect] detected Minecraft version: " + (id == null ? "<unknown> -> " + cached : id + " -> " + cached));
               return cached;
            }
         }
      }
   }

   public static String versionId() {
      detect();
      return cachedId;
   }

   private static String detectVersionId() {
      String id = viaSharedConstants();
      if (id != null) {
         return id;
      } else {
         id = viaMinecraftInstance();
         return id != null ? id : viaProbing();
      }
   }

   private static String viaSharedConstants() {
      try {
         Class<?> shared = Class.forName("net.minecraft.SharedConstants");
         Object version = shared.getMethod("getCurrentVersion").invoke(null);
         if (version == null) {
            return null;
         } else {
            try {
               if (version.getClass().getMethod("id").invoke(version) instanceof String s && !s.isBlank()) {
                  return s;
               }
            } catch (NoSuchMethodException var4) {
            }

            String s = String.valueOf(version);
            return s.isBlank() ? null : s;
         }
      } catch (Throwable var5) {
         return null;
      }
   }

   private static String viaMinecraftInstance() {
      try {
         Class<?> mcClass = Class.forName("net.minecraft.client.Minecraft");
         Object mc = mcClass.getMethod("getInstance").invoke(null);
         if (mc == null) {
            return null;
         }

         if (mcClass.getMethod("getLaunchedVersion").invoke(mc) instanceof String s && !s.isBlank()) {
            return s;
         }
      } catch (Throwable var4) {
      }

      return null;
   }

   private static String viaProbing() {
      if (present("com.mojang.blaze3d.TracyFrameCapture")) {
         return "26.probed";
      } else {
         return present("net.minecraft.client.renderer.SubmitNodeCollector") ? "1.21.probed" : null;
      }
   }

   private static boolean present(String className) {
      try {
         Class.forName(className, false, VersionDetector.class.getClassLoader());
         return true;
      } catch (Throwable var2) {
         return false;
      }
   }

   public static boolean hasTracyFrameCapture() {
      return present("com.mojang.blaze3d.TracyFrameCapture");
   }

   public static boolean hasCameraRenderState() {
      return present("net.minecraft.client.renderer.CameraRenderState");
   }

   public static boolean hasSubmitNodeCollector() {
      return present("net.minecraft.client.renderer.SubmitNodeCollector");
   }

   public static boolean hasNewInputSystem() {
      return present("net.minecraft.client.input.KeyEvent");
   }

   public static boolean hasRaycastHitResult() {
      try {
         Class<?> lp = Class.forName("net.minecraft.client.player.LocalPlayer");

         for (Method m : lp.getDeclaredMethods()) {
            if (m.getName().equals("raycastHitResult")) {
               return true;
            }
         }

         return false;
      } catch (Throwable var5) {
         return false;
      }
   }

   static void resetForTests() {
      synchronized (VersionDetector.class) {
         cached = null;
         cachedId = null;
      }
   }
}
