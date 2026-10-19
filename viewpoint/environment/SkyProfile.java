package viewpoint.environment;

import viewpoint.render.SceneData;

final class SkyProfile {
   private static final int SIDE = 48;
   private static final double STEP = 512.0;
   private static final int RING = 96;
   private static final double MAX_MOVE = 2048.0;
   private static final float MAX_FORECAST_HOURS = 1.0F;
   private static final float LEAST_SPEED = 0.1F;
   private static final int WIDEN = 3;
   private static final int SOFTEN = 3;
   private final float[] ringCover = new float[96];
   private final float[] ringType = new float[96];
   private final float[] ringRain = new float[96];
   private final long[] ringKey = new long[96];
   private final float[] aheadCover = new float[48];
   private final float[] aheadType = new float[48];
   private final float[] aheadRain = new float[48];
   private final float[] widened = new float[48];
   private final float[] softened = new float[97];
   private final SkyWeather ahead = new SkyWeather();
   private final SkyWeather.Climate future = new SkyWeather.Climate();
   private boolean started;
   private double along;
   private double lastX;
   private double lastZ;
   private long lastKey;

   void update(SceneData var1, double var2, double var4, SkyWeather var6, SkyWeather.Climate var7, float var8, GameForecast var9) {
      float var10 = Math.max(0.1F, (float)Math.hypot(var1.cloudWindX, var1.cloudWindZ));
      float var11 = var1.cloudWindX / var10;
      float var12 = var1.cloudWindZ / var10;
      float var13 = var1.cloudCover;
      float var14 = var6.type();
      float var15 = var7.precipitation;
      double var16 = var2 - this.lastX;
      double var18 = var4 - this.lastZ;
      if (this.started && !var6.restarted() && !(var16 * var16 + var18 * var18 > 4194304.0)) {
         this.along += var16 * var11 + var18 * var12;
      } else {
         this.started = true;
         this.along = 0.0;
         this.lastKey = 0L;

         for (int var20 = 0; var20 < 96; var20++) {
            this.keep(var20, var13, var14, var15);
         }
      }

      this.lastX = var2;
      this.lastZ = var4;
      this.record(var13, var14, var15);
      this.forecast(var6, var7, 512.0 / var10 * var8, var13, var9);
      this.build(var1, var13, var14, var15);
   }

   private void record(float var1, float var2, float var3) {
      long var4 = (long)Math.floor(this.along / 512.0);
      long var6 = Math.max(var4, this.lastKey);

      for (long var8 = Math.max(Math.min(var4, this.lastKey), var6 - 96L + 1L); var8 <= var6; var8++) {
         this.keep(var8, var1, var2, var3);
      }

      this.lastKey = var4;
   }

   private void keep(long var1, float var3, float var4, float var5) {
      int var6 = (int)Math.floorMod(var1, 96L);
      this.ringKey[var6] = var1;
      this.ringCover[var6] = var3;
      this.ringType[var6] = var4;
      this.ringRain[var6] = var5;
   }

   private void forecast(SkyWeather var1, SkyWeather.Climate var2, double var3, float var5, GameForecast var6) {
      var1.copyTo(this.ahead);
      this.future.set(var2);
      float var7 = var1.trend();

      for (int var8 = 0; var8 < 48; var8++) {
         for (double var9 = var3; var9 > 1.0E-6; var9--) {
            float var11 = (float)Math.min(var9, 1.0);
            this.future.hours += var11;
            this.future.airMass = Math.max(-1.0F, Math.min(1.0F, this.future.airMass + var7 * var11));
            var6.into(this.future.hours, this.future);
            var5 = this.ahead.step(this.future, var11);
         }

         this.aheadCover[var8] = var5;
         this.aheadType[var8] = this.ahead.type();
         this.aheadRain[var8] = this.future.precipitation;
      }

      this.widen(var2.precipitation);
   }

   private void widen(float var1) {
      for (int var2 = 0; var2 < 3; var2++) {
         for (int var3 = 0; var3 < 48; var3++) {
            float var4 = var3 == 0 ? var1 : this.aheadRain[var3 - 1];
            float var5 = var3 == 47 ? this.aheadRain[var3] : this.aheadRain[var3 + 1];
            this.widened[var3] = 0.25F * var4 + 0.5F * this.aheadRain[var3] + 0.25F * var5;
         }

         System.arraycopy(this.widened, 0, this.aheadRain, 0, 48);
      }
   }

   private void build(SceneData var1, float var2, float var3, float var4) {
      var1.coverAlong[48] = var2;
      var1.typeAlong[48] = var3;
      var1.rainAlong[48] = var4;
      float var5 = var3;

      for (int var6 = 1; var6 <= 48; var6++) {
         var1.coverAlong[48 - var6] = this.aheadCover[var6 - 1];
         var1.typeAlong[48 - var6] = this.aheadType[var6 - 1];
         var1.rainAlong[48 - var6] = this.aheadRain[var6 - 1];
         double var7 = (this.along + var6 * 512.0) / 512.0 - 0.5;
         long var9 = (long)Math.floor(var7);
         float var11 = (float)(var7 - var9);
         var1.coverAlong[48 + var6] = lerp(this.kept(this.ringCover, var9, var2), this.kept(this.ringCover, var9 + 1L, var2), var11);
         var1.typeAlong[48 + var6] = lerp(this.kept(this.ringType, var9, var3), this.kept(this.ringType, var9 + 1L, var3), var11);
         var1.rainAlong[48 + var6] = lerp(this.kept(this.ringRain, var9, var4), this.kept(this.ringRain, var9 + 1L, var4), var11);
      }

      this.soften(var1.coverAlong);
      this.soften(var1.typeAlong);

      for (float var14 : var1.typeAlong) {
         var5 = Math.max(var5, var14);
      }

      var1.cloudType = var5;
   }

   private void soften(float[] var1) {
      for (int var2 = 0; var2 < 3; var2++) {
         for (int var3 = 0; var3 < var1.length; var3++) {
            float var4 = var1[Math.max(var3 - 1, 0)];
            float var5 = var1[Math.min(var3 + 1, var1.length - 1)];
            this.softened[var3] = var3 == 48 ? var1[var3] : 0.25F * var4 + 0.5F * var1[var3] + 0.25F * var5;
         }

         System.arraycopy(this.softened, 0, var1, 0, var1.length);
      }
   }

   private float kept(float[] var1, long var2, float var4) {
      int var5 = (int)Math.floorMod(var2, 96L);
      return this.ringKey[var5] == var2 ? var1[var5] : var4;
   }

   private static float lerp(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }
}
