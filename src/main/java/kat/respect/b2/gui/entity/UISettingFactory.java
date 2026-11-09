package kat.respect.b2.gui.entity;

import java.util.ArrayList;
import java.util.List;
import kat.respect.b2.gui.GuiTheme;
import kat.respect.b2.module.Module;
import kat.respect.b2.module.setting.BooleanSetting;
import kat.respect.b2.module.setting.ColorSetting;
import kat.respect.b2.module.setting.EnumSetting;
import kat.respect.b2.module.setting.KeySetting;
import kat.respect.b2.module.setting.ModeSetting;
import kat.respect.b2.module.setting.NumberSetting;
import kat.respect.b2.module.setting.RangeSetting;
import kat.respect.b2.module.setting.Setting;
import kat.respect.b2.module.setting.StringSetting;

public final class UISettingFactory {
   private UISettingFactory() {
   }

   public static List<UIEntity> createEntitiesForModule(Module module) {
      List<UIEntity> list = new ArrayList<>();
      if (module == null) {
         return list;
      } else {
         UIKeybind keybind = new UIKeybind("Keybind", module::getKey);
         keybind.onBind(module::setKey);
         keybind.fillWidth(true);
         keybind.margin(3.0F);
         list.add(keybind);

         for (Setting<?> setting : module.getSettings()) {
            UIEntity entity = createForSetting(setting);
            if (entity != null) {
               entity.fillWidth(true);
               entity.margin(3.0F);
               list.add(entity);
            }
         }

         return list;
      }
   }

   public static UIEntity createForSetting(Setting<?> setting) {
      if (setting == null) {
         return null;
      } else if (setting instanceof BooleanSetting bs) {
         UIContainer row = UIContainer.row(6.0F);
         row.fillWidth(true);
         row.size(0.0F, 20.0F);
         UIText label = new UIText(bs.getName()).size(9.5F).color(GuiTheme.current().fontColor | 0xFF000000);
         label.fillWidth(true);
         label.fillHeight(true);
         label.centerY();
         UISwitch sw = new UISwitch(bs.getValue());
         sw.onToggle(val -> bs.setValue(val));
         row.add(label);
         row.add(sw);
         return row;
      } else if (setting instanceof NumberSetting ns) {
         UISlider slider = new UISlider(ns.getName(), ns.getMin(), ns.getMax(), ns.getValue(), ns.getIncrement());
         slider.onChange(ns::setValue);
         slider.fillWidth(true);
         return slider;
      } else if (setting instanceof ModeSetting ms) {
         UIDropdown dd = new UIDropdown(ms.getName(), ms.getModes(), ms.getValue());
         dd.onSelect(ms::setValue);
         dd.fillWidth(true);
         return dd;
      } else if (!(setting instanceof EnumSetting<?> es)) {
         if (setting instanceof ColorSetting cs) {
            UIColorPicker cp = new UIColorPicker(cs);
            cp.fillWidth(true);
            return cp;
         } else if (setting instanceof KeySetting ks) {
            UIKeybind kb = new UIKeybind(ks.getName(), ks::getValue);
            kb.onBind(ks::setValue);
            kb.fillWidth(true);
            return kb;
         } else if (setting instanceof StringSetting ss) {
            UIContainer row = UIContainer.column(4.0F);
            row.fillWidth(true);
            row.size(0.0F, 48.0F);
            UIText label = new UIText(ss.getName()).size(9.5F).color(GuiTheme.current().fontColor | 0xFF000000);
            UITextInput input = new UITextInput("Enter text...").text(ss.getValue());
            input.onChange(ss::setValue);
            input.fillWidth(true);
            row.add(label);
            row.add(input);
            return row;
         } else if (setting instanceof RangeSetting rs) {
            UIContainer col = UIContainer.column(4.0F);
            col.fillWidth(true);
            col.size(0.0F, 52.0F);
            UISlider minSlider = new UISlider(rs.getName() + " Min", rs.getMin(), rs.getMax(), rs.getMinValue(), rs.getIncrement());
            UISlider maxSlider = new UISlider(rs.getName() + " Max", rs.getMin(), rs.getMax(), rs.getMaxValue(), rs.getIncrement());
            minSlider.onChange(rs::setMinValue);
            maxSlider.onChange(rs::setMaxValue);
            minSlider.fillWidth(true);
            maxSlider.fillWidth(true);
            col.add(minSlider);
            col.add(maxSlider);
            return col;
         } else {
            return null;
         }
      } else {
         List<String> modes = new ArrayList<>();

         for (Enum<?> e : (Enum[])es.getValue().getDeclaringClass().getEnumConstants()) {
            modes.add(e.name());
         }

         UIDropdown dd = new UIDropdown(es.getName(), modes, es.getValue().name());
         dd.onSelect(name -> {
            for (Enum<?> ex : (Enum[])es.getValue().getDeclaringClass().getEnumConstants()) {
               if (ex.name().equalsIgnoreCase(name)) {
                  setEnumHelper(es, ex);
                  break;
               }
            }
         });
         dd.fillWidth(true);
         return dd;
      }
   }

   private static <T extends Enum<T>> void setEnumHelper(EnumSetting<?> es, Enum<?> e) {
      ((EnumSetting)es).setValue(e);
   }
}
