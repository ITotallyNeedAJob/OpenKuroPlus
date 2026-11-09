package kat.respect.b2.module.setting;

public class ColorSetting extends Setting<Integer> {
   private int value;

   public ColorSetting(String name, int value) {
      this(name, "", value);
   }

   public ColorSetting(String name, String description, int value) {
      super(name, description);
      this.value = value;
   }

   public Integer getValue() {
      return this.value;
   }

   public void setValue(Integer value) {
      this.value = value;
   }

   public int getRed() {
      return this.value >> 16 & 0xFF;
   }

   public int getGreen() {
      return this.value >> 8 & 0xFF;
   }

   public int getBlue() {
      return this.value & 0xFF;
   }

   public int getAlpha() {
      return this.value >> 24 & 0xFF;
   }
}
