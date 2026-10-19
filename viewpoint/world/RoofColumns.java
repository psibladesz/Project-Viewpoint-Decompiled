package viewpoint.world;

import java.util.Arrays;

final class RoofColumns {
   final int width;
   final int height;
   final int originX;
   final int originZ;
   final int reference;
   final boolean[] in;
   final boolean[] tiled;
   final boolean[] porch;
   final RoofPiece[] pieces;
   final float[] floor;
   final float[] cap;
   final byte[] level;
   final byte[] pieceLevel;

   private RoofColumns(int var1, int var2, int var3, int var4, int var5) {
      int var6 = var1 * var2;
      this.width = var1;
      this.height = var2;
      this.originX = var3;
      this.originZ = var4;
      this.reference = var5;
      this.in = new boolean[var6];
      this.tiled = new boolean[var6];
      this.porch = new boolean[var6];
      this.pieces = new RoofPiece[var6];
      this.floor = new float[var6];
      this.cap = new float[var6];
      this.level = new byte[var6];
      this.pieceLevel = new byte[var6];
      Arrays.fill(this.floor, Float.NaN);
      Arrays.fill(this.cap, Float.POSITIVE_INFINITY);
   }

   static RoofColumns of(RoofBuildings.Levels var0, int[] var1, int var2, int var3, int var4, int var5, int var6) {
      int var7 = Math.max(0, var3 - 1);
      int var8 = Math.max(0, var4 - 1);
      int var9 = Math.min(var0.width() - 1, var5 + 1) - var7 + 1;
      int var10 = Math.min(var0.height() - 1, var6 + 1) - var8 + 1;
      int var11 = Integer.MAX_VALUE;

      for (int var12 = var8; var12 < var8 + var10; var12++) {
         for (int var13 = var7; var13 < var7 + var9; var13++) {
            int var14 = var12 * var0.width() + var13;
            var11 = var1[var14] == var2 ? Math.min(var11, drawn(var0, var14)) : var11;
         }
      }

      RoofColumns var16 = new RoofColumns(var9, var10, var7, var8, var11);

      for (int var17 = 0; var17 < var10; var17++) {
         for (int var18 = 0; var18 < var9; var18++) {
            int var15 = (var8 + var17) * var0.width() + var7 + var18;
            if (var1[var15] == var2) {
               var16.column(var0, var15, var17 * var9 + var18);
            }
         }
      }

      return var16;
   }

   int grid(int var1, int var2) {
      return (this.originZ + var2 / this.width) * var1 + this.originX + var2 % this.width;
   }

   private void column(RoofBuildings.Levels var1, int var2, int var3) {
      int var4 = var1.room().length;
      byte var5 = -1;
      int var6 = taken(var1, var2);

      for (int var7 = 0; var7 < var4; var7++) {
         var5 = (byte)(var1.room()[var7][var2] ? var7 : var5);
         this.tiled[var3] = this.tiled[var3] | var1.tiled()[var7][var2];
      }

      this.in[var3] = true;
      this.floor[var3] = var5 < 0 ? Float.NaN : (var5 - this.reference) * 2.4494896F;
      this.level[var3] = (byte)drawn(var1, var2);
      this.porch[var3] = porchLevel(var1, var2) >= 0;

      for (int var9 = var5 + 1; var5 >= 0 && var9 < var4 && !var1.tiled()[var9][var2]; var9++) {
         if (var1.floor()[var9][var2] || var1.flat()[var9][var2]) {
            this.cap[var3] = (var9 - this.reference) * 2.4494896F;
            break;
         }
      }

      if (var6 >= 0) {
         RoofPiece var10 = var1.pieces()[var6][var2];
         boolean var8 = var10.kind == 7 || var10.step + var10.perStorey * (var6 - this.reference) <= 127;
         this.pieces[var3] = var8 ? RoofLevels.lowered(var10, var6 - this.reference) : null;
         this.pieceLevel[var3] = (byte)var6;
      }
   }

   private static int taken(RoofBuildings.Levels var0, int var1) {
      byte var2 = -1;
      byte var3 = -1;

      for (int var4 = 0; var4 < var0.room().length; var4++) {
         RoofPiece var5 = var0.pieces()[var4][var1];
         var3 = (byte)(var5 != null && var5.planar() ? var4 : var3);
         var2 = (byte)(var5 != null && var5.planar() && !var5.porch() ? var4 : var2);
      }

      return var2 >= 0 ? var2 : var3;
   }

   private static int drawn(RoofBuildings.Levels var0, int var1) {
      byte var2 = -1;
      int var3 = taken(var0, var1);
      int var4 = porchLevel(var0, var1);

      for (int var5 = 0; var5 < var0.room().length; var5++) {
         var2 = (byte)(var0.room()[var5][var1] ? var5 : var2);
      }

      if (var3 >= 1 && MapRoofs.carries(var0.pieces()[var3][var1], var0.floor()[var3 - 1][var1], var0.room()[var3 - 1][var1])) {
         return var3 - 1;
      } else {
         return var3 >= 0 ? var3 : (var4 >= 0 ? var4 : var2 + 1);
      }
   }

   private static int porchLevel(RoofBuildings.Levels var0, int var1) {
      byte var2 = -1;

      for (int var3 = var0.room().length - 1; var3 >= 0; var3--) {
         if (var0.room()[var3][var1] || var0.tiled()[var3][var1]) {
            return -1;
         }

         var2 = (byte)(var0.porch()[var3][var1] ? var3 : var2);
      }

      return var2;
   }
}
