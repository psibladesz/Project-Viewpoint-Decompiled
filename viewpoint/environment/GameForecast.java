package viewpoint.environment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import zombie.iso.weather.ClimateForecaster;
import zombie.iso.weather.ClimateManager;
import zombie.iso.weather.SimplexNoise;
import zombie.iso.weather.WeatherPeriod;
import zombie.iso.weather.ClimateForecaster.DayForecast;
import zombie.iso.weather.WeatherPeriod.StrLerpVal;
import zombie.iso.weather.WeatherPeriod.WeatherStage;
import zombie.network.GameClient;

final class GameForecast {
   static final double HORIZON_HOURS = 72.0;
   private static final int STORM = 3;
   private static final int TROPICAL = 8;
   private static final int SCRIPTED = 11;
   private static final int NONE = 0;
   private static final int ENTRY = 1;
   private static final int TARGET = 2;
   private static final int NEXT_TARGET = 3;
   private static final int MAX_STAGES = 96;
   private static final int MAX_PERIODS = 8;
   private static final float PERIOD_CLOUD = 0.75F;
   private static final int CHECK_AHEAD = 3;
   private static final float CHECK_LEAST = 0.01F;
   private static final Field ENTRY_STRENGTH = field("entryStrength");
   private static final Field TARGET_STRENGTH = field("targetStrength");
   private static final Field LERP_MID = field("lerpMidVal");
   private static final Field LERP_END = field("lerpEndVal");
   private final double[] start = new double[96];
   private final double[] end = new double[96];
   private final int[] kind = new int[96];
   private final int[] mid = new int[96];
   private final int[] last = new int[96];
   private final int[] periodOf = new int[96];
   private final float[] entry = new float[96];
   private final float[] target = new float[96];
   private final float[] front = new float[8];
   private int stages;
   private int periods;
   private boolean known;
   private boolean clientRunning;
   private double clientEnd;
   private float baseCloud;
   private long lastMinute = Long.MIN_VALUE;
   private final float[] foretold = new float[24];
   private final long[] foretoldFor = new long[24];
   private long lastHour = Long.MIN_VALUE;
   private final SkyWeather.Climate probe = new SkyWeather.Climate();

   void read(ClimateManager var1, double var2) {
      this.baseCloud = var1.getClimateFloat(8).getInternalValue();
      long var4 = (long)Math.floor(var2 * 60.0);
      if (var4 != this.lastMinute) {
         this.lastMinute = var4;
         WeatherPeriod var6 = var1.getWeatherPeriod();
         this.known = !GameClient.client;
         this.stages = 0;
         this.periods = 0;
         if (!this.known) {
            this.clientRunning = var6 != null && var6.isRunning();
            this.clientEnd = this.clientRunning ? var2 + (1.0 - var6.getTotalProgress()) * var6.getDuration() : var2;
         } else {
            double var7 = var2;
            if (var6 != null && var6.isRunning() && var6.getCurrentStage() != null) {
               var7 = this.add(var6, var6.getCurrentStage());
            }

            ClimateForecaster var9 = var1.getClimateForecaster();

            for (int var10 = 0; var10 * 24.0 < 96.0 && this.periods < 8; var10++) {
               DayForecast var11 = var9.getForecast(var10);
               if (var11 != null && var11.isWeatherStarts()) {
                  WeatherPeriod var12 = var11.getWeatherPeriod();
                  ArrayList var13 = var12.getWeatherStages();
                  if (!var13.isEmpty() && ((WeatherStage)var13.get(0)).getStageStart() >= var7 && ((WeatherStage)var13.get(0)).getStageStart() > var2) {
                     this.add(var12, null);
                  }
               }
            }
         }
      }
   }

   private double add(WeatherPeriod var1, WeatherStage var2) {
      ArrayList var3 = var1.getWeatherStages();
      int var4 = var2 == null ? 0 : Math.max(0, var3.indexOf(var2));
      double var5 = var3.isEmpty() ? 0.0 : ((WeatherStage)var3.get(var4)).getStageStart();
      int var7 = this.periods++;
      this.front[var7] = var1.getFrontCache().getStrength();

      for (int var8 = var4; var8 < var3.size() && this.stages < 96; var8++) {
         WeatherStage var9 = (WeatherStage)var3.get(var8);
         int var10 = this.stages++;
         this.periodOf[var10] = var7;
         this.kind[var10] = var9.getStageID();
         this.start[var10] = var5;
         this.end[var10] = var5 + var9.getStageDuration();
         var5 = this.end[var10];
         this.target[var10] = value(TARGET_STRENGTH, var9);
         this.mid[var10] = lerp(LERP_MID, var9);
         this.last[var10] = lerp(LERP_END, var9);
         this.entry[var10] = var8 == var4 ? value(ENTRY_STRENGTH, var9) : this.strength(var10 - 1, this.end[var10 - 1]);
      }

      return var5;
   }

   void plan(int var1, double var2, double var4, float var6, float var7) {
      this.known = true;
      int var8 = this.periods++;
      int var9 = this.stages++;
      this.front[var8] = var7;
      this.periodOf[var9] = var8;
      this.kind[var9] = var1;
      this.start[var9] = var2;
      this.end[var9] = var4;
      this.entry[var9] = var6;
      this.target[var9] = var6;
      this.mid[var9] = 0;
      this.last[var9] = 2;
   }

