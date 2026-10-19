package viewpoint.far;

import java.util.Arrays;

final class ReliefWater {
   private static final float RIVER_WIDTH = 24.0F;
   private static final float LONG = 6.0F;
   static final float RIVER_FALL = 2.0E-4F;
   private static final int SIDE_STEP = 2;
   private static final int CORNER_STEP = 3;
   private static final int[] NONE = new int[0];

   private ReliefWater() {
   }

   static boolean water(int var0) {
      int var1 = var0 >> 16 & 0xFF;
      int var2 = var0 >> 8 & 0xFF;
      int var3 = var0 & 0xFF;
      return var3 >= var2 && var3 > var1 + 12;
   }

   static ReliefWater.Bodies of(ReliefGround var0, float[] var1, int var2, int var3, int var4, ReliefRules var5) {
      return of(var0, var1, var2, var3, var4, var5, FlowPoints.NONE);
   }

   static ReliefWater.Bodies of(ReliefGround var0, float[] var1, int var2, int var3, int var4, ReliefRules var5, FlowPoints var6) {
      int[] var7 = label(var0);
      int var8 = 0;

      for (int var12 : var7) {
         var8 = Math.max(var8, var12);
      }

      float[] var25 = new float[var7.length];
      Arrays.fill(var25, Float.NaN);
      float[] var26 = new float[var8 + 1];
      int var27 = levels(var0, var7, var8, var1, var2, var3, var4, var5, var25, var26);
      float[] var28 = new float[var2 * var3];
      Arrays.fill(var28, Float.NaN);
      boolean[] var13 = new boolean[var2 * var3];
      byte var14 = 4;

      for (int var15 = 0; var15 < var0.height; var15++) {
         for (int var16 = 0; var16 < var0.width; var16++) {
            float var17 = var25[var15 * var0.width + var16];
            if (!Float.isNaN(var17)) {
               float var18 = var16 * var14 + var14 * 0.5F;
               float var19 = var15 * var14 + var14 * 0.5F;
               int var20 = (int)Math.floor(var18 / var4);
               int var21 = (int)Math.floor(var19 / var4);

               for (int var22 = var21; var22 <= var21 + 1; var22++) {
                  for (int var23 = var20; var23 <= var20 + 1; var23++) {
                     if (var23 >= 0 && var22 >= 0 && var23 < var2 && var22 < var3) {
                        int var24 = var22 * var2 + var23;
                        var28[var24] = Float.isNaN(var28[var24]) ? var17 : Math.min(var28[var24], var17);
                        var13[var24] |= Math.abs(var18 - var23 * var4) <= var4 * 0.5F + var14 * 0.5F
                           && Math.abs(var19 - var22 * var4) <= var4 * 0.5F + var14 * 0.5F;
                     }
                  }
               }
            }
         }
      }

      byte[] var29 = WaterFlow.of(var0, var7, var25, var26, var2, var3, var6);
      return new ReliefWater.Bodies(var28, var13, var8, var27, var26, var29);
   }

   static int[] label(ReliefGround var0) {
      int var1 = var0.width;
      int var2 = var0.height;
      int[] var3 = new int[var1 * var2];
      int[] var4 = new int[var1 * var2];
      int var5 = 0;

      for (int var6 = 0; var6 < var3.length; var6++) {
         if (var0.kind[var6] == 1 && var3[var6] == 0) {
            var3[var6] = ++var5;
            int var7 = 0;
            int var8 = 0;
            var4[var8++] = var6;

            while (var7 < var8) {
               for (int var12 : around(var4[var7++], var1, var2)) {
                  if (var12 >= 0 && var3[var12] == 0 && var0.kind[var12] == 1) {
                     var3[var12] = var5;
                     var4[var8++] = var12;
                  }
               }
            }
         }
      }

      return var3;
   }

   private static int levels(ReliefGround var0, int[] var1, int var2, float[] var3, int var4, int var5, int var6, ReliefRules var7, float[] var8, float[] var9) {
      int var10 = var0.width;
      byte var11 = 4;
      int[] var12 = new int[var2 + 1];
      int[] var13 = new int[var2 + 1];
      Arrays.fill(var12, -1);
      float[] var14 = new float[var2 + 1];
      Arrays.fill(var14, Float.POSITIVE_INFINITY);
      float[] var15 = new float[var1.length];

      for (int var16 = 0; var16 < var1.length; var16++) {
         int var17 = var1[var16];
         if (var17 != 0) {
            var15[var16] = sample(var3, var4, var5, var6, var16 % var10 * var11 + var11 * 0.5F, var16 / var10 * var11 + var11 * 0.5F);
            var13[var17]++;
            if (var15[var16] < var14[var17]) {
               var14[var17] = var15[var16];
               var12[var17] = var16;
            }
         }
      }

      int[] var23 = walk(var1, var10, var12);
      int[] var24 = new int[var2 + 1];

      for (int var18 = 0; var18 < var1.length; var18++) {
         var24[var1[var18]] = Math.max(var24[var1[var18]], var23[var18]);
      }

      byte var25 = 0;

      for (int var19 = 1; var19 <= var2; var19++) {
         float var20 = (var24[var19] / 2.0F + 1.0F) * var11;
         float var21 = var13[var19] * var11 * var11 / var20;
         boolean var22 = var20 >= 6.0F * var21;
         var25 += var22 ? 1 : 0;
         var9[var19] = !var22 ? 0.0F : (var21 >= 24.0F ? 2.0E-4F : var7.metresASquare(var7.creekFall()));
      }

      float[] var26 = shores(var0, var1, var2, var3, var4, var5, var6);

      for (int var27 = 1; var27 <= var2; var27++) {
         if (var9[var27] == 0.0F && var12[var27] >= 0 && !Float.isNaN(var26[var27])) {
            var15[var12[var27]] = Math.max(var15[var12[var27]], var26[var27]);
         }
      }

      fill(var1, var10, var12, var15, var9, var8);
      return var25;
   }

