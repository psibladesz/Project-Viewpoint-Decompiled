package viewpoint.gunplay;

final class Focus {
   static final double SLOWEST = 6.0;
   static final double FASTEST = 1.5;
   static final double RAMP = 1.0;
   private static final int TOP = 10;
   private double focus;
   private double settled;

   void advance(double var1, int var3, boolean var4) {
      if (!var4) {
         this.reset();
      } else {
         double var5 = Math.min(1.0, (this.settled + var1 / 2.0) / 1.0);
         this.settled += var1;
         this.focus = Math.min(1.0, this.focus + var1 * var5 / (seconds(var3) - 0.5));
      }
   }

   void reset() {
      this.focus = 0.0;
      this.settled = 0.0;
   }

   double level() {
      return this.focus;
   }

   double tighten(double var1, double var3) {
      return var3 >= var1 ? var1 : var1 - (var1 - var3) * this.focus;
   }

   static double seconds(int var0) {
      return 6.0 + -4.5 * skill(var0);
   }

   static double skill(int var0) {
      return Math.max(0, Math.min(10, var0)) / 10.0;
   }
}
