package viewpoint.far;

import java.util.Arrays;

final class WaterFlow {
   static final int NONE = 0;
   static final int POND = 1;
   static final int LAKE = 2;
   static final int CREEK = 3;
   static final int RIVER = 4;
   static final int KIND_STEP = 60;
   static final float CREEK_SPEED = 0.6F;
   static final float RIVER_SPEED = 0.8F;
   static final int POND_PIXELS = 64;
   private static final int ACROSS = 128;
   private static final float END = 0.1F;
   private static final int POTENTIAL_SWEEPS = 400;
   private static final float FASTEST = 2.0F;
   private static final float SLOWEST = 0.5F;
   private static final int POINT_REACH = 2;

   private WaterFlow() {
   }

   static byte[] of(ReliefGround var0, int[] var1, float[] var2, float[] var3, int var4, int var5, FlowPoints var6) {
      byte var7 = 4;
      int var8 = (var4 - 1) / var7 + 1;
      int var9 = (var5 - 1) / var7 + 1;
      int var10 = var3.length - 1;
      int[] var11 = new int[var8 * var9];
      int[] var12 = new int[var10 + 1];
      float[] var13 = new float[var8 * var9];
      float[] var14 = new float[var8 * var9];
      cells(var0, var1, var2, var11, var13, var14, var8, var9, var12);
      int[] var15 = pointCells(var6, var11, var8, var9);
      float[] var16 = new float[var10 + 1];
      float[] var17 = runs(var6, var15, var11, var3, var16);
      int[][] var18 = ends(var11, var13, var17, var10, var8);
      float[] var19 = along(ReliefWater.walk(var11, var8, var18[0]), ReliefWater.walk(var11, var8, var18[1]));
      potential(var19, var11, var18, var8, var9);
      float[][] var20 = ways(var11, var19, var8, var9);
      orient(var20, var6, var15, var11, var10);
      float[] var21 = new float[var11.length];
      float[] var22 = new float[var11.length];
      across(var11, var20, var8, var9, var21, var22);
      float[] var23 = usualHalfWidth(var11, var21, var10);
      byte[] var24 = new byte[var8 * var9 * 4];

      for (int var25 = 0; var25 < var11.length; var25++) {
         int var26 = var11[var25];
         float var27 = var26 == 0 ? 0.0F : speed(var26, var17, var16[var26], var22[var25], var21[var25], var23[var26]);
         var24[var25 * 4] = signed(var20[0][var25] * var27);
         var24[var25 * 4 + 1] = signed(var20[1][var25] * var27);
         var24[var25 * 4 + 2] = (byte)((var26 == 0 ? 0 : kind(var26, var17, var12)) * 60);
         var24[var25 * 4 + 3] = (byte)Math.round(255.0F * var14[var25]);
      }

      return var24;
   }

   private static void cells(ReliefGround var0, int[] var1, float[] var2, int[] var3, float[] var4, float[] var5, int var6, int var7, int[] var8) {
      byte var9 = 4;
      int var10 = var0.width;
      int var11 = var0.height;
      int[] var12 = new int[var9 * var9];
      int[] var13 = new int[var9 * var9];

      for (int var17 : var1) {
         var8[var17]++;
      }

      for (int var23 = 0; var23 < var7; var23++) {
         for (int var24 = 0; var24 < var6; var24++) {
            int var25 = var23 * var6 + var24;
            int var26 = 0;
            int var18 = 0;
            var4[var25] = Float.POSITIVE_INFINITY;

            for (int var19 = var23 * var9 - var9 / 2; var19 < var23 * var9 + var9 / 2; var19++) {
               for (int var20 = var24 * var9 - var9 / 2; var20 < var24 * var9 + var9 / 2; var20++) {
                  int var21 = var20 >= 0 && var19 >= 0 && var20 < var10 && var19 < var11 ? var1[var19 * var10 + var20] : 0;
                  if (var21 != 0) {
                     var18++;
                     var4[var25] = Math.min(var4[var25], var2[var19 * var10 + var20]);
                     int var22 = 0;

                     while (var22 < var26 && var12[var22] != var21) {
                        var22++;
                     }

                     var12[var22] = var21;
                     var13[var22] = var22 == var26 ? 1 : var13[var22] + 1;
                     var26 = Math.max(var26, var22 + 1);
                  }
               }
            }

            byte var27 = 0;

            for (int var28 = 1; var28 < var26; var28++) {
               var27 = (byte)(var13[var28] > var13[var27] ? var28 : var27);
            }

            var3[var25] = var18 > 0 ? var12[var27] : 0;
            var5[var25] = (float)var18 / (var9 * var9);
         }
      }
   }

