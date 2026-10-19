package viewpoint.models;

import java.util.Arrays;

final class CorpsePicks {
   static final float MARGIN = 0.15F;
   static final float FRONT = 2.0F;
   static final int SWAPS_PER_FRAME = 4;
   private static long[] order = new long[64];
   private static float[] scores = new float[64];

   static void pick(CorpsePicks.Bodies var0, CorpsePicks.Limits var1) {
      int var2 = rank(var0, var1);
      int var3 = 0;

      for (int var4 = 0; var4 < var0.count; var4++) {
         var0.picked[var4] = var0.picked[var4] && allowed(var0, var4, var1);
         var3 += var0.picked[var4] ? 1 : 0;
      }

      for (int var6 = var2 - 1; var6 >= 0 && var3 > var1.most(); var6--) {
         int var5 = (int)order[var6];
         if (var0.picked[var5]) {
            var0.picked[var5] = false;
            var3--;
         }
      }

      for (int var7 = 0; var7 < var2 && var3 < var1.most(); var7++) {
         int var8 = (int)order[var7];
         if (!var0.picked[var8]) {
            var0.picked[var8] = true;
            var3++;
         }
      }

      swap(var0, var2, var1);
      progress(var0, var1);
   }

   private static int rank(CorpsePicks.Bodies var0, CorpsePicks.Limits var1) {
      if (order.length < var0.count) {
         order = new long[var0.count * 2];
         scores = new float[var0.count * 2];
      }

      int var2 = 0;

      for (int var3 = 0; var3 < var0.count; var3++) {
         if (allowed(var0, var3, var1)) {
            scores[var3] = score(var0, var3, var1);
            order[var2++] = (long)Float.floatToIntBits(scores[var3]) << 32 | var3;
         }
      }

      Arrays.sort(order, 0, var2);
      return var2;
   }

   private static boolean allowed(CorpsePicks.Bodies var0, int var1, CorpsePicks.Limits var2) {
      return var0.eligible[var1] && var0.distance[var1] < var2.limit();
   }

   private static float score(CorpsePicks.Bodies var0, int var1, CorpsePicks.Limits var2) {
      return (var0.distance[var1] + 2.0F) * (float)Math.pow(var0.random[var1], var2.randomness());
   }

   private static void swap(CorpsePicks.Bodies var0, int var1, CorpsePicks.Limits var2) {
      int var3 = 0;

      for (int var4 = 0; var4 < 4; var4++) {
         while (var3 < var1 && var0.picked[(int)order[var3]]) {
            var3++;
         }

         int var5 = worstPicked(var0);
         if (var3 == var1 || var5 < 0) {
            return;
         }

         int var6 = (int)order[var3];
         if (scores[var6] * 1.15F >= scores[var5]) {
            return;
         }

         var0.picked[var5] = false;
         var0.picked[var6] = true;
      }
   }

   private static int worstPicked(CorpsePicks.Bodies var0) {
      int var1 = -1;

      for (int var2 = 0; var2 < var0.count; var2++) {
         if (var0.picked[var2] && (var1 < 0 || scores[var2] > scores[var1])) {
            var1 = var2;
         }
      }

      return var1;
   }

   private static void progress(CorpsePicks.Bodies var0, CorpsePicks.Limits var1) {
      for (int var2 = 0; var2 < var0.count; var2++) {
         float var3 = var0.progress[var2];
         if (!var0.ready[var2]) {
            var3 = 0.0F;
         } else if (var0.picked[var2]) {
            var3 = var0.fresh[var2] && var3 == 0.0F ? 1.0F : Math.min(1.0F, var3 + var1.step());
         } else {
            var3 = Math.max(0.0F, var3 - var1.step());
         }

         var0.progress[var2] = var3;
         var0.share[var2] = var0.eligible[var2] ? Math.min(distanceShare(var0.distance[var2], var1), var3) : 0.0F;
      }
   }

   static float distanceShare(float var0, CorpsePicks.Limits var1) {
      if (var1.band() <= 0.0F) {
         return var0 < var1.limit() ? 1.0F : 0.0F;
      } else {
         return Math.max(0.0F, Math.min(1.0F, (var1.limit() - var0) / var1.band()));
      }
   }

   private CorpsePicks() {
   }

   static final class Bodies {
      int count;
      float[] distance = new float[0];
      float[] random = new float[0];
      float[] progress = new float[0];
      float[] share = new float[0];
      boolean[] ready = new boolean[0];
      boolean[] fresh = new boolean[0];
      boolean[] eligible = new boolean[0];
      boolean[] picked = new boolean[0];

      void size(int var1) {
         if (this.distance.length < var1) {
            int var2 = Math.max(var1, this.distance.length * 2);
            this.distance = Arrays.copyOf(this.distance, var2);
            this.random = Arrays.copyOf(this.random, var2);
            this.progress = Arrays.copyOf(this.progress, var2);
            this.share = Arrays.copyOf(this.share, var2);
            this.ready = Arrays.copyOf(this.ready, var2);
            this.fresh = Arrays.copyOf(this.fresh, var2);
            this.eligible = Arrays.copyOf(this.eligible, var2);
            this.picked = Arrays.copyOf(this.picked, var2);
         }

         this.count = var1;
      }
   }

   record Limits(float limit, float band, int most, float randomness, float step) {
   }
}
