package viewpoint.world;

final class RoofCaps {
   private static final float LID_EPS = 0.01F;

   static void uncap(int var0, int var1, RoofPiece[] var2, byte[] var3, RoofSurface var4) {
      boolean[] var5 = new boolean[var3.length];

      for (int var6 = 0; var6 < var3.length; var6++) {
         int var7 = var6 % var0;
         int var8 = var6 / var0;
         if ((var3[var6] & 32) != 0 && !planar(var2[var6]) && var4.built(var7, var8)) {
            float var9 = Float.NEGATIVE_INFINITY;

            for (int var10 = 0; var10 < 9; var10++) {
               var9 = Math.max(var9, var4.height(var7, var8, var10));
            }

            if (var9 <= 2.4594896F) {
               var4.lid(var6, 2.4494896F);
               var5[var6] = true;
            }
         }
      }

      float[] var12 = new float[9];

      for (int var13 = 0; var13 < var3.length; var13++) {
         int var14 = var13 % var0;
         int var15 = var13 / var0;
         boolean var16 = false;

         for (int var11 = 0; var11 < 9 && !planar(var2[var13]) && var4.built(var14, var15); var11++) {
            var12[var11] = var4.height(var14, var15, var11);
            if (var12[var11] < 2.4494896F && besideLeft(var0, var1, var5, var14, var15, var11)) {
               var12[var11] = 2.4494896F;
               var16 = true;
            }
         }

         if (var16) {
            var4.set(var13, var4.source(var14, var15), var12);
         }
      }
   }

   private static boolean besideLeft(int var0, int var1, boolean[] var2, int var3, int var4, int var5) {
      int var6 = var5 % 3 - 1;
      int var7 = var5 / 3 - 1;

      for (int var8 = Math.min(0, var7); var8 <= Math.max(0, var7); var8++) {
         for (int var9 = Math.min(0, var6); var9 <= Math.max(0, var6); var9++) {
            int var10 = var3 + var9;
            int var11 = var4 + var8;
            if ((var9 | var8) != 0 && var10 >= 0 && var11 >= 0 && var10 < var0 && var11 < var1 && var2[var11 * var0 + var10]) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean planar(RoofPiece var0) {
      return var0 != null && var0.planar();
   }

   private RoofCaps() {
   }
}
