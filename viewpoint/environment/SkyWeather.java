package viewpoint.environment;

final class SkyWeather {
   static final float LAYER = 0.0F;
   static final float HEAPED = 0.5F;
   static final float TOWERING = 1.0F;
   private static final float DECK_TYPE = 0.25F;
   private static final float DAY_MOST = 0.75F;
   private static final float DAY_SKEW = 1.7F;
   private static final float DAY_HOURS = 22.0F;
   private static final float DAY_TURN = 0.35F;
   private static final float GAME_CLOUDS = 0.35F;
   private static final float SPREAD_FROM = 0.45F;
   private static final float RAIN_DECK_RAMP = 0.1F;
   private static final float PERIOD_BROKEN = 0.55F;
   private static final float NOISE = 0.18F;
   private static final float NOISE_HOURS = 5.0F;
   private static final float TREND_HOURS = 0.5F;
   private static final float MAX_LEAD_HOURS = 14.0F;
   private static final float DUE_HOURS = 1.5F;
   private static final float RAIN_CATCH_HOURS = 0.1F;
   private static final float TYPE_HOURS = 0.5F;
   private static final float MAX_STEP_HOURS = 2.0F;
   private static final SkyWeather.Front FAIR = new SkyWeather.Front(0.0F, 0.0F, 1.0F, 1.5F, 1.5F, 0.5F, 0.0F);
   private static final float DRAW_HOURS = 36.0F;
   private static final float DRAW_TURN = 0.6F;
   private static final double YEAR_DAYS = 365.0;
   private static final double MONTH_DAYS = 30.416666666666668;
   private static final float FRONT_TURN = 40.0F;
   private static final float TURN_HOURS = 3.0F;
   private static final float FRONT_FRESHENS = 0.5F;
   private final float[] aloftFrom = new float[3];
   private final float[] aloftSpeed = new float[3];
   private boolean started;
   private boolean restarted;
   private boolean building;
   private float cover;
   private float type = 0.5F;
   private float front;
   private float trend;
   private float sampleAirMass;
   private float buildingSign;
   private float turn;
   private float freshen;
   private double sampleHours;
   private double turnHours;
   private SkyWeather.Front character = FAIR;

   float step(SkyWeather.Climate var1, float var2) {
      this.restarted = !this.started || var2 < 0.0F || var2 > 2.0F;
      if (this.restarted) {
         this.started = true;
         this.building = false;
         this.front = 0.0F;
         this.trend = 0.0F;
         this.sampleAirMass = var1.airMass;
         this.sampleHours = var1.hours;
         this.cover = this.target(var1);
         this.type = this.typeTarget(var1);
         this.turn = this.turnTarget(var1);
         this.freshen = this.freshenTarget(var1);
         this.aloft(var1);
         return this.cover;
      } else {
         this.followAirMass(var1);
         float var3 = this.target(var1);
         float var4 = var3 <= this.cover ? this.character.clearHours() : (this.cover < rainDeck(var1.precipitation) ? 0.1F : this.character.buildHours());
         this.cover = this.cover + (var3 - this.cover) * (1.0F - (float)Math.exp(-var2 / var4));
         this.type = this.type + (this.typeTarget(var1) - this.type) * (1.0F - (float)Math.exp(-var2 / 0.5F));
         float var5 = 1.0F - (float)Math.exp(-var2 / 3.0F);
         this.turn = this.turn + (this.turnTarget(var1) - this.turn) * var5;
         this.freshen = this.freshen + (this.freshenTarget(var1) - this.freshen) * var5;
         this.aloft(var1);
         return this.cover;
      }
   }

   float type() {
      return this.type;
   }

   float aloftFrom(int var1) {
      return this.aloftFrom[var1];
   }

   float aloftSpeed(int var1) {
      return this.aloftSpeed[var1];
   }

   float front() {
      return this.front;
   }

   boolean restarted() {
      return this.restarted;
   }

   void restart() {
      this.started = false;
   }

   float trend() {
      return this.trend;
   }

   void copyTo(SkyWeather var1) {
      var1.started = this.started;
      var1.restarted = this.restarted;
      var1.building = this.building;
      var1.cover = this.cover;
      var1.type = this.type;
      var1.front = this.front;
      var1.trend = this.trend;
      var1.sampleAirMass = this.sampleAirMass;
      var1.buildingSign = this.buildingSign;
      var1.turn = this.turn;
      var1.freshen = this.freshen;
      System.arraycopy(this.aloftFrom, 0, var1.aloftFrom, 0, this.aloftFrom.length);
      System.arraycopy(this.aloftSpeed, 0, var1.aloftSpeed, 0, this.aloftSpeed.length);
      var1.sampleHours = this.sampleHours;
      var1.turnHours = this.turnHours;
      var1.character = this.character;
   }

