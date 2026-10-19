package viewpoint.camera;

final class Framing {
   static float right(float var0, float var1, float var2, float var3) {
      return (0.5F - var0) * 2.0F * var1 * var2 * var3;
   }

   static float up(float var0, float var1, float var2) {
      return (var0 - 0.5F) * 2.0F * var1 * var2;
   }

   static float shifted(float var0, float var1, float var2, float var3, float var4) {
      float var5 = 2.0F * var2 * var3 * var4;
      return var5 > 0.0F ? var0 - var1 / var5 : var0;
   }

   private Framing() {
   }
}
