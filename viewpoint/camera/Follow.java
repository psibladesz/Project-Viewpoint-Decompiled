package viewpoint.camera;

final class Follow {
   static final float ACROSS_MOST = 0.35F;
   static final float UP_MOST = 0.5F;
   static final float DOWN = 0.5F;
   static final float TELEPORT = 3.0F;
   private final Spring x = new Spring();
   private final Spring y = new Spring();
   private final Spring h = new Spring();
   private double lastX;
   private double lastY;
   private double lastH;
   private boolean started;

   void reset() {
      this.started = false;
   }

   boolean update(double var1, double var3, double var5, float var7, float var8, float var9, float var10, float var11) {
      double var12 = var1 - this.lastX;
      double var14 = var3 - this.lastY;
      double var16 = var5 - this.lastH;
      this.lastX = var1;
      this.lastY = var3;
      this.lastH = var5;
      if (this.started && !(var12 * var12 + var14 * var14 + var16 * var16 > 9.0)) {
         this.x.value -= (float)var12;
         this.y.value -= (float)var14;
         this.h.value -= (float)var16;
         this.x.damped(0.0F, var7, var9, var11);
         this.y.damped(0.0F, var7, var9, var11);
         this.h.toward(0.0F, this.h.value > 0.0F ? var8 * 0.5F : var8, var11);
         float var18 = (float)Math.hypot(this.x.value, this.y.value);
         if (var18 > var10) {
            this.x.set(this.x.value * var10 / var18);
            this.y.set(this.y.value * var10 / var18);
         }

         if (Math.abs(this.h.value) > 0.5F) {
            this.h.set(Math.copySign(0.5F, this.h.value));
         }

         return false;
      } else {
         this.started = true;
         this.x.set(0.0F);
         this.y.set(0.0F);
         this.h.set(0.0F);
         return true;
      }
   }

   float lagX() {
      return this.x.value;
   }

   float lagY() {
      return this.y.value;
   }

   float lagH() {
      return this.h.value;
   }
}
