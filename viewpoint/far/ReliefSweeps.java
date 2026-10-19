package viewpoint.far;

final class ReliefSweeps {
   private static final int BLURS = 3;
   private static final int LOCAL = 2;
   private static final float DIAGONAL = (float)Math.sqrt(2.0);
   private static final int[][] FORWARD = new int[][]{{-1, 0}, {0, -1}, {-1, -1}, {1, -1}};
   private static final int[][] BACKWARD = new int[][]{{1, 0}, {0, 1}, {1, 1}, {-1, 1}};
   private static final int[][] NONE = new int[0][];

   private ReliefSweeps() {
   }

   static void nearest(float[] var0, float[] var1, int var2, int var3) {
      for (int var4 = 0; var4 < 2; var4++) {
         int[][] var5 = var4 == 0 ? FORWARD : BACKWARD;

         for (int var6 = 0; var6 < var0.length; var6++) {
            int var7 = var4 == 0 ? var6 : var0.length - 1 - var6;
            int var8 = var7 % var2;
            int var9 = var7 / var2;

            for (int[] var13 : var5) {
               int var14 = var8 + var13[0];
               int var15 = var9 + var13[1];
               if (var14 >= 0 && var15 >= 0 && var14 < var2 && var15 < var3) {
                  float var16 = var0[var15 * var2 + var14] + (var13[0] != 0 && var13[1] != 0 ? DIAGONAL : 1.0F);
                  if (var16 < var0[var7]) {
                     var0[var7] = var16;
                     var1[var7] = var1[var15 * var2 + var14];
                  }
               }
            }
         }
      }
   }

   static int bank(float[] var0, float[] var1, int var2, int var3, float var4) {
      return bank(var0, var1, new boolean[var0.length], var2, var3, var4, 0);
   }

   static int bank(float[] var0, float[] var1, boolean[] var2, int var3, int var4, float var5, int var6) {
      float[] var7 = new float[var0.length];
      float[] var8 = new float[var0.length];
      envelope(var1, var7, var8, null, var3, var4, var5);
      boolean[] var9 = new boolean[var0.length];
      float[][] var10 = null;
      int var11 = 0;

      for (int var12 = 0; var12 < var0.length; var12++) {
         float var13 = var0[var12];
         if (!Float.isNaN(var1[var12])) {
            var0[var12] = var1[var12];
         } else if (var7[var12] <= var8[var12]) {
            var0[var12] = Math.max(var7[var12], Math.min(var8[var12], var0[var12]));
         } else {
            float[] var14 = local(var1, var12, var3, var4, var5);
            if (var14[0] > var14[1]) {
               var10 = var10 == null ? nearestFixed(var1, var3, var4) : var10;
               float var15 = var10[0][var12] * var5;
               var14[0] = var10[1][var12] - var15;
               var14[1] = var10[1][var12] + var15;
            }

            var0[var12] = Math.max(var14[0], Math.min(var14[1], var13));
            var11++;
         }

         var9[var12] = !Float.isNaN(var1[var12]) || var2[var12] || var0[var12] != var13;
      }

      if (var6 > 0) {
         round(var0, var1, var2, var7, var8, var9, var3, var4, var6);
      }

      return var11;
   }

   private static void round(float[] var0, float[] var1, boolean[] var2, float[] var3, float[] var4, boolean[] var5, int var6, int var7, int var8) {
      boolean[] var9 = new boolean[var0.length];

      for (int var10 = 0; var10 < var0.length; var10++) {
         int var11 = var10 % var6;
         int var12 = var10 / var6;
         boolean var13 = var5[var10]
            || var11 > 0 && var5[var10 - 1]
            || var11 < var6 - 1 && var5[var10 + 1]
            || var12 > 0 && var5[var10 - var6]
            || var12 < var7 - 1 && var5[var10 + var6];
         var9[var10] = var13 && Float.isNaN(var1[var10]) && !var2[var10] && var3[var10] <= var4[var10];
      }

      for (int var16 = 0; var16 < 2 * var8; var16++) {
         for (int var17 = 0; var17 < var0.length; var17++) {
            int var18 = var17 % var6;
            int var19 = var17 / var6;
            if (var9[var17] && (var18 + var19 + var16) % 2 == 0) {
               float var14 = var0[var18 > 0 ? var17 - 1 : var17]
                  + var0[var18 < var6 - 1 ? var17 + 1 : var17]
                  + var0[var19 > 0 ? var17 - var6 : var17]
                  + var0[var19 < var7 - 1 ? var17 + var6 : var17];
               float var15 = (var0[var17] + var14 * 0.25F) * 0.5F;
               var0[var17] = Math.max(var3[var17], Math.min(var4[var17], var15));
            }
         }
      }
   }

