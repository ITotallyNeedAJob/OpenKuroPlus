package kat.respect.b2.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class ConfigCodec {
   public static final String PREFIX = "r1:";
   private static final Gson GSON = new Gson();

   private ConfigCodec() {
   }

   public static String encode(JsonObject root) {
      if (root == null) {
         return null;
      } else {
         try {
            byte[] raw = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            try (GZIPOutputStream gzip = new GZIPOutputStream(buffer)) {
               gzip.write(raw);
            }

            return "r1:" + Base64.getEncoder().encodeToString(buffer.toByteArray());
         } catch (Throwable var8) {
            return null;
         }
      }
   }

   public static JsonObject decode(String code) {
      if (code == null) {
         return null;
      } else {
         String payload = code.trim().replaceAll("\\s+", "");
         int colon = payload.indexOf(58);
         if (colon > 0 && colon <= 8) {
            payload = payload.substring(colon + 1);
         }

         if (payload.isEmpty()) {
            return null;
         } else {
            try {
               byte[] bytes = Base64.getDecoder().decode(payload);

               JsonObject var7;
               try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
                  String json = new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
                  JsonElement parsed = JsonParser.parseString(json);
                  var7 = parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
               }

               return var7;
            } catch (Throwable var10) {
               return null;
            }
         }
      }
   }
}
