package kat.respect.b2.util.animation;

public enum Easing {
   LINEAR {
      @Override
      public double ease(double factor) {
         return factor;
      }
   },
   SINE_IN {
      @Override
      public double ease(double factor) {
         return 1.0 - Math.cos(factor * Math.PI / 2.0);
      }
   },
   SINE_OUT {
      @Override
      public double ease(double factor) {
         return Math.sin(factor * Math.PI / 2.0);
      }
   },
   SINE_IN_OUT {
      @Override
      public double ease(double factor) {
         return -(Math.cos(Math.PI * factor) - 1.0) / 2.0;
      }
   },
   CUBIC_IN {
      @Override
      public double ease(double factor) {
         return Math.pow(factor, 3.0);
      }
   },
   CUBIC_OUT {
      @Override
      public double ease(double factor) {
         return 1.0 - Math.pow(1.0 - factor, 3.0);
      }
   },
   CUBIC_IN_OUT {
      @Override
      public double ease(double factor) {
         return factor < 0.5 ? 4.0 * Math.pow(factor, 3.0) : 1.0 - Math.pow(-2.0 * factor + 2.0, 3.0) / 2.0;
      }
   },
   QUAD_IN {
      @Override
      public double ease(double factor) {
         return Math.pow(factor, 2.0);
      }
   },
   QUAD_OUT {
      @Override
      public double ease(double factor) {
         return 1.0 - (1.0 - factor) * (1.0 - factor);
      }
   },
   QUAD_IN_OUT {
      @Override
      public double ease(double factor) {
         return factor < 0.5 ? 8.0 * Math.pow(factor, 4.0) : 1.0 - Math.pow(-2.0 * factor + 2.0, 4.0) / 2.0;
      }
   },
   CIRC_IN_OUT {
      @Override
      public double ease(double factor) {
         return factor < 0.5 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * factor, 2.0))) / 2.0 : (Math.sqrt(1.0 - Math.pow(-2.0 * factor + 2.0, 2.0)) + 1.0) / 2.0;
      }
   },
   BACK_OUT {
      @Override
      public double ease(double factor) {
         double c1 = 1.70158;
         double c3 = c1 + 1.0;
         return 1.0 + c3 * Math.pow(factor - 1.0, 3.0) + c1 * Math.pow(factor - 1.0, 2.0);
      }
   };

   public abstract double ease(double var1);
}
