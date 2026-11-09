package kat.respect.b2.gui.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kat.respect.b2.config.ConfigManager;

public final class HudLayout {
   public static final String ARRAYLIST = "arraylist";
   public static final String WATERMARK = "hud.watermark";
   public static final String COORDINATES = "hud.coordinates";
   public static final String FPS = "hud.fps";
   public static final String PING = "hud.ping";
   public static final String TIME = "hud.time";
   public static final String BPS = "hud.bps";
   public static final String TARGET_HUD = "hud.targethud";
   public static final String NOTIFICATIONS = "notifications";
   public static final String[] IDS = new String[]{
      "arraylist", "hud.watermark", "hud.coordinates", "hud.fps", "hud.ping", "hud.time", "hud.bps", "hud.targethud", "notifications"
   };
   private static final String[] LABELS = new String[]{"Arraylist", "Watermark", "Coordinates", "FPS", "Ping", "Time", "BPS", "TargetHUD", "Notifications"};
   public static final float MIN_SCALE = 0.5F;
   public static final float MAX_SCALE = 2.5F;
   private static final HudLayout INSTANCE = new HudLayout();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final Map<String, HudLayout.Entry> entries = new ConcurrentHashMap<>();
   private final Map<String, float[]> bounds = new ConcurrentHashMap<>();

   private HudLayout() {
      for (String id : IDS) {
         this.entries.put(id, new HudLayout.Entry());
      }
   }

   public static HudLayout get() {
      return INSTANCE;
   }

   public static HudLayout.Entry of(String id) {
      return INSTANCE.entries.computeIfAbsent(id, key -> new HudLayout.Entry());
   }

   public static String label(String id) {
      for (int i = 0; i < IDS.length; i++) {
         if (IDS[i].equals(id)) {
            return LABELS[i];
         }
      }

      return id;
   }

   public static float[] bounds(String id) {
      return INSTANCE.bounds.get(id);
   }

   public static void reportBounds(String id, float x, float y, float w, float h) {
      INSTANCE.bounds.put(id, new float[]{x, y, w, h});
   }

   public static void clearBounds(String id) {
      INSTANCE.bounds.remove(id);
   }

   public static float clampScale(float scale) {
      return Math.max(0.5F, Math.min(2.5F, scale));
   }

   public static boolean supportsScale(String id) {
      return id != null && !"notifications".equals(id);
   }

   public void reset(String id) {
      HudLayout.Entry entry = of(id);
      entry.offsetX = 0.0F;
      entry.offsetY = 0.0F;
      entry.scale = 1.0F;
      entry.enabled = true;
      this.markDirty();
   }

   public void resetAll() {
      for (String id : IDS) {
         this.reset(id);
      }

      this.markDirty();
   }

   public Path file() {
      try {
         return ConfigManager.getInstance().getPaths().hud();
      } catch (Throwable var2) {
         return ConfigManager.getInstance().getConfigDir().resolve("hud.json");
      }
   }

   public void markDirty() {
      try {
         ConfigManager.getInstance().markDirty();
      } catch (Throwable var2) {
      }
   }

   public synchronized void save() {
      this.markDirty();

      try {
         ConfigManager.getInstance().markDirty();
      } catch (Throwable var2) {
      }
   }

   public synchronized void load() {
      try {
         Path path = this.file();
         if (!Files.exists(path)) {
            return;
         }

         JsonObject root = ConfigManager.getInstance().readJsonFile(path);
         if (root == null) {
            return;
         }

         JsonObject elements = root.has("elements") && root.get("elements").isJsonObject() ? root.getAsJsonObject("elements") : root;

         for (String id : IDS) {
            if (elements.has(id) && elements.get(id).isJsonObject()) {
               JsonObject node = elements.getAsJsonObject(id);
               HudLayout.Entry entry = of(id);
               if (node.has("x")) {
                  entry.offsetX = node.get("x").getAsFloat();
               }

               if (node.has("y")) {
                  entry.offsetY = node.get("y").getAsFloat();
               }

               if (node.has("scale")) {
                  entry.scale = clampScale(node.get("scale").getAsFloat());
               }

               if (node.has("enabled")) {
                  entry.enabled = node.get("enabled").getAsBoolean();
               }
            }
         }
      } catch (Throwable var10) {
      }
   }

   public static final class Entry {
      public float offsetX;
      public float offsetY;
      public float scale = 1.0F;
      public boolean enabled = true;
   }
}
