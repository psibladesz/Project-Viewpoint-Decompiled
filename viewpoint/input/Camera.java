package viewpoint.input;

import viewpoint.core.Frame;

public final class Camera {
   private static final float EYE_FORWARD = 0.12F;

   public static void direction(float var0, float var1, float[] var2) {
      float var3 = (float)Math.cos(var1);
      var2[0] = -((float)Math.cos(var0)) * var3;
      var2[1] = (float)Math.sin(var1);
      var2[2] = -((float)Math.sin(var0)) * var3;
   }

   public static void eye(Frame var0, float var1, float[] var2) {
      float var3 = 0.12F + var0.eyeLean;
      var2[0] = var0.eyeX - (float)Math.cos(var1) * var3;
      var2[1] = var0.eyeY;
      var2[2] = var0.eyeZ - (float)Math.sin(var1) * var3;
   }

   public static void at(Frame var0, FreeCam.Place var1, float[] var2, float[] var3) {
      var2[0] = (float)(var0.camX - var1.x());
      var2[1] = (float)((var1.z() - var0.camZ) * 2.4494896F);
      var2[2] = (float)(var0.camY - var1.y());
      direction(var1.yaw(), var1.pitch(), var3);
   }

   private Camera() {
   }
}
