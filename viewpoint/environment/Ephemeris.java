package viewpoint.environment;

import java.time.LocalDate;

final class Ephemeris {
   static final double LUNATION = 29.530588853;
   static final double NEW_MOON = 5.2597;
   static final double UT_BEHIND = 5.0;
   private static final long J2000_DAY = LocalDate.of(2000, 1, 1).toEpochDay();

   static double days(int var0, int var1, int var2, double var3) {
      long var5 = LocalDate.of(var0, var1 + 1, var2).toEpochDay();
      return var5 - J2000_DAY + (var3 + 5.0 - 12.0) / 24.0;
   }

   static double sunLongitude(double var0) {
      double var2 = Math.toRadians(357.529 + 0.98560028 * var0);
      return Math.toRadians(280.459 + 0.98564736 * var0 + 1.915 * Math.sin(var2) + 0.02 * Math.sin(2.0 * var2));
   }

   static double obliquity(double var0) {
      return Math.toRadians(23.439 - 3.6E-7 * var0);
   }

   static double declination(double var0, double var2) {
      return Math.asin(Math.sin(var2) * Math.sin(var0));
   }

   static double rightAscension(double var0, double var2) {
      return Math.atan2(Math.cos(var2) * Math.sin(var0), Math.cos(var0));
   }

   static double moonAge(double var0) {
      double var2 = (var0 - 5.2597) % 29.530588853;
      return var2 < 0.0 ? var2 + 29.530588853 : var2;
   }

   static double elongation(double var0) {
      return (Math.PI * 2) * var0 / 29.530588853;
   }

   static double lit(double var0) {
      return 0.5 * (1.0 - Math.cos(var0));
   }

   static double moonLight(double var0) {
      double var2 = Math.abs(180.0 - Math.toDegrees(var0));
      return Math.pow(10.0, -0.4 * (0.026 * var2 + 4.0E-9 * Math.pow(var2, 4.0)));
   }

   static void starFrame(double var0, double var2, float[] var4) {
      float[] var5 = new float[3];

      for (int var6 = 0; var6 < 3; var6++) {
         double var7 = var6 == 2 ? Math.PI / 2 : 0.0;
         double var9 = var2 - (var6 == 1 ? Math.PI / 2 : 0.0);
         direction(var0, var7, var9, var5);
         System.arraycopy(var5, 0, var4, 3 * var6, 3);
      }
   }

   static void direction(double var0, double var2, double var4, float[] var6) {
      double var7 = Math.sin(var0);
      double var9 = Math.cos(var0);
      double var11 = Math.sin(var2);
      double var13 = Math.cos(var2);
      double var15 = Math.cos(var4);
      double var17 = var7 * var11 + var9 * var13 * var15;
      double var19 = -var13 * Math.sin(var4);
      double var21 = var9 * var11 - var7 * var13 * var15;
      var6[0] = (float)(-var19);
      var6[1] = (float)var17;
      var6[2] = (float)var21;
   }

   static double halfDay(double var0, double var2) {
      double var4 = -Math.tan(var0) * Math.tan(var2);
      return var4 >= 1.0 ? 0.0 : (var4 <= -1.0 ? Math.PI : Math.acos(var4));
   }

   static double wrap(double var0) {
      double var2 = var0 % (Math.PI * 2);
      return var2 > Math.PI ? var2 - (Math.PI * 2) : (var2 < -Math.PI ? var2 + (Math.PI * 2) : var2);
   }

   private Ephemeris() {
   }
}
