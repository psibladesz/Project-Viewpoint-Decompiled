package viewpoint.input;

public final class StickCurve {
   static final float RIM = 0.95F;
   static final float BOOST = 1.6F;
   static final float BOOST_DELAY = 0.3F;
   static final float BOOST_RAMP = 0.25F;
   static final float LEVEL = 0.866F;
   private double fullSince = Double.NaN;

   public StickCurve.Turn sample(float var1, float var2, double var3, StickCurve.Settings var5) {
      float var6 = (float)Math.hypot(var1, var2);
      float var7 = shape(var6, var5.deadZone(), var5.power());
      if (var7 <= 0.0F) {
         this.fullSince = Double.NaN;
         return StickCurve.Turn.NONE;
      }

      float var8 = var1 / var6;
      float var9 = -var2 / var6;
      boolean var10 = var5.boost() && var7 >= 1.0F && Math.abs(var8) >= 0.866F;
      if (!var10) {
         this.fullSince = Double.NaN;
      } else if (Double.isNaN(this.fullSince)) {
         this.fullSince = var3;
      }

      float var11 = var9 * var7 * var5.pitchSpeed();
      return new StickCurve.Turn(var8 * var7 * var5.yawSpeed(), var5.invertY() ? -var11 : var11, this.fullSince);
   }

   public void reset() {
      this.fullSince = Double.NaN;
   }

   static float shape(float var0, float var1, float var2) {
      if (var0 <= var1) {
         return 0.0F;
      }

      float var3 = Math.min(1.0F, (var0 - var1) / (0.95F - var1));
      return (float)Math.pow(var3, var2);
   }

   static double boosted(double var0) {
      if (var0 <= 0.3F) {
         return var0;
      }

      double var2 = var0 - 0.3F;
      return var2 <= 0.25 ? var0 + 0.6F * var2 * var2 / 0.5 : var0 + 0.6F * (0.125 + var2 - 0.25);
   }

   public record Settings(float deadZone, float power, float yawSpeed, float pitchSpeed, boolean invertY, boolean boost) {
   }

   public record Turn(float yawRate, float pitchRate, double boostSince) {
      public static final StickCurve.Turn NONE = new StickCurve.Turn(0.0F, 0.0F, Double.NaN);

      public boolean still() {
         return this.yawRate == 0.0F && this.pitchRate == 0.0F;
      }

      public float yaw(double var1, double var3) {
         return Double.isNaN(this.boostSince)
            ? this.yawRate * (float)(var3 - var1)
            : this.yawRate * (float)(StickCurve.boosted(var3 - this.boostSince) - StickCurve.boosted(var1 - this.boostSince));
      }

      public float pitch(double var1, double var3) {
         return this.pitchRate * (float)(var3 - var1);
      }
   }
}