   void into(double var1, SkyWeather.Climate var3) {
      if (!this.known) {
         if (this.clientRunning && var1 >= this.clientEnd) {
            this.clear(var3);
         }
      } else {
         for (int var4 = 0; var4 < this.stages; var4++) {
            if (var1 >= this.start[var4] && var1 < this.end[var4]) {
               float var5 = this.strength(var4, var1);
               var3.periodRunning = true;
               var3.period = var5;
               var3.storm = this.kind[var4] == 3 || this.kind[var4] == 8 || this.kind[var4] == 11;
               var3.precipitation = this.precipitation(var4, var1, var5);
               var3.cloud = this.baseCloud + (Math.max(0.75F, this.baseCloud) - this.baseCloud) * var5;
               return;
            }
         }

         this.clear(var3);
      }
   }

   private void clear(SkyWeather.Climate var1) {
      var1.periodRunning = false;
      var1.period = 0.0F;
      var1.storm = false;
      var1.precipitation = 0.0F;
      var1.cloud = this.baseCloud;
   }

   void check(double var1, float var3) {
      long var4 = (long)Math.floor(var1);
      if (this.known && var4 != this.lastHour) {
         if (this.lastHour == Long.MIN_VALUE) {
            Arrays.fill(this.foretoldFor, -1L);
         }

         this.lastHour = var4;
         int var6 = (int)Math.floorMod(var4, 24L);
         if (this.foretoldFor[var6] == var4 && (this.foretold[var6] > 0.01F || var3 > 0.01F)) {
            System.out.printf("[Viewpoint] weather forecast: hour %d, foretold %d h before %.2f, falls %.2f%n", var4, 3, this.foretold[var6], var3);
         }

         this.probe.precipitation = 0.0F;
         this.into(var4 + 3L + 0.5, this.probe);
         int var7 = (int)Math.floorMod(var4 + 3L, 24L);
         this.foretold[var7] = this.probe.precipitation;
         this.foretoldFor[var7] = var4 + 3L;
      }
   }

   boolean known() {
      return this.known;
   }

   private float strength(int var1, double var2) {
      float var4 = (float)Math.max(0.0, Math.min(1.0, (var2 - this.start[var1]) / (this.end[var1] - this.start[var1])));
      boolean var5 = var4 < 0.5F;
      float var6 = var5 ? var4 * 2.0F : 2.0F - var4 * 2.0F;
      float var7 = this.lerpValue(var1, this.last[var1]);
      if (this.mid[var1] == 0) {
         return clerp(var4, this.entry[var1], var7);
      }

      float var8 = this.lerpValue(var1, this.mid[var1]);
      return var5 ? clerp(var6, this.entry[var1], var8) : clerp(var6, var7, var8);
   }

   private float lerpValue(int var1, int var2) {
      return switch (var2) {
         case 1 -> this.entry[var1];
         case 2 -> this.target[var1];
         case 3 -> var1 + 1 < this.stages && this.periodOf[var1 + 1] == this.periodOf[var1] ? this.target[var1 + 1] : 0.0F;
         default -> 0.0F;
      };
   }

   private float precipitation(int var1, double var2, float var4) {
      float var5 = this.front[this.periodOf[var1]];
      float var6 = 0.3F * var5 + (float)SimplexNoise.noise(var2, 24000.0) * (1.0F - 0.3F * var5);
      var6 = (var6 + 1.0F) * 0.5F;
      if (pushesUp(this.kind[var1])) {
         double var7 = (this.end[var1] - this.start[var1]) * 0.1;
         float var9 = var5 * 0.5F;
         if (var2 - this.start[var1] < var7) {
            var9 = clerp((float)((var2 - this.start[var1]) / var7), 0.0F, var9);
         } else if (this.end[var1] - var2 < var7) {
            var9 = clerp((float)(1.0 - (this.end[var1] - var2) / var7), var9, 0.0F);
         }

         var6 = var9 + var6 * (1.0F - var9);
      }

      float var11 = 0.35F - var5 * 0.2F;
      return var6 > var11 ? (var6 - var11) / (1.0F - var11) * var4 : 0.0F;
   }

   private static boolean pushesUp(int var0) {
      return var0 == 2 || var0 == 3 || var0 == 7 || var0 == 8 || var0 == 11;
   }

   private static float clerp(float var0, float var1, float var2) {
      float var3 = (float)(1.0 - Math.cos(var0 * Math.PI)) / 2.0F;
      return var1 * (1.0F - var3) + var2 * var3;
   }

   private static float value(Field var0, WeatherStage var1) {
      try {
         return var0.getFloat(var1);
      } catch (IllegalAccessException var3) {
         throw new IllegalStateException("the game's weather stage hides its " + var0.getName(), var3);
      }
   }

   private static int lerp(Field var0, WeatherStage var1) {
      try {
         Object var2 = var0.get(var1);
         return var2 == null ? 0 : ((StrLerpVal)var2).getValue();
      } catch (IllegalAccessException var3) {
         throw new IllegalStateException("the game's weather stage hides its " + var0.getName(), var3);
      }
   }

   private static Field field(String var0) {
      try {
         Field var1 = WeatherStage.class.getDeclaredField(var0);
         var1.setAccessible(true);
         return var1;
      } catch (NoSuchFieldException var2) {
         throw new IllegalStateException("the game's weather stage has no " + var0 + ": the build changed", var2);
      }
   }
}
