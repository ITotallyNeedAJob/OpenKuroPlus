package kat.respect.b2.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.ModuleManager;
import kat.respect.b2.module.setting.BooleanSetting;
import kat.respect.b2.module.setting.ColorSetting;
import kat.respect.b2.module.setting.CurveSetting;
import kat.respect.b2.module.setting.EnumSetting;
import kat.respect.b2.module.setting.KeySetting;
import kat.respect.b2.module.setting.ModeSetting;
import kat.respect.b2.module.setting.NumberSetting;
import kat.respect.b2.module.setting.RangeSetting;
import kat.respect.b2.module.setting.Setting;
import kat.respect.b2.module.setting.StringSetting;

public final class ConfigSerializer {
   private static final Map<String, JsonObject> UNKNOWN_SETTINGS = new ConcurrentHashMap<>();

   private ConfigSerializer() {
   }

   public static String getModuleId(Module module) {
      if (module == null) {
         return "";
      } else {
         try {
            Method m = module.getClass().getMethod("getId");
            if (m.invoke(module) instanceof String s && !s.isBlank()) {
               return s;
            }
         } catch (Throwable var4) {
         }

         return module.getName();
      }
   }

   public static Module findModule(String idOrName) {
      if (idOrName != null && !idOrName.isBlank() && ModuleManager.INSTANCE != null) {
         for (Module m : ModuleManager.INSTANCE.getModules()) {
            if (getModuleId(m).equalsIgnoreCase(idOrName) || m.getName().equalsIgnoreCase(idOrName)) {
               return m;
            }
         }

         for (Module mx : ModuleManager.INSTANCE.getModules()) {
            String idClean = getModuleId(mx).replaceAll("[^a-zA-Z0-9]", "");
            String nameClean = mx.getName().replaceAll("[^a-zA-Z0-9]", "");
            String targetClean = idOrName.replaceAll("[^a-zA-Z0-9]", "");
            if (idClean.equalsIgnoreCase(targetClean) || nameClean.equalsIgnoreCase(targetClean)) {
               return mx;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static boolean hasUnknownSettings(Module module) {
      if (module == null) {
         return false;
      } else {
         JsonObject unk = UNKNOWN_SETTINGS.get(getModuleId(module));
         return unk != null && !unk.isEmpty();
      }
   }

   public static JsonObject serializeModule(Module module) {
      return serializeModule(module, false);
   }

   public static JsonObject serializeModule(Module module, boolean onlyModifiedSettings) {
      if (module == null) {
         return new JsonObject();
      } else {
         JsonObject obj = new JsonObject();
         obj.addProperty("enabled", module.isEnabled());
         obj.addProperty("key", module.getKey());
         JsonObject settingsObj = new JsonObject();

         for (Setting<?> setting : module.getSettings()) {
            if (!onlyModifiedSettings || setting.isModified()) {
               settingsObj.add(setting.getName(), serializeSetting(setting));
            }
         }

         obj.add("settings", settingsObj);
         String id = getModuleId(module);
         JsonObject unknown = UNKNOWN_SETTINGS.get(id);
         if (unknown != null && !unknown.isEmpty()) {
            obj.add("_unknown", unknown.deepCopy());
         }

         return obj;
      }
   }

   public static void deserializeModule(Module module, JsonObject moduleObj) {
      if (module != null && moduleObj != null) {
         String id = getModuleId(module);
         if (moduleObj.has("_unknown") && moduleObj.get("_unknown").isJsonObject()) {
            JsonObject unkObj = moduleObj.getAsJsonObject("_unknown");
            JsonObject current = UNKNOWN_SETTINGS.computeIfAbsent(id, k -> new JsonObject());

            for (Entry<String, JsonElement> entry : unkObj.entrySet()) {
               current.add(entry.getKey(), entry.getValue().deepCopy());
            }
         }

         JsonElement key = moduleObj.has("key") ? moduleObj.get("key") : moduleObj.get("k");
         if (key != null && key.isJsonPrimitive()) {
            try {
               int k = key.getAsInt();
               if (k >= -1 && k <= 348) {
                  module.setKey(k);
               } else {
                  module.setKey(-1);
               }
            } catch (Throwable var14) {
               module.setKey(-1);
            }
         }

         JsonElement settings = moduleObj.has("settings") ? moduleObj.get("settings") : moduleObj.get("s");
         if (settings != null && settings.isJsonObject()) {
            JsonObject settingsObj = settings.getAsJsonObject();

            for (Entry<String, JsonElement> entry : settingsObj.entrySet()) {
               String settingName = entry.getKey();
               JsonElement element = entry.getValue();
               Setting<?> matched = null;

               for (Setting<?> s : module.getSettings()) {
                  if (s.getName().equalsIgnoreCase(settingName)) {
                     matched = s;
                     break;
                  }
               }

               if (matched != null) {
                  deserializeSetting(matched, element);
               } else if (!"_unknown".equals(settingName)) {
                  JsonObject current = UNKNOWN_SETTINGS.computeIfAbsent(id, k -> new JsonObject());
                  current.add(settingName, element.deepCopy());
               }
            }
         }

         JsonElement enabled = moduleObj.has("enabled") ? moduleObj.get("enabled") : moduleObj.get("e");
         if (enabled != null && enabled.isJsonPrimitive()) {
            try {
               module.setEnabled(enabled.getAsBoolean());
            } catch (Throwable var13) {
               module.setEnabled(false);
            }
         }
      }
   }

   public static JsonObject serializeSetting(Setting<?> setting) {
      JsonObject out = new JsonObject();
      if (setting instanceof BooleanSetting bs) {
         out.addProperty("type", "boolean");
         out.addProperty("value", bs.getValue());
      } else if (setting instanceof NumberSetting ns) {
         out.addProperty("type", "number");
         out.addProperty("value", ns.getValue());
      } else if (setting instanceof RangeSetting rs) {
         out.addProperty("type", "range");
         JsonObject value = new JsonObject();
         value.addProperty("min", rs.getMinValue());
         value.addProperty("max", rs.getMaxValue());
         out.add("value", value);
      } else if (setting instanceof ModeSetting ms) {
         out.addProperty("type", "mode");
         out.addProperty("value", ms.getValue());
      } else if (setting instanceof EnumSetting<?> es) {
         out.addProperty("type", "enum");
         out.addProperty("value", es.getValue().name());
      } else if (setting instanceof KeySetting ks) {
         out.addProperty("type", "key");
         out.addProperty("value", ks.getValue());
      } else if (setting instanceof ColorSetting cs) {
         out.addProperty("type", "color");
         out.addProperty("value", cs.getValue());
      } else if (setting instanceof StringSetting ss) {
         out.addProperty("type", "string");
         out.addProperty("value", ss.getValue());
      } else if (setting instanceof CurveSetting cs) {
         out.addProperty("type", "curve");
         JsonObject value = new JsonObject();
         value.addProperty("x1", cs.controlX1());
         value.addProperty("y1", cs.controlY1());
         value.addProperty("x2", cs.controlX2());
         value.addProperty("y2", cs.controlY2());
         out.add("value", value);
      } else {
         out.addProperty("type", "unknown");
         out.addProperty("value", String.valueOf(setting.getValue()));
      }

      return out;
   }

   public static void deserializeSetting(Setting<?> setting, JsonElement element) {
      if (setting != null && element != null) {
         try {
            JsonElement value = element;
            if (element.isJsonObject() && element.getAsJsonObject().has("value")) {
               value = element.getAsJsonObject().get("value");
            }

            if (setting instanceof BooleanSetting bs) {
               if (value.isJsonPrimitive()) {
                  bs.setValue(value.getAsBoolean());
               }
            } else if (setting instanceof NumberSetting ns) {
               if (value.isJsonPrimitive()) {
                  double v = value.getAsDouble();
                  double clamped = Math.max(ns.getMin(), Math.min(ns.getMax(), v));
                  ns.setValue(clamped);
               }
            } else if (setting instanceof RangeSetting rs) {
               if (value.isJsonObject()) {
                  JsonObject obj = value.getAsJsonObject();
                  if (obj.has("min") && obj.has("max")) {
                     double minVal = obj.get("min").getAsDouble();
                     double maxVal = obj.get("max").getAsDouble();
                     minVal = Math.max(rs.getMin(), Math.min(rs.getMax(), minVal));
                     maxVal = Math.max(rs.getMin(), Math.min(rs.getMax(), maxVal));
                     if (minVal > maxVal) {
                        maxVal = minVal;
                     }

                     rs.setMinValue(minVal);
                     rs.setMaxValue(maxVal);
                  }
               }
            } else if (setting instanceof ModeSetting ms) {
               if (value.isJsonPrimitive()) {
                  String modeStr = value.getAsString();
                  if (ms.getModes().contains(modeStr)) {
                     ms.setValue(modeStr);
                  } else {
                     System.err
                        .println("[Kuro+] Warning: Invalid mode '" + modeStr + "' for setting '" + ms.getName() + "', keeping default '" + ms.getValue() + "'");
                  }
               }
            } else if (setting instanceof EnumSetting<?> es) {
               if (value.isJsonPrimitive()) {
                  String enumName = value.getAsString();
                  boolean matched = false;

                  for (Enum<?> constant : es.getValues()) {
                     if (constant.name().equalsIgnoreCase(enumName)) {
                        es.setValueByName(constant.name());
                        matched = true;
                        break;
                     }
                  }

                  if (!matched) {
                     System.err
                        .println(
                           "[Kuro+] Warning: Invalid enum value '"
                              + enumName
                              + "' for setting '"
                              + es.getName()
                              + "', keeping default '"
                              + es.getValue().name()
                              + "'"
                        );
                  }
               }
            } else if (setting instanceof KeySetting ks) {
               if (value.isJsonPrimitive()) {
                  int k = value.getAsInt();
                  if (k >= -1 && k <= 348) {
                     ks.setValue(k);
                  } else {
                     System.err.println("[Kuro+] Warning: Invalid key code " + k + " for setting '" + ks.getName() + "', setting to GLFW_KEY_UNKNOWN");
                     ks.setValue(-1);
                  }
               }
            } else if (setting instanceof ColorSetting cs) {
               if (value.isJsonPrimitive()) {
                  cs.setValue(value.getAsInt());
               }
            } else if (setting instanceof StringSetting ss) {
               if (value.isJsonPrimitive()) {
                  ss.setValue(value.getAsString());
               }
            } else if (setting instanceof CurveSetting csx && value.isJsonObject()) {
               JsonObject obj = value.getAsJsonObject();
               if (obj.has("x1") && obj.has("y1") && obj.has("x2") && obj.has("y2")) {
                  double x1 = Math.max(0.0, Math.min(1.0, obj.get("x1").getAsDouble()));
                  double y1 = obj.get("y1").getAsDouble();
                  double x2 = Math.max(0.0, Math.min(1.0, obj.get("x2").getAsDouble()));
                  double y2 = obj.get("y2").getAsDouble();
                  csx.setCurve(x1, y1, x2, y2);
               }
            }
         } catch (Throwable var21) {
            System.err.println("[Kuro+] Warning: Failed to deserialize setting '" + setting.getName() + "': " + var21);
         }
      }
   }

   public static void recordUnknownSetting(String moduleId, String settingName, JsonElement element) {
      if (moduleId != null && settingName != null && element != null) {
         JsonObject obj = UNKNOWN_SETTINGS.computeIfAbsent(moduleId, k -> new JsonObject());
         obj.add(settingName, element.deepCopy());
      }
   }

   public static JsonObject getUnknownSettings(String moduleId) {
      return moduleId == null ? null : UNKNOWN_SETTINGS.get(moduleId);
   }

   public static void clearUnknownSettings(String moduleId) {
      if (moduleId != null) {
         UNKNOWN_SETTINGS.remove(moduleId);
      }
   }

   public static void clearAllUnknownSettings() {
      UNKNOWN_SETTINGS.clear();
   }
}
