package viewpoint.environment;

import viewpoint.platform.EarthAir;
import viewpoint.render.SceneData;
import zombie.GameTime;
import zombie.erosion.season.ErosionSeason;

final class SkyBodies {
   private static final float MIN_SHADOW_ELEVATION = (float)Math.toRadians(14.0);
   private static final double STEP = Math.toRadians(0.25);
   private static final float MOON_LIGHT = 0.14F;
   private static final float FULL_MOON_SHARE = (0.2126F * Atmosphere.NIGHT[0] + 0.7152F * Atmosphere.NIGHT[1] + 0.0722F * Atmosphere.NIGHT[2]) / 0.289F;
   private static final int MOON_PHASES = 8;
   private static final float[] sun = new float[3];
   private static final float[] moon = new float[3];
   private static final float[] light = new float[3];
   private static final float[] colour = new float[3];
   static float sunDegrees;
   static float moonSky;

   static void place(SceneData var0, ErosionSeason var1, float var2) {
      GameTime var3 = GameTime.getInstance();
      double var4 = var3.getTimeOfDay();
      double var6 = Ephemeris.days(var3.getYear(), var3.getMonth(), var3.getDayPlusOne(), var4);
      double var8 = Math.toRadians(var1 != null ? var1.getLat() : 38.0);
      place(var0, var8, var6, hourAngle(var1, var4), var2);
   }

   static void place(SceneData var0, double var1, double var3, double var5, float var7) {
      double var8 = Ephemeris.obliquity(var3);
      double var10 = Ephemeris.sunLongitude(var3);
      double var12 = Ephemeris.declination(var10, var8);
      double var14 = Ephemeris.elongation(Ephemeris.moonAge(var3));
      double var16 = var10 + var14;
      double var18 = Ephemeris.declination(var16, var8);
      double var20 = var5 - (Ephemeris.rightAscension(var16, var8) - Ephemeris.rightAscension(var10, var8));
      Ephemeris.direction(var1, var12, var5, sun);
      Ephemeris.direction(var1, var18, var20, moon);
      sunDegrees = degrees(sun[1]);
      float var22 = degrees(moon[1]);
      progress(var0, Ephemeris.halfDay(var1, var12), Ephemeris.wrap(var5));
      float var23 = smooth(-1.0F, 6.0F, var22);
      moonSky = (float)Ephemeris.moonLight(var14) * var23;
      float var24 = 0.14F * moonSky * (1.0F - 0.9F * var7);
      float var25 = var24 * var0.nightLight;
      var0.moonFace = 0.14F * var23 * (1.0F - 0.9F * var7);
      Ephemeris.starFrame(var1, var5 + Ephemeris.rightAscension(var10, var8), var0.starFrame);
      if (sunDegrees > -6.0F) {
         sunLight(var0, var7);
         lightFrom(var1, var12, var5);
         set(var0, sun, var24);
         var0.moonLight = 0.0F;
      } else {
         float var26 = smooth(-6.0F, -12.0F, sunDegrees);
         var0.sunStrength = var0.skyGlow = var25 * var26;
         var0.sunR = 0.62F;
         var0.sunG = 0.74F;
         var0.sunB = 1.0F;
         lightFrom(var1, var18, var20);
         set(var0, moon, var24 * (1.0F - var26));
         var0.moonLight = FULL_MOON_SHARE * moonSky * var0.nightLight * (1.0F - 0.9F * var7) * var26;
      }

      var0.sunTrueX = sun[0];
      var0.sunTrueY = sun[1];
      var0.sunTrueZ = sun[2];
      var0.sunSine = sun[1];
      var0.skyMoonX = moon[0];
      var0.skyMoonY = moon[1];
      var0.skyMoonZ = moon[2];
      long var28 = Math.round(var14 / (Math.PI * 2) * 8.0) % 8L;
      var0.moonPhase = (int)(var28 + 4L) % 8;
   }

   private static double hourAngle(ErosionSeason var0, double var1) {
      if (var0 == null) {
         return Math.toRadians(15.0 * (var1 - 12.0));
      } else if (var0.getDaylight() >= 24.0F) {
         return 0.0;
      } else {
         return var0.getDaylight() <= 0.0F ? Math.PI : Math.toRadians(15.0 * (var1 - var0.getDayHighNoon()));
      }
   }

   private static void progress(SceneData var0, double var1, double var3) {
      double var5 = Math.max(0.001, 2.0 * var1);
      double var7 = Math.max(0.001, (Math.PI * 2) - 2.0 * var1);
      var0.dayFraction = (float)((var3 + var1) / var5);
      boolean var9 = Math.abs(var3) <= var1;
      double var10 = var3 > var1 ? var3 - var1 : var3 + (Math.PI * 2) - var1;
      var0.nightFraction = var9 ? -1.0F : (float)(var10 / var7);
   }

   private static void sunLight(SceneData var0, float var1) {
      var0.sunStrength = smooth(0.0F, 12.0F, sunDegrees) * (1.0F - 0.85F * var1);
      EarthAir.sunColour(sun[1], colour);
      var0.sunR = colour[0];
      var0.sunG = colour[1];
      var0.sunB = colour[2];
      var0.skyGlow = smooth(-6.0F, 4.0F, sunDegrees) * (1.0F - 0.85F * var1);
   }

   private static void lightFrom(double var0, double var2, double var4) {
      Ephemeris.direction(var0, Math.round(var2 / STEP) * STEP, Math.round(var4 / STEP) * STEP, light);
      float var6 = (float)Math.sin(MIN_SHADOW_ELEVATION);
      if (light[1] < var6) {
         float var7 = (float)Math.sqrt(light[0] * light[0] + light[2] * light[2]);
         float var8 = var7 > 0.0F ? (float)Math.cos(MIN_SHADOW_ELEVATION) / var7 : 0.0F;
         light[0] = light[0] * var8;
         light[1] = var6;
         light[2] = light[2] * var8;
      }
   }

   private static void set(SceneData var0, float[] var1, float var2) {
      var0.sunX = light[0];
      var0.sunY = light[1];
      var0.sunZ = light[2];
      var0.skySunX = var1[0];
      var0.skySunY = var1[1];
      var0.skySunZ = var1[2];
      var0.moonDisc = var2;
   }

   private static float degrees(float var0) {
      return (float)Math.toDegrees(Math.asin(Math.max(-1.0F, Math.min(1.0F, var0))));
   }

   private static float smooth(float var0, float var1, float var2) {
      float var3 = Math.max(0.0F, Math.min(1.0F, (var2 - var0) / (var1 - var0)));
      return var3 * var3 * (3.0F - 2.0F * var3);
   }

   private static float lerp(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   private SkyBodies() {
   }
}