   private static float[] shores(ReliefGround var0, int[] var1, int var2, float[] var3, int var4, int var5, int var6) {
      int var7 = var0.width;
      int var8 = var1.length / var7;
      byte var9 = 4;
      int[] var10 = new int[var2 + 2];

      for (int var11 = 0; var11 < var1.length; var11++) {
         for (int var15 : land(var0, var1, var11) ? around(var11, var7, var8) : NONE) {
            var10[var15 >= 0 && var1[var15] != 0 ? var1[var15] + 1 : 0]++;
         }
      }

      var10[0] = 0;

      for (int var18 = 1; var18 < var10.length; var18++) {
         var10[var18] += var10[var18 - 1];
      }

      float[] var19 = new float[var10[var2 + 1]];
      int[] var20 = (int[])var10.clone();

      for (int var21 = 0; var21 < var1.length; var21++) {
         for (int var17 : land(var0, var1, var21) ? around(var21, var7, var8) : NONE) {
            if (var17 >= 0 && var1[var17] != 0) {
               var19[var20[var1[var17]]++] = sample(var3, var4, var5, var6, var21 % var7 * var9 + var9 * 0.5F, var21 / var7 * var9 + var9 * 0.5F);
            }
         }
      }

      float[] var22 = new float[var2 + 1];
      Arrays.fill(var22, Float.NaN);

      for (int var24 = 1; var24 <= var2; var24++) {
         if (var10[var24 + 1] > var10[var24]) {
            Arrays.sort(var19, var10[var24], var10[var24 + 1]);
            var22[var24] = var19[var10[var24] + (var10[var24 + 1] - var10[var24]) / 2];
         }
      }

      return var22;
   }

   private static boolean land(ReliefGround var0, int[] var1, int var2) {
      return var1[var2] == 0 && var0.kind[var2] != 3;
   }

   static int[] walk(int[] var0, int var1, int[] var2) {
      int[] var3 = new int[var0.length];
      Arrays.fill(var3, Integer.MAX_VALUE);
      IndexHeap var4 = new IndexHeap(1024);

      for (int var8 : var2) {
         if (var8 >= 0) {
            var3[var8] = 0;
            var4.push(var8, 0.0F);
         }
      }

      while (!var4.empty()) {
         float var11 = var4.peekKey();
         int var13 = var4.pop();
         int[] var14 = around(var13, var1, var0.length / var1);

         for (int var15 = 0; var11 <= var3[var13] && var15 < var14.length; var15++) {
            int var9 = var14[var15];
            int var10 = var3[var13] + (var15 != 0 && var15 != 2 && var15 != 5 && var15 != 7 ? 2 : 3);
            if (var9 >= 0 && var0[var9] == var0[var13] && var10 < var3[var9]) {
               var3[var9] = var10;
               var4.push(var9, var10);
            }
         }
      }

      for (int var12 = 0; var12 < var3.length; var12++) {
         var3[var12] = var3[var12] == Integer.MAX_VALUE ? -1 : var3[var12];
      }

      return var3;
   }

   private static int[] around(int var0, int var1, int var2) {
      int var3 = var0 % var1;
      int var4 = var0 / var1;
      int var5 = 0;
      int[] var6 = new int[8];

      for (int var7 = -1; var7 <= 1; var7++) {
         for (int var8 = -1; var8 <= 1; var8++) {
            if (var8 != 0 || var7 != 0) {
               int var9 = var3 + var8;
               int var10 = var4 + var7;
               var6[var5++] = var9 >= 0 && var10 >= 0 && var9 < var1 && var10 < var2 ? var10 * var1 + var9 : -1;
            }
         }
      }

      return var6;
   }

   private static void fill(int[] var0, int var1, int[] var2, float[] var3, float[] var4, float[] var5) {
      int[] var6 = new int[var0.length];
      int var7 = 0;
      int var8 = 0;
      float var9 = 4.0F;

      for (int var13 : var2) {
         if (var13 >= 0) {
            var5[var13] = var3[var13];
            var6[var8++] = var13;
         }
      }

      while (var7 < var8) {
         int var16 = var6[var7++];
         float var17 = var4[var0[var16]] * var9;

         for (int var15 : around(var16, var1, var0.length / var1)) {
            if (var15 >= 0 && var0[var15] == var0[var16] && Float.isNaN(var5[var15])) {
               var5[var15] = Math.max(var5[var16], Math.min(var3[var15], var5[var16] + var17));
               var6[var8++] = var15;
            }
         }
      }
   }

   static float sample(float[] var0, int var1, int var2, int var3, float var4, float var5) {
      float var6 = Math.max(0.0F, Math.min(var1 - 1.001F, var4 / var3));
      float var7 = Math.max(0.0F, Math.min(var2 - 1.001F, var5 / var3));
      int var8 = (int)var6;
      int var9 = (int)var7;
      float var10 = var6 - var8;
      float var11 = var7 - var9;
      float var12 = var0[var9 * var1 + var8] * (1.0F - var10) + var0[var9 * var1 + var8 + 1] * var10;
      float var13 = var0[(var9 + 1) * var1 + var8] * (1.0F - var10) + var0[(var9 + 1) * var1 + var8 + 1] * var10;
      return var12 * (1.0F - var11) + var13 * var11;
   }

   record Bodies(float[] level, boolean[] ground, int bodies, int running, float[] fall, byte[] flow) {
   }
}
