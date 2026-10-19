package viewpoint.input;

final class Stride {
   static final int WALK = 0;
   static final int SNEAK = 1;
   static final float WALK_TAU = 0.3F;
   static final float FACTOR_TAU = 0.1F;
   static final float MOST = 5.0F;
   private final float[] walk = new float[2];
   private float factor = 1.0F;

   void walked(int var1, float var2, float var3) {
      this.walk[var1] = this.walk[var1] == 0.0F ? var2 : Ease.toward(this.walk[var1], var2, var3, 0.3F);
   }

   float strafed(int var1, float var2, float var3, float var4, float var5) {
      if (this.walk[var1] > 0.0F && var2 > 0.0F && var3 > 0.0F) {
         float var6 = Math.max(1.0F, Math.min(5.0F, var5 * this.walk[var1] * var3 / var2));
         this.factor = Ease.toward(this.factor, var6, var4, 0.1F);
      }

      return this.factor;
   }

   void stop() {
      this.factor = 1.0F;
   }

   float factor() {
      return this.factor;
   }
}
