package viewpoint.world;

import java.util.Arrays;

final class EarClip {
   static int[] triangulate(float[] var0) {
      int var1 = var0.length / 2;
      int[] var2 = new int[var1];
      boolean var3 = signedArea(var0) > 0.0F;

      for (int var4 = 0; var4 < var1; var4++) {
         var2[var4] = var3 ? var4 : var1 - 1 - var4;
      }

      int[] var13 = new int[(var1 - 2) * 3];
      int var5 = 0;
      int var6 = var1;
      int var7 = 0;
      int var8 = 0;

      while (var6 > 3 && var8 < var6) {
         int var9 = var2[(var7 + var6 - 1) % var6];
         int var10 = var2[var7 % var6];
         int var11 = var2[(var7 + 1) % var6];
         if (isEar(var0, var2, var6, var9, var10, var11)) {
            var13[var5++] = var9;
            var13[var5++] = var10;
            var13[var5++] = var11;
            int var12 = var7 % var6;
            System.arraycopy(var2, var12 + 1, var2, var12, var6 - var12 - 1);
            var6--;
            var8 = 0;
         } else {
            var7++;
            var8++;
         }
      }

      for (int var18 = 1; var18 + 1 < var6; var18++) {
         var13[var5++] = var2[0];
         var13[var5++] = var2[var18];
         var13[var5++] = var2[var18 + 1];
      }

      return var5 == var13.length ? var13 : Arrays.copyOf(var13, var5);
   }

   private static boolean isEar(float[] var0, int[] var1, int var2, int var3, int var4, int var5) {
      if (cross(var0, var3, var4, var5) <= 0.0F) {
         return false;
      }

      for (int var6 = 0; var6 < var2; var6++) {
         int var7 = var1[var6];
         if (var7 != var3
            && var7 != var4
            && var7 != var5
            && cross(var0, var3, var4, var7) >= 0.0F
            && cross(var0, var4, var5, var7) >= 0.0F
            && cross(var0, var5, var3, var7) >= 0.0F) {
            return false;
         }
      }

      return true;
   }

   private static float cross(float[] var0, int var1, int var2, int var3) {
      return (var0[var2 * 2] - var0[var1 * 2]) * (var0[var3 * 2 + 1] - var0[var1 * 2 + 1])
         - (var0[var2 * 2 + 1] - var0[var1 * 2 + 1]) * (var0[var3 * 2] - var0[var1 * 2]);
   }

   private static float signedArea(float[] var0) {
      float var1 = 0.0F;
      int var2 = var0.length / 2;
      int var3 = 0;

      for (int var4 = var2 - 1; var3 < var2; var4 = var3++) {
         var1 += var0[var4 * 2] * var0[var3 * 2 + 1] - var0[var3 * 2] * var0[var4 * 2 + 1];
      }

      return var1;
   }

   private EarClip() {
   }
}
