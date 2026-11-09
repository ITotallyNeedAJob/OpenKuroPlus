package kat.respect.b2.module.setting;

public class CurveSetting extends Setting<double[]> {
   private final double[] defaultValue;
   private double[] value;

   public CurveSetting(String name, String description, double x1, double y1, double x2, double y2) {
      super(name, description);
      this.defaultValue = sanitize(new double[]{x1, y1, x2, y2});
      this.value = (double[])this.defaultValue.clone();
   }

   public CurveSetting(String name, String description) {
      this(name, description, 0.25, 0.1, 0.25, 1.0);
   }

   public double[] getValue() {
      return this.value;
   }

   public void setValue(double[] newValue) {
      this.value = sanitize(newValue);
      this.notifyChange(this.value);
   }

   public void setCurve(double x1, double y1, double x2, double y2) {
      this.setValue(new double[]{x1, y1, x2, y2});
   }

   public double[] getDefaultCurve() {
      return (double[])this.defaultValue.clone();
   }

   public void reset() {
      this.setValue((double[])this.defaultValue.clone());
   }

   public double controlX1() {
      return this.value[0];
   }

   public double controlY1() {
      return this.value[1];
   }

   public double controlX2() {
      return this.value[2];
   }

   public double controlY2() {
      return this.value[3];
   }

   public double sample(double x) {
      return sampleCurve(this.value, x);
   }

   public static double sampleCurve(double[] curve, double x) {
      if (curve != null && curve.length >= 4) {
         if (x <= 0.0) {
            return 0.0;
         } else if (x >= 1.0) {
            return 1.0;
         } else {
            double lo = 0.0;
            double hi = 1.0;
            double t = x;

            for (int i = 0; i < 24; i++) {
               t = (lo + hi) * 0.5;
               double u = 1.0 - t;
               double bx = 3.0 * u * u * t * curve[0] + 3.0 * u * t * t * curve[2] + t * t * t;
               if (bx < x) {
                  lo = t;
               } else {
                  hi = t;
               }
            }

            double u = 1.0 - t;
            double by = 3.0 * u * u * t * curve[1] + 3.0 * u * t * t * curve[3] + t * t * t;
            return Math.max(0.0, by);
         }
      } else {
         return Math.max(0.0, Math.min(1.0, x));
      }
   }

   private static double[] sanitize(double[] raw) {
      return raw != null && raw.length >= 4
         ? new double[]{clamp(raw[0], 0.0, 1.0), clamp(raw[1], -0.25, 1.5), clamp(raw[2], 0.0, 1.0), clamp(raw[3], -0.25, 1.5)}
         : new double[]{0.25, 0.1, 0.25, 1.0};
   }

   private static double clamp(double v, double lo, double hi) {
      return Math.max(lo, Math.min(hi, v));
   }
}
