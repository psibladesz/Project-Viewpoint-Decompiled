package viewpoint.world;

final class RoofSettle {
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   private static final float WHOLE = 0.01F;

   static RoofPiece[] settle(int var0, int var1, RoofPiece[] var2, byte[] var3) {
      RoofPiece[] var4 = (RoofPiece[])var2.clone();

      for (int var5 = 0; var5 < var2.length; var5++) {
         RoofPiece var6 = var2[var5];
         if (var6 != null && var6.kind == 0 && !same(var2, next(var0, var1, var5, back(var6)), var6)) {
            int var7 = var5;
            int var8 = 1;

            for (int var9 = next(var0, var1, var5, var6.facing); same(var2, var9, var6); var9 = next(var0, var1, var9, var6.facing)) {
               var7 = var9;
               var8++;
            }

            int var10 = next(var0, var1, var7, var6.facing);
            if (var10 >= 0 && var2[var10] == null && (var3[var10] & 3) == 2 && (var8 == 1 || !spread(var0, var1, var2, var4, var5, var8)) && var6.step >= 2) {
               var4[var7] = slope(var6, var6.perStorey, var6.step - 1);
            }
         }
      }

      return var4;
   }

   private static boolean spread(int var0, int var1, RoofPiece[] var2, RoofPiece[] var3, int var4, int var5) {
      RoofPiece var6 = var2[var4];
      int var7 = next(var0, var1, var4, back(var6));
      RoofPiece var8 = var7 < 0 ? null : var2[var7];
      if (var8 != null && var8.planar()) {
         float var9 = var8.height(DX[var6.facing] * 0.5F, DZ[var6.facing] * 0.5F);
         float var10 = var5 * 2.4494896F / var9;
         int var11 = Math.round(var10);
         if (!(var9 <= 0.0F) && !(Math.abs(var10 - var11) > 0.01F) && var11 >= 1 && var11 <= 127) {
            int var12 = 0;

            for (int var13 = var4; var12 < var5; var13 = next(var0, var1, var13, var6.facing)) {
               var3[var13] = slope(var6, var11, var5 - var12);
               var12++;
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean same(RoofPiece[] var0, int var1, RoofPiece var2) {
      RoofPiece var3 = var1 < 0 ? null : var0[var1];
      return var3 != null && var3.kind == 0 && var3.facing == var2.facing && var3.perStorey == var2.perStorey && var3.step == var2.step;
   }

   private static RoofPiece slope(RoofPiece var0, int var1, int var2) {
      return new RoofPiece(0, var0.facing, -1, var1, var2);
   }

   private static int back(RoofPiece var0) {
      return (var0.facing + 2) % 4;
   }

   private static int next(int var0, int var1, int var2, int var3) {
      int var4 = var2 % var0 + DX[var3];
      int var5 = var2 / var0 + DZ[var3];
      return var4 >= 0 && var5 >= 0 && var4 < var0 && var5 < var1 ? var5 * var0 + var4 : -1;
   }

   private RoofSettle() {
   }
}
