package viewpoint.far;

import java.util.Arrays;

final class RoadGrades {
   private static final int DECK_REACH = 8;
   private static final int SWEEPS = 8000;
   private static final int TIED_SWEEPS = 1500;
   private static final float MOST_MOVE = 4.0F;
   private static final float AIM = 0.95F;
   private static final float OVER = 1.6F;
   private static final int STENCIL = 5;
   private final int[] street;
   private final int[] root;
   private final float[] xs;
   private final float[] ys;
   private final float[] floor;
   private final int count;
   private final ReliefRules rules;

   RoadGrades(int[] var1, float[] var2, float[] var3, int[] var4, float[] var5, int var6, ReliefRules var7) {
      this.street = var1;
      this.xs = var2;
      this.ys = var3;
      this.root = var4;
      this.floor = var5;
      this.count = var6;
      this.rules = var7;
   }

   void relax(float[] var1, int[][] var2, RoadGrades.Ties var3) {
      float var4 = this.rules.metresASquare(this.rules.grade()) * 0.95F;
      float var5 = this.rules.metresASquare(this.rules.gradeChange()) * 8.0F * 0.95F;
      int[] var6 = new int[this.count * 5];
      float[] var7 = new float[this.count * 5];
      float[] var8 = new float[this.count];
      float[] var9 = this.floors();
      int var10 = this.stencils(var6, var7);

      for (int var11 = 0; var11 < this.count; var11++) {
         var8[var11] = var1[this.root[var11]];
      }

      boolean[] var23 = new boolean[this.count];
      boolean[] var12 = new boolean[this.count];
      Arrays.fill(var23, true);
      int var13 = var3 == null ? 0 : var3.pad().length;
      boolean[] var14 = new boolean[var13];
      boolean[] var15 = new boolean[var13];
      Arrays.fill(var14, true);
      float[] var16 = (float[])var8.clone();
      float[] var17 = var3 == null ? null : (float[])var3.pad().clone();

      for (int var18 = 0; var18 < (var3 == null ? 8000 : 1500); var18++) {
         boolean var19 = var3 != null && keepTies(var8, var3, var23, var12, var14, var15);

         for (int var20 = 0; var3 != null && var20 < this.count; var20++) {
            var8[var20] = Math.max(var16[var20] - 4.0F, Math.min(var16[var20] + 4.0F, var8[var20]));
         }

         for (int var25 = 0; var25 < var13; var25++) {
            var3.pad()[var25] = Math.max(var17[var25] - 4.0F, Math.min(var17[var25] + 4.0F, var3.pad()[var25]));
         }

         for (int var26 = 0; var26 < var10; var26++) {
            if (touches(var23, var6, var26 * 5)) {
               var19 |= keepTurn(var8, var6, var7, var26 * 5, var5, var12);
            }
         }

         for (int var27 = 0; var27 < this.count; var27++) {
            for (int var21 = 0; var21 < var2[var27].length; var21 += 2) {
               int var22 = var2[var27][var21];
               if (var22 > var27 && (var23[var27] || var23[var22])) {
                  var19 |= keepGrade(var8, var27, var22, var4 * len(var2[var27], var21), var12);
               }
            }

            if (var8[var27] < var9[var27]) {
               var8[var27] = var9[var27];
               var19 = true;
               var12[var27] = true;
            }
         }

         if (!var19) {
            break;
         }

         boolean[] var28 = var23;
         var23 = var12;
         var12 = var28;
         Arrays.fill(var12, false);
         var28 = var14;
         var14 = var15;
         var15 = var28;
         Arrays.fill(var15, false);
      }

      for (int var24 = 0; var24 < this.count; var24++) {
         var1[var24] = var8[this.root[var24]];
      }
   }

   private static boolean keepTurn(float[] var0, int[] var1, float[] var2, int var3, float var4, boolean[] var5) {
      float var6 = 0.0F;

      for (int var7 = 0; var7 < 5; var7++) {
         var6 += var2[var3 + var7] * var0[var1[var3 + var7]];
      }

      if (Math.abs(var6) <= var4) {
         return false;
      }

      float var11 = 0.0F;

      for (int var8 = 0; var8 < 5; var8++) {
         float var9 = 0.0F;

         for (int var10 = 0; var10 < 5; var10++) {
            var9 += var1[var3 + var10] == var1[var3 + var8] ? var2[var3 + var10] : 0.0F;
         }

         var11 += var9 * var2[var3 + var8];
      }

      float var12 = Math.min(1.6F * (Math.abs(var6) - var4), Math.abs(var6)) * Math.signum(var6) / var11;

      for (int var13 = 0; var13 < 5; var13++) {
         var0[var1[var3 + var13]] = var0[var1[var3 + var13]] - var12 * var2[var3 + var13];
         var5[var1[var3 + var13]] = true;
      }

      return Math.abs(var6) > var4 / 0.95F;
   }

   private static boolean keepGrade(float[] var0, int var1, int var2, float var3, boolean[] var4) {
      float var5 = var0[var1] - var0[var2];
      if (Math.abs(var5) <= var3) {
         return false;
      }

      float var6 = Math.min(1.6F * (Math.abs(var5) - var3), Math.abs(var5)) * Math.signum(var5) * 0.5F;
      var0[var1] -= var6;
      var0[var2] += var6;
      var4[var1] = var4[var2] = true;
      return Math.abs(var5) > var3 / 0.95F;
   }

