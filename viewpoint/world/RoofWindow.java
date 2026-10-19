package viewpoint.world;

import java.util.Arrays;

public final class RoofWindow {
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   private static final byte ANY = -1;
   final int width;
   final int height;
   final RoofWindow.Inner inner;
   final RoofPiece[] pieces;
   final byte[] flags;
   final byte[] lifts;
   final int[] lenders;
   final RoofBuildings.Levels column;
   final RoofSurface built;
   final int level;
   final long hash;

   RoofWindow(int var1, int var2, RoofWindow.Inner var3, RoofPiece[] var4, byte[] var5, byte[] var6, int[] var7) {
      this(var1, var2, var3, var4, var5, var6, var7, null, null, -1);
   }

   private RoofWindow(
      int var1, int var2, RoofWindow.Inner var3, RoofPiece[] var4, byte[] var5, byte[] var6, int[] var7, RoofBuildings.Levels var8, RoofSurface var9, int var10
   ) {
      this.width = var1;
      this.height = var2;
      this.inner = var3;
      this.pieces = var4;
      this.flags = var5;
      this.lifts = var6;
      this.lenders = var7;
      this.column = var8;
      this.built = var9;
      this.level = var10;
      this.hash = this.hash();
   }

   RoofWindow perBuilding(RoofBuildings.Levels var1, int var2) {
      return new RoofWindow(this.width, this.height, this.inner, this.pieces, this.flags, this.lifts, this.lenders, var1, null, var2);
   }

   RoofWindow perBuilding(RoofSurface var1) {
      return new RoofWindow(this.width, this.height, this.inner, this.pieces, this.flags, this.lifts, this.lenders, null, var1, -1);
   }

   static RoofWindow of(int var0, int var1, RoofWindow.Inner var2, RoofPiece[] var3, byte[] var4, byte[] var5, boolean[] var6, boolean[] var7) {
      int[] var8 = new int[var3.length];
      Arrays.fill(var8, -1);
      lend(var0, var1, var3, var4, var6, var7, var8, (byte)0);
      lend(var0, var1, var3, var4, var6, var7, var8, (byte)5);
      lend(var0, var1, var3, var4, var6, var7, var8, (byte)-1);
      int[] var9 = new int[var2.side() * var2.side()];

      for (int var10 = 0; var10 < var9.length; var10++) {
         var9[var10] = var8[(var2.z() + var10 / var2.side()) * var0 + var2.x() + var10 % var2.side()];
      }

      return new RoofWindow(var0, var1, var2, var3, var4, var5, var9);
   }

   private static void lend(int var0, int var1, RoofPiece[] var2, byte[] var3, boolean[] var4, boolean[] var5, int[] var6, byte var7) {
      int[] var8 = new int[var2.length];
      int var9 = 0;
      int var10 = 0;

      for (int var11 = 0; var11 < var2.length; var11++) {
         RoofPiece var12 = var2[var11];
         if (var6[var11] < 0 && var12 != null && (var7 == -1 || var12.kind == var7 || slope(var12, var7)) && !var12.unseen() && var4[var11]) {
            var6[var11] = var11;
            var8[var10++] = var11;
         }
      }

      while (var9 < var10) {
         int var18 = var8[var9++];
         int var19 = var18 % var0;
         int var13 = var18 / var0;

         for (int var14 = 0; var14 < DX.length; var14++) {
            int var15 = var19 + DX[var14];
            int var16 = var13 + DZ[var14];
            int var17 = var16 * var0 + var15;
            if (var15 >= 0
               && var16 >= 0
               && var15 < var0
               && var16 < var1
               && var6[var17] < 0
               && (!var5[var17] || var2[var17] != null)
               && (var3[var17] != 0 || var2[var17] != null || rim(var0, var1, var2, var17))) {
               var6[var17] = var6[var18];
               var8[var10++] = var17;
            }
         }
      }
   }

   private static boolean slope(RoofPiece var0, byte var1) {
      return var1 == 0 && var0.kind == 7;
   }

   int square(int var1) {
      return (this.inner.z() + var1 / this.inner.side()) * this.width + this.inner.x() + var1 % this.inner.side();
   }

   int innerSquare(float var1, float var2) {
      return ((int)var2 - this.inner.originZ()) * this.inner.side() + (int)var1 - this.inner.originX();
   }

