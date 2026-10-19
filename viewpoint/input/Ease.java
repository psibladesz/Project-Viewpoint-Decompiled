package viewpoint.input;

final class Ease {
   private static final float TWO_PI = (float) (Math.PI * 2);
   private static final float ARRIVED = 0.001F;

   static float toward(float var0, float var1, float var2, float var3) {
      return var3 <= 0.0F ? var1 : var0 + (var1 - var0) * (1.0F - (float)Math.exp(-var2 / var3));
   }

   static float step(float var0, float var1, float var2, float var3) {
      float var4 = var3 * var2;
      return var0 + Math.max(-var4, Math.min(var4, var1 - var0));
   }

   static float between(float var0, float var1) {
      return wrap(var1 - var0);
   }

   static float angleToward(float var0, float var1, float var2, float var3) {
      float var4 = between(var0, var1);
      return Math.abs(var4) < 0.001F ? wrap(var1) : wrap(toward(var0, var0 + var4, var2, var3));
   }

   static float wrap(float var0) {
      float var1 = var0 % (float) (Math.PI * 2);
      return var1 > Math.PI ? var1 - (float) (Math.PI * 2) : (var1 < -Math.PI ? var1 + (float) (Math.PI * 2) : var1);
   }

   private Ease() {
   }
}
