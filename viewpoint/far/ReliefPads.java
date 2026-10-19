package viewpoint.far;

import java.util.Arrays;

final class ReliefPads {
   private static final float NEAR_SQUARES = 16.0F;
   private static final int LOT_MOST = 10000;

   private ReliefPads() {
   }

   static ReliefPads.Pads of(
      int[] var0, ReliefGround var1, boolean[] var2, float[] var3, float[] var4, float[] var5, int var6, int var7, int var8, ReliefRules var9
   ) {
      int[] var10 = new int[var6 * var7];
      Arrays.fill(var10, -1);
      boolean[] var11 = new boolean[var1.width * var1.height];
      boolean[] var12 = new boolean[var6 * var7];
      int var13 = 0;

      for (int var14 = 0; var14 + 4 <= var0.length; var14 += 4) {
         float var15 = var9.padMargin();
         pad(var10, var12, var0, var14, var6, var7, var8, var15);
         mark(var11, var1, var0[var14] - var15, var0[var14 + 1] - var15, var0[var14 + 2] + var15, var0[var14 + 3] + var15);
         var13++;
      }

      int var23 = lots(var1, var2, var11, var10, var6, var7, var8, var9);
      closed(var10, var6, var7);
      int[] var24 = new int[var10.length];
      int var16 = number(var10, var24);
      int[] var17 = new int[var16];

      for (int var21 : var24) {
         if (var21 >= 0) {
            var17[var21]++;
         }
      }

      int var25 = 0;

      for (int var22 : var17) {
         var25 = Math.max(var25, var22 * var8 * var8);
      }

      float[] var27 = new float[var16];
      float[] var29 = heights(var24, var16, var3, var4, var5, var6, var7, var8, var9, var27);
      int[] var31 = sharing(var0, var24, var6, var7, var8, var16);
      return new ReliefPads.Pads(var24, var29, var12, var27, var13, var23, var31[0], var25, Arrays.copyOfRange(var31, 1, 5));
   }

   private static void pad(int[] var0, boolean[] var1, int[] var2, int var3, int var4, int var5, int var6, float var7) {
      int var8 = clamp((int)Math.floor((var2[var3] - var7) / var6), var4 - 2);
      int var9 = clamp((int)Math.floor((var2[var3 + 2] + var7 - 1.0F) / var6), var4 - 2);
      int var10 = clamp((int)Math.floor((var2[var3 + 1] - var7) / var6), var5 - 2);
      int var11 = clamp((int)Math.floor((var2[var3 + 3] + var7 - 1.0F) / var6), var5 - 2);
      int var12 = clamp(var2[var3 + 1] / var6, var5 - 1) * var4 + clamp(var2[var3] / var6, var4 - 1);

      for (int var13 = var10; var13 <= var11; var13++) {
         for (int var14 = var8; var14 <= var9; var14++) {
            float var15 = (var14 + 0.5F) * var6;
            float var16 = (var13 + 0.5F) * var6;
            boolean var17 = var2[var3] < (var14 + 1) * var6
               && var2[var3 + 2] > var14 * var6
               && var2[var3 + 1] < (var13 + 1) * var6
               && var2[var3 + 3] > var13 * var6;
            boolean var18 = var15 >= var2[var3] - var7 && var15 < var2[var3 + 2] + var7 && var16 >= var2[var3 + 1] - var7 && var16 < var2[var3 + 3] + var7;
            if (var17 || var18) {
               for (int var22 : new int[]{0, 1, var4, var4 + 1}) {
                  union(var0, var12, var13 * var4 + var14 + var22);
                  var1[var13 * var4 + var14 + var22] = var1[var13 * var4 + var14 + var22] | var17;
               }
            }
         }
      }
   }

   private static int join(int[] var0, int var1, int var2, int var3, int var4, float var5, float var6, float var7, float var8) {
      int var9 = clamp((int)Math.floor(var5 / var4), var2 - 1);
      int var10 = clamp((int)Math.floor((var7 - 1.0F) / var4) + 1, var2 - 1);
      int var11 = clamp((int)Math.floor(var6 / var4), var3 - 1);
      int var12 = clamp((int)Math.floor((var8 - 1.0F) / var4) + 1, var3 - 1);

      for (int var13 = var11; var13 <= var12; var13++) {
         for (int var14 = var9; var14 <= var10; var14++) {
            int var15 = var13 * var2 + var14;
            var1 = var1 < 0 ? var15 : var1;
            union(var0, var1, var15);
         }
      }

      return var1;
   }

