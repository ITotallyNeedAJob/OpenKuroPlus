package kat.respect.b2.module.setting;

import java.util.List;

public class ModeSetting extends Setting<String> {
   private final List<String> modes;
   private int index;

   public ModeSetting(String name, String defaultValue, String... modes) {
      super(name, "");
      this.modes = List.of(modes);
      this.index = Math.max(0, this.modes.indexOf(defaultValue));
   }

   public ModeSetting(String name, String description, String defaultValue, List<String> modes) {
      super(name, description);
      this.modes = List.copyOf(modes);
      this.index = Math.max(0, this.modes.indexOf(defaultValue));
   }

   public String getValue() {
      return this.modes.get(this.index);
   }

   public void setValue(String value) {
      int i = this.modes.indexOf(value);
      if (i >= 0) {
         this.index = i;
      }

      this.notifyChange(this.getValue());
   }

   public List<String> getModes() {
      return this.modes;
   }

   public int getIndex() {
      return this.index;
   }

   public void cycle() {
      this.index = (this.index + 1) % this.modes.size();
      this.notifyChange(this.getValue());
   }

   public void cycleBack() {
      this.index = (this.index - 1 + this.modes.size()) % this.modes.size();
      this.notifyChange(this.getValue());
   }

   public boolean isMode(String mode) {
      return this.getValue().equals(mode);
   }
}
