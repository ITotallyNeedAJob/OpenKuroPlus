package kat.respect.b2.module.setting;

import java.util.Arrays;
import java.util.List;

public class EnumSetting<E extends Enum<E>> extends Setting<E> {
   private final Class<E> type;
   private E value;

   public EnumSetting(String name, E value) {
      this(name, "", value);
   }

   public EnumSetting(String name, String description, E value) {
      super(name, description);
      this.type = value.getDeclaringClass();
      this.value = value;
   }

   public E getValue() {
      return this.value;
   }

   public void setValue(E value) {
      this.value = value;
   }

   public void setValueByName(String name) {
      if (name != null) {
         for (E constant : this.type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(name)) {
               this.value = constant;
               break;
            }
         }
      }
   }

   public List<E> getValues() {
      return Arrays.asList(this.type.getEnumConstants());
   }

   public void cycle() {
      E[] constants = this.type.getEnumConstants();
      this.value = constants[(this.value.ordinal() + 1) % constants.length];
   }

   public boolean is(E other) {
      return this.value == other;
   }
}