   private static void meet(int[] var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      for (int var8 = Math.max(0, var5); var8 <= Math.min(var3 - 1, var7); var8++) {
         for (int var9 = Math.max(0, var4); var9 <= Math.min(var2 - 1, var6); var9++) {
            if (var0[var8 * var2 + var9] >= 0) {
               union(var0, var1, var8 * var2 + var9);
            }
         }
      }
   }

   private static void mark(boolean[] var0, ReliefGround var1, float var2, float var3, float var4, float var5) {
      byte var6 = 4;
      int var7 = clamp((int)Math.ceil(var2 / var6 - 0.5F), var1.width - 1);
      int var8 = clamp((int)Math.floor(var4 / var6 - 0.5F), var1.width - 1);
      int var9 = clamp((int)Math.ceil(var3 / var6 - 0.5F), var1.height - 1);
      int var10 = clamp((int)Math.floor(var5 / var6 - 0.5F), var1.height - 1);

      for (int var11 = var9; var11 <= var10; var11++) {
         for (int var12 = var7; var12 <= var8; var12++) {
            var0[var11 * var1.width + var12] = true;
         }
      }
   }

   private static int lots(ReliefGround var0, boolean[] var1, boolean[] var2, int[] var3, int var4, int var5, int var6, ReliefRules var7) {
      int var8 = var0.width;
      byte var9 = 4;
      boolean[] var10 = opened(var0, var1, var2);
      boolean[] var11 = new boolean[var0.kind.length];
      int[] var12 = new int[var0.kind.length];
      int var13 = Math.max(1, var7.lotSquares() / (var9 * var9));
      int var14 = 10000 / (var9 * var9);
      int var15 = 0;

      for (int var16 = 0; var16 < var11.length; var16++) {
         if (!var11[var16] && var10[var16]) {
            var11[var16] = true;
            int var17 = 0;
            int var18 = 0;
            var12[var18++] = var16;

            while (var17 < var18) {
               int var19 = var12[var17++];
               int var20 = var19 % var8;

               for (int var24 : new int[]{var20 > 0 ? var19 - 1 : -1, var20 < var8 - 1 ? var19 + 1 : -1, var19 - var8, var19 + var8}) {
                  if (var24 >= 0 && var24 < var11.length && !var11[var24] && var10[var24]) {
                     var11[var24] = true;
                     var12[var18++] = var24;
                  }
               }
            }

            if (var18 >= var13 && var18 <= var14) {
               var15++;
               int var26 = -1;

               for (int var27 = 0; var27 < var18; var27++) {
                  int var29 = var12[var27];
                  float var31 = var29 % var8 * var9;
                  float var33 = var29 / var8 * var9;
                  var26 = join(var3, var26, var4, var5, var6, var31, var33, var31 + var9, var33 + var9);
               }

               for (int var28 = 0; var28 < var18; var28++) {
                  int var30 = var12[var28] % var8 * var9;
                  int var32 = var12[var28] / var8 * var9;
                  meet(var3, var26, var4, var5, var30 / var6 - 1, var32 / var6 - 1, (var30 + var9) / var6 + 1, (var32 + var9) / var6 + 1);
               }
            }
         }
      }

      return var15;
   }

   private static boolean[] opened(ReliefGround var0, boolean[] var1, boolean[] var2) {
      int var3 = var0.width;
      int var4 = var0.height;
      boolean[] var5 = new boolean[var3 * var4];

      for (int var6 = 0; var6 < var5.length; var6++) {
         var5[var6] = paving(var0, var1, var2, var6);
      }

      boolean[] var8 = eroded(grown(var5, var3, var4), var3, var4);

      for (int var7 = 0; var7 < var8.length; var7++) {
         var8[var7] &= !var1[var7] && !var2[var7] && var0.kind[var7] != 1;
      }

      return grown(eroded(var8, var3, var4), var3, var4);
   }

