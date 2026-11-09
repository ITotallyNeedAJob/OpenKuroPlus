package kat.respect.b2.module.setting;

public class StringSetting extends Setting<String> {
   private String value;

   public StringSetting(String name, String value) {
      this(name, "", value);
   }

   public StringSetting(String name, String description, String value) {
      super(name, description);
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }
}
