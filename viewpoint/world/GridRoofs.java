package viewpoint.world;

import java.util.Arrays;

public final class GridRoofs {
   private static final int POINTS = 9;
   private final int width;
   private final int height;
   private final int[][] squares;
   private final byte[][] sources;
   private final float[][] heights;

   private GridRoofs(int var1, int var2, int[][] var3, byte[][] var4, float[][] var5) {
      this.width = var1;
      this.height = var2;
      this.squares = var3;
      this.sources = var4;
      this.heights = var5;
   }

   public static GridRoofs build(RoofBuildings.Levels var0) {
      return of(RoofBuildings.build(var0));
   }

   static GridRoofs of(RoofSurface[] var0) {
      int var1 = var0[0].width;
      int var2 = var0[0].height;
      int[][] var3 = new int[var0.length][];
      byte[][] var4 = new byte[var0.length][];
      float[][] var5 = new float[var0.length][];

      for (int var6 = 0; var6 < var0.length; var6++) {
         RoofSurface var7 = var0[var6];
         byte var8 = 0;

         for (int var9 = 0; var9 < var1 * var2; var9++) {
            var8 += var7.source(var9 % var1, var9 / var1) == 0 ? 0 : 1;
         }

         if (var8 != 0) {
            var3[var6] = new int[var8];
            var4[var6] = new byte[var8];
            var5[var6] = new float[var8 * 9];
            int var13 = 0;
            int var10 = 0;

            while (var13 < var1 * var2) {
               byte var11 = var7.source(var13 % var1, var13 / var1);
               if (var11 != 0) {
                  var3[var6][var10] = var13;
                  var4[var6][var10] = var11;

                  for (int var12 = 0; var12 < 9; var12++) {
                     var5[var6][var10 * 9 + var12] = var7.height(var13 % var1, var13 / var1, var12);
                  }

                  var10++;
               }

               var13++;
            }
         }
      }

      return new GridRoofs(var1, var2, var3, var4, var5);
   }

   public RoofSurface window(int var1, int var2, int var3, int var4, int var5) {
      RoofSurface var6 = RoofSurface.empty(var4, var5);
      int[] var7 = var1 >= 0 && var1 < this.squares.length ? this.squares[var1] : null;
      int var8 = Math.max(var2, 0);
      int var9 = Math.min(var2 + var4, this.width);
      float[] var10 = new float[9];

      for (int var11 = Math.max(var3, 0); var7 != null && var11 < Math.min(var3 + var5, this.height) && var8 < var9; var11++) {
         int var12 = Arrays.binarySearch(var7, var11 * this.width + var8);

         for (int var13 = var12 >= 0 ? var12 : -var12 - 1; var13 < var7.length && var7[var13] < var11 * this.width + var9; var13++) {
            System.arraycopy(this.heights[var1], var13 * 9, var10, 0, 9);
            var6.set((var11 - var3) * var4 + var7[var13] % this.width - var2, this.sources[var1][var13], var10);
         }
      }

      return var6;
   }

   public long bytes() {
      long var1 = 64L;

      for (int[] var6 : this.squares) {
         var1 += var6 == null ? 8L : 48L + var6.length * 41L;
      }

      return var1;
   }
}
