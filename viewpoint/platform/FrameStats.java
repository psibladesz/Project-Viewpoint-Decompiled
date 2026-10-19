package viewpoint.platform;

import java.util.Arrays;

record FrameStats(float meanMs, float medianMs, float p99Ms, float p999Ms, float worstMs, float lowFps, float jitterMs, int spikes, int lastSecond) {
   private static final long SECOND = 1000000000L;

   static FrameStats of(FrameStats.Frames var0) {
      int var1 = var0.count();
      if (var1 == 0) {
         return null;
      }

      float[] var2 = Arrays.copyOf(var0.ms(), var1);
      double var3 = 0.0;
      double var5 = 0.0;
      byte var7 = 0;

      for (int var8 = 0; var8 < var1; var8++) {
         var3 += var2[var8];
         var5 += var8 > 0 ? Math.abs(var2[var8] - var2[var8 - 1]) : 0.0;
         var7 += var0.now() - var0.at()[var8] <= 1000000000L ? 1 : 0;
      }

      float[] var16 = (float[])var2.clone();
      Arrays.sort(var16);
      float var9 = rank(var16, 50.0);
      float var10 = rank(var16, 99.0);
      byte var11 = 0;

      for (float var15 : var2) {
         var11 += var15 > 2.0F * var9 ? 1 : 0;
      }

      return new FrameStats(
         (float)(var3 / var1),
         var9,
         var10,
         rank(var16, 99.9),
         var16[var1 - 1],
         var10 > 0.0F ? 1000.0F / var10 : 0.0F,
         var1 > 1 ? (float)(var5 / (var1 - 1)) : 0.0F,
         var11,
         var7
      );
   }

   static float rank(float[] var0, double var1) {
      if (var0.length == 0) {
         return 0.0F;
      }

      int var3 = (int)Math.ceil(var0.length * var1 / 100.0);
      return var0[Math.min(var0.length, Math.max(1, var3)) - 1];
   }

   record Frames(float[] ms, long[] at, int count, long now) {
   }
}
