package viewpoint.gunplay;

final class Reference {
   static final int STANDING = 0;
   static final int LYING = 1;
   private static final int ELEVATIONS = 19;
   private static final double ELEVATION_STEP = Math.toRadians(5.0);
   private static final double[] DISTANCES = new double[]{1.0, 1.25, 1.5, 2.0, 3.0, 5.0, 10.0, 20.0, Double.POSITIVE_INFINITY};
   private static final int STEPS = 64;
   private static final double LEAST = 0.002;
   private static final double MOST = 8.0;
   static final float[] BIND = new float[]{
      0.0F,
      0.505F,
      0.011F,
      0.0F,
      0.663F,
      0.017F,
      0.0F,
      0.792F,
      0.038F,
      0.0F,
      0.825F,
      0.029F,
      0.103F,
      0.768F,
      0.038F,
      0.127F,
      0.633F,
      0.038F,
      0.151F,
      0.498F,
      0.038F,
      -0.103F,
      0.768F,
      0.038F,
      -0.127F,
      0.633F,
      0.038F,
      -0.151F,
      0.498F,
      0.038F,
      0.055F,
      0.505F,
      0.011F,
      0.083F,
      0.27F,
      0.023F,
      0.106F,
      0.066F,
      0.051F,
      -0.055F,
      0.505F,
      0.011F,
      -0.083F,
      0.27F,
      0.023F,
      -0.106F,
      0.066F,
      0.051F
   };
   private static final Outline[][][] outlines = new Outline[2][19][DISTANCES.length];
   private static final double[][][][] chances = new double[2][19][DISTANCES.length][];

   static double radius(int var0, double var1, double var3, double var5) {
      double var7 = Math.min(18.0, Math.abs(var1) / ELEVATION_STEP);
      int var9 = (int)var7;
      int var10 = Math.min(18, var9 + 1);
      double var11 = distanceStep(var3);
      int var13 = (int)var11;
      int var14 = Math.min(DISTANCES.length - 1, var13 + 1);
      double var15 = var7 - var9;
      double var17 = var11 - var13;
      double var19 = lerp(radiusAt(table(var0, var9, var13), var5), radiusAt(table(var0, var10, var13), var5), var15);
      double var21 = lerp(radiusAt(table(var0, var9, var14), var5), radiusAt(table(var0, var10, var14), var5), var15);
      return lerp(var19, var21, var17);
   }

   static double chance(int var0, double var1, double var3, double var5) {
      double var7 = Math.min(18.0, Math.abs(var1) / ELEVATION_STEP);
      int var9 = (int)var7;
      int var10 = Math.min(18, var9 + 1);
      double var11 = distanceStep(var3);
      int var13 = (int)var11;
      int var14 = Math.min(DISTANCES.length - 1, var13 + 1);
      double var15 = var7 - var9;
      double var17 = var11 - var13;
      double var19 = lerp(chanceAt(table(var0, var9, var13), var5), chanceAt(table(var0, var10, var13), var5), var15);
      double var21 = lerp(chanceAt(table(var0, var9, var14), var5), chanceAt(table(var0, var10, var14), var5), var15);
      return lerp(var19, var21, var17);
   }

   static double centre(int var0, double var1, double var3, double var5, double var7) {
      double var9 = radius(var0, var1, var3, var5 * chance(var0, var1, var3, var7));
      return Math.sqrt(Math.max(0.0, var9 * var9 - var7 * var7));
   }

   static Outline outline(int var0, double var1, double var3) {
      int var5 = (int)Math.round(Math.min(18.0, Math.abs(var1) / ELEVATION_STEP));
      int var6 = (int)Math.round(distanceStep(var3));
      table(var0, var5, var6);
      return outlines[var0][var5][var6];
   }

   static Outline exact(int var0, double var1, double var3) {
      return Outline.of(stadiums(var0, Math.min(Math.PI / 2, Math.abs(var1)), Math.max(1.0, var3)));
   }

   static double tabled(int var0) {
      return 0.002 * Math.pow(4000.0, var0 / 63.0);
   }

