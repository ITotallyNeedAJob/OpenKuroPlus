package kat.respect.b2.rotation;

public class FixedRotController extends FixedRotationController {
   public FixedRotController(float yaw, float pitch) {
      super(yaw, pitch);
   }

   public FixedRotController(RotationAngles rotationAngles) {
      super(rotationAngles);
   }
}