   private static boolean keepTies(float[] var0, RoadGrades.Ties var1, boolean[] var2, boolean[] var3, boolean[] var4, boolean[] var5) {
      boolean var6 = false;
      float[] var7 = var1.pad();

      for (int var8 = 0; var8 < var1.roadPadMost().length; var8++) {
         int var9 = var1.roadPad()[2 * var8];
         int var10 = var1.roadPad()[2 * var8 + 1];
         if (var2[var9] || var4[var10]) {
            float var11 = var0[var9] - var7[var10];
            float var12 = var1.roadPadMost()[var8];
            if (Math.abs(var11) > var12) {
               float var13 = Math.min(1.6F * (Math.abs(var11) - var12), Math.abs(var11)) * Math.signum(var11) * 0.5F;
               var0[var9] -= var13;
               var7[var10] += var13;
               var3[var9] = var5[var10] = true;
               var6 |= Math.abs(var11) > var12 / 0.95F;
            }
         }
      }

      for (int var14 = 0; var14 < var1.padPadMost().length; var14++) {
         int var16 = var1.padPad()[2 * var14];
         int var18 = var1.padPad()[2 * var14 + 1];
         if (var4[var16] || var4[var18]) {
            float var19 = var7[var16] - var7[var18];
            float var20 = var1.padPadMost()[var14];
            if (Math.abs(var19) > var20) {
               float var21 = Math.min(1.6F * (Math.abs(var19) - var20), Math.abs(var19)) * Math.signum(var19) * 0.5F;
               var7[var16] -= var21;
               var7[var18] += var21;
               var5[var16] = var5[var18] = true;
               var6 |= Math.abs(var19) > var20 / 0.95F;
            }
         }
      }

      for (int var15 = 0; var15 < var7.length; var15++) {
         float var17 = Math.max(var1.padFloor()[var15], Math.min(var1.padCeiling()[var15], var7[var15]));
         if (var17 != var7[var15]) {
            var7[var15] = var17;
            var6 = true;
            var5[var15] = true;
         }
      }

      return var6;
   }

   private static boolean touches(boolean[] var0, int[] var1, int var2) {
      for (int var3 = 0; var3 < 5; var3++) {
         if (var0[var1[var2 + var3]]) {
            return true;
         }
      }

      return false;
   }

   private static float len(int[] var0, int var1) {
      return Float.intBitsToFloat(var0[var1 + 1]);
   }

   private float[] floors() {
      float[] var1 = new float[this.count];
      Arrays.fill(var1, Float.NEGATIVE_INFINITY);

      for (int var2 = 0; var2 < this.count; var2++) {
         if (!Float.isNaN(this.floor[var2])) {
            var1[this.root[var2]] = Math.max(var1[this.root[var2]], this.floor[var2]);
         }
      }

      return var1;
   }

   private int stencils(int[] var1, float[] var2) {
      int var3 = 0;

      for (int var4 = 1; var4 + 1 < this.count; var4++) {
         int var5 = var3 * 5;
         if (this.street[var4 - 1] == this.street[var4]
            && this.street[var4 + 1] == this.street[var4]
            && this.span(var4, -1, var1, var2, var5)
            && this.span(var4, 1, var1, var2, var5 + 3)) {
            var1[var5 + 2] = this.root[var4];
            var2[var5 + 2] = -2.0F;
            var3++;
         }
      }

      return var3;
   }

   private boolean span(int var1, int var2, int[] var3, float[] var4, int var5) {
      float var6 = 8.0F;
      int var7 = var1;

      while (true) {
         int var8 = var7 + var2;
         if (var8 < 0 || var8 >= this.count || this.street[var8] != this.street[var1]) {
            return false;
         }

         float var9 = (float)Math.hypot(this.xs[var8] - this.xs[var7], this.ys[var8] - this.ys[var7]);
         if (var9 >= var6) {
            float var10 = var6 / var9;
            var3[var5] = this.root[var7];
            var4[var5] = 1.0F - var10;
            var3[var5 + 1] = this.root[var8];
            var4[var5 + 1] = var10;
            return true;
         }

         var6 -= var9;
         var7 += var2;
      }
   }

   float turn(int var1, float[] var2) {
      int var3 = var1 + 1;
      float var4 = this.spanFrom(var3, var2, -1);
      float var5 = this.spanFrom(var3, var2, 1);
      return !Float.isNaN(var4) && !Float.isNaN(var5) ? Math.abs(var5 - 2.0F * var2[var3] + var4) / 8.0F : 0.0F;
   }

   void tally(int var1, float var2, int[] var3) {
      if (!(var2 <= this.rules.gradeChange() * 1.01F)) {
         var3[0]++;
         boolean var4 = false;

         for (int var5 = Math.max(0, var1 - 8); var5 <= Math.min(this.count - 1, var1 + 2 + 8); var5++) {
            var4 |= this.street[var5] == this.street[var1] && !Float.isNaN(this.floor[this.root[var5]]);
         }

         var3[1] += var4 ? 1 : 0;
         var3[2] += var4 || this.root[var1 + 1] == var1 + 1 && this.root[var1] == var1 && this.root[var1 + 2] == var1 + 2 ? 0 : 1;
      }
   }

   private float spanFrom(int var1, float[] var2, int var3) {
      float var4 = 8.0F;
      int var5 = var1;

      while (true) {
         int var6 = var5 + var3;
         if (var6 < 0 || var6 >= this.count || this.street[var6] != this.street[var1]) {
            return Float.NaN;
         }

         float var7 = (float)Math.hypot(this.xs[var6] - this.xs[var5], this.ys[var6] - this.ys[var5]);
         if (var7 >= var4) {
            return var2[var5] + (var2[var6] - var2[var5]) * (var4 / var7);
         }

         var4 -= var7;
         var5 += var3;
      }
   }

   record Ties(float[] pad, int[] roadPad, float[] roadPadMost, int[] padPad, float[] padPadMost, float[] padFloor, float[] padCeiling) {
   }
}
