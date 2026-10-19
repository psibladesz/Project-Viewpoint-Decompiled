package viewpoint.world;

import java.util.Arrays;

final class RoofShallow {
   private static final int NEAR = 16;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};

   static boolean[] level(int var0, int var1, RoofPiece[] var2, byte[] var3) {
      boolean[] var4 = new boolean[var3.length];
      int[] var5 = new int[var3.length];
      int[] var6 = new int[var3.length];
      int[] var7 = new int[var3.length];
      int[] var8 = new int[var3.length];
      int[] var9 = new int[128];

      for (int var10 = 0; var10 < var3.length; var10++) {
         if (var2[var10] != null && var2[var10].porch() && var2[var10].kind != 7 && var2[var10].kind != 5 && (var3[var10] & 4) != 0) {
            var5[var10] = pitch(var0, var1, var2, var3, var10, var6, var7, var8, var9);
            var4[var10] = var5[var10] > 0;
         }
      }

      RoofPiece[] var13 = (RoofPiece[])var2.clone();

      for (int var11 = 0; var11 < var3.length; var11++) {
         RoofPiece var12 = var2[var11];
         var13[var11] = var4[var11] ? new RoofPiece(var12.kind, var12.facing, var12.other, var5[var11], step(var0, var2, var4, var11, var5[var11])) : var12;
      }

      System.arraycopy(var13, 0, var2, 0, var2.length);
      return var4;
   }

   private static int step(int var0, RoofPiece[] var1, boolean[] var2, int var3, int var4) {
      RoofPiece var5 = var1[var3];
      int var6 = var3;

      int var7;
      for (var7 = 0; var5.kind == 0 && var7 < 16; var7++) {
         int var8 = var6 % var0 + DX[(var5.facing + 2) % 4];
         int var9 = var6 / var0 + DZ[(var5.facing + 2) % 4];
         int var10 = var9 * var0 + var8;
         if (var8 < 0 || var9 < 0 || var8 >= var0 || var10 >= var1.length || !var2[var10] || !sameSlope(var1[var10], var5)) {
            break;
         }

         var6 = var10;
      }

      RoofPiece var11 = var1[var6];
      int var12 = Math.round((float)var11.step * var4 / var11.perStorey) - var7;
      return Math.max(1, var12);
   }

   private static boolean sameSlope(RoofPiece var0, RoofPiece var1) {
      return var0.kind == 0 && var0.facing == var1.facing;
   }

   private static int pitch(int var0, int var1, RoofPiece[] var2, byte[] var3, int var4, int[] var5, int[] var6, int[] var7, int[] var8) {
      int var9 = 0;
      int var10 = 0;
      int var11 = var4 + 1;
      var7[var4] = var11;
      var5[var4] = 0;
      var6[var10++] = var4;
      Arrays.fill(var8, 0);

      while (var9 < var10) {
         int var12 = var6[var9++];
         RoofPiece var13 = var2[var12];
         if (var13 != null && var13.planar() && !var13.porch()) {
            return 0;
         }

         if (var13 != null && var13.porch() && var13.kind != 7 && var13.kind != 5) {
            var8[var13.perStorey]++;
         }

         for (int var14 = 0; var14 < 4 && var5[var12] < 16; var14++) {
            int var15 = var12 % var0 + DX[var14];
            int var16 = var12 / var0 + DZ[var14];
            int var17 = var16 * var0 + var15;
            if (var15 >= 0 && var16 >= 0 && var15 < var0 && var16 < var1 && var3[var17] != 0 && var7[var17] != var11) {
               var7[var17] = var11;
               var5[var17] = var5[var12] + 1;
               var6[var10++] = var17;
            }
         }
      }

      byte var19 = 0;

      for (int var20 = 1; var20 < var8.length; var20++) {
         var19 = (byte)(var8[var20] > var8[var19] ? var20 : var19);
      }

      return var19;
   }

   private RoofShallow() {
   }
}
