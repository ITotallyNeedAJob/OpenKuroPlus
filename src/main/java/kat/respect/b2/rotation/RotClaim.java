package kat.respect.b2.rotation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kat.respect.b2.module.Module;

public class RotClaim {
   private final Map<Module, Integer> priorities = new ConcurrentHashMap<>();
   private Module owner;
   private Module pendingOwner;

   public void setPriority(Module mod, int priority) {
      if (mod != null) {
         this.priorities.put(mod, priority);
      }
   }

   public int getPriority(Module mod) {
      return mod == null ? 0 : this.priorities.getOrDefault(mod, 0);
   }

   public boolean isClaimed() {
      return this.owner != null;
   }

   public Module getCurrentOwner() {
      return this.owner;
   }

   public Module getOwner() {
      return this.owner;
   }

   public boolean hasPendingOwner() {
      return this.pendingOwner != null;
   }

   public Module getPendingOwner() {
      return this.pendingOwner;
   }

   public boolean isOwnedBy(Module mod) {
      return this.isClaimed() && this.owner == mod && this.pendingOwner == null;
   }

   private boolean isCurrentOwner(Module mod) {
      return this.owner == mod;
   }

   private boolean isPendingOwner(Module mod) {
      return this.pendingOwner == mod;
   }

   private boolean hasHigherPriorityThanCurrent(Module mod) {
      return this.getPriority(mod) > this.getPriority(this.owner);
   }

   private boolean hasHigherPriorityThan(Module mod, Module other) {
      return this.getPriority(mod) > this.getPriority(other);
   }

   public boolean isBlockedFor(Module mod) {
      return this.isClaimed() && !this.isCurrentOwner(mod) && !this.hasHigherPriorityThanCurrent(mod)
         ? true
         : this.hasPendingOwner() && !this.isPendingOwner(mod) && this.hasHigherPriorityThan(this.pendingOwner, this.owner);
   }

   private boolean tryAcquire(Module mod) {
      if (this.owner != null && this.owner != mod) {
         this.pendingOwner = mod;
         return false;
      } else {
         this.owner = mod;
         this.pendingOwner = null;
         return true;
      }
   }

   public boolean acquire(Module mod) {
      return this.acquire(mod, false);
   }

   public boolean acquire(Module mod, boolean allowPreemption) {
      if (allowPreemption
         && this.isClaimed()
         && !this.isCurrentOwner(mod)
         && RotationManager.INSTANCE.hasAdaptiveController()
         && (this.hasHigherPriorityThanCurrent(mod) || ((AdaptiveRotationController)RotationManager.INSTANCE.getActiveController()).isRelativeMode())) {
         Module previousOwner = this.owner;
         this.pendingOwner = null;
         this.release(previousOwner);
      }

      return this.tryAcquire(mod);
   }

   public boolean release(Module mod) {
      if (this.owner == mod) {
         if (this.pendingOwner != null) {
            this.owner = this.pendingOwner;
            this.pendingOwner = null;
         } else {
            this.owner = null;
         }

         return true;
      } else {
         if (this.pendingOwner == mod) {
            this.pendingOwner = null;
         }

         return false;
      }
   }
}
