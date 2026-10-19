package viewpoint.render;

import viewpoint.platform.EarthAir;

public final class AirTables {
   public static final int TRANSMITTANCE_WIDTH = 256;
   public static final int TRANSMITTANCE_HEIGHT = 64;
   public static final int SCATTERING_SIZE = 32;
   private static final int SPHERE_SIDE = 8;
   private static final int SCATTERING_STEPS = 64;
   private static final int BRIGHTNESS_ENTRIES = 256;
   private static final int SKY_STEPS = 48;
   private static final double BRIGHTNESS_LOW = -0.35;
   private static final double[] HEIGHTS = new double[]{3.0, 15.0, 35.0, 60.0, 90.0};
   private static final double[] BAND_EDGES = new double[]{0.0, 9.0, 25.0, 47.0, 75.0, 90.0};
   private static final int AROUND = 5;
   private static final int PATCH = 16;
   private static final double ISOTROPIC = 0.07957747154594767;
   private static final double BOTTOM2 = 4.04496E7;
   private static final double TOP2 = 4.17316E7;
   public final float[] transmittance;
   public final float[] scattering;
   private final float[] zenith = new float[256];
   private final float[] mean = new float[256];

   private AirTables() {
      this.transmittance = transmittanceTable();
      this.scattering = scatteringTable(this.transmittance);
      this.brightness();
   }

   public static AirTables get() {
      return AirTables.Holder.TABLES;
   }

   public float zenith(double var1) {
      return bySun(this.zenith, var1);
   }

   public float mean(double var1) {
      return bySun(this.mean, var1);
   }

   private static float bySun(float[] var0, double var1) {
      double var3 = (Math.max(-0.35, Math.min(1.0, var1)) - -0.35) / 1.35;
      double var5 = var3 * 255.0;
      int var7 = Math.min((int)var5, 254);
      return (float)(var0[var7] + (var0[var7 + 1] - var0[var7]) * (var5 - var7));
   }

   static void transmittanceRayOf(double var0, double var2, double[] var4) {
      double var5 = Math.sqrt(1282000.0);
      double var7 = var5 * var2;
      double var9 = Math.sqrt(var7 * var7 + 4.04496E7);
      double var11 = 6460.0 - var9;
      double var13 = var7 + var5;
      double var15 = var11 + var0 * (var13 - var11);
      double var17 = var15 == 0.0 ? 1.0 : (var5 * var5 - var7 * var7 - var15 * var15) / (2.0 * var9 * var15);
      var4[0] = var9;
      var4[1] = Math.max(-1.0, Math.min(1.0, var17));
   }

   static void transmittancePlaceOf(double var0, double var2, double[] var4) {
      double var5 = Math.sqrt(1282000.0);
      double var7 = Math.sqrt(Math.max(var0 * var0 - 4.04496E7, 0.0));
      double var9 = EarthAir.toTop(var0, var2);
      double var11 = 6460.0 - var0;
      double var13 = var7 + var5;
      var4[0] = (var9 - var11) / (var13 - var11);
      var4[1] = var7 / var5;
   }

   static void transmittanceAt(float[] var0, double var1, double var3, double[] var5) {
      if (EarthAir.meetsGround(var1, var3)) {
         var5[0] = var5[1] = var5[2] = 0.0;
      } else {
         double[] var6 = new double[2];
         transmittancePlaceOf(Math.max(6360.0, Math.min(6460.0, var1)), var3, var6);
         bilinear(var0, 256, 64, var6[0], var6[1], var5);
      }
   }

   static void scatteringAt(float[] var0, double var1, double var3, double[] var5) {
      double var6 = (var1 - 6360.0) / 100.0;
      bilinear(var0, 32, 32, 0.5 + 0.5 * var3, var6, var5);
   }

   private static void bilinear(float[] var0, int var1, int var2, double var3, double var5, double[] var7) {
      double var8 = Math.max(0.0, Math.min(1.0, var3)) * (var1 - 1);
      double var10 = Math.max(0.0, Math.min(1.0, var5)) * (var2 - 1);
      int var12 = Math.min((int)var8, var1 - 2);
      int var13 = Math.min((int)var10, var2 - 2);
      double var14 = var8 - var12;
      double var16 = var10 - var13;

      for (int var18 = 0; var18 < 3; var18++) {
         double var19 = var0[(var13 * var1 + var12) * 4 + var18];
         double var21 = var0[(var13 * var1 + var12 + 1) * 4 + var18];
         double var23 = var0[((var13 + 1) * var1 + var12) * 4 + var18];
         double var25 = var0[((var13 + 1) * var1 + var12 + 1) * 4 + var18];
         var7[var18] = (var19 + (var21 - var19) * var14) * (1.0 - var16) + (var23 + (var25 - var23) * var14) * var16;
      }
   }

