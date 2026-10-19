package viewpoint.environment;

import viewpoint.render.SceneData;
import zombie.core.Core;

public final class Lightning {
   private static final int KEPT = 4;
   private static final float LASTS = 1.2F;
   private static final int MOST_STROKES = 4;
   private static final float STROKE_GAP = 0.15F;
   private static final float STROKE_FADE = 0.045F;
   private static final float GLOW = 0.25F;
   private static final float GLOW_FADE = 0.3F;
   private static final float HEIGHT = 900.0F;
   private static final int[] strikeX = new int[4];
   private static final int[] strikeY = new int[4];
   private static final long[] struckNanos = new long[4];
   private static int next;

   public static void struck(int var0, int var1, boolean var2) {
      if (var2 && !Core.getInstance().getOptionDisableLightningDuringStorms()) {
         flash(var0, var1);
      }
   }

   static void flash(int var0, int var1) {
      strikeX[next] = var0;
      strikeY[next] = var1;
      struckNanos[next] = System.nanoTime();
      next = (next + 1) % 4;
   }

   public static void snapshot(SceneData var0, float var1, float var2) {
      long var3 = System.nanoTime();
      float var5 = 0.0F;

      for (int var6 = 0; var6 < 4; var6++) {
         float var7 = struckNanos[var6] == 0L ? 1.2F : (float)(var3 - struckNanos[var6]) * 1.0E-9F;
         if (!(var7 >= 1.2F)) {
            float var8 = brightness(var7, seed(var6));
            if (var8 > var5) {
               var5 = var8;
               var0.lightningX = -(strikeX[var6] - var1);
               var0.lightningZ = -(strikeY[var6] - var2);
            }
         }
      }

      var0.lightningY = 900.0F;
      var0.lightning = var5;
   }

   static float brightness(float var0, long var1) {
      if (!(var0 < 0.0F) && !(var0 >= 1.2F)) {
         int var3 = 2 + (int)(unit(var1, 0) * 3.0F);
         float var4 = 0.0F;
         float var5 = 1.0F;
         float var6 = 0.0F;

         for (int var7 = 0; var7 < var3 && var4 <= var0; var7++) {
            var6 = Math.max(var6, var5 * (float)Math.exp(-(var0 - var4) / 0.045F));
            var4 += 0.15F * (0.3F + 0.7F * unit(var1, var7 + 1));
            var5 *= 0.55F + 0.3F * unit(var1, var7 + 11);
         }

         return Math.max(var6, 0.25F * (float)Math.exp(-var0 / 0.3F)) * (1.0F - var0 / 1.2F);
      } else {
         return 0.0F;
      }
   }

   private static long seed(int var0) {
      return strikeX[var0] * 73856093L ^ strikeY[var0] * 19349663L ^ struckNanos[var0];
   }

   private static float unit(long var0, int var2) {
      return SkyWeather.random(var0, var2 + 40);
   }

   private Lightning() {
   }
}
