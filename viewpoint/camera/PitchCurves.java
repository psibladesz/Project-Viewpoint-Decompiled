package viewpoint.camera;

final class PitchCurves {
   private static final float[] PITCH = new float[]{-60.0F, -30.0F, 0.0F, 30.0F, 60.0F};
   private static final float[] DISTANCE = new float[]{1.15F, 1.05F, 1.0F, 0.85F, 0.65F};
   private static final float[] HEIGHT = new float[]{0.3F, 0.1F, 0.0F, -0.15F, -0.3F};
   private static final float[] FOV = new float[]{1.0F, 1.0F, 1.0F, 1.04F, 1.1F};
   private static final float[] CENTRE = new float[]{1.0F, 0.0F, 0.0F, 0.0F, 0.0F};
   private static final float[] LOWER = new float[]{0.0F, 0.0F, 0.0F, 0.05F, 0.12F};

   static float distance(float var0) {
      return at(DISTANCE, var0);
   }

   static float height(float var0) {
      return at(HEIGHT, var0);
   }

   static float fov(float var0) {
      return at(FOV, var0);
   }

   static float fromTop(float var0, float var1) {
      return var0 + (0.5F - var0) * at(CENTRE, var1) + at(LOWER, var1);
   }

   private static float at(float[] var0, float var1) {
      float var2 = (float)Math.toDegrees(var1);
      if (var2 <= PITCH[0]) {
         return var0[0];
      }

      for (int var3 = 1; var3 < PITCH.length; var3++) {
         if (var2 <= PITCH[var3]) {
            float var4 = (var2 - PITCH[var3 - 1]) / (PITCH[var3] - PITCH[var3 - 1]);
            float var5 = var4 * var4 * (3.0F - 2.0F * var4);
            return var0[var3 - 1] + (var0[var3] - var0[var3 - 1]) * var5;
         }
      }

      return var0[var0.length - 1];
   }

   private PitchCurves() {
   }
}