   private static float[] transmittanceTable() {
      float[] var0 = new float[65536];
      double[] var1 = new double[2];
      double[] var2 = new double[3];

      for (int var3 = 0; var3 < 64; var3++) {
         for (int var4 = 0; var4 < 256; var4++) {
            transmittanceRayOf(var4 / 255.0, var3 / 63.0, var1);
            double var5 = var1[0];
            double var7 = -Math.sqrt(Math.max(1.0 - 4.04496E7 / (var5 * var5), 0.0));
            EarthAir.transmittance(var5, Math.max(var1[1], var7 + 1.0E-7), var2);
            put(var0, (var3 * 256 + var4) * 4, var2);
         }
      }

      return var0;
   }

   private static float[] scatteringTable(float[] var0) {
      float[] var1 = new float[4096];
      double[] var2 = new double[3];

      for (int var3 = 0; var3 < 32; var3++) {
         double var4 = 6360.0 + 100.0 * var3 / 31.0;
         var4 = Math.max(6360.01, Math.min(6459.99, var4));

         for (int var6 = 0; var6 < 32; var6++) {
            multipleScattering(var0, var4, 2.0 * var6 / 31.0 - 1.0, var2);
            put(var1, (var3 * 32 + var6) * 4, var2);
         }
      }

      return var1;
   }

   private static void multipleScattering(float[] var0, double var1, double var3, double[] var5) {
      double[] var6 = new double[]{Math.sqrt(Math.max(0.0, 1.0 - var3 * var3)), var3, 0.0};
      double[] var7 = new double[3];
      double[] var8 = new double[3];
      double[] var9 = new double[3];
      byte var10 = 64;

      for (int var11 = 0; var11 < 8; var11++) {
         for (int var12 = 0; var12 < 8; var12++) {
            double var13 = (Math.PI * 2) * (var11 + 0.5) / 8.0;
            double var15 = 1.0 - 2.0 * (var12 + 0.5) / 8.0;
            double var17 = Math.sqrt(Math.max(0.0, 1.0 - var15 * var15));
            double[] var19 = new double[]{var17 * Math.cos(var13), var15, var17 * Math.sin(var13)};
            secondOrder(var0, var1, var19, var6, var9, var8);

            for (int var20 = 0; var20 < 3; var20++) {
               var7[var20] += var9[var20] / var10;
            }
         }
      }

      for (int var21 = 0; var21 < 3; var21++) {
         var5[var21] = var7[var21] / (1.0 - var8[var21] / var10);
      }
   }

   private static void secondOrder(float[] var0, double var1, double[] var3, double[] var4, double[] var5, double[] var6) {
      double var7 = var3[1];
      boolean var9 = EarthAir.meetsGround(var1, var7);
      double var10 = var9 ? -var1 * var7 - Math.sqrt(var1 * var1 * (var7 * var7 - 1.0) + 4.04496E7) : EarthAir.toTop(var1, var7);
      double var12 = var10 / 64.0;
      double[] var14 = new double[]{1.0, 1.0, 1.0};
      double[] var15 = new double[3];
      double[] var16 = new double[3];
      var5[0] = var5[1] = var5[2] = 0.0;

      for (int var17 = 0; var17 < 64; var17++) {
         double var18 = (var17 + 0.5) * var12;
         double var20 = var3[0] * var18;
         double var22 = var1 + var3[1] * var18;
         double var24 = var3[2] * var18;
         double var26 = Math.sqrt(var20 * var20 + var22 * var22 + var24 * var24);
         double var28 = var26 - 6360.0;
         transmittanceAt(var0, var26, (var20 * var4[0] + var22 * var4[1] + var24 * var4[2]) / var26, var16);
         EarthAir.extinction(var28, var15);

         for (int var30 = 0; var30 < 3; var30++) {
            double var31 = EarthAir.rayleighScattering(var28, var30) + EarthAir.mieScattering(var28);
            double var33 = Math.exp(-var15[var30] * var12);
            double var35 = (1.0 - var33) / var15[var30];
            var5[var30] += var14[var30] * var31 * var16[var30] * 0.07957747154594767 * var35;
            var6[var30] += var14[var30] * var31 * var35;
            var14[var30] *= var33;
         }
      }

      if (var9) {
         double var37 = var3[0] * var10;
         double var19 = var1 + var3[1] * var10;
         double var21 = var3[2] * var10;
         double var23 = Math.sqrt(var37 * var37 + var19 * var19 + var21 * var21);
         double var25 = (var37 * var4[0] + var19 * var4[1] + var21 * var4[2]) / var23;
         transmittanceAt(var0, 6360.0, var25, var16);

         for (int var27 = 0; var27 < 3; var27++) {
            var5[var27] += var14[var27] * var16[var27] * Math.max(var25, 0.0) * 0.3 / Math.PI;
         }
      }
   }

