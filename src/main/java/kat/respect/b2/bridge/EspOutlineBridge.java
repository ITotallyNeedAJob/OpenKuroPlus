package kat.respect.b2.bridge;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public final class EspOutlineBridge {
   public static final int NO_OUTLINE = 0;
   private static volatile EspOutlineBridge.OutlineSource source;
   private static volatile Identifier postChainId;
   private static volatile boolean bodyOnlySilhouette = true;

   private EspOutlineBridge() {
   }

   public static void install(EspOutlineBridge.OutlineSource outlineSource) {
      source = outlineSource;
   }

   public static void uninstall() {
      source = null;
      postChainId = null;
   }

   public static boolean isBodyOnlySilhouette() {
      return bodyOnlySilhouette;
   }

   public static void setBodyOnlySilhouette(boolean bodyOnly) {
      bodyOnlySilhouette = bodyOnly;
   }

   public static int outlineColor(Entity entity) {
      EspOutlineBridge.OutlineSource current = source;
      if (current != null && entity != null) {
         try {
            return current.outlineColor(entity);
         } catch (Throwable var3) {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static boolean hasPostChain() {
      return source != null && postChainId != null;
   }

   public static Identifier postChainId() {
      return source == null ? null : postChainId;
   }

   public static void setPostChainId(Identifier id) {
      postChainId = id;
   }

   @FunctionalInterface
   public interface OutlineSource {
      int outlineColor(Entity var1);
   }
}
