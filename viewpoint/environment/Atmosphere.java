package viewpoint.environment;

import viewpoint.core.View;
import viewpoint.far.FarWorld;
import viewpoint.platform.EarthAir;
import viewpoint.platform.Profile;
import viewpoint.platform.Tuning;
import viewpoint.render.AirTables;
import viewpoint.render.SceneData;
import zombie.GameTime;
import zombie.SandboxOptions;
import zombie.characters.IsoPlayer;
import zombie.core.opengl.RenderSettings;
import zombie.core.opengl.RenderSettings.PlayerRenderSettings;
import zombie.erosion.season.ErosionSeason;
import zombie.iso.weather.ClimateManager;
import zombie.iso.weather.ClimateMoon;
import zombie.iso.weather.WeatherPeriod;
import zombie.scripting.objects.CharacterTrait;

public final class Atmosphere {
   private static final float FOGGY_END = 40.0F;
   private static final float CLEAR_UP_TO = 0.22F;
   private static final float MOONLESS = 0.15F;
   static final float[] NIGHT = new float[]{0.004F, 0.007F, 0.022F, 0.018F, 0.028F, 0.055F};
   static final float[] DAY = new float[]{0.1F, 0.3F, 0.74F, 0.6F, 0.76F, 0.95F};
   static final float[] DECK = new float[]{0.85F, 0.88F, 0.95F, 1.0F, 1.02F, 1.06F};
   private static final float[] TWILIGHT_HORIZON = new float[]{0.92F, 0.55F, 0.34F};
   private static final float ADAPTATION = 0.6F;
   private static final float DARKEST = 1.0E-5F;
   private static final float[] NIGHT_FLOORS = new float[]{0.0F, 0.07F, 0.15F, 0.25F};
   private static final float NORMAL_FLOOR = 0.15F;
   private static final float NIGHT_VISION_FLOOR = 0.2F;
   private static final float GOGGLES_FLOOR = 0.8F;
   private static final float MOON_FLOOR = 0.075F;
   private static final float EYE_DARKEST = -8.0F;
   private static final double NOON_LUMINANCE = 4096.0;
   private static final double FULL_MOON_SUN = 2.5E-6;
   private static final double STARS_MOON = 0.0033333333333333335;
   private static final float OVERCAST_DIMS = 0.9F;
   private static final float DECK_NOON = 0.6F;
   private static final float DECK_NIGHT = 0.03F;
   private static final int[] DAYS_BEFORE = new int[]{0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
   private static final long START_NANOS = System.nanoTime();
   private static final SkyClock clock = new SkyClock();
   private static final SkyWeather weather = new SkyWeather();
   private static final SkyWeather.Climate now = new SkyWeather.Climate();
   private static final SkyProfile profile = new SkyProfile();
   private static final GameForecast forecast = new GameForecast();
   private static final SheetFrame midSheet = new SheetFrame();
   private static final SheetFrame highSheet = new SheetFrame();
   private static long lastNanos = START_NANOS;
   private static double carriedX;
   private static double carriedZ;

   public static void gameLoaded() {
      weather.restart();
   }

   public static void apply(SceneData var0) {
      ClimateManager var1 = ClimateManager.getInstance();
      float var2 = clamp01(var1.getFogIntensity());
      float var3 = Math.max(var0.rain, var0.snow);
      float var4 = Math.max(var2, Math.max(0.5F * var0.rain, 0.35F * var0.snow));
      sky(var0, var1, var3);
      float var5 = overcast(var0.cloudCover, var2, var3);
      var0.overcast = var5;
      ErosionSeason var6 = var1.getSeason();
      var0.nightLight = nightLight(nightFloor(), ClimateMoon.getInstance().getMoonFloat());
      SkyBodies.place(var0, var6, var5);
      float var7 = SkyBodies.sunDegrees;
      float var8 = Math.max(clamp01(var1.getDayLightStrength()), 0.6F * smooth(-5.0F, 14.0F, var7));
      skyColours(var0, var8, clamp01(1.0F - Math.abs(var7 - 2.0F) / 12.0F), var5);
      airLight(var0);
      adapted(var0, var5);
      landLight(var0);
      float var9 = var6 != null ? var6.getDawn() : 6.0F;
      float var10 = var6 != null ? var6.getDusk() : 20.0F;
      float var11 = GameTime.getInstance().getTimeOfDay();
      float var12 = var11 - var9;
      float var13 = var10 - var11;
      float var14 = smooth(-0.75F, 0.25F, var12) * smooth(3.0F, 1.0F, var12);
      float var15 = smooth(1.5F, 0.0F, var13) * smooth(-2.0F, -0.5F, var13);
      float var16 = 1.0F - 0.6F * clamp01(var1.getWindIntensity());
      float var17 = var0.rain > 0.0F ? 0.1F + 0.55F * var0.rain : 0.0F;
      var0.mist = clamp01(Math.max(Math.max(var2, var17), Math.max(0.4F * var0.snow, (0.4F * var14 + 0.15F * var15) * var16)));
      var0.night = 1.0F - var8;
      tint(var0, var1);
      fogReach(var0, var4);
   }

   private static void skyColours(SceneData var0, float var1, float var2, float var3) {
      float var4 = nightSky(var0);
      float var5 = deck(var0, var1, var4);
      float[] var6 = new float[6];

      for (int var7 = 0; var7 < 6; var7++) {
         float var8 = lerp(NIGHT[var7] * var4, DAY[var7], var1);
         if (var7 >= 3) {
            var8 = lerp(var8, TWILIGHT_HORIZON[var7 - 3], var2 * 0.8F);
         }

         var6[var7] = lerp(var8, var5 * DECK[var7], var3);
      }

      skyBase(var0, var3);
      var0.zenithR = var6[0];
      var0.zenithG = var6[1];
      var0.zenithB = var6[2];
      var0.fogR = var6[3];
      var0.fogG = var6[4];
      var0.fogB = var6[5];
   }

   public static void skyBase(SceneData var0, float var1) {
      float var2 = nightSky(var0);
      float var3 = lerp(0.03F * var2, 0.6F, litShare(var0.sunTrueY)) * weathered(var0);

      for (int var4 = 0; var4 < 6; var4++) {
         (var4 < 3 ? var0.skyBaseZenith : var0.skyBaseHorizon)[var4 % 3] = lerp(NIGHT[var4] * var2, var3 * DECK[var4], var1);
      }
   }

   static float litShare(float var0) {
      AirTables var1 = AirTables.get();
      float var2 = var1.mean(EarthAir.NOON_SINE);
      return Math.min(1.0F, (float)Math.pow(Math.max(var1.mean(var0), 0.0F) / var2, 0.39999998F));
   }

   static float nightSky(SceneData var0) {
      return lerp(0.15F, 1.0F, SkyBodies.moonSky) * var0.nightLight;
   }

   static float nightLight(float var0, float var1) {
      return (var0 + 0.075F * var1) / (0.15F + 0.075F * var1);
   }

   private static float nightFloor() {
      float var0 = darknessFloor();
      IsoPlayer var1 = IsoPlayer.getInstance();
      if (var1 != null && var1.hasTrait(CharacterTrait.NIGHT_VISION)) {
         var0 = Math.max(var0, 0.2F);
      }

      if (var1 != null && var1.isWearingNightVisionGoggles()) {
         var0 = Math.max(var0, 0.8F);
      }

      return var0;
   }

   private static float darknessFloor() {
      int var0 = SandboxOptions.instance.nightDarkness.getValue();
      return var0 >= 1 && var0 <= NIGHT_FLOORS.length ? NIGHT_FLOORS[var0 - 1] : 0.15F;
   }

   private static float weathered(SceneData var0) {
      return 1.0F - 0.35F * var0.rain - 0.1F * var0.snow;
   }

   private static float deck(SceneData var0, float var1, float var2) {
      return lerp(0.03F * var2, 0.6F, var1) * weathered(var0);
   }

   public static void airLight(SceneData var0) {
      AirTables var1 = AirTables.get();
      float var2 = var1.mean(EarthAir.NOON_SINE);
      float var3 = Math.max(var1.mean(var0.sunTrueY), 1.0E-5F * var2);
      float var4 = (float)Math.pow(var2 / var3, 0.6F);
      var0.airLight[0] = var0.airLight[1] = var0.airLight[2] = var4;
   }

   static void adapted(SceneData var0, float var1) {
      AirTables var2 = AirTables.get();
      double var3 = var2.mean(var0.sunTrueY) / var2.mean(EarthAir.NOON_SINE);
      double var5 = 2.5E-6 * (SkyBodies.moonSky + 0.0033333333333333335) * var0.nightLight;
      var0.adapted = (float)(4096.0 * (var3 + var5) * (1.0F - 0.9F * var1));
   }

   public static void landLight(SceneData var0) {
      PlayerRenderSettings var1 = RenderSettings.getInstance().getPlayerSettings(0);
      IsoPlayer var2 = IsoPlayer.getInstance();
      float var3 = litShare(var0.sunTrueY);
      float var4;
      if (var2 != null && var2.isWearingNightVisionGoggles()) {
         var4 = var1.getAmbient();
      } else {
         if (var2 != null && var2.hasTrait(CharacterTrait.NIGHT_VISION)) {
            var3 = Math.max(var3, 0.2F);
         }

         var4 = landAmbient(var3, darknessFloor(), ClimateMoon.getInstance().getMoonFloat());
      }

      var0.airSkyLevel[0] = clamp01(2.0F * var1.getBmod() * var4);
      var0.airSkyLevel[1] = clamp01(2.0F * var1.getGmod() * var4);
      var0.airSkyLevel[2] = clamp01(2.0F * var1.getRmod() * var4);
      var0.eyeExpected = eyeExpected(var4);
      var0.eyeWindow = lerp(Tuning.eyeNightWindow, Tuning.eyeDayWindow, var3);
   }

   static float eyeExpected(float var0) {
      return Math.max(-8.0F, (float)(Math.log(Math.max(var0, 1.0E-9F)) / Math.log(2.0)));
   }

   static float landAmbient(float var0, float var1, float var2) {
      float var3 = var1 + 0.075F * var2 * (1.0F - var0);
      return var3 + (1.0F - var3) * var0;
   }

   public static float overcast(float var0, float var1, float var2) {
      float var3 = clamp01((var0 - 0.22F) / 0.78F);
      return Math.max(Math.max(var1, var2), var3 * 0.8F);
   }

   private static void sky(SceneData var0, ClimateManager var1, float var2) {
      long var3 = System.nanoTime();
      var0.time = (float)(var3 - START_NANOS) * 1.0E-9F;
      float var5 = skyStep(var3);
      var0.cloudTime = (float)clock.seconds();
      GameTime var6 = GameTime.getInstance();
      WeatherPeriod var7 = var1.getWeatherPeriod();
      now.cloud = clamp01(var1.getCloudIntensity());
      now.precipitation = var2;
      now.airMass = var1.getAirMass();
      now.periodRunning = var7 != null && var7.isRunning();
      now.period = now.periodRunning ? clamp01(var7.getCurrentStrength()) : 0.0F;
      now.storm = now.periodRunning && (var7.isThunderStorm() || var7.isTropicalStorm());
      now.hours = var6.getWorldAgeHours();
      now.yearStart = now.hours - (DAYS_BEFORE[var6.getMonth()] + var6.getDay()) * 24.0 - var6.getTimeOfDay();
      float var8 = 24.0F / (60.0F * var6.getMinutesPerDay());
      var0.cloudCover = weather.step(now, var5 * var8);
      driftClouds(var0, var5);
      forecast.read(var1, now.hours);
      forecast.check(now.hours, var2);
      profile.update(var0, var0.originX - var0.cloudCarriedX, var0.originZ - var0.cloudCarriedZ, weather, now, var8, forecast);
   }

   private static float skyStep(long var0) {
      float var2 = (float)(var0 - lastNanos) * 1.0E-9F;
      lastNanos = var0;
      GameTime var3 = GameTime.getInstance();
      return clock.advance(var2, var3.getTimeOfDay(), GameTime.isGamePaused(), var3.getMinutesPerDay());
   }

   private static void driftClouds(SceneData var0, float var1) {
      var0.cloudWindX = windX(0);
      var0.cloudWindZ = windZ(0);
      carriedX = carriedX + var1 * var0.cloudWindX;
      carriedZ = carriedZ + var1 * var0.cloudWindZ;
      var0.cloudCarriedX = carriedX;
      var0.cloudCarriedZ = carriedZ;
      midSheet.step(windX(1), windZ(1), var0.originX, var0.originZ, var1, var0.cloudSheetMid);
      highSheet.step(windX(2), windZ(2), var0.originX, var0.originZ, var1, var0.cloudSheetHigh);
   }

   private static float windX(int var0) {
      return (float)Math.sin(Math.toRadians(weather.aloftFrom(var0))) * weather.aloftSpeed(var0);
   }

   private static float windZ(int var0) {
      return (float)(-Math.cos(Math.toRadians(weather.aloftFrom(var0)))) * weather.aloftSpeed(var0);
   }

   private static void tint(SceneData var0, ClimateManager var1) {
      PlayerRenderSettings var2 = RenderSettings.getInstance().getPlayerSettings(0);
      float var3 = var2.getRmod();
      float var4 = var2.getGmod();
      float var5 = var2.getBmod();
      float var6 = Math.max(0.001F, Math.max(var3, Math.max(var4, var5)));
      float var7 = lerp(Math.min(1.5F, var6), 1.0F, var0.night);
      float var8 = var3 / var6 * var7;
      float var9 = var4 / var6 * var7;
      float var10 = var5 / var6 * var7;
      var0.zenithR *= var8;
      var0.zenithG *= var9;
      var0.zenithB *= var10;
      var0.fogR *= var8;
      var0.fogG *= var9;
      var0.fogB *= var10;

      for (float[] var14 : new float[][]{var0.airLight, var0.skyBaseZenith, var0.skyBaseHorizon}) {
         var14[0] *= var8;
         var14[1] *= var9;
         var14[2] *= var10;
      }

      var0.desaturation = clamp01(var2.getDesaturation());
      Profile.note = String.format(
         "day %.2f night %.2f night light %.2f moon %.2f sky %.3f air %.1f land %.3f mod %.2f/%.2f/%.2f desat %.2f sun %.2f fog %.0f-%.0f rain %.2f cover %.2f type %.2f front %.2f aloft %.0f/%.0f/%.0f at %.0f/%.0f/%.0f upwind %.2f downwind %.2f",
         clamp01(var1.getDayLightStrength()),
         var0.night,
         var0.nightLight,
         SkyBodies.moonSky,
         Math.max(var0.skyR, Math.max(var0.skyG, var0.skyB)),
         var0.airLight[1],
         var0.airSkyLevel[1],
         var3,
         var4,
         var5,
         var2.getDesaturation(),
         var0.sunStrength,
         var0.fogStart,
         var0.fogEnd,
         var0.rain,
         var0.cloudCover,
         weather.type(),
         weather.front(),
         weather.aloftFrom(0),
         weather.aloftFrom(1),
         weather.aloftFrom(2),
         weather.aloftSpeed(0),
         weather.aloftSpeed(1),
         weather.aloftSpeed(2),
         var0.coverAlong[0],
         var0.coverAlong[var0.coverAlong.length - 1]
      );
   }

   private static void fogReach(SceneData var0, float var1) {
      float var2 = View.radiusChunks() * 8;
      float var3 = Math.max(var2, FarWorld.reach());
      var0.nearReach = var2;
      var0.fogEnd = lerp(var3, 40.0F, var1);
      var0.fogStart = var0.fogEnd * lerp(0.88F, 0.3F, var1);
   }

   private static float smooth(float var0, float var1, float var2) {
      float var3 = clamp01((var2 - var0) / (var1 - var0));
      return var3 * var3 * (3.0F - 2.0F * var3);
   }

   private static float lerp(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   private static float clamp01(float var0) {
      return var0 < 0.0F ? 0.0F : Math.min(var0, 1.0F);
   }

   private Atmosphere() {
   }
}
