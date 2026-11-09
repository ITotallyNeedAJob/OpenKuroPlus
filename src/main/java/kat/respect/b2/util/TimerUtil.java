package kat.respect.b2.util;

public class TimerUtil {
   public long lastMs = System.currentTimeMillis();

   public boolean delay(float ms) {
      return (float)(System.currentTimeMillis() - this.lastMs) > ms;
   }

   public float getElapsedTime() {
      return (float)(System.currentTimeMillis() - this.lastMs);
   }

   public void reset() {
      this.lastMs = System.currentTimeMillis();
   }
}
