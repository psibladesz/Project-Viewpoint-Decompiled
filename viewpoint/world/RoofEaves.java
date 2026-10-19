package viewpoint.world;

final class RoofEaves {
   static final float OVERHANG = 0.25F;
   static final float FASCIA = 0.08F;
   static final int TRIM = 14999766;
   static final float FLAT_DROP = 0.20412414F;
   private static final float LEVEL = 0.01F;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   private static final int[][] SIDE_POINTS = new int[][]{{6, 7, 8}, {2, 5, 8}, {0, 1, 2}, {0, 3, 6}};
   private static final int CENTRE = 4;

   static void square(RoofSurface var0, byte[] var1, int var2, int var3, RoofEaves.Sink var4) {
      byte var5 = 0;
      float[] var6 = new float[DX.length];

      for (int var7 = 0; var7 < DX.length; var7++) {
         var6[var7] = drop(var0, var1, var2, var3, var7);
         var5 |= var6[var7] > 0.0F ? 1 << var7 : 0;
      }

      for (int var8 = 0; var8 < DX.length; var8++) {
         if ((var5 & 1 << var8) != 0) {
            side(var0, var2, var3, var8, var5, var6, var4);
         }
      }
   }

   private static float drop(RoofSurface var0, byte[] var1, int var2, int var3, int var4) {
      int var5 = var2 + DX[var4];
      int var6 = var3 + DZ[var4];
      boolean var7 = var5 >= 0 && var6 >= 0 && var5 < var0.width && var6 < var0.height;
      if (var0.built(var2, var3) && (!var7 || !var0.stands(var5, var6) && (var1[var6 * var0.width + var5] & 2) == 0)) {
         int[] var8 = SIDE_POINTS[var4];
         float var9 = var0.height(var2, var3, var8[0]);
         float var10 = var0.height(var2, var3, var8[1]);
         float var11 = var0.height(var2, var3, var8[2]);
         float var12 = var0.height(var2, var3, 4) - var10;
         if (!(Math.abs(var9 - var10) > 0.01F) && !(Math.abs(var11 - var10) > 0.01F) && !(var12 < -0.01F)) {
            return var12 > 0.01F ? var12 * 2.0F * 0.25F : (flat(var0, var2, var3) ? 0.20412414F : 0.0F);
         } else {
            return 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   private static boolean flat(RoofSurface var0, int var1, int var2) {
      float var3 = var0.height(var1, var2, 4);

      for (int var4 = 0; var4 < 9; var4++) {
         if (Math.abs(var0.height(var1, var2, var4) - var3) > 0.01F) {
            return false;
         }
      }

      return true;
   }

   private static void side(RoofSurface var0, int var1, int var2, int var3, int var4, float[] var5, RoofEaves.Sink var6) {
      int[] var7 = SIDE_POINTS[var3];
      float var8 = var0.height(var1, var2, var7[1]);
      float var9 = var8 - var5[var3];
      float var10 = var9 - 0.08F;
      int var11 = DZ[var3] != 0 ? 1 : 0;
      int var12 = var11 == 1 ? 3 : 2;
      int var13 = var11 == 1 ? 1 : 0;
      float var14 = DX[var3] * 0.25F;
      float var15 = DZ[var3] * 0.25F;
      float[] var16 = point(var1, var2, var7[0], var8);
      float[] var17 = point(var1, var2, var7[2], var8);
      float[] var18 = new float[]{var16[0] + var14, var9, var16[2] + var15};
      float[] var19 = new float[]{var17[0] + var14, var9, var17[2] + var15};
      var6.slope(var16, var17, var19, var3);
      var6.slope(var16, var19, var18, var3);
      boolean var20 = (var4 & 1 << var12) != 0;
      boolean var21 = (var4 & 1 << var13) != 0;
      float var22 = var11 * 0.25F;
      float var23 = (1 - var11) * 0.25F;
      float[] var24 = var20 ? new float[]{var18[0] - var22, var9, var18[2] - var23} : var18;
      float[] var25 = var21 ? new float[]{var19[0] + var22, var9, var19[2] + var23} : var19;
      if (var21) {
         var6.slope(var17, var25, var19, var3);
      }

      if (var20) {
         var6.slope(var16, var18, var24, var3);
      }

      float[] var26 = new float[]{var24[0], var10, var24[2]};
      float[] var27 = new float[]{var25[0], var10, var25[2]};
      var6.trim(var24, var27, var25, DX[var3], 0.0F, DZ[var3]);
      var6.trim(var24, var26, var27, DX[var3], 0.0F, DZ[var3]);
      float[] var28 = new float[]{var24[0] - var14, var10, var24[2] - var15};
      float[] var29 = new float[]{var25[0] - var14, var10, var25[2] - var15};
      var6.trim(var26, var29, var27, 0.0F, -1.0F, 0.0F);
      var6.trim(var26, var28, var29, 0.0F, -1.0F, 0.0F);
   }

   private static float[] point(int var0, int var1, int var2, float var3) {
      return new float[]{var0 + var2 % 3 / 2.0F, var3, var1 + var2 / 3 / 2.0F};
   }

   private RoofEaves() {
   }

   interface Sink {
      void slope(float[] var1, float[] var2, float[] var3, int var4);

      void trim(float[] var1, float[] var2, float[] var3, float var4, float var5, float var6);
   }
}