   static int kind(int var0, float[] var1, int[] var2) {
      if (var1[var0] > 0.0F) {
         return var1[var0] <= 2.0E-4F ? 4 : 3;
      } else {
         return var2[var0] < 64 ? 1 : 2;
      }
   }

   private static float speed(int var0, float[] var1, float var2, float var3, float var4, float var5) {
      if (!(var1[var0] <= 0.0F) && !(var4 <= 0.0F)) {
         float var6 = var2 > 0.0F ? var2 : (var1[var0] <= 2.0E-4F ? 0.8F : 0.6F);
         float var7 = Math.min(1.0F, var3 / var4);
         float var8 = var7 * (2.0F - var7);
         return var6 * var8 * Math.max(0.5F, Math.min(2.0F, var5 / var4));
      } else {
         return 0.0F;
      }
   }

   private static int[] pointCells(FlowPoints var0, int[] var1, int var2, int var3) {
      int[] var4 = new int[var0.size()];

      for (int var5 = 0; var5 < var4.length; var5++) {
         float var6 = var0.x(var5) / 16.0F;
         float var7 = var0.y(var5) / 16.0F;
         int var8 = Math.round(var6);
         int var9 = Math.round(var7);
         float var10 = Float.MAX_VALUE;
         var4[var5] = -1;

         for (int var11 = var9 - 2; var11 <= var9 + 2; var11++) {
            for (int var12 = var8 - 2; var12 <= var8 + 2; var12++) {
               float var13 = (var12 - var6) * (var12 - var6) + (var11 - var7) * (var11 - var7);
               if (var12 >= 0 && var11 >= 0 && var12 < var2 && var11 < var3 && var1[var11 * var2 + var12] != 0 && var13 < var10) {
                  var10 = var13;
                  var4[var5] = var11 * var2 + var12;
               }
            }
         }
      }

      return var4;
   }

   private static float[] runs(FlowPoints var0, int[] var1, int[] var2, float[] var3, float[] var4) {
      int[] var5 = new int[var3.length];
      int[] var6 = new int[var3.length];

      for (int var7 = 0; var7 < var1.length; var7++) {
         int var8 = var1[var7] < 0 ? 0 : var2[var1[var7]];
         var5[var8]++;
         if (var0.speed(var7) > 0.0F) {
            var6[var8]++;
            var4[var8] += var0.speed(var7);
         }
      }

      float[] var9 = (float[])var3.clone();

      for (int var10 = 1; var10 < var3.length; var10++) {
         var4[var10] = var6[var10] > 0 ? Math.min(2.0F, var4[var10] / var6[var10]) : 0.0F;
         var9[var10] = var5[var10] > 0 && var6[var10] == 0 ? 0.0F : var9[var10];
      }

      return var9;
   }

   private static void orient(float[][] var0, FlowPoints var1, int[] var2, int[] var3, int var4) {
      float[] var5 = new float[var4 + 1];

      for (int var6 = 0; var6 < var2.length; var6++) {
         int var7 = var2[var6];
         if (var7 >= 0) {
            var5[var3[var7]] = var5[var3[var7]] + var1.speed(var6) * (var1.wayX(var6) * var0[0][var7] + var1.wayY(var6) * var0[1][var7]);
         }
      }

      for (int var8 = 0; var8 < var3.length; var8++) {
         if (var5[var3[var8]] < 0.0F) {
            var0[0][var8] = -var0[0][var8];
            var0[1][var8] = -var0[1][var8];
         }
      }
   }