   private static boolean[] grown(boolean[] var0, int var1, int var2) {
      boolean[] var3 = new boolean[var1 * var2];

      for (int var4 = 0; var4 < var2; var4++) {
         for (int var5 = 0; var5 < var1; var5++) {
            int var6 = var4 * var1 + var5;
            var3[var6] = var0[var6] || var5 > 0 && var0[var6 - 1] || var4 > 0 && var0[var6 - var1] || var5 > 0 && var4 > 0 && var0[var6 - var1 - 1];
         }
      }

      return var3;
   }

   private static boolean[] eroded(boolean[] var0, int var1, int var2) {
      boolean[] var3 = new boolean[var1 * var2];

      for (int var4 = 0; var4 + 1 < var2; var4++) {
         for (int var5 = 0; var5 + 1 < var1; var5++) {
            int var6 = var4 * var1 + var5;
            var3[var6] = var0[var6] && var0[var6 + 1] && var0[var6 + var1] && var0[var6 + var1 + 1];
         }
      }

      return var3;
   }

   private static boolean paving(ReliefGround var0, boolean[] var1, boolean[] var2, int var3) {
      return var0.kind[var3] == 2 && !var1[var3] && !var2[var3];
   }

   private static float[] heights(int[] var0, int var1, float[] var2, float[] var3, float[] var4, int var5, int var6, int var7, ReliefRules var8, float[] var9) {
      float[][] var10 = new float[var1][];
      int[] var11 = new int[var1];

      for (int var15 : var0) {
         if (var15 >= 0) {
            var11[var15]++;
         }
      }

      for (int var19 = 0; var19 < var1; var19++) {
         var10[var19] = new float[var11[var19]];
         var11[var19] = 0;
      }

      for (int var20 = 0; var20 < var0.length; var20++) {
         if (var0[var20] >= 0) {
            var10[var0[var20]][var11[var0[var20]]++] = var2[var20];
         }
      }

      float[] var21 = new float[var1];
      float[] var22 = new float[var1];
      float[] var23 = new float[var1];
      Arrays.fill(var22, Float.NEGATIVE_INFINITY);
      Arrays.fill(var23, Float.POSITIVE_INFINITY);

      for (int var24 = 0; var24 < var1; var24++) {
         Arrays.sort(var10[var24]);
         var21[var24] = var10[var24][var10[var24].length / 2];
      }

      hold(var0, var3, var5, var6, var7, var8.metresASquare(var8.driveway()), 0.0F, var22, var23, false);
      float[] var25 = new float[var1];
      Arrays.fill(var9, Float.NEGATIVE_INFINITY);
      Arrays.fill(var25, Float.POSITIVE_INFINITY);
      hold(var0, var4, var5, var6, var7, 0.0F, var8.freeboard(), var9, var25, true);

      for (int var16 = 0; var16 < var1; var16++) {
         var22[var16] = Math.max(var22[var16], var9[var16]);
      }

      float[] var26 = new float[var0.length];
      float[] var17 = new float[var0.length];
      ReliefSweeps.envelope(var3, var26, var17, var5, var6, var8.metresASquare(var8.bank()) * var7);

      for (int var18 = 0; var18 < var0.length; var18++) {
         if (var0[var18] >= 0) {
            var22[var0[var18]] = Math.max(var22[var0[var18]], var26[var18]);
            var23[var0[var18]] = Math.min(var23[var0[var18]], var17[var18]);
         }
      }

      for (int var27 = 0; var27 < var1; var27++) {
         var21[var27] = var22[var27] <= var23[var27] ? Math.max(var22[var27], Math.min(var23[var27], var21[var27])) : var22[var27];
      }

      return var21;
   }

