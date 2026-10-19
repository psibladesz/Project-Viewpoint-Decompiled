package viewpoint.far;

import java.util.Arrays;

public final class FarCell {
   public static final int CHUNKS = 32;
   static final int STEPS = 8;
   static final int TREE_SHORTS = 4;
   static final int TURN_SHIFT = 8;
   private static final int GRASS = 5135133;
   final int cellX;
   final int cellY;
   public final short[] height = new short[65536];
   public final int[] top = new int[65536];
   public final int[] side = new int[65536];
   public float maxHeight;
   byte[] biomes;
   short[] trees;
   int[] treesBelow;

   private FarCell(int var1, int var2) {
      this.cellX = var1;
      this.cellY = var2;
   }

   static FarCell read(
      int var0, int var1, byte[] var2, int var3, int var4, FarTiles.Tile[] var5, int[] var6, byte[] var7, BiomeTrees.Table var8, BiomeTrees.Legend var9
   ) {
      FarCell var10 = new FarCell(var0, var1);
      float[] var11 = new float[65536];
      float[] var12 = new float[65536];
      FarCell.Trees var13 = new FarCell.Trees();
      BiomeTrees.Ground var14 = new BiomeTrees.Ground(0, 0, 256);
      int[] var15 = var10.top;
      int[] var16 = var10.side;
      boolean[] var17 = new boolean[65536];
      Arrays.fill(var15, -1);
      Arrays.fill(var16, -1);
      ShellMesher.read(var2, var3, var4, 0, 0, 32, (var8x, var9x, var10x, var11x) -> {
         if (!var14.add(var8x, var5, var9x, var10x, var11x) && var8x < var5.length) {
            if (var5[var8x].treeKind >= 0) {
               var13.add(var5[var8x], var9x, var10x, var11x, 50);
            } else {
               add(var5[var8x], var11x, var10x * 256 + var9x, var11, var12, var15, var16, var17);
            }
         }
      });
      var10.biomes = var7;
      var14.grow(var8, var7, var0, var1, var5, var9, null, (var4x, var5x, var6x) -> {
         if (!var13.on[var6x * 256 + var5x]) {
            int var7x = var0 * 256 + var5x;
            int var8x = var1 * 256 + var6x;
            var13.add(var4x, var5x, var6x, 0, var8 == null ? 50 : var8.turn(var7x, var8x));
         }
      }, null);
      finishSquares(var10, var6, var11, var15, var16);
      var10.treesBelow = new int[257];
      var10.trees = var13.ranked(var0, var1, var10.treesBelow);
      var10.maxHeight = Math.max(var10.maxHeight, var13.top);
      return var10;
   }

   private static void finishSquares(FarCell var0, int[] var1, float[] var2, int[] var3, int[] var4) {
      for (int var5 = 0; var5 < 65536; var5++) {
         int var6 = var1 != null ? var1[var5] & 16777215 : -1;
         int var7 = Math.min(32767, Math.round(var2[var5] * 8.0F));
         var0.height[var5] = (short)var7;
         var0.maxHeight = Math.max(var0.maxHeight, var7 / 8.0F);
         if (var3[var5] < 0) {
            var3[var5] = var6 >= 0 ? var6 : 5135133;
         }

         if (var4[var5] < 0) {
            var4[var5] = var3[var5];
         }
      }
   }

   private static void add(FarTiles.Tile var0, int var1, int var2, float[] var3, float[] var4, int[] var5, int[] var6, boolean[] var7) {
      if (var0.kind != 0) {
         float var8 = var1 + var0.height;
         if (var8 >= var3[var2]) {
            var3[var2] = var8;
            if (var0.colour >= 0) {
               var5[var2] = var0.colour;
            }
         }

         if (var0.kind != 1 && var0.colour >= 0) {
            boolean var9 = var0.kind == 2;
            if (var9 && (!var7[var2] || var8 >= var4[var2]) || !var9 && !var7[var2] && var8 >= var4[var2]) {
               var4[var2] = var8;
               var6[var2] = var0.colour;
               var7[var2] |= var9;
            }
         }
      }
   }

   static final class Trees {
      final boolean[] on = new boolean[65536];
      short[] list = new short[1024];
      int count;
      float top;

      void add(FarTiles.Tile var1, int var2, int var3, int var4, int var5) {
         if ((this.count + 1) * 4 > this.list.length) {
            this.list = Arrays.copyOf(this.list, this.list.length * 2);
         }

         int var6 = this.count++ * 4;
         this.list[var6] = (short)var2;
         this.list[var6 + 1] = (short)var3;
         this.list[var6 + 2] = (short)(var4 | var5 << 8);
         this.list[var6 + 3] = (short)var1.treeKind;
         this.top = Math.max(this.top, var4 + var1.height);
         this.on[var3 * 256 + var2] = true;
      }

      short[] ranked(int var1, int var2, int[] var3) {
         int[] var4 = new int[this.count];

         for (int var5 = 0; var5 < this.count; var5++) {
            short var6 = this.list[var5 * 4];
            short var7 = this.list[var5 * 4 + 1];
            var4[var5] = FarTrees.rank(var1 * 256 + var6, var2 * 256 + var7);
            var3[var4[var5] + 1]++;
         }

         for (int var8 = 0; var8 < 256; var8++) {
            var3[var8 + 1] = var3[var8 + 1] + var3[var8];
         }

         int[] var9 = Arrays.copyOf(var3, 256);
         short[] var10 = new short[this.count * 4];

         for (int var11 = 0; var11 < this.count; var11++) {
            System.arraycopy(this.list, var11 * 4, var10, var9[var4[var11]]++ * 4, 4);
         }

         return var10;
      }
   }
}