   int ownDraws(int var1, boolean var2, boolean var3) {
      RoofPiece var4 = this.pieces[this.square(var1)];
      if (var2 && var4 != null) {
         return var3 ? 1 : 3;
      } else {
         return 0;
      }
   }

   int lentDraws(int var1, boolean var2, boolean var3) {
      int var4 = this.square(var1);
      int var5 = var4 + this.width + 1;
      RoofPiece var6 = this.pieces[var4];
      if (!var3) {
         return 0;
      }

      if (var2 && var6 != null) {
         return 6;
      }

      int var7 = this.column == null && this.built == null ? 7 : 5;
      boolean var8 = (this.flags[var4] & var7) == 4;
      boolean var9 = var4 % this.width < this.width - 1 && var5 < this.pieces.length && ridge(this.pieces[var5]);
      boolean var10 = (this.flags[var4] & 64) != 0;
      boolean var11 = var6 != null || var8 || var9 || var10 || rim(this.width, this.height, this.pieces, var4);
      return var11 ? 7 : 0;
   }

   private static boolean ridge(RoofPiece var0) {
      return var0 != null && var0.kind == 1 && (var0.facing + 2) % 4 == var0.other;
   }

   private static boolean rim(int var0, int var1, RoofPiece[] var2, int var3) {
      int var4 = var3 % var0;
      int var5 = var3 / var0;
      return var2[var3] == null
         && (top(var0, var1, var2, var4, var5 + 1) || top(var0, var1, var2, var4 + 1, var5) || top(var0, var1, var2, var4 + 1, var5 + 1));
   }

   private static boolean top(int var0, int var1, RoofPiece[] var2, int var3, int var4) {
      RoofPiece var5 = var3 < var0 && var4 < var1 ? var2[var4 * var0 + var3] : null;
      return var5 != null && var5.kind == 5;
   }

   RoofPiece art(int var1) {
      return this.lifts[var1] == 0 ? this.pieces[var1] : RoofLevels.lowered(this.pieces[var1], -this.lifts[var1]);
   }

   RoofSurface build() {
      if (this.built != null) {
         return this.built;
      } else {
         return this.column == null ? new RoofBuilder(this.width, this.height, this.pieces, this.flags).build() : RoofBuildings.build(this.column)[this.level];
      }
   }

   private long hash() {
      long var1 = Recipe.mix(-7046029254386353131L, (long)this.width << 32 | this.height);
      var1 = Recipe.mix(var1, (long)this.inner.x() << 32 | this.inner.z());
      var1 = Recipe.mix(var1, this.inner.side());
      var1 = Recipe.mix(var1, (long)this.inner.originX() << 32 | this.inner.originZ() & 4294967295L);

      for (int var3 = 0; var3 < this.pieces.length; var3++) {
         RoofPiece var4 = this.pieces[var3];
         long var5 = var4 == null ? -1L : var4.kind | (var4.facing & 255) << 8 | (var4.other & 255) << 16 | var4.perStorey << 24 | (long)var4.step << 32;
         var1 = Recipe.mix(var1, var5 << 16 | (this.flags[var3] & 255) << 8 | this.lifts[var3] & 0xFF);
         if (var4 != null && var4.kind == 7) {
            var1 = Recipe.mix(var1, (long)Float.floatToRawIntBits(var4.rise) << 32 | Float.floatToRawIntBits(var4.middle));
         }
      }

      for (int var6 : this.lenders) {
         var1 = Recipe.mix(var1, var6);
      }

      for (int var11 = 0; this.built != null && var11 < this.width * this.height; var11++) {
         var1 = Recipe.mix(var1, this.built.source(var11 % this.width, var11 / this.width));

         for (int var13 = 0; var13 < 9; var13++) {
            var1 = Recipe.mix(var1, Float.floatToRawIntBits(this.built.height(var11 % this.width, var11 / this.width, var13)));
         }
      }

      return Recipe.mix(var1, this.column == null ? -1L : this.level);
   }

   public record Inner(int x, int z, int side, int originX, int originZ) {
      static RoofWindow.Inner chunk(int var0, int var1) {
         return new RoofWindow.Inner(var0, var1, 8, 0, 0);
      }
   }
}
