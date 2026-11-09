package kat.respect.b2.util.animation;

import java.util.function.Supplier;

public class Animation {
   private final Supplier<Float> length;
   private final Supplier<Easing> easing;
   private long lastMillis = 0L;
   private final boolean initialState;
   private boolean state = false;

   public Animation(Supplier<Float> length, boolean initialState, Supplier<Easing> easing) {
      this.length = length;
      this.initialState = initialState;
      this.setState(initialState);
      this.easing = easing;
   }

   public Animation(float length, boolean initialState, Easing easing) {
      this(() -> length, initialState, () -> easing);
   }

   public float getAnimationFactor() {
      return (float)this.easing.get().ease(this.getLinearFactor());
   }

   public void resetToDefault() {
      this.state = this.initialState;
      this.lastMillis = (long)(
         this.initialState
            ? System.currentTimeMillis() - (1.0 - this.getLinearFactor()) * this.length.get().floatValue()
            : System.currentTimeMillis() - this.getLinearFactor() * this.length.get().floatValue()
      );
   }

   public boolean getState() {
      return this.state;
   }

   public void setState(boolean in) {
      this.lastMillis = (long)(
         !in
            ? System.currentTimeMillis() - (1.0 - this.getLinearFactor()) * this.length.get().floatValue()
            : System.currentTimeMillis() - this.getLinearFactor() * this.length.get().floatValue()
      );
      this.state = in;
   }

   public double getLinearFactor() {
      return this.state
         ? this.clamp((float)(System.currentTimeMillis() - this.lastMillis) / this.length.get())
         : this.clamp(1.0F - (float)(System.currentTimeMillis() - this.lastMillis) / this.length.get());
   }

   private double clamp(double in) {
      return in < 0.0 ? 0.0 : Math.min(in, 1.0);
   }
}
