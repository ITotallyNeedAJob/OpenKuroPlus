package kat.respect.b2.util;

import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;

public final class VulkanUtil {
   private static Boolean vulkanDetected = null;

   private VulkanUtil() {
   }

   public static boolean isVulkan() {
      if (vulkanDetected != null) {
         return vulkanDetected;
      } else if (FabricLoader.getInstance().isModLoaded("vulkanmod")) {
         vulkanDetected = true;
         return true;
      } else {
         try {
            Class<?> renderSystemClass = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
            Method getDeviceMethod = renderSystemClass.getMethod("getDevice");
            Object device = getDeviceMethod.invoke(null);
            if (device != null) {
               String devName = device.getClass().getName().toLowerCase();
               if (devName.contains("vulkan")) {
                  vulkanDetected = true;
                  return true;
               }

               try {
                  Method getBackend = device.getClass().getMethod("getBackend");
                  Object backend = getBackend.invoke(device);
                  if (backend != null && backend.getClass().getName().toLowerCase().contains("vulkan")) {
                     vulkanDetected = true;
                     return true;
                  }
               } catch (NoSuchMethodException var6) {
               }
            }
         } catch (Throwable var7) {
         }

         vulkanDetected = false;
         return false;
      }
   }

   public static boolean isOpenGL() {
      return !isVulkan();
   }
}
