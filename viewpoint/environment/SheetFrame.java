package viewpoint.environment;

final class SheetFrame {
   private static final double WRAP = 1048576.0;
   private static final float LEAST_SPEED = 0.05F;
   private boolean started;
   private float downX = 1.0F;
   private float downZ;
   private double placeX;
   private double placeZ;
   private double lastX;
   private double lastZ;

   void step(float var1, float var2, double var3, double var5, float var7, float[] var8) {
      if (this.started) {
         double var9 = var3 - this.lastX;
         double var11 = var5 - this.lastZ;
         this.placeX = this.placeX + (var9 * this.downX + var11 * this.downZ);
         this.placeZ = this.placeZ + (var11 * this.downX - var9 * this.downZ);
      }

      this.started = true;
      this.lastX = var3;
      this.lastZ = var5;
      float var13 = (float)Math.hypot(var1, var2);
      this.placeX -= var13 * var7;
      if (var13 > 0.05F) {
         this.downX = var1 / var13;
         this.downZ = var2 / var13;
      }

      this.placeX = this.placeX - 1048576.0 * Math.floor(this.placeX / 1048576.0);
      this.placeZ = this.placeZ - 1048576.0 * Math.floor(this.placeZ / 1048576.0);
      var8[0] = this.downX;
      var8[1] = this.downZ;
      var8[2] = (float)this.placeX;
      var8[3] = (float)this.placeZ;
   }
}