   private static int[][] ends(int[] var0, float[] var1, float[] var2, int var3, int var4) {
      int[] var5 = new int[var3 + 1];
      Arrays.fill(var5, -1);

      for (int var6 = 0; var6 < var0.length; var6++) {
         int var7 = var0[var6];
         if (var7 != 0 && var2[var7] > 0.0F && var5[var7] < 0) {
            var5[var7] = var6;
         }
      }

      int[] var22 = ReliefWater.walk(var0, var4, var5);
      int[] var23 = farthest(var0, var22, var3);
      int[] var8 = ReliefWater.walk(var0, var4, var23);
      int[] var9 = new int[var3 + 1];

      for (int var10 = 0; var10 < var0.length; var10++) {
         var9[var0[var10]] = Math.max(var9[var0[var10]], var8[var10]);
      }

      double[] var24 = new double[var3 + 1];
      double[] var11 = new double[var3 + 1];
      int[] var12 = new int[var3 + 1];
      int[] var13 = new int[var3 + 1];

      for (int var14 = 0; var14 < var0.length; var14++) {
         int var15 = var0[var14];
         if (var15 != 0 && var8[var14] >= 0 && var9[var15] != 0) {
            if (var8[var14] <= 0.1F * var9[var15]) {
               var24[var15] += var1[var14];
               var12[var15]++;
            } else if (var8[var14] >= 0.9F * var9[var15]) {
               var11[var15] += var1[var14];
               var13[var15]++;
            }
         }
      }

      int[][] var25 = new int[][]{new int[var0.length], new int[var0.length]};
      int[] var26 = new int[2];

      for (int var16 = 0; var16 < var0.length; var16++) {
         int var17 = var0[var16];
         if (var17 != 0 && var8[var16] >= 0 && var9[var17] != 0 && var12[var17] != 0 && var13[var17] != 0) {
            boolean var18 = var24[var17] / var12[var17] <= var11[var17] / var13[var17];
            boolean var19 = var8[var16] <= 0.1F * var9[var17];
            boolean var20 = var8[var16] >= 0.9F * var9[var17];
            int var21 = var19 ? (var18 ? 0 : 1) : (var20 ? (var18 ? 1 : 0) : -1);
            if (var21 >= 0) {
               var25[var21][var26[var21]++] = var16;
            }
         }
      }

      return new int[][]{Arrays.copyOf(var25[0], var26[0]), Arrays.copyOf(var25[1], var26[1])};
   }

   private static float[] along(int[] var0, int[] var1) {
      float[] var2 = new float[var0.length];

      for (int var3 = 0; var3 < var2.length; var3++) {
         var2[var3] = var0[var3] >= 0 && var1[var3] >= 0 ? (float)var0[var3] / Math.max(1, var0[var3] + var1[var3]) : Float.NaN;
      }

      return var2;
   }

   private static void potential(float[] var0, int[] var1, int[][] var2, int var3, int var4) {
      boolean[] var5 = new boolean[var1.length];

      for (int[] var9 : var2) {
         for (int var13 : var9) {
            var5[var13] = true;
         }
      }

      int[] var19 = new int[var1.length];
      int var20 = 0;

      for (int var21 = 0; var21 < var1.length; var21++) {
         if (!Float.isNaN(var0[var21]) && !var5[var21]) {
            var19[var20++] = var21;
         }
      }

      for (int var22 = 0; var22 < 400; var22++) {
         for (int var23 = 0; var23 < var20; var23++) {
            int var24 = var19[var23];
            int var25 = var24 % var3;
            int var26 = var24 / var3;
            float var27 = 0.0F;
            int var14 = 0;

            for (int var15 = 0; var15 < 4; var15++) {
               int var16 = var25 + (var15 == 0 ? 1 : (var15 == 1 ? -1 : 0));
               int var17 = var26 + (var15 == 2 ? 1 : (var15 == 3 ? -1 : 0));
               int var18 = var17 * var3 + var16;
               if (var16 >= 0 && var17 >= 0 && var16 < var3 && var17 < var4 && var1[var18] == var1[var24] && !Float.isNaN(var0[var18])) {
                  var27 += var0[var18];
                  var14++;
               }
            }

            if (var14 > 0) {
               var0[var24] = var27 / var14;
            }
         }
      }
   }

