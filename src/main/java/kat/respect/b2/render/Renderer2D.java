package kat.respect.b2.render;

import kat.respect.b2.render.mobile.MobileRenderer2D;
import kat.respect.b2.util.VulkanUtil;

public final class Renderer2D {
   private static final Blaze3DRenderer2D BLAZE3D = new Blaze3DRenderer2D();
   private static final MobileRenderer2D MOBILE = new MobileRenderer2D();

   private Renderer2D() {
   }

   public static boolean isMobileActive() {
      return !isForceVulkan();
   }

   public static boolean isForceVulkan() {
      return !Boolean.getBoolean("kuroplus.forceVulkan")
            && !Boolean.getBoolean("respect.forceVulkan")
            && !"blaze3d".equalsIgnoreCase(System.getProperty("kuroplus.renderer"))
            && !"blaze3d".equalsIgnoreCase(System.getProperty("respect.renderer"))
            && !"vulkan".equalsIgnoreCase(System.getProperty("kuroplus.renderer"))
            && !"vulkan".equalsIgnoreCase(System.getProperty("respect.renderer"))
         ? VulkanUtil.isVulkan()
         : true;
   }

   public static boolean isDirectGlActive() {
      return !isForceVulkan() && !VulkanUtil.isVulkan();
   }

   public static IRenderer2D get() {
      return (IRenderer2D)(isForceVulkan() ? BLAZE3D : MOBILE);
   }

   public static MobileRenderer2D getMobile() {
      return MOBILE;
   }

   public static Blaze3DRenderer2D getBlaze3D() {
      return BLAZE3D;
   }

   public static void logRendererStatus() {
      boolean mobile = isMobileActive();
      System.out.println("[KuroPlus/Render] Active 2D renderer: " + (mobile ? "Mobile (Shaders)" : "Blaze3D (Fallback)"));
      System.out
         .println("[KuroPlus/Render] isForceVulkan=" + isForceVulkan() + " (system property kuroplus.renderer=" + System.getProperty("kuroplus.renderer") + ")");
   }
}
