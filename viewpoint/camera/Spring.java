package viewpoint.camera;

final class Spring {
   private static final float LN2 = 0.6931472F;
   private static final float CRITICAL = 0.999F;
   float value;
   float velocity;

   void set(float var1) {
      this.value = var1;
      this.velocity = 0.0F;
   }

   void toward(float var1, float var2, float var3) {
      this.damped(var1, var2, 1.0F, var3);
   }

   void damped(float var1, float var2, float var3, float var4) {
      if (var2 <= 0.0F) {
         this.set(var1);
      } else {
         float var5 = 1.3862944F / var2;
         if (var3 >= 0.999F) {
            this.critical(var1, var5, var4);
         } else {
            this.under(var1, var5, var5 * (float)Math.sqrt(1.0 / (var3 * var3) - 1.0), var4);
         }
      }
   }

   private void critical(float var1, float var2, float var3) {
      float var4 = this.value - var1;
      float var5 = this.velocity + var4 * var2;
      float var6 = (float)Math.exp(-var2 * var3);
      this.value = var1 + var6 * (var4 + var5 * var3);
      this.velocity = var6 * (this.velocity - var5 * var2 * var3);
   }

   private void under(float var1, float var2, float var3, float var4) {
      float var5 = this.value - var1;
      float var6 = (this.velocity + var5 * var2) / var3;
      float var7 = (float)Math.exp(-var2 * var4);
      float var8 = (float)Math.cos(var3 * var4);
      float var9 = (float)Math.sin(var3 * var4);
      this.value = var1 + var7 * (var5 * var8 + var6 * var9);
      this.velocity = var7 * ((var3 * var6 - var2 * var5) * var8 - (var2 * var6 + var3 * var5) * var9);
   }
}
