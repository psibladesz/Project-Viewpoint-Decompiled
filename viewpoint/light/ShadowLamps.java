package viewpoint.light;

import java.util.Arrays;

final class ShadowLamps {
   static final float LINK = 12.0F;
   static final float STICK = 3.0F;
   static final float HANDOVER = 2.0F;
   private static final int MAX = 48;
   private static final int[] group = new int[48];
   private static final int[] order = new int[48];
   private static final float[] groupDistance = new float[48];
   private static final long[] cast = new long[48];
   private static int castCount;

   static int choose(int var0, int[] var1, int[] var2, int[] var3, float[] var4, long[] var5, float[] var6, int var7, boolean[] var8, float[] var9) {
      Arrays.fill(var8, 0, var0, false);
      Arrays.fill(var9, 0, var0, 0.0F);
      group(var0, var1, var2, var3, var5, var6);
      sort(var0, var4, var5);
      int var10 = 0;
      int var11 = -1;

      int var12;
      for (var12 = 0; var12 < var0 && var10 < var7; var12++) {
         int var13 = order[var12];
         var8[var13] = true;
         var9[var13] = 1.0F;
         var11 = group[var13];
         var10++;
      }

      handOver(var0, var12, var11, var10 == var7, var8, var9);
      castCount = 0;

      for (int var14 = 0; var14 < var0; var14++) {
         if (var8[var14] && !castLast(var5[group[var14]])) {
            cast[castCount++] = var5[group[var14]];
         }
      }

      return var10;
   }

   private static void group(int var0, int[] var1, int[] var2, int[] var3, long[] var4, float[] var5) {
      for (int var6 = 0; var6 < var0; order[var6] = var6++) {
         group[var6] = -1;
      }

      for (int var9 = 1; var9 < var0; var9++) {
         for (int var7 = var9; var7 > 0 && var4[order[var7]] < var4[order[var7 - 1]]; var7--) {
            swap(var7, var7 - 1);
         }
      }

      for (int var10 = 0; var10 < var0; var10++) {
         int var11 = order[var10];
         if (group[var11] < 0) {
            groupDistance[var11] = Float.MAX_VALUE;

            for (int var8 = 0; var8 < var0; var8++) {
               if (group[var8] < 0 && linked(var11, var8, var1, var2, var3)) {
                  group[var8] = var11;
                  groupDistance[var11] = Math.min(groupDistance[var11], var5[var8]);
               }
            }

            if (castLast(var4[var11])) {
               groupDistance[var11] = groupDistance[var11] - 3.0F;
            }
         }
      }
   }

   private static void sort(int var0, float[] var1, long[] var2) {
      for (int var3 = 1; var3 < var0; var3++) {
         for (int var4 = var3; var4 > 0 && before(order[var4], order[var4 - 1], var1, var2); var4--) {
            swap(var4, var4 - 1);
         }
      }
   }

   private static boolean before(int var0, int var1, float[] var2, long[] var3) {
      if (group[var0] != group[var1]) {
         float var4 = groupDistance[group[var0]];
         float var5 = groupDistance[group[var1]];
         return var4 != var5 ? var4 < var5 : group[var0] < group[var1];
      } else {
         return var2[var0] != var2[var1] ? var2[var0] > var2[var1] : var3[var0] < var3[var1];
      }
   }

   private static void handOver(int var0, int var1, int var2, boolean var3, boolean[] var4, float[] var5) {
      if (var3 && var2 >= 0) {
         for (int var6 = var1; var6 < var0; var6++) {
            int var7 = group[order[var6]];
            if (var7 != var2) {
               float var8 = Math.min(1.0F, (groupDistance[var7] - groupDistance[var2]) / 2.0F);

               for (int var9 = 0; var9 < var0; var9++) {
                  if (var4[var9] && group[var9] == var2) {
                     var5[var9] = Math.max(0.0F, var8);
                  }
               }

               return;
            }
         }
      }
   }

   private static boolean linked(int var0, int var1, int[] var2, int[] var3, int[] var4) {
      float var5 = var2[var0] - var2[var1];
      float var6 = var3[var0] - var3[var1];
      float var7 = (var4[var0] - var4[var1]) * 2.0F;
      return var5 * var5 + var6 * var6 + var7 * var7 <= 144.0F;
   }

   private static boolean castLast(long var0) {
      for (int var2 = 0; var2 < castCount; var2++) {
         if (cast[var2] == var0) {
            return true;
         }
      }

      return false;
   }

   private static void swap(int var0, int var1) {
      int var2 = order[var0];
      order[var0] = order[var1];
      order[var1] = var2;
   }

   static void reset() {
      castCount = 0;
   }

   private ShadowLamps() {
   }
}
