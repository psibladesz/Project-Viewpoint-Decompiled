package viewpoint.gunplay;

import java.util.Random;

final class Sway {
   private static final double[] RIGHT_HZ = new double[]{0.203, 0.251, 0.307, 0.353, 0.409, 0.457, 0.509, 0.563, 0.619, 0.677};
   private static final double[] UP_HZ = new double[]{0.227, 0.281, 0.331, 0.383, 0.433, 0.487, 0.541, 0.593, 0.647, 0.691};
   static final double HALF_LIFE = 0.1;
   static final double FOCUSED_PACE = 0.5;
   private static final double AMPLITUDE = Math.sqrt(2.0 / RIGHT_HZ.length);
   private static final double TURN = Math.PI * 2;
   private static final double INSIDE;
   private static final double SMALL = 1.0E-6;
   private final double[] phases = new double[RIGHT_HZ.length + UP_HZ.length];
   private double radius;
   private double time;
   private double right;
   private double up;

   void start(Random var1, double var2) {
      for (int var4 = 0; var4 < this.phases.length; var4++) {
         this.phases[var4] = var1.nextDouble() * (Math.PI * 2);
      }

      this.time = 0.0;
      this.radius = var2;
      this.place();
   }

   void advance(double var1, double var3, double var5) {
      this.time += var1 * (1.0 - 0.5 * var5);
      this.radius = this.radius + (var3 - this.radius) * (1.0 - Math.pow(0.5, var1 / 0.1));
      this.place();
   }

   double right() {
      return this.right;
   }

   double up() {
      return this.up;
   }

   double radius() {
      return this.radius;
   }

   private void place() {
      double var1 = this.axis(RIGHT_HZ, 0);
      double var3 = this.axis(UP_HZ, RIGHT_HZ.length);
      double var5 = Math.hypot(var1, var3);
      double var7 = -2.0 * Math.log(1.0 - INSIDE * (1.0 - Math.exp(-var5 * var5 / 2.0)));
      double var9 = var5 < 1.0E-6 ? Math.sqrt(INSIDE) : Math.sqrt(var7) / var5;
      this.right = this.radius / 2.0 * var9 * var1;
      this.up = this.radius / 2.0 * var9 * var3;
   }

   private double axis(double[] var1, int var2) {
      double var3 = 0.0;

      for (int var5 = 0; var5 < var1.length; var5++) {
         var3 += Math.sin((Math.PI * 2) * var1[var5] * this.time + this.phases[var2 + var5]);
      }

      return AMPLITUDE * var3;
   }

   static {
      double var0 = 2.0;
      INSIDE = 1.0 - Math.exp(-var0 * var0 / 2.0);
   }
}