   private static float[] local(float[] var0, int var1, int var2, int var3, float var4) {
      int var5 = var1 % var2;
      int var6 = var1 / var2;
      float var7 = Float.NEGATIVE_INFINITY;
      float var8 = Float.POSITIVE_INFINITY;

      for (int var9 = Math.max(0, var6 - 2); var9 <= Math.min(var3 - 1, var6 + 2); var9++) {
         for (int var10 = Math.max(0, var5 - 2); var10 <= Math.min(var2 - 1, var5 + 2); var10++) {
            float var11 = var0[var9 * var2 + var10];
            float var12 = var4 * (float)Math.hypot(var10 - var5, var9 - var6);
            if (!Float.isNaN(var11)) {
               var7 = Math.max(var7, var11 - var12);
               var8 = Math.min(var8, var11 + var12);
            }
         }
      }

      return new float[]{var7, var8};
   }

   private static float[][] nearestFixed(float[] var0, int var1, int var2) {
      float[] var3 = new float[var0.length];
      float[] var4 = new float[var0.length];

      for (int var5 = 0; var5 < var0.length; var5++) {
         boolean var6 = !Float.isNaN(var0[var5]);
         var3[var5] = var6 ? 0.0F : Float.POSITIVE_INFINITY;
         var4[var5] = var6 ? var0[var5] : 0.0F;
      }

      nearest(var3, var4, var1, var2);
      return new float[][]{var3, var4};
   }

   static void envelope(float[] var0, float[] var1, float[] var2, int var3, int var4, float var5) {
      envelope(var0, var1, var2, null, var3, var4, var5);
   }

   private static void envelope(float[] var0, float[] var1, float[] var2, boolean[] var3, int var4, int var5, float var6) {
      ReliefSweeps.Along var7 = (var1x, var2x, var3x) -> var6 * (var3x ? DIAGONAL : 1.0F);
      start(var0, var1, var2);

      for (int var8 = 0; var8 < 2; var8++) {
         sweep(var1, var2, var3, var7, var4, var5);
      }
   }

   static void settledEnvelope(float[] var0, float[] var1, float[] var2, boolean[] var3, ReliefSweeps.Along var4, int var5, int var6) {
      start(var0, var1, var2);
      boolean var7 = true;

      while (var7) {
         var7 = sweep(var1, var2, var3, var4, var5, var6);
      }
   }

   private static void start(float[] var0, float[] var1, float[] var2) {
      for (int var3 = 0; var3 < var0.length; var3++) {
         boolean var4 = !Float.isNaN(var0[var3]);
         var1[var3] = var4 ? var0[var3] : Float.NEGATIVE_INFINITY;
         var2[var3] = var4 ? var0[var3] : Float.POSITIVE_INFINITY;
      }
   }

   private static boolean sweep(float[] var0, float[] var1, boolean[] var2, ReliefSweeps.Along var3, int var4, int var5) {
      boolean var6 = false;

      for (int var7 = 0; var7 < 2; var7++) {
         int[][] var8 = var7 == 0 ? FORWARD : BACKWARD;

         for (int var9 = 0; var9 < var0.length; var9++) {
            int var10 = var7 == 0 ? var9 : var0.length - 1 - var9;
            int var11 = var10 % var4;
            int var12 = var10 / var4;

            for (int[] var16 : var2 != null && !var2[var10] ? NONE : var8) {
               int var17 = var11 + var16[0];
               int var18 = var12 + var16[1];
               int var19 = var18 * var4 + var17;
               if (var17 >= 0 && var18 >= 0 && var17 < var4 && var18 < var5) {
                  float var20 = var3.step(var10, var19, var16[0] != 0 && var16[1] != 0);
                  if (!Float.isNaN(var20)) {
                     float var21 = var0[var19] - var20;
                     float var22 = var1[var19] + var20;
                     var6 |= var21 > var0[var10] || var22 < var1[var10];
                     var0[var10] = Math.max(var0[var10], var21);
                     var1[var10] = Math.min(var1[var10], var22);
                  }
               }
            }
         }
      }

      return var6;
   }

   static void graded(float[] var0, boolean[] var1, int var2, int var3, float var4) {
      graded(var0, var1, null, var2, var3, var4, Float.POSITIVE_INFINITY);
   }