   private void brightness() {
      for (int var1 = 0; var1 < 256; var1++) {
         double var2 = -0.35 + 1.35 * var1 / 255.0;
         double[] var4 = new double[]{Math.sqrt(1.0 - var2 * var2), var2, 0.0};
         double var5 = 0.0;

         for (int var7 = 0; var7 < HEIGHTS.length; var7++) {
            double var8 = Math.toRadians(HEIGHTS[var7]);
            double var10 = sine(BAND_EDGES[var7 + 1]) - sine(BAND_EDGES[var7]);

            for (int var12 = 0; var12 < 5; var12++) {
               double var13 = Math.PI * var12 / 4.0;
               double[] var15 = new double[]{Math.cos(var8) * Math.cos(var13), Math.sin(var8), Math.cos(var8) * Math.sin(var13)};
               double var16 = var10 * (var12 != 0 && var12 != 4 ? 1.0 : 0.5) / 4.0;
               var5 += var16 * this.luminance(var15, var4, patchMie(var7, var12, var4));
            }
         }

         this.zenith[var1] = (float)this.luminance(new double[]{0.0, 1.0, 0.0}, var4, EarthAir.miePhase(var2));
         this.mean[var1] = (float)var5;
      }
   }

   private static double patchMie(int var0, int var1, double[] var2) {
      double var3 = sine(BAND_EDGES[var0]);
      double var5 = sine(BAND_EDGES[var0 + 1]);
      double var7 = Math.PI / 8;
      double var9 = Math.PI * var1 / 4.0;
      double var11 = Math.max(0.0, var9 - var7);
      double var13 = Math.min(Math.PI, var9 + var7);
      double var15 = 0.0;

      for (int var17 = 0; var17 < 16; var17++) {
         double var18 = var3 + (var5 - var3) * (var17 + 0.5) / 16.0;
         double var20 = Math.sqrt(1.0 - var18 * var18);

         for (int var22 = 0; var22 < 16; var22++) {
            double var23 = var11 + (var13 - var11) * (var22 + 0.5) / 16.0;
            double var25 = var20 * Math.cos(var23) * var2[0] + var18 * var2[1] + var20 * Math.sin(var23) * var2[2];
            var15 += EarthAir.miePhase(var25);
         }
      }

      return var15 / 256.0;
   }

   private static double sine(double var0) {
      return Math.sin(Math.toRadians(var0));
   }

   private double luminance(double[] var1, double[] var2, double var3) {
      double var5 = 6360.2;
      double var7 = EarthAir.toTop(var5, var1[1]);
      double var9 = 0.0;
      double var11 = var1[0] * var2[0] + var1[1] * var2[1] + var1[2] * var2[2];
      double var13 = EarthAir.rayleighPhase(var11);
      double[] var15 = new double[]{1.0, 1.0, 1.0};
      double[] var16 = new double[3];
      double[] var17 = new double[3];
      double[] var18 = new double[3];
      double[] var19 = new double[3];

      for (int var20 = 0; var20 < 48; var20++) {
         double var21 = (var20 + 1.0) / 48.0;
         double var23 = var7 * var21 * var21;
         double var25 = var23 - var9;
         double var27 = 0.5 * (var23 + var9);
         var9 = var23;
         double var29 = var1[0] * var27;
         double var31 = var5 + var1[1] * var27;
         double var33 = var1[2] * var27;
         double var35 = Math.sqrt(var29 * var29 + var31 * var31 + var33 * var33);
         double var37 = var35 - 6360.0;
         double var39 = (var29 * var2[0] + var31 * var2[1] + var33 * var2[2]) / var35;
         transmittanceAt(this.transmittance, var35, var39, var18);
         scatteringAt(this.scattering, var35, var39, var19);
         EarthAir.extinction(var37, var17);
         double var41 = EarthAir.mieScattering(var37);

         for (int var43 = 0; var43 < 3; var43++) {
            double var44 = EarthAir.rayleighScattering(var37, var43);
            double var46 = (var44 * var13 + var41 * var3) * var18[var43] + (var44 + var41) * var19[var43];
            double var48 = Math.exp(-var17[var43] * var25);
            var16[var43] += var15[var43] * var46 * (1.0 - var48) / var17[var43];
            var15[var43] *= var48;
         }
      }

      return 0.2126 * var16[0] * EarthAir.sun(0) + 0.7152 * var16[1] * EarthAir.sun(1) + 0.0722 * var16[2] * EarthAir.sun(2);
   }

   private static void put(float[] var0, int var1, double[] var2) {
      var0[var1] = (float)var2[0];
      var0[var1 + 1] = (float)var2[1];
      var0[var1 + 2] = (float)var2[2];
      var0[var1 + 3] = 1.0F;
   }

   private static final class Holder {
      static final AirTables TABLES = new AirTables();
   }
}
