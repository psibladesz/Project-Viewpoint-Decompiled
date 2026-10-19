package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Arrays;

final class EyeAdaptation {
   static final int BINS = 64;
   static final float LOWEST = -24.0F;
   static final float HIGHEST = 8.0F;
   static final float DARK_OUT = 0.1F;
   static final float BRIGHT_OUT = 0.1F;
   static final float EASE_STOPS = 1.5F;
   private final double[] weights = new double[64];
   private final double[] sums = new double[64];

   float mean(FloatBuffer var1, int var2) {
      Arrays.fill(this.weights, 0.0);
      Arrays.fill(this.sums, 0.0);
      double var3 = 0.0;

      for (int var5 = 0; var5 < var2; var5++) {
         float var6 = var1.get(2 * var5);
         float var7 = var1.get(2 * var5 + 1);
         if (var7 > 0.0F && !Float.isNaN(var6)) {
            float var8 = Math.max(-24.0F, Math.min(8.0F, var6));
            int var9 = Math.min(63, (int)((var8 - -24.0F) / 32.0F * 64.0F));
            this.weights[var9] = this.weights[var9] + var7;
            this.sums[var9] = this.sums[var9] + var7 * var8;
            var3 += var7;
         }
      }

      return this.trimmedMean(var3);
   }

   private float trimmedMean(double var1) {
      if (var1 <= 0.0) {
         return Float.NaN;
      }

      double var3 = var1 * 0.1F;
      double var5 = var1 * 0.8999999985098839;
      double var7 = 0.0;
      double var9 = 0.0;
      double var11 = 0.0;

      for (int var13 = 0; var13 < 64; var13++) {
         double var14 = this.weights[var13];
         if (!(var14 <= 0.0)) {
            double var16 = Math.min(var7 + var14, var5) - Math.max(var7, var3);
            if (var16 > 0.0) {
               var9 += var16;
               var11 += this.sums[var13] / var14 * var16;
            }

            var7 += var14;
         }
      }

      return var9 > 0.0 ? (float)(var11 / var9) : Float.NaN;
   }

   static float aim(float var0, float var1, float var2) {
      return Math.max(-var2, Math.min(var2, var1 - var0));
   }

   static float step(float var0, float var1, float var2, float var3, float var4) {
      float var5 = var1 - var0;
      float var6 = (var5 > 0.0F ? var3 : var4) * Math.max(var2, 0.0F);
      float var7 = Math.min(var6, Math.abs(var5) * Math.min(1.0F, var6 / 1.5F));
      return var0 + Math.signum(var5) * var7;
   }
}
