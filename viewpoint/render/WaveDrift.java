package viewpoint.render;

final class WaveDrift {
   static final double SPEED = 1.6;
   static final double RIPPLE_SPEED = 2.08;
   private static double longX;
   private static double longZ;
   private static double rippleX;
   private static double rippleZ;
   private static double last = Double.NaN;

   private WaveDrift() {
   }

   static void advance(double var0, float var2, float var3) {
      double var4 = !Double.isNaN(last) && !(var0 < last) ? var0 - last : 0.0;
      last = var0;
      double var6 = Math.hypot(var2, var3);
      double var8 = var6 > 0.001 ? var2 / var6 : 0.0;
      double var10 = var6 > 0.001 ? var3 / var6 : 1.0;
      double var12 = 0.8164965311686198 * WaveTile.TILE_METRES[0];
      double var14 = 0.8164965311686198 * WaveTile.TILE_METRES[1];
      longX = (longX + var8 * 1.6 * var4) % var12;
      longZ = (longZ + var10 * 1.6 * var4) % var12;
      rippleX = (rippleX + var8 * 2.08 * var4) % var14;
      rippleZ = (rippleZ + var10 * 2.08 * var4) % var14;
   }

   static float[] value() {
      return new float[]{(float)longX, (float)longZ, (float)rippleX, (float)rippleZ};
   }
}
