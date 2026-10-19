package viewpoint.platform;

import java.util.Locale;

public final class EarthAir {
   public static final double BOTTOM = 6360.0;
   public static final double TOP = 6460.0;
   public static final double GROUND = 0.2;
   public static final double GROUND_ALBEDO = 0.3;
   private static final double[] RAYLEIGH = new double[]{0.007189, 0.013763, 0.03252};
   private static final double RAYLEIGH_HEIGHT = 8.0;
   private static final double MIE_SCATTERING = 0.15;
   private static final double MIE_EXTINCTION = 0.1667;
   private static final double MIE_HEIGHT = 1.2;
   private static final double MIE_PEAK = 0.15;
   private static final double MIE_PEAK_G = 0.97;
   private static final double MIE_BROAD_G = 0.5;
   private static final double[] OZONE = new double[]{0.002605, 0.001662, 0.0};
   private static final double OZONE_CENTRE = 25.0;
   private static final double OZONE_HALF = 15.0;
   public static final double NOON_SINE = Math.sin(Math.toRadians(60.0));
   private static final double[] NOON = new double[]{1.0, 0.96, 0.88};
   public static final float NOON_ZENITH = 0.289F;
   private static final int DEPTH_STEPS = 256;
   private static final double[] SUN = sunAtTop();

   public static double sun(int var0) {
      return SUN[var0];
   }

   public static double rayleighScattering(double var0, int var2) {
      return RAYLEIGH[var2] * Math.exp(-var0 / 8.0);
   }

   public static double mieScattering(double var0) {
      return 0.15 * Math.exp(-var0 / 1.2);
   }

   public static void extinction(double var0, double[] var2) {
      double var3 = 0.1667 * Math.exp(-var0 / 1.2);
      double var5 = Math.max(0.0, 1.0 - Math.abs(var0 - 25.0) / 15.0);

      for (int var7 = 0; var7 < 3; var7++) {
         var2[var7] = rayleighScattering(var0, var7) + var3 + OZONE[var7] * var5;
      }
   }

   public static double rayleighPhase(double var0) {
      return 0.05968310365946075 * (1.0 + var0 * var0);
   }

   public static double miePhase(double var0) {
      return 0.15 * henyeyGreenstein(var0, 0.97) + 0.85 * henyeyGreenstein(var0, 0.5);
   }

   private static double henyeyGreenstein(double var0, double var2) {
      return (1.0 - var2 * var2) / ((Math.PI * 4) * Math.pow(1.0 + var2 * var2 - 2.0 * var2 * var0, 1.5));
   }

   public static double toTop(double var0, double var2) {
      double var4 = var0 * var0 * (var2 * var2 - 1.0) + 4.17316E7;
      return Math.max(0.0, -var0 * var2 + Math.sqrt(Math.max(var4, 0.0)));
   }

   public static boolean meetsGround(double var0, double var2) {
      return var2 < 0.0 && var0 * var0 * (var2 * var2 - 1.0) + 4.04496E7 >= 0.0;
   }

   public static void transmittance(double var0, double var2, double[] var4) {
      if (meetsGround(var0, var2)) {
         var4[0] = var4[1] = var4[2] = 0.0;
      } else {
         double var5 = toTop(var0, var2) / 256.0;
         double[] var7 = new double[3];
         double[] var8 = new double[3];

         for (int var9 = 0; var9 < 256; var9++) {
            double var10 = (var9 + 0.5) * var5;
            extinction(Math.sqrt(var0 * var0 + var10 * var10 + 2.0 * var0 * var2 * var10) - 6360.0, var7);

            for (int var12 = 0; var12 < 3; var12++) {
               var8[var12] += var7[var12] * var5;
            }
         }

         for (int var13 = 0; var13 < 3; var13++) {
            var4[var13] = Math.exp(-var8[var13]);
         }
      }
   }

   public static void sunColour(double var0, float[] var2) {
      double[] var3 = new double[3];
      transmittance(6360.2, Math.max(var0, 0.0), var3);
      double var4 = 0.0;

      for (int var6 = 0; var6 < 3; var6++) {
         var3[var6] *= SUN[var6];
         var4 = Math.max(var4, var3[var6]);
      }

      for (int var7 = 0; var7 < 3; var7++) {
         var2[var7] = (float)(var3[var7] / Math.max(var4, 1.0E-30));
      }
   }

   static String defines() {
      return "// The earth's air (EarthAir): km from the planet's centre, per km, the sun at the air's top.\n"
         + define("AIR_BOTTOM", 6360.0)
         + define("AIR_TOP", 6460.0)
         + define("AIR_GROUND", 0.2)
         + define("AIR_GROUND_ALBEDO", 0.3)
         + define("AIR_RAYLEIGH", RAYLEIGH)
         + define("AIR_RAYLEIGH_HEIGHT", 8.0)
         + define("AIR_MIE_SCATTERING", 0.15)
         + define("AIR_MIE_EXTINCTION", 0.1667)
         + define("AIR_MIE_HEIGHT", 1.2)
         + define("AIR_MIE_PEAK", 0.15)
         + define("AIR_MIE_PEAK_G", 0.97)
         + define("AIR_MIE_BROAD_G", 0.5)
         + define("AIR_OZONE", OZONE)
         + define("AIR_OZONE_CENTRE", 25.0)
         + define("AIR_OZONE_HALF", 15.0)
         + define("AIR_SUN", SUN);
   }

   private static String define(String var0, double var1) {
      return "#define " + var0 + " " + String.format(Locale.ROOT, "%.9g", var1) + "\n";
   }

   private static String define(String var0, double[] var1) {
      return "#define " + var0 + " " + String.format(Locale.ROOT, "vec3(%.9g, %.9g, %.9g)", var1[0], var1[1], var1[2]) + "\n";
   }

   private static double[] sunAtTop() {
      double[] var0 = new double[3];
      double[] var1 = new double[3];
      transmittance(6360.2, NOON_SINE, var0);

      for (int var2 = 0; var2 < 3; var2++) {
         var1[var2] = NOON[var2] / var0[var2];
      }

      return var1;
   }

   private EarthAir() {
   }
}
