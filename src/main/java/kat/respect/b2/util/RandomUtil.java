package kat.respect.b2.util;

import java.util.Random;

public class RandomUtil {
   public static final RandomUtil INSTANCE = new RandomUtil();
   private final Random random = new Random();

   public Random getRandom() {
      return this.random;
   }

   public int randomInRange(int min, int max) {
      return min >= max ? min : min + this.random.nextInt(max - min + 1);
   }

   public float randomInRange(float min, float max) {
      return MathUtil.findMiddleValue(min + this.random.nextFloat() * (max - min), min, max);
   }

   public double randomInRange(double min, double max) {
      return min >= max ? min : min + this.random.nextDouble() * (max - min);
   }
}
