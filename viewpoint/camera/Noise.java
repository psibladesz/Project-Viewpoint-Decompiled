package viewpoint.camera;

final class Noise {
   static float at(float var0, int var1) {
      int var2 = (int)Math.floor(var0);
      float var3 = var0 - var2;
      float var4 = var3 * var3 * var3 * (var3 * (var3 * 6.0F - 15.0F) + 10.0F);
      float var5 = gradient(var2, var1) * var3;
      float var6 = gradient(var2 + 1, var1) * (var3 - 1.0F);
      return 2.0F * (var5 + (var6 - var5) * var4);
   }

   static float layered(float var0, int var1) {
      return 0.75F * at(var0, var1) + 0.25F * at(var0 * 2.13F + 17.3F, var1 * 31 + 7);
   }

   private static float gradient(int var0, int var1) {
      int var2 = var0 * 668265261 ^ var1 * 374761393;
      var2 ^= var2 >>> 15;
      var2 *= 739982445;
      var2 ^= var2 >>> 12;
      var2 *= 695872825;
      var2 ^= var2 >>> 15;
      return (var2 & 65535) / 32767.5F - 1.0F;
   }

   private Noise() {
   }
}
