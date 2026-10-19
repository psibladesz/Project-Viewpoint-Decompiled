package viewpoint.far;

final class TownPaving {
   static final float TOWN = 0.5F;
   static final float TALLEST = 4.0F;
   static final byte EAST = 1;
   static final byte SOUTH = 2;

   private TownPaving() {
   }

   static boolean[] of(ReliefGround var0, float[] var1, ReliefPads.Pads var2, boolean[] var3, float[] var4, float[] var5, int var6, int var7, int var8) {
      boolean[] var9 = new boolean[var6 * var7];
      byte var10 = 4;

      for (int var11 = 0; var11 < var0.height; var11++) {
         for (int var12 = 0; var12 < var0.width; var12++) {
            byte var13 = var0.kind[var11 * var0.width + var12];
            if (var13 == 2 || var13 == 4) {
               for (int var14 = var11 * var10 / var8; var14 <= Math.min(var7 - 1, ((var11 + 1) * var10 - 1) / var8 + 1); var14++) {
                  for (int var15 = var12 * var10 / var8; var15 <= Math.min(var6 - 1, ((var12 + 1) * var10 - 1) / var8 + 1); var15++) {
                     var9[var14 * var6 + var15] = true;
                  }
               }
            }
         }
      }

      for (int var16 = 0; var16 < var9.length; var16++) {
         boolean var17 = var9[var16] || !Float.isNaN(var1[var16]);
         var9[var16] = var17 && var4[var16] >= 0.5F && !var2.core()[var16] && !var3[var16] && !Float.isNaN(var5[var16]);
      }

      return var9;
   }

   static int join(float[] var0, float[] var1, boolean[] var2, boolean[] var3, int var4, int var5, float var6) {
      float[] var7 = new float[var0.length];
      float[] var8 = new float[var0.length];
      float[] var9 = new float[var0.length];
      float[] var10 = new float[var0.length];
      float[] var11 = new float[var0.length];
      float[] var12 = new float[var0.length];
      ReliefSweeps.Along var13 = ReliefSweeps.Along.over(var1, var0, var6, 4.0F);

      for (int var14 = 0; var14 < var0.length; var14++) {
         var7[var14] = !var2[var14] && !var3[var14] ? Float.NaN : var0[var14];
         var10[var14] = var3[var14] ? var0[var14] : Float.NaN;
      }

      ReliefSweeps.settledEnvelope(var7, var8, var9, var2, var13, var4, var5);
      ReliefSweeps.settledEnvelope(var10, var11, var12, var2, var13, var4, var5);
      byte var17 = 0;

      for (int var15 = 0; var15 < var0.length; var15++) {
         if (var2[var15]) {
            float var16 = Math.max(var11[var15], Math.min(var12[var15], (var8[var15] + var9[var15]) * 0.5F));
            var17 += var16 != var0[var15] ? 1 : 0;
            var0[var15] = var16;
         }
      }

      return var17;
   }

   static byte[] apart(float[] var0, float[] var1, boolean[] var2, boolean[] var3, int var4, int var5, float var6) {
      byte[] var7 = new byte[var0.length];
      ReliefSweeps.Along var8 = ReliefSweeps.Along.over(var1, var0, var6, 4.0F);

      for (int var9 = 0; var9 < var0.length; var9++) {
         int var10 = var9 % var4;
         if (var2[var9] || var3[var9]) {
            for (int var11 = 0; var11 < 2; var11++) {
               int var12 = var11 == 0 ? (var10 + 1 < var4 ? var9 + 1 : -1) : (var9 + var4 < var0.length ? var9 + var4 : -1);
               boolean var13 = var12 >= 0 && (var2[var9] || var2[var12]) && (var2[var12] || var3[var12]);
               if (var13 && Float.isNaN(var8.step(var9, var12, false))) {
                  var7[var9] = (byte)(var7[var9] | (var11 == 0 ? 1 : 2));
               }
            }
         }
      }

      return var7;
   }
}