   private static void hold(int[] var0, float[] var1, int var2, int var3, int var4, float var5, float var6, float[] var7, float[] var8, boolean var9) {
      float[] var10 = new float[var1.length];
      float[] var11 = new float[var1.length];

      for (int var12 = 0; var12 < var1.length; var12++) {
         boolean var13 = !Float.isNaN(var1[var12]);
         var10[var12] = var13 ? 0.0F : Float.POSITIVE_INFINITY;
         var11[var12] = var13 ? var1[var12] : 0.0F;
      }

      ReliefSweeps.nearest(var10, var11, var2, var3);

      for (int var15 = 0; var15 < var0.length; var15++) {
         int var16 = var0[var15];
         if (var16 >= 0 && !(var10[var15] * var4 > 16.0F)) {
            float var14 = var5 * var10[var15] * var4;
            var7[var16] = Math.max(var7[var16], var11[var15] - var14 + var6);
            if (!var9) {
               var8[var16] = Math.min(var8[var16], var11[var15] + var14);
            }
         }
      }
   }

   private static int[] sharing(int[] var0, int[] var1, int var2, int var3, int var4, int var5) {
      int[] var6 = new int[var5];

      for (int var7 = 0; var7 + 4 <= var0.length; var7 += 4) {
         int var8 = clamp(var0[var7] / var4, var2 - 1);
         int var9 = clamp(var0[var7 + 1] / var4, var3 - 1);
         int var10 = var1[var9 * var2 + var8];
         if (var10 >= 0) {
            var6[var10]++;
         }
      }

      int[] var12 = new int[5];

      for (int var11 : var6) {
         var12[0] += var11 > 1 ? 1 : 0;
         var12[var11 == 1 ? 1 : (var11 <= 5 ? 2 : (var11 <= 20 ? 3 : 4))] = var12[var11 == 1 ? 1 : (var11 <= 5 ? 2 : (var11 <= 20 ? 3 : 4))]
            + (var11 > 0 ? var11 : 0);
      }

      return var12;
   }

   private static void closed(int[] var0, int var1, int var2) {
      for (int var3 = 0; var3 < 2; var3++) {
         for (int var4 = 0; var4 < var0.length; var4++) {
            int var5 = var4 % var1;
            int var6 = var4 / var1;
            if (var0[var4] < 0 && var5 != 0 && var6 != 0 && var5 != var1 - 1 && var6 != var2 - 1) {
               int var7 = same(var0, var4 - 1, var4 + 1);
               int var8 = same(var0, var4 - var1, var4 + var1);
               if (var7 >= 0 || var8 >= 0) {
                  union(var0, var7 >= 0 ? var7 : var8, var4);
               }
            }
         }
      }
   }

   private static int same(int[] var0, int var1, int var2) {
      if (var0[var1] >= 0 && var0[var2] >= 0) {
         int var3 = root(var0, var1);
         return var3 == root(var0, var2) ? var3 : -1;
      } else {
         return -1;
      }
   }

   private static int number(int[] var0, int[] var1) {
      int[] var2 = new int[var0.length];
      Arrays.fill(var2, -1);
      int var3 = 0;

      for (int var4 = 0; var4 < var0.length; var4++) {
         if (var0[var4] < 0) {
            var1[var4] = -1;
         } else {
            int var5 = root(var0, var4);
            if (var2[var5] < 0) {
               var2[var5] = var3++;
            }

            var1[var4] = var2[var5];
         }
      }

      return var3;
   }

   private static void union(int[] var0, int var1, int var2) {
      var0[var1] = var0[var1] < 0 ? var1 : var0[var1];
      var0[var2] = var0[var2] < 0 ? var2 : var0[var2];
      int var3 = root(var0, var1);
      int var4 = root(var0, var2);
      if (var3 != var4) {
         var0[var4] = var3;
      }
   }

   private static int root(int[] var0, int var1) {
      while (var0[var1] != var1) {
         var0[var1] = var0[var0[var1]];
         var1 = var0[var1];
      }

      return var1;
   }

   private static int clamp(int var0, int var1) {
      return Math.max(0, Math.min(var1, var0));
   }

   record Pads(int[] pad, float[] height, boolean[] core, float[] floor, int buildings, int lots, int merged, int largest, int[] sharing) {
      String report() {
         return String.format(
            "pads %d (buildings %d, lots %d, holding more than one building %d, the largest %d squares; buildings alone %d, with up to 4 others %d, up to 19 %d, more %d)",
            this.height.length,
            this.buildings,
            this.lots,
            this.merged,
            this.largest,
            this.sharing[0],
            this.sharing[1],
            this.sharing[2],
            this.sharing[3]
         );
      }
   }
}
