package viewpoint.far;

final class Dissolve {
   static final long APPEAR = 600000000L;
   static final long CROSS = 400000000L;
   static final long LEAVE = 600000000L;
   private long start;
   private long nanos;

   void begin(long var1, long var3, float var5) {
      this.nanos = var3;
      this.start = var1 - (long)(var5 * (float)var3);
   }

   float progress(long var1) {
      if (this.nanos <= 0L) {
         return 1.0F;
      }

      float var3 = (float)(var1 - this.start) / (float)this.nanos;
      if (var3 < 1.0F) {
         return Math.max(var3, 0.0F);
      }

      this.nanos = 0L;
      return 1.0F;
   }

   static float top(float var0) {
      return var0 < 1.0F ? var0 : 2.0F;
   }

   static byte share(float var0) {
      return (byte)Math.min(255, Math.round(var0 * 255.0F));
   }
}
