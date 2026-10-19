package viewpoint.gunplay;

import java.util.Random;

final class Cone {
   static final double SPREADS_IN_RADIUS = 2.0;
   static final double VARIANCE;
   private static final double P = 0.3275911;
   private static final double A1 = 0.254829592;
   private static final double A2 = -0.284496736;
   private static final double A3 = 1.421413741;
   private static final double A4 = -1.453152027;
   private static final double A5 = 1.061405429;
   private static final double RAY = 2.0;
   private static final double NEGLIGIBLE = 1.0E-12;

   static double hit(Outline var0, double var1, double var3, double var5) {
      if (var5 < 0.025F) {
         return var0.covers(var1, var3) ? 1.0 : 0.0;
      }

      double var7 = var5 / 2.0;
      double var9 = 1.0 / var7;
      double[] var11 = var0.across;
      double[] var12 = var0.up;
      integrals(var11, var0.columns, (var0.left - var1) * var9, 0.0125F * var9);
      integrals(var12, var0.rows, (var0.bottom - var3) * var9, 0.0125F * var9);
      double var13 = 0.0;

      for (int var15 = 0; var15 < var0.rows; var15++) {
         double var16 = var0.bottom + (var15 + 0.5) * 0.0125F - var3;
         double var18 = var5 * var5 - var16 * var16;
         if (!(var12[var15] < 1.0E-12) && !(var18 < 0.0)) {
            var18 = Math.sqrt(var18);
            int var20 = Math.max(0, first(var0.left, var1 - var18));
            int var21 = Math.min(var0.columns - 1, last(var0.left, var1 + var18));
            double var22 = 0.0;
            int var24 = var15 * var0.columns;

            for (int var25 = var20; var25 <= var21; var25++) {
               var22 += var0.covered[var24 + var25] * var11[var25];
            }

            var13 += var12[var15] * var22;
         }
      }

      double var26 = disc(var0.left, var0.bottom, var1, var3, var5);
      return var26 > 0.0 ? Math.min(1.0, var13 / var26) : 0.0;
   }

   static void draw(Random var0, double var1, double[] var3) {
      if (var1 <= 0.0) {
         var3[0] = var3[1] = 0.0;
      } else {
         double var4 = var1 / 2.0;

         double var6;
         double var8;
         do {
            var6 = var0.nextGaussian() * var4;
            var8 = var0.nextGaussian() * var4;
         } while (var6 * var6 + var8 * var8 > var1 * var1);

         var3[0] = var6;
         var3[1] = var8;
      }
   }

   static double phi(double var0) {
      double var2 = Math.abs(var0) / Math.sqrt(2.0);
      double var4 = 1.0 / (1.0 + 0.3275911 * var2);
      double var6 = 0.5
         * var4
         * (0.254829592 + var4 * (-0.284496736 + var4 * (1.421413741 + var4 * (-1.453152027 + var4 * 1.061405429))))
         * Math.exp(-var2 * var2);
      return var0 < 0.0 ? var6 : 1.0 - var6;
   }

   private static double disc(double var0, double var2, double var4, double var6, double var8) {
      double var10 = 2.0 / var8;
      double var12 = 0.0;
      int var14 = (int)Math.floor((var6 - var8 - var2) / 0.0125F);
      int var15 = (int)Math.ceil((var6 + var8 - var2) / 0.0125F);

      for (int var16 = var14; var16 <= var15; var16++) {
         double var17 = var2 + (var16 + 0.5) * 0.0125F - var6;
         double var19 = var8 * var8 - var17 * var17;
         if (!(var19 < 0.0)) {
            var19 = Math.sqrt(var19);
            int var21 = first(var0, var4 - var19);
            int var22 = last(var0, var4 + var19);
            if (var21 <= var22) {
               double var23 = (var2 + var16 * 0.0125F - var6) * var10;
               double var25 = (var0 + var21 * 0.0125F - var4) * var10;
               double var27 = (var0 + (var22 + 1) * 0.0125F - var4) * var10;
               var12 += (phi(var23 + 0.0125F * var10) - phi(var23)) * (phi(var27) - phi(var25));
            }
         }
      }

      return var12;
   }

   private static int first(double var0, double var2) {
      return (int)Math.ceil((var2 - var0) / 0.0125F - 0.5);
   }

   private static int last(double var0, double var2) {
      return (int)Math.floor((var2 - var0) / 0.0125F - 0.5);
   }

   private static void integrals(double[] var0, int var1, double var2, double var4) {
      double var6 = phi(var2);

      for (int var8 = 0; var8 < var1; var8++) {
         double var9 = phi(var2 + (var8 + 1) * var4);
         var0[var8] = var9 - var6;
         var6 = var9;
      }
   }

   private Cone() {
   }

   static {
      double var0 = 4.0;
      double var2 = Math.exp(-var0 / 2.0);
      VARIANCE = (1.0 - var0 / 2.0 * var2 / (1.0 - var2)) / var0;
   }
}
