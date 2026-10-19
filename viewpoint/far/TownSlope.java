package viewpoint.far;

final class TownSlope {
   private TownSlope() {
   }

   static void cap(float[] var0, float[] var1, float[] var2, int var3, int var4, float var5) {
      int var6 = var0.length;
      float[] var7 = new float[var6];
      float[] var8 = new float[var6];
      float[] var9 = new float[var6];
      float[] var10 = new float[var6];
      float[] var11 = new float[var6];

      for (int var12 = 0; var12 < var6; var12++) {
         var7[var12] = var2[var12] > 0.0F ? var5 / var2[var12] : Float.POSITIVE_INFINITY;
      }

      ReliefSweeps.Along var14 = ReliefSweeps.Along.limited(var7);
      ReliefSweeps.settledEnvelope(var0, var8, var9, null, var14, var3, var4);
      ReliefSweeps.settledEnvelope(var1, var10, var11, null, var14, var3, var4);

      for (int var13 = 0; var13 < var6; var13++) {
         var0[var13] = Math.min(var11[var13], (var8[var13] + var9[var13]) * 0.5F);
      }
   }

   static float[] shore(ReliefWater.Bodies var0) {
      float[] var1 = new float[var0.level().length];

      for (int var2 = 0; var2 < var1.length; var2++) {
         var1[var2] = var0.ground()[var2] ? var0.level()[var2] : Float.NaN;
      }

      return var1;
   }
}
