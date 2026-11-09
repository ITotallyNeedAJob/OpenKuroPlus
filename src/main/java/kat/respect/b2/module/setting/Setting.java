package kat.respect.b2.module.setting;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting<T> {
   private final String name;
   private final String description;
   private Supplier<Boolean> visibility = () -> true;
   private Object defaultValue;
   private boolean defaultCaptured;
   private Consumer<T> onChange;

   protected Setting(String name) {
      this(name, "");
   }

   protected Setting(String name, String description) {
      this.name = name;
      this.description = description;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public boolean isVisible() {
      return this.visibility.get();
   }

   public Setting<T> setVisibility(Supplier<Boolean> visibility) {
      this.visibility = visibility;
      return this;
   }

   public Setting<T> onChange(Consumer<T> onChange) {
      this.onChange = onChange;
      return this;
   }

   protected void notifyChange(T newValue) {
      if (this.onChange != null) {
         this.onChange.accept(newValue);
      }
   }

   public abstract T getValue();

   public abstract void setValue(T var1);

   public final void captureDefault() {
      if (!this.defaultCaptured) {
         this.defaultCaptured = true;
         this.defaultValue = copyOf(this.getValue());
      }
   }

   public final boolean isDefaultCaptured() {
      return this.defaultCaptured;
   }

   public final Object getDefaultValue() {
      return copyOf(this.defaultValue);
   }

   public final boolean isModified() {
      return !this.defaultCaptured ? false : !valuesEqual(this.getValue(), this.defaultValue);
   }

   public final void resetToDefault() {
      if (this.defaultCaptured) {
         this.setValue((T)copyOf(this.defaultValue));
      }
   }

   private static Object copyOf(Object value) {
      return value instanceof double[] arr ? arr.clone() : value;
   }

   private static boolean valuesEqual(Object a, Object b) {
      if (a == b) {
         return true;
      } else if (a != null && b != null) {
         if (a instanceof double[] x && b instanceof double[] y) {
            if (x.length != y.length) {
               return false;
            } else {
               for (int i = 0; i < x.length; i++) {
                  if (Math.abs(x[i] - y[i]) > 1.0E-6) {
                     return false;
                  }
               }

               return true;
            }
         } else {
            return a instanceof Number na && b instanceof Number nb
               ? Double.compare(na.doubleValue(), nb.doubleValue()) == 0 || Math.abs(na.doubleValue() - nb.doubleValue()) < 1.0E-6
               : Objects.equals(a, b);
         }
      } else {
         return false;
      }
   }
}
