package kat.respect.b2.module.setting;

public class NumberSetting extends Setting<Double> {
   private final double min;
   private final double max;
   private final double increment;
   private double value;

   public NumberSetting(String name, double val1, double val2, double val3, double increment) {
      this(name, "", val1, val2, val3, increment);
   }

   public NumberSetting(String name, String description, double val1, double val2, double val3, double increment) {
      super(name, description);
      double calculatedMin;
      double calculatedMax;
      double initialVal;
      if (val1 < val2 && val2 < val3) {
         calculatedMin = val1;
         calculatedMax = val3;
         initialVal = val2;
      } else if (val2 < val1 && val1 < val3) {
         calculatedMin = val2;
         calculatedMax = val3;
         initialVal = val1;
      } else if (val1 < val3 && val3 < val2) {
         calculatedMin = val1;
         calculatedMax = val2;
         initialVal = val3;
      } else {
         calculatedMin = Math.min(val1, Math.min(val2, val3));
         calculatedMax = Math.max(val1, Math.max(val2, val3));
         initialVal = val1 >= calculatedMin && val1 <= calculatedMax ? val1 : (val2 >= calculatedMin && val2 <= calculatedMax ? val2 : val3);
      }

      this.min = calculatedMin;
      this.max = calculatedMax;
      this.increment = increment;
      this.value = this.clamp(initialVal);
   }

   private double clamp(double v) {
      v = Math.max(this.min, Math.min(this.max, v));
      if (this.increment > 0.0) {
         v = Math.round(v / this.increment) * this.increment;
      }

      return v;
   }

   public Double getValue() {
      return this.value;
   }

   public void setValue(Double value) {
      this.value = this.clamp(value);
   }

   public double getMin() {
      return this.min;
   }

   public double getMax() {
      return this.max;
   }

   public double getIncrement() {
      return this.increment;
   }

   public int getValueInt() {
      return (int)Math.round(this.value);
   }

   public float getValueFloat() {
      return (float)this.value;
   }

   public long getValueLong() {
      return Math.round(this.value);
   }
}