   static void graded(float[] var0, boolean[] var1, float[] var2, int var3, int var4, float var5, float var6) {
      float[] var7 = new float[var0.length];
      float[] var8 = new float[var0.length];
      float[] var9 = new float[var0.length];

      for (int var10 = 0; var10 < var0.length; var10++) {
         var7[var10] = var1[var10] ? (var2 == null ? var0[var10] : top(var0, var1, var10, var3, var4)) : Float.NaN;
      }

      envelope(var7, var9, var8, var2 == null ? null : var1, var3, var4, var5);

      for (int var12 = 0; var12 < var0.length; var12++) {
         if (var1[var12]) {
            float var11 = (var8[var12] + var9[var12]) * 0.5F;
            var0[var12] = Math.max(var7[var12] - var6, Math.min(var7[var12] + var6, var11));
         }
      }

      if (var2 != null) {
         envelope(var2, var9, var8, var1, var3, var4, var5);

         for (int var13 = 0; var13 < var0.length; var13++) {
            if (var1[var13]) {
               var0[var13] = Math.max(var9[var13], Math.min(var8[var13], var0[var13]));
            }
         }
      }
   }

   private static float top(float[] var0, boolean[] var1, int var2, int var3, int var4) {
      int var5 = var2 % var3;
      int var6 = var2 / var3;
      float var7 = var0[var2];

      for (int var8 = Math.max(0, var6 - 1); var8 <= Math.min(var4 - 1, var6 + 1); var8++) {
         for (int var9 = Math.max(0, var5 - 1); var9 <= Math.min(var3 - 1, var5 + 1); var9++) {
            if (var1[var8 * var3 + var9]) {
               var7 = Math.max(var7, var0[var8 * var3 + var9]);
            }
         }
      }

      return var7;
   }

   static void between(float[] var0, float[] var1, float[] var2, int var3, int var4, float var5) {
      float[] var6 = new float[var0.length];
      float[] var7 = new float[var0.length];
      float[] var8 = new float[var0.length];
      float[] var9 = new float[var0.length];
      seeds(var1, var6, var7);
      seeds(var2, var8, var9);
      nearest(var6, var7, var3, var4);
      nearest(var8, var9, var3, var4);

      for (int var10 = 0; var10 < var0.length; var10++) {
         if (Float.isNaN(var1[var10]) && Float.isNaN(var2[var10]) && var6[var10] <= var5 && var8[var10] <= var5) {
            float var11 = Math.min(var7[var10], var9[var10]);
            float var12 = Math.max(var7[var10], var9[var10]);
            var0[var10] = Math.max(var11, Math.min(var12, var0[var10]));
         }
      }
   }

   private static void seeds(float[] var0, float[] var1, float[] var2) {
      for (int var3 = 0; var3 < var0.length; var3++) {
         boolean var4 = !Float.isNaN(var0[var3]);
         var1[var3] = var4 ? 0.0F : Float.POSITIVE_INFINITY;
         var2[var3] = var4 ? var0[var3] : 0.0F;
      }
   }

   static float[] blurred(float[] var0, int var1, int var2, int var3) {
      float[] var4 = (float[])var0.clone();

      for (int var5 = 0; var3 > 0 && var5 < 3; var5++) {
         var4 = blur(var4, var1, var2, var3, 1, var1);
         var4 = blur(var4, var1, var2, var3, var1, var2);
      }

      return var4;
   }

   private static float[] blur(float[] var0, int var1, int var2, int var3, int var4, int var5) {
      float[] var6 = new float[var0.length];
      int var7 = var4 == 1 ? var2 : var1;

      for (int var8 = 0; var8 < var7; var8++) {
         int var9 = var4 == 1 ? var8 * var1 : var8;
         double var10 = 0.0;

         for (int var12 = -var3; var12 <= var3; var12++) {
            var10 += var0[var9 + Math.max(0, Math.min(var5 - 1, var12)) * var4];
         }

         for (int var13 = 0; var13 < var5; var13++) {
            var6[var9 + var13 * var4] = (float)(var10 / (2 * var3 + 1));
            var10 += var0[var9 + Math.min(var5 - 1, var13 + var3 + 1) * var4] - var0[var9 + Math.max(0, var13 - var3) * var4];
         }
      }

      return var6;
   }

   interface Along {
      float step(int var1, int var2, boolean var3);

      static ReliefSweeps.Along over(float[] var0, float[] var1, float var2, float var3) {
         return (var4, var5, var6) -> {
            float var7 = var2 * (var6 ? ReliefSweeps.DIAGONAL : 1.0F) + Math.abs(var0[var4] - var0[var5]);
            return Math.abs(var1[var4] - var1[var5]) - var7 > var3 ? Float.NaN : var7;
         };
      }

      static ReliefSweeps.Along limited(float[] var0) {
         return (var1, var2, var3) -> (var0[var1] + var0[var2]) * 0.5F * (var3 ? ReliefSweeps.DIAGONAL : 1.0F);
      }
   }
}
