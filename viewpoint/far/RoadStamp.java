package viewpoint.far;

import java.util.Arrays;
import viewpoint.platform.Tuning;

final class RoadStamp {
   private static final float HALF_DIAGONAL = 0.7072F;
   private final int[] street;
   private final float[] xs;
   private final float[] ys;
   private final float[] widths;
   private final float[] natural;
   private final float[] water;
   private final int count;
   private final int width;
   private final int height;
   private final int spacing;
   private final ReliefGround ground;
   private final ReliefRules rules;

   RoadStamp(ReliefNetwork.Samples var1, float[] var2, float[] var3, ReliefGround var4, int var5, int var6, int var7, ReliefRules var8) {
      this.street = var1.street();
      this.xs = var1.xs();
      this.ys = var1.ys();
      this.widths = var1.widths();
      this.count = var1.count();
      this.natural = var2;
      this.water = var3;
      this.ground = var4;
      this.width = var5;
      this.height = var6;
      this.spacing = var7;
      this.rules = var8;
   }

   ReliefNetwork.Roads stamp(float[] var1, RoadGrades var2) {
      float[] var3 = new float[this.width * this.height];
      float[] var4 = new float[this.width * this.height];
      float[] var5 = new float[this.width * this.height];
      Arrays.fill(var3, Float.NaN);
      Arrays.fill(var4, Float.POSITIVE_INFINITY);
      boolean[] var6 = new boolean[this.ground.width * this.ground.height];
      float var7 = 0.0F;
      float var8 = 0.0F;
      float var9 = Tuning.reliefSquaresPerMetre * this.rules.height();
      int[] var10 = new int[3];

      for (int var11 = 0; var11 + 1 < this.count; var11++) {
         if (this.street[var11] == this.street[var11 + 1]) {
            float var12 = (float)Math.hypot(this.xs[var11 + 1] - this.xs[var11], this.ys[var11 + 1] - this.ys[var11]);
            var7 = Math.max(var7, var12 > 0.0F ? Math.abs(var1[var11 + 1] - var1[var11]) / var12 * var9 : 0.0F);
            float var13 = var2.turn(var11, var1) * var9;
            var8 = Math.max(var8, var13);
            var2.tally(var11, var13, var10);
            this.segment(var11, var1, var3, var4, var5);
            this.band(var11, var6);
         }
      }

      float var14 = this.rules.metresASquare(this.rules.crossSlope());

      for (int var15 = 0; var15 < var3.length; var15++) {
         if (!Float.isNaN(var3[var15])) {
            float var16 = var14 * Math.min(var4[var15], var5[var15]);
            var3[var15] = Math.max(var3[var15] - var16, Math.min(var3[var15] + var16, this.natural[var15]));
            var3[var15] = this.meetsWater(var15) ? Math.max(var3[var15], this.water[var15]) : var3[var15];
         }
      }

      return new ReliefNetwork.Roads(var3, var4, var6, var7, var8, var10, this.count);
   }

   private void segment(int var1, float[] var2, float[] var3, float[] var4, float[] var5) {
      float var6 = this.widths[var1] * 0.5F;
      float var7 = var6 + this.spacing * 0.7072F;
      float var8 = this.xs[var1];
      float var9 = this.ys[var1];
      float var10 = this.xs[var1 + 1];
      float var11 = this.ys[var1 + 1];
      int var12 = Math.max(0, (int)Math.floor((Math.min(var8, var10) - var7) / this.spacing));
      int var13 = Math.min(this.width - 2, (int)Math.floor((Math.max(var8, var10) + var7) / this.spacing));
      int var14 = Math.max(0, (int)Math.floor((Math.min(var9, var11) - var7) / this.spacing));
      int var15 = Math.min(this.height - 2, (int)Math.floor((Math.max(var9, var11) + var7) / this.spacing));

      for (int var16 = var14; var16 <= var15; var16++) {
         for (int var17 = var12; var17 <= var13; var17++) {
            float var18 = (var17 + 0.5F) * this.spacing;
            float var19 = (var16 + 0.5F) * this.spacing;
            if (!(along(var8, var9, var10, var11, var18, var19)[1] > var7)) {
               for (int var20 = 0; var20 < 4; var20++) {
                  int var21 = var17 + (var20 & 1);
                  int var22 = var16 + (var20 >> 1);
                  int var23 = var22 * this.width + var21;
                  float[] var24 = along(var8, var9, var10, var11, var21 * this.spacing, var22 * this.spacing);
                  if (var24[1] < var4[var23]) {
                     var4[var23] = var24[1];
                     var5[var23] = var6;
                     var3[var23] = var2[var1] + (var2[var1 + 1] - var2[var1]) * var24[0];
                  }
               }
            }
         }
      }
   }

   private void band(int var1, boolean[] var2) {
      byte var3 = 4;
      float var4 = this.widths[var1] * 0.5F + 1.0F;
      float var5 = this.xs[var1];
      float var6 = this.ys[var1];
      float var7 = this.xs[var1 + 1];
      float var8 = this.ys[var1 + 1];
      int var9 = Math.max(0, (int)Math.floor((Math.min(var5, var7) - var4) / var3));
      int var10 = Math.min(this.ground.width - 1, (int)Math.floor((Math.max(var5, var7) + var4) / var3));
      int var11 = Math.max(0, (int)Math.floor((Math.min(var6, var8) - var4) / var3));
      int var12 = Math.min(this.ground.height - 1, (int)Math.floor((Math.max(var6, var8) + var4) / var3));

      for (int var13 = var11; var13 <= var12; var13++) {
         for (int var14 = var9; var14 <= var10; var14++) {
            if (along(var5, var6, var7, var8, (var14 + 0.5F) * var3, (var13 + 0.5F) * var3)[1] <= var4) {
               var2[var13 * this.ground.width + var14] = true;
            }
         }
      }
   }

   private static float[] along(float var0, float var1, float var2, float var3, float var4, float var5) {
      float var6 = var2 - var0;
      float var7 = var3 - var1;
      float var8 = var6 * var6 + var7 * var7;
      float var9 = var8 > 0.0F ? Math.max(0.0F, Math.min(1.0F, ((var4 - var0) * var6 + (var5 - var1) * var7) / var8)) : 0.0F;
      return new float[]{var9, (float)Math.hypot(var4 - (var0 + var6 * var9), var5 - (var1 + var7 * var9))};
   }

   private boolean meetsWater(int var1) {
      byte var2 = 4;
      int var3 = var1 % this.width * this.spacing;
      int var4 = var1 / this.width * this.spacing;

      for (int var5 = (var4 - 1) / var2; var5 <= var4 / var2; var5++) {
         for (int var6 = (var3 - 1) / var2; var6 <= var3 / var2; var6++) {
            if (this.ground.at(var6, var5) == 1) {
               return true;
            }
         }
      }

      return false;
   }
}
