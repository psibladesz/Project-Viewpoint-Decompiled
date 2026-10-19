package viewpoint.gunplay;

final class Sight {
   private static final double STEEP = 1.0E-9;
   double x;
   double y;
   double up;
   double fx;
   double fy;
   double fu;
   double rx;
   double ry;
   double ru;
   double ux;
   double uy;
   double uu;

   void set(double[] var1, double var2, double var4, double var6, double var8) {
      this.x = var1[0];
      this.y = var1[1];
      this.up = var1[2];
      double var10 = Math.cos(var2);
      double var12 = Math.sin(var2);
      double var14 = Math.cos(var4);
      double var16 = Math.sin(var4);
      this.fx = var10 * var14 - var12 * var6 - var10 * var16 * var8;
      this.fy = var12 * var14 + var10 * var6 - var12 * var16 * var8;
      this.fu = var16 + var14 * var8;
      double var18 = Math.sqrt(this.fx * this.fx + this.fy * this.fy + this.fu * this.fu);
      this.fx /= var18;
      this.fy /= var18;
      this.fu /= var18;
      double var20 = Math.hypot(this.fx, this.fy);
      this.rx = var20 > 1.0E-9 ? -this.fy / var20 : -var12;
      this.ry = var20 > 1.0E-9 ? this.fx / var20 : var10;
      this.ru = 0.0;
      this.ux = -this.fu * this.ry;
      this.uy = this.fu * this.rx;
      this.uu = this.fx * this.ry - this.fy * this.rx;
   }

   void turned(double var1, double var3, double[] var5) {
      double var6 = this.fx + this.rx * var1 + this.ux * var3;
      double var8 = this.fy + this.ry * var1 + this.uy * var3;
      double var10 = this.fu + this.ru * var1 + this.uu * var3;
      double var12 = Math.sqrt(var6 * var6 + var8 * var8 + var10 * var10);
      var5[0] = var6 / var12;
      var5[1] = var8 / var12;
      var5[2] = var10 / var12;
   }

   double along(double var1, double var3, double var5) {
      return (var1 - this.x) * this.fx + (var3 - this.y) * this.fy + (var5 - this.up) * this.fu;
   }

   boolean offset(double var1, double var3, double var5, double[] var7) {
      double var8 = var1 - this.x;
      double var10 = var3 - this.y;
      double var12 = var5 - this.up;
      double var14 = var8 * this.fx + var10 * this.fy + var12 * this.fu;
      if (var14 <= 0.0) {
         return false;
      }

      var7[0] = (var8 * this.rx + var10 * this.ry + var12 * this.ru) / var14;
      var7[1] = (var8 * this.ux + var10 * this.uy + var12 * this.uu) / var14;
      return true;
   }
}