   private static int[] farthest(int[] var0, int[] var1, int var2) {
      int[] var3 = new int[var2 + 1];
      int[] var4 = new int[var2 + 1];
      Arrays.fill(var3, -1);
      Arrays.fill(var4, -1);

      for (int var5 = 0; var5 < var0.length; var5++) {
         int var6 = var0[var5];
         if (var6 != 0 && var1[var5] > var4[var6]) {
            var4[var6] = var1[var5];
            var3[var6] = var5;
         }
      }

      return var3;
   }

   private static float[][] ways(int[] var0, float[] var1, int var2, int var3) {
      float[][] var4 = new float[2][var0.length];

      for (int var5 = 0; var5 < var3; var5++) {
         for (int var6 = 0; var6 < var2; var6++) {
            int var7 = var5 * var2 + var6;
            if (var0[var7] != 0 && !Float.isNaN(var1[var7])) {
               float var8 = at(var1, var0, var2, var3, var6 + 1, var5, var7) - at(var1, var0, var2, var3, var6 - 1, var5, var7);
               float var9 = at(var1, var0, var2, var3, var6, var5 + 1, var7) - at(var1, var0, var2, var3, var6, var5 - 1, var7);
               float var10 = (float)Math.hypot(var8, var9);
               if (var10 > 0.0F) {
                  var4[0][var7] = -var8 / var10;
                  var4[1][var7] = -var9 / var10;
               }
            }
         }
      }

      return var4;
   }

   private static float at(float[] var0, int[] var1, int var2, int var3, int var4, int var5, int var6) {
      if (var4 >= 0 && var5 >= 0 && var4 < var2 && var5 < var3) {
         int var7 = var5 * var2 + var4;
         return var1[var7] == var1[var6] && !Float.isNaN(var0[var7]) ? var0[var7] : var0[var6];
      } else {
         return var0[var6];
      }
   }

   private static void across(int[] var0, float[][] var1, int var2, int var3, float[] var4, float[] var5) {
      for (int var6 = 0; var6 < var3; var6++) {
         for (int var7 = 0; var7 < var2; var7++) {
            int var8 = var6 * var2 + var7;
            if (var0[var8] != 0 && (var1[0][var8] != 0.0F || var1[1][var8] != 0.0F)) {
               float var9 = bank(var0, var2, var3, var7, var6, -var1[1][var8], var1[0][var8]);
               float var10 = bank(var0, var2, var3, var7, var6, var1[1][var8], -var1[0][var8]);
               var4[var8] = (var9 + var10 + 1.0F) * 0.5F;
               var5[var8] = Math.min(var9, var10) + 0.5F;
            }
         }
      }
   }

   private static float bank(int[] var0, int var1, int var2, int var3, int var4, float var5, float var6) {
      int var7 = var0[var4 * var1 + var3];

      for (int var8 = 1; var8 <= 128; var8++) {
         int var9 = Math.round(var3 + var5 * var8);
         int var10 = Math.round(var4 + var6 * var8);
         if (var9 < 0 || var10 < 0 || var9 >= var1 || var10 >= var2 || var0[var10 * var1 + var9] != var7) {
            return var8 - 1;
         }
      }

      return 128.0F;
   }

   private static float[] usualHalfWidth(int[] var0, float[] var1, int var2) {
      int[] var3 = new int[var2 + 2];

      for (int var7 : var0) {
         var3[var7 + 1]++;
      }

      for (int var8 = 1; var8 < var3.length; var8++) {
         var3[var8] += var3[var8 - 1];
      }

      float[] var9 = new float[var0.length];
      int[] var10 = (int[])var3.clone();

      for (int var11 = 0; var11 < var0.length; var11++) {
         var9[var10[var0[var11]]++] = var1[var11];
      }

      float[] var12 = new float[var2 + 1];

      for (int var13 = 1; var13 <= var2; var13++) {
         if (var3[var13 + 1] > var3[var13]) {
            Arrays.sort(var9, var3[var13], var3[var13 + 1]);
            var12[var13] = var9[var3[var13] + (var3[var13 + 1] - var3[var13]) / 2];
         }
      }

      return var12;
   }

   private static byte signed(float var0) {
      return (byte)Math.round(128.0F + 127.0F * Math.max(-1.0F, Math.min(1.0F, var0 / 2.0F)));
   }
}
