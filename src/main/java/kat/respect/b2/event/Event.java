package kat.respect.b2.event;

public abstract class Event {
   private final boolean cancellable;
   private boolean cancelled;

   protected Event(boolean cancellable) {
      this.cancellable = cancellable;
   }

   protected Event() {
      this(false);
   }

   public final boolean isCancellable() {
      return this.cancellable;
   }

   public final boolean isCancelled() {
      return this.cancelled;
   }

   public final boolean cancel() {
      if (!this.cancellable) {
         EventDebug.warnNonCancellable(this);
         return false;
      } else {
         this.cancelled = true;
         return true;
      }
   }

   public final void uncancel() {
      this.cancelled = false;
   }

   public final void setCancelled(boolean value) {
      if (value && !this.cancellable) {
         EventDebug.warnNonCancellable(this);
      } else {
         this.cancelled = value;
      }
   }
}