   private void followAirMass(SkyWeather.Climate var1) {
      if (var1.hours - this.sampleHours >= 0.5) {
         float var2 = (float)((var1.airMass - this.sampleAirMass) / (var1.hours - this.sampleHours));
         this.trend = this.trend == 0.0F ? var2 : 0.5F * (this.trend + var2);
         this.sampleAirMass = var1.airMass;
         this.sampleHours = var1.hours;
      }

      boolean var4 = var1.airMass * this.trend < 0.0F;
      float var3 = var4 ? Math.abs(var1.airMass / this.trend) : Float.POSITIVE_INFINITY;
      if (!this.building && var4 && var3 < 14.0F) {
         this.building = true;
         this.buildingSign = Math.signum(var1.airMass);
         this.turnHours = Double.NaN;
         this.character = SkyWeather.Front.of((long)Math.floor(var1.hours), this.trend > 0.0F);
      }

      if (!this.building) {
         this.front = 0.0F;
      } else {
         if (var4) {
            this.front = clamp01(1.0F - var3 / this.character.leadHours());
         } else if (Double.isNaN(this.turnHours) && Math.signum(var1.airMass) != this.buildingSign) {
            this.turnHours = var1.hours;
            this.front = 1.0F;
         } else if (Double.isNaN(this.turnHours) || var1.hours - this.turnHours > 1.5) {
            this.building = false;
            this.front = 0.0F;
         }
      }
   }

   private float target(SkyWeather.Climate var1) {
      float var2 = fairCover(var1.hours, var1.cloud);
      float var3 = this.front > 0.0F ? lerp(var2, Math.max(var2, this.character.cover()), (float)Math.pow(this.front, this.character.curve())) : var2;
      float var4 = var1.periodRunning ? lerp(0.55F, 1.0F, var1.period) : 0.0F;
      float var5 = Math.max(var3, var4);
      float var6 = clamp01(var5 + 0.72F * var5 * (1.0F - var5) * noise(var1.hours));
      return Math.max(var6, rainDeck(var1.precipitation));
   }

   private float typeTarget(SkyWeather.Climate var1) {
      if (var1.storm) {
         return 1.0F;
      }

      float var2 = var1.periodRunning ? 1.0F : (this.front > 0.0F ? (float)Math.pow(this.front, this.character.curve()) : 0.0F);
      float var3 = lerp(fairType(fairCover(var1.hours, var1.cloud)), this.character.type(), var2);
      return lerp(var3, Math.min(var3, 0.25F), Math.min(1.0F, var1.precipitation / 0.1F));
   }

   private void aloft(SkyWeather.Climate var1) {
      double var2 = var1.hours / 36.0;
      long var4 = (long)Math.floor(var2);
      float var6 = clamp01(((float)(var2 - var4) - 0.39999998F) / 0.6F);
      float var7 = var6 * var6 * (3.0F - 2.0F * var6);
      int var8 = month(var4, var1.yearStart);
      int var9 = month(var4 + 1L, var1.yearStart);
      int var10 = sounding(var4, var8);
      int var11 = sounding(var4 + 1L, var9);

      for (int var12 = 0; var12 < 3; var12++) {
         float var13 = WindsAloft.from(var8, var10, var12);
         float var14 = WindsAloft.from(var9, var11, var12);
         float var15 = (float)Math.IEEEremainder(var14 - var13, 360.0);
         this.aloftFrom[var12] = compass(var13 + var7 * var15 + 40.0F * this.turn);
         this.aloftSpeed[var12] = lerp(WindsAloft.speed(var8, var10, var12), WindsAloft.speed(var9, var11, var12), var7) * (1.0F + 0.5F * this.freshen);
      }
   }

   private static int month(long var0, double var2) {
      double var4 = (var0 * 36.0 - var2) / 24.0;
      double var6 = (var4 - 365.0 * Math.floor(var4 / 365.0)) / 30.416666666666668 - 0.5;
      int var8 = (int)Math.floor(var6);
      return Math.floorMod(random(var0, 16) < var6 - var8 ? var8 + 1 : var8, 12);
   }

