package kat.respect.b2.module.setting;

public class RangeSetting extends Setting<RangeSetting.Range> {
   private final double min;
   private final double max;
   private final double increment;
   private double minValue;
   private double maxValue;

   public RangeSetting(String name, double min, double max, double minValue, double maxValue, double increment) {
      super(name);
      this.min = min;
      this.max = max;
      this.increment = increment;
      this.minValue = this.clamp(minValue);
      this.maxValue = this.clamp(maxValue);
      if (this.minValue > this.maxValue) {
         this.maxValue = this.minValue;
      }
   }

   private double clamp(double v) {
      v = Math.max(this.min, Math.min(this.max, v));
      if (this.increment > 0.0) {
         v = Math.round(v / this.increment) * this.increment;
      }

      return v;
   }

   public RangeSetting.Range getValue() {
      return new RangeSetting.Range(this.minValue, this.maxValue);
   }

   public void setValue(RangeSetting.Range value) {
      this.minValue = this.clamp(value.min());
      this.maxValue = this.clamp(value.max());
      if (this.minValue > this.maxValue) {
         this.maxValue = this.minValue;
      }
   }

   public double getMinValue() {
      return this.minValue;
   }

   public double getMaxValue() {
      return this.maxValue;
   }

   public void setMinValue(double v) {
      this.minValue = this.clamp(v);
      if (this.minValue > this.maxValue) {
         this.maxValue = this.minValue;
      }
   }

   public void setMaxValue(double v) {
      this.maxValue = this.clamp(v);
      if (this.maxValue < this.minValue) {
         this.minValue = this.maxValue;
      }
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

   public double getRandomValue() {
      return this.minValue >= this.maxValue ? this.minValue : this.minValue + Math.random() * (this.maxValue - this.minValue);
   }

   public int getRandomValueInt() {
      return (int)Math.round(this.getRandomValue());
   }

   public record Range(double min, double max) {
   }
}
