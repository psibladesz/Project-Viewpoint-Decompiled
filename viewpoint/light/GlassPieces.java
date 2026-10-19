package viewpoint.light;

import java.util.Arrays;

final class GlassPieces {
   static final float LINK = 0.05F;
   private static final int INDEX_BITS = 21;
   private static final int CELL_BITS = 14;
   private static final int BIAS = 8192;
   private static final long CELL_MASK = 16383L;
   final int count;
   final float[] weight;
   final float[] place;
   final float[] facing;
   private final LampGlass.Samples samples;
   private final int[] pieceOf;

   GlassPieces(LampGlass.Samples var1) {
      this.samples = var1;
      int var2 = Math.min(var1.count, 2097152);
      int[] var3 = linked(var1, var2);
      this.pieceOf = new int[var2];
      int var4 = 0;

      for (int var5 = 0; var5 < var2; var5++) {
         this.pieceOf[var5] = root(var3, var5) == var5 ? var4++ : -1;
      }

      for (int var11 = 0; var11 < var2; var11++) {
         this.pieceOf[var11] = this.pieceOf[root(var3, var11)];
      }

      this.count = var4;
      this.weight = new float[var4];
      this.place = new float[3 * var4];
      this.facing = new float[3 * var4];
      float[] var12 = var1.data;

      for (int var6 = 0; var6 < var2; var6++) {
         int var7 = this.pieceOf[var6];
         int var8 = var6 * 7;
         float var9 = var12[var8 + 6];
         this.weight[var7] = this.weight[var7] + var9;

         for (int var10 = 0; var10 < 3; var10++) {
            this.place[3 * var7 + var10] = this.place[3 * var7 + var10] + var12[var8 + var10] * var9;
            this.facing[3 * var7 + var10] = this.facing[3 * var7 + var10] + var12[var8 + 3 + var10] * var9;
         }
      }
   }

   float x(int var1) {
      return this.place[3 * var1] / this.weight[var1];
   }

   float y(int var1) {
      return this.place[3 * var1 + 1] / this.weight[var1];
   }

   float z(int var1) {
      return this.place[3 * var1 + 2] / this.weight[var1];
   }

   void spread(int var1, float var2, float var3, float var4, float[] var5, float[] var6, float[] var7) {
      float[] var8 = this.samples.data;

      for (int var9 = 0; var9 < this.pieceOf.length; var9++) {
         if (this.pieceOf[var9] == var1) {
            int var10 = var9 * 7;
            float var11 = var8[var10] - var2;
            float var12 = var8[var10 + 1] - var3;
            float var13 = var8[var10 + 2] - var4;
            float var14 = var8[var10 + 6];
            float var15 = var11 * var5[0] + var12 * var5[1] + var13 * var5[2];
            float var16 = var11 * var6[0] + var12 * var6[1] + var13 * var6[2];
            var7[0] += var14 * var15 * var15;
            var7[1] += var14 * var16 * var16;
         }
      }
   }

   private static int[] linked(LampGlass.Samples var0, int var1) {
      float[] var2 = var0.data;
      long[] var3 = new long[var1];
      int[] var4 = new int[var1];

      for (int var5 = 0; var5 < var1; var4[var5] = var5++) {
         int var6 = var5 * 7;
         var3[var5] = cell(cellOf(var2[var6]), cellOf(var2[var6 + 1]), cellOf(var2[var6 + 2])) << 21 | var5;
      }

      Arrays.sort(var3);

      for (int var15 = 0; var15 < var1; var15++) {
         int var16 = var15 * 7;
         int var7 = cellOf(var2[var16]);
         int var8 = cellOf(var2[var16 + 1]);
         int var9 = cellOf(var2[var16 + 2]);

         for (int var10 = 0; var10 < 27; var10++) {
            long var11 = cell(var7 + var10 % 3 - 1, var8 + var10 / 3 % 3 - 1, var9 + var10 / 9 - 1);

            for (int var13 = first(var3, var11); var13 < var1 && var3[var13] >>> 21 == var11; var13++) {
               int var14 = (int)(var3[var13] & 2097151L);
               if (var14 < var15 && near(var2, var15, var14)) {
                  join(var4, var15, var14);
               }
            }
         }
      }

      return var4;
   }

   private static int cellOf(float var0) {
      return (int)Math.floor(var0 / 0.05F);
   }

   private static long cell(int var0, int var1, int var2) {
      return (var0 + 8192 & 16383L) << 28 | (var1 + 8192 & 16383L) << 14 | var2 + 8192 & 16383L;
   }

   private static int first(long[] var0, long var1) {
      int var3 = 0;
      int var4 = var0.length;
      long var5 = var1 << 21;

      while (var3 < var4) {
         int var7 = var3 + var4 >>> 1;
         if (var0[var7] < var5) {
            var3 = var7 + 1;
         } else {
            var4 = var7;
         }
      }

      return var3;
   }

   private static boolean near(float[] var0, int var1, int var2) {
      int var3 = var1 * 7;
      int var4 = var2 * 7;
      float var5 = var0[var3] - var0[var4];
      float var6 = var0[var3 + 1] - var0[var4 + 1];
      float var7 = var0[var3 + 2] - var0[var4 + 2];
      return var5 * var5 + var6 * var6 + var7 * var7 <= 0.0025000002F;
   }

   private static int root(int[] var0, int var1) {
      while (var0[var1] != var1) {
         var0[var1] = var0[var0[var1]];
         var1 = var0[var1];
      }

      return var1;
   }

   private static void join(int[] var0, int var1, int var2) {
      int var3 = root(var0, var1);
      int var4 = root(var0, var2);
      if (var3 != var4) {
         var0[Math.max(var3, var4)] = Math.min(var3, var4);
      }
   }
}
