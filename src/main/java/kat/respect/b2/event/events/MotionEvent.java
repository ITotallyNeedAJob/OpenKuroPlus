package kat.respect.b2.event.events;

import kat.respect.b2.event.Event;

public class MotionEvent extends Event {
   private final MotionEvent.State state;
   private double x;
   private double y;
   private double z;
   private float yaw;
   private float pitch;
   private boolean onGround;

   public MotionEvent(MotionEvent.State state, double x, double y, double z, float yaw, float pitch, boolean onGround) {
      super(true);
      this.state = state;
      this.x = x;
      this.y = y;
      this.z = z;
      this.yaw = yaw;
      this.pitch = pitch;
      this.onGround = onGround;
   }

   public MotionEvent.State getState() {
      return this.state;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public void setX(double x) {
      this.x = x;
   }

   public void setY(double y) {
      this.y = y;
   }

   public void setZ(double z) {
      this.z = z;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }

   public static enum State {
      PRE,
      POST;
   }
}