   private static int sounding(long var0, int var2) {
      return Math.min((int)(random(var0, 17) * WindsAloft.soundings(var2)), WindsAloft.soundings(var2) - 1);
   }

   private float turnTarget(SkyWeather.Climate var1) {
      if (var1.periodRunning) {
         return this.character.veer();
      } else {
         return this.front > 0.0F ? -((float)Math.pow(this.front, this.character.curve())) : 0.0F;
      }
   }

   private float freshenTarget(SkyWeather.Climate var1) {
      float var2 = this.front > 0.0F ? (float)Math.pow(this.front, this.character.curve()) : 0.0F;
      return Math.max(var2, var1.periodRunning ? var1.period : 0.0F);
   }

   static float fairCover(double var0, float var2) {
      float var3 = daySky(var0);
      return var3 + (1.0F - var3) * 0.35F * var2;
   }

   static float daySky(double var0) {
      double var2 = var0 / 22.0;
      long var4 = (long)Math.floor(var2);
      float var6 = clamp01(((float)(var2 - var4) - 0.65F) / 0.35F);
      return lerp(dayDraw(var4), dayDraw(var4 + 1L), var6 * var6 * (3.0F - 2.0F * var6));
   }

   private static float dayDraw(long var0) {
      return 0.75F * (float)Math.pow(random(var0, 15), 1.7F);
   }

   private static float fairType(float var0) {
      float var1 = clamp01((var0 - 0.45F) / 0.3F);
      return lerp(0.5F, 0.25F, var1 * var1 * (3.0F - 2.0F * var1));
   }

   static float rainDeck(float var0) {
      return Math.min(1.0F, var0 / 0.1F) * (0.9F + 0.1F * var0);
   }

   static float noise(double var0) {
      return 0.7F * wave(var0 / 5.0, 11) + 0.3F * wave(var0 / 1.35, 12);
   }

   private static float wave(double var0, int var2) {
      long var3 = (long)Math.floor(var0);
      float var5 = (float)(var0 - var3);
      float var6 = var5 * var5 * (3.0F - 2.0F * var5);
      return lerp(random(var3, var2), random(var3 + 1L, var2), var6) * 2.0F - 1.0F;
   }

   static float random(long var0, int var2) {
      long var3 = var0 * -7046029254386353131L + var2 * -4417276706812531889L;
      var3 = (var3 ^ var3 >>> 31) * -4658895280553007687L;
      var3 = (var3 ^ var3 >>> 29) * -7723592293110705685L;
      return (float)(var3 >>> 40) / 1.6777216E7F;
   }

   private static float lerp(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   static float compass(float var0) {
      return var0 - 360.0F * (float)Math.floor(var0 / 360.0F);
   }

   private static float clamp01(float var0) {
      return var0 < 0.0F ? 0.0F : Math.min(var0, 1.0F);
   }

   static final class Climate {
      float cloud;
      float precipitation;
      float airMass;
      boolean periodRunning;
      float period;
      boolean storm;
      double hours;
      double yearStart;

      void set(SkyWeather.Climate var1) {
         this.cloud = var1.cloud;
         this.precipitation = var1.precipitation;
         this.airMass = var1.airMass;
         this.periodRunning = var1.periodRunning;
         this.period = var1.period;
         this.storm = var1.storm;
         this.hours = var1.hours;
         this.yearStart = var1.yearStart;
      }
   }

   record Front(float leadHours, float cover, float curve, float buildHours, float clearHours, float type, float veer) {
      static SkyWeather.Front of(long var0, boolean var2) {
         return new SkyWeather.Front(
            SkyWeather.lerp(3.0F, 9.0F, SkyWeather.random(var0, 1)) * (var2 ? 1.5F : 1.0F),
            SkyWeather.lerp(0.6F, 0.92F, SkyWeather.random(var0, 2)),
            SkyWeather.lerp(0.6F, 1.8F, SkyWeather.random(var0, 3)),
            SkyWeather.lerp(0.8F, 2.5F, SkyWeather.random(var0, 4)),
            SkyWeather.lerp(0.8F, 2.5F, SkyWeather.random(var0, 5)) * (var2 ? 1.4F : 0.7F),
            var2 ? SkyWeather.lerp(0.05F, 0.25F, SkyWeather.random(var0, 6)) : SkyWeather.lerp(0.6F, 0.85F, SkyWeather.random(var0, 6)),
            var2 ? 0.0F : SkyWeather.lerp(0.6F, 1.0F, SkyWeather.random(var0, 7))
         );
      }
   }
}