   static float[] bones(int var0) {
      float[] var1 = new float[BIND.length];
      byte var2 = 3;

      for (int var3 = 0; var3 < BIND.length; var3 += 3) {
         float var4 = (BIND[var3] - BIND[var2]) * 1.5F;
         float var5 = (BIND[var3 + 1] - BIND[var2 + 1]) * 1.5F;
         float var6 = (BIND[var3 + 2] - BIND[var2 + 2]) * 1.5F;
         var1[var3] = var0 == 0 ? var4 : var5;
         var1[var3 + 1] = var0 == 0 ? var6 : var4;
         var1[var3 + 2] = var0 == 0 ? var5 : var6;
      }

      return var1;
   }

   static float[] capsules(int var0) {
      float[] var1 = new float[104];
      Parts.capsules(bones(var0), var1);
      return var1;
   }

   private static double distanceStep(double var0) {
      double var2 = 1.0 / Math.max(DISTANCES[0], var0);

      for (int var4 = 1; var4 < DISTANCES.length; var4++) {
         double var5 = 1.0 / DISTANCES[var4];
         if (var2 >= var5) {
            double var7 = 1.0 / DISTANCES[var4 - 1];
            return var4 - 1 + (var7 - var2) / (var7 - var5);
         }
      }

      return DISTANCES.length - 1.0;
   }

   private static double[] table(int var0, int var1, int var2) {
      double[] var3 = chances[var0][var1][var2];
      if (var3 != null) {
         return var3;
      }

      Outline var4 = Outline.of(stadiums(var0, var1 * ELEVATION_STEP, DISTANCES[var2]));
      var3 = new double[64];

      for (int var5 = 0; var5 < 64; var5++) {
         var3[var5] = Cone.hit(var4, 0.0, 0.0, tabled(var5));
      }

      outlines[var0][var1][var2] = var4;
      chances[var0][var1][var2] = var3;
      return var3;
   }

   private static double radiusAt(double[] var0, double var1) {
      if (var1 >= 1.0) {
         return 0.0;
      }

      if (var1 >= var0[0]) {
         return var0[0] >= 1.0 ? 0.0 : 0.002 * (1.0 - var1) / (1.0 - var0[0]);
      }

      for (int var3 = 1; var3 < 64; var3++) {
         if (var1 >= var0[var3]) {
            double var4 = (var0[var3 - 1] - var1) / (var0[var3 - 1] - var0[var3]);
            return tabled(var3 - 1) * Math.pow(tabled(var3) / tabled(var3 - 1), var4);
         }
      }

      return 8.0;
   }

   private static double chanceAt(double[] var0, double var1) {
      if (var1 <= 0.002) {
         return var1 <= 0.0 ? (var0[0] > 0.0 ? 1.0 : 0.0) : 1.0 - (1.0 - var0[0]) * var1 / 0.002;
      }

      if (var1 >= 8.0) {
         return var0[63];
      }

      double var3 = Math.log(var1 / 0.002) / Math.log(4000.0) * 63.0;
      int var5 = Math.min(62, (int)var3);
      return lerp(var0[var5], var0[var5 + 1], var3 - var5);
   }

   private static float[] stadiums(int var0, double var1, double var3) {
      float[] var5 = capsules(var0);
      double var6 = Math.cos(var1);
      double var8 = Math.sin(var1);
      float[] var10 = new float[65];

      for (int var11 = 0; var11 < 13; var11++) {
         int var12 = var11 * 8;
         int var13 = var11 * 5;
         double var14 = project(var5, var12 + 1, var6, var8, var3, var10, var13);
         double var16 = project(var5, var12 + 4, var6, var8, var3, var10, var13 + 2);
         var10[var13 + 4] = (float)(var5[var12 + 7] * (var14 + var16) / 2.0);
      }

      return var10;
   }

   private static double project(float[] var0, int var1, double var2, double var4, double var6, float[] var8, int var9) {
      double var10 = var0[var1];
      double var12 = var0[var1 + 1];
      double var14 = var0[var1 + 2];
      double var16 = var14 * var2 - var12 * var4;
      double var18 = -var12 * var2 - var14 * var4;
      double var20 = Double.isInfinite(var6) ? 1.0 : var6 / (var6 + var18);
      var8[var9] = (float)(var10 * var20);
      var8[var9 + 1] = (float)(var16 * var20);
      return var20;
   }

   private static double lerp(double var0, double var2, double var4) {
      return var0 + (var2 - var0) * var4;
   }

   private Reference() {
   }
}
