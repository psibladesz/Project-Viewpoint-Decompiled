package viewpoint.render;

final class IrisSun {
   static final double TILT = Math.toRadians(38.0);
   static final String PATH_ROTATION = "-38.0";
   final double[] sun = new double[3];
   final double[] moon = new double[3];
   double sunAngle;
   double shadowAngle;
   int worldTime;
   boolean sunLights;

   void update(SceneData var1, boolean var2) {
      boolean var3 = var1.nightFraction >= 0.0F;
      if (!var2) {
         set(this.sun, var1.sunTrueX, var1.sunTrueY, var1.sunTrueZ);
         set(this.moon, var1.skyMoonX, var1.skyMoonY, var1.skyMoonZ);
         this.sunAngle = var3 ? 0.5 + 0.5 * var1.nightFraction : 0.5 * Math.max(0.0, Math.min(1.0, var1.dayFraction));
      } else {
         double var4;
         if (var3) {
            var4 = Math.PI + Math.PI * var1.nightFraction;
         } else {
            double var6 = Math.max(-1.0, Math.min(1.0, var1.sunTrueY / Math.cos(TILT)));
            double var8 = Math.acos(var6);
            var4 = var1.dayFraction < 0.5 ? (Math.PI / 2) - var8 : (Math.PI / 2) + var8;
         }

         this.sun[0] = Math.cos(var4);
         this.sun[1] = Math.sin(var4) * Math.cos(TILT);
         this.sun[2] = Math.sin(var4) * Math.sin(TILT);
         this.moon[0] = -this.sun[0];
         this.moon[1] = -this.sun[1];
         this.moon[2] = -this.sun[2];
         this.sunAngle = var4 / (Math.PI * 2);
      }

      this.sunAngle = this.sunAngle - Math.floor(this.sunAngle);
      this.sunLights = this.sunAngle < 0.5;
      this.shadowAngle = this.sunLights ? this.sunAngle : this.sunAngle - 0.5;
      this.worldTime = worldTime(this.sunAngle);
   }

   private static void set(double[] var0, float var1, float var2, float var3) {
      double var4 = Math.sqrt(var1 * var1 + var2 * var2 + var3 * var3);
      var4 = var4 > 0.0 ? var4 : 1.0;
      var0[0] = -var1 / var4;
      var0[1] = var2 / var4;
      var0[2] = -var3 / var4;
   }

   static int worldTime(double var0) {
      double var2 = var0 - 0.25;
      var2 -= Math.floor(var2);
      double var4 = 0.0;
      double var6 = 1.0;

      for (int var8 = 0; var8 < 40; var8++) {
         double var9 = (var4 + var6) * 0.5;
         if (eased(var9) < var2) {
            var4 = var9;
         } else {
            var6 = var9;
         }
      }

      double var12 = (var4 + var6) * 0.5 + 0.25;
      var12 -= Math.floor(var12);
      return (int)Math.round(var12 * 24000.0) % 24000;
   }

   static double eased(double var0) {
      double var2 = 0.5 - Math.cos(var0 * Math.PI) / 2.0;
      return (var0 * 2.0 + var2) / 3.0;
   }
}
