package kat.respect.b2.event;

public enum EventPriority {
   HIGHEST(4),
   HIGH(3),
   NORMAL(2),
   LOW(1),
   LOWEST(0);

   public final int value;

   private EventPriority(int value) {
      this.value = value;
   }
}
