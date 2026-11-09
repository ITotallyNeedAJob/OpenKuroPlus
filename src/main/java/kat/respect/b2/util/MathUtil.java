package kat.respect.b2.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MathUtil {
   public static final Random random = new Random(System.currentTimeMillis());

   public static int randomInt(int start, int bound) {
      return random.nextInt(start, bound);
   }

   public static double smoothStepLerp(double delta, double start, double end) {
      delta = Math.max(0.0, Math.min(1.0, delta));
      double t = delta * delta * (3.0 - 2.0 * delta);
      return start + (end - start) * t;
   }

   public static float lerp(float start, float end, float factor) {
      return start + (end - start) * factor;
   }

   public static float findMiddleValue(float f, float f2, float f3) {
      return f < f2 ? f2 : Math.min(f, f3);
   }

   public static float interpolate(float n, float n2, float n3) {
      return n - (n - n2) * findMiddleValue(n3, 0.0F, 1.0F);
   }

   public static double interpolate(double n, double n2, double n3) {
      return n - (n - n2) * Math.max(0.0, Math.min(1.0, n3));
   }

   public static float scaleAndRoundFloat(float n, int newScale) {
      return newScale >= 0 ? BigDecimal.valueOf((double)n).setScale(newScale, RoundingMode.FLOOR).floatValue() : -1.0F;
   }
}
