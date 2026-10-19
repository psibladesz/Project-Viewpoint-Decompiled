package viewpoint.environment;

final class SkyClock {
   private static final float SMOOTHING = 0.5F;
   private static final float MAX_PACE = 250.0F;
   private static final float MAX_FRAME = 0.1F;
   private static final float HOURS_A_DAY = 24.0F;
   private float pace = 1.0F;
   private float lastTimeOfDay = Float.NaN;
   private double seconds;

   float advance(float var1, float var2, boolean var3, float var4) {
      float var5 = var2 - this.lastTimeOfDay;
      boolean var6 = Float.isNaN(this.lastTimeOfDay);
      this.lastTimeOfDay = var2;
      if (!var3 && !var6 && !(var1 <= 0.0F)) {
         if (var5 < -12.0F) {
            var5 += 24.0F;
         }

         float var7 = var1 * 24.0F / (60.0F * var4);
         float var8 = var5 / var7;
         if (var8 >= 0.0F && var8 <= 250.0F) {
            this.pace = this.pace + (var8 - this.pace) * Math.min(1.0F, var1 / 0.5F);
         }

         float var9 = Math.min(var1, 0.1F) * this.pace;
         this.seconds += var9;
         return var9;
      } else {
         return 0.0F;
      }
   }

   double seconds() {
      return this.seconds;
   }
}
