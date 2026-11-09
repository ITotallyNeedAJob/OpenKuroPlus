package kat.respect.b2.module.setting;

public class BooleanSetting extends Setting<Boolean> {
   private boolean value;

   public BooleanSetting(String name, boolean value) {
      this(name, "", value);
   }

   public BooleanSetting(String name, String description, boolean value) {
      super(name, description);
      this.value = value;
   }

   public Boolean getValue() {
      return this.value;
   }

   public void setValue(Boolean value) {
      this.value = value;
      this.notifyChange(value);
   }

   public void toggle() {
      this.setValue(!this.value);
   }
}
