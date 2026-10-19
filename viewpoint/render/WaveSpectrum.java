package viewpoint.render;

import java.util.Random;

final class WaveSpectrum {
   static final double G = 9.81;
   private static final double GAMMA = 3.3;
   private static final double SWELL = 0.3;
   private static final double SPREAD = 0.2;

   private WaveSpectrum() {
   }

   static float[] initial(int var0, double var1, double var3, double var5, WaveSpectrum.Sea var7, long var8) {
      double var10 = (Math.PI * 2) / var1;
      double var12 = 0.076 * Math.pow(var7.windSpeed() * var7.windSpeed() / (var7.fetch() * 9.81), 0.22);
      double var14 = 22.0 * Math.pow(96.23610000000001 / (var7.windSpeed() * var7.fetch()), 0.3333333333333333);
      double[] var16 = new double[var0 * var0];
      double[] var17 = new double[var0 * var0];
      Random var18 = new Random(var8);

      for (int var19 = 0; var19 < var0; var19++) {
         for (int var20 = 0; var20 < var0; var20++) {
            double var21 = (var20 - var0 / 2) * var10;
            double var23 = (var19 - var0 / 2) * var10;
            double var25 = Math.hypot(var21, var23);
            double var27 = var18.nextGaussian();
            double var29 = var18.nextGaussian();
            double var31 = !(var25 < var3) && !(var25 >= var5) && var25 != 0.0 ? amplitude(var25, Math.atan2(var21, var23), var10, var12, var14, var7) : 0.0;
            var16[var19 * var0 + var20] = var27 * var31;
            var17[var19 * var0 + var20] = var29 * var31;
         }
      }

      float[] var33 = new float[var0 * var0 * 4];

      for (int var34 = 0; var34 < var0; var34++) {
         for (int var35 = 0; var35 < var0; var35++) {
            int var22 = var34 * var0 + var35;
            int var36 = Math.floorMod(-var34, var0) * var0 + Math.floorMod(-var35, var0);
            var33[var22 * 4] = (float)var16[var22];
            var33[var22 * 4 + 1] = (float)var17[var22];
            var33[var22 * 4 + 2] = (float)var16[var36];
            var33[var22 * 4 + 3] = (float)(-var17[var36]);
         }
      }

      return var33;
   }

   static double dispersion(double var0, double var2) {
      return Math.sqrt(9.81 * var0 * Math.tanh(var0 * var2));
   }

   private static double amplitude(double var0, double var2, double var4, double var6, double var8, WaveSpectrum.Sea var10) {
      double var11 = var0 * var10.depth();
      double var13 = Math.tanh(var11);
      double var15 = Math.sqrt(9.81 * var0 * var13);
      double var17 = 4.905 * (var13 + var11 * (1.0 - var13 * var13)) / var15;
      double var19 = tma(var15, var8, var6, var10.depth());
      double var21 = 0.03183098861837907 + 0.8 * hasselmann(var15, var8, var10.windSpeed(), var2 - var10.windAngle());
      return Math.sqrt(2.0 * var19 * var21 * var17 / var0 * var4 * var4);
   }

   private static double tma(double var0, double var2, double var4, double var6) {
      double var8 = var0 <= var2 ? 0.07 : 0.09;
      double var10 = Math.exp(-(var0 - var2) * (var0 - var2) / (2.0 * var8 * var8 * var2 * var2));
      double var12 = var4 * 9.81 * 9.81 / Math.pow(var0, 5.0) * Math.exp(-1.25 * Math.pow(var2 / var0, 4.0)) * Math.pow(3.3, var10);
      double var14 = Math.min(var0 * Math.sqrt(var6 / 9.81), 2.0);
      double var16 = var14 <= 1.0 ? 0.5 * var14 * var14 : 1.0 - 0.5 * (2.0 - var14) * (2.0 - var14);
      return var12 * var16;
   }

   private static double hasselmann(double var0, double var2, double var4, double var6) {
      double var8 = var0 / var2;
      double var10 = var0 <= var2 ? 6.97 * Math.pow(var8, 4.06) : 9.77 * Math.pow(var8, -2.33 - 1.45 * (var4 * var2 / 9.81 - 1.17));
      var10 += 16.0 * Math.tanh(var2 / var0) * 0.3 * 0.3;
      return longuetHiggins(var10) * Math.pow(Math.abs(Math.cos(var6 * 0.5)), 2.0 * var10);
   }

   private static double longuetHiggins(double var0) {
      return var0 < 0.4
         ? 0.15915494309189535 + var0 * (0.220636 + var0 * (-0.109 + var0 * 0.09))
         : 1.0 / Math.sqrt(Math.PI) * (Math.sqrt(var0) * 0.5 + 0.0625 / Math.sqrt(var0));
   }

   record Sea(double windSpeed, double windAngle, double fetch, double depth) {
   }
}
