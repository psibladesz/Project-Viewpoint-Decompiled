package viewpoint.world;

public final class RoofBuilder {
   public static final byte TILED = 1;
   public static final byte COVERED = 2;
   public static final byte UNDER = 4;
   public static final byte WALLED = 8;
   public static final byte INSIDE = 16;
   public static final byte CAPPED = 32;
   public static final byte PORCH = 64;
   private static final int NEAR = 16;
   private static final int CITY = 8;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   private final int width;
   private final int height;
   private final RoofPiece[] pieces;
   private final byte[] flags;
   private final boolean[] shallow;

   public RoofBuilder(int var1, int var2, RoofPiece[] var3, byte[] var4) {
      this.width = var1;
      this.height = var2;
      this.pieces = RoofSettle.settle(var1, var2, var3, var4);
      this.flags = var4;
      this.shallow = RoofShallow.level(var1, var2, this.pieces, var4);
   }

   public RoofSurface build() {
      boolean[] var1 = this.seat();
      RoofSurface var2 = RoofSurface.empty(this.width, this.height);
      float[] var3 = new float[9];

      for (int var4 = 0; var4 < this.flags.length; var4++) {
         if (var1[var4] && !planar(this.pieces[var4])) {
            var2.moved(var4);
         } else if (planar(this.pieces[var4])) {
            for (int var5 = 0; var5 < 9; var5++) {
               var3[var5] = this.pieces[var4].height(var5 % 3 / 2.0F - 0.5F, var5 / 3 / 2.0F - 0.5F);
            }

            var2.set(var4, (byte)1, var3);
         }
      }

      RoofSurface var8 = var2.copy();
      boolean[] var9 = RoofSections.build(this.width, this.height, this.pieces, this.flags, this.shallow, var2);
      new RoofOutline(this.width, this.height, this.pieces, this.flags, this.shallow, var9).build(var2);
      boolean[] var6 = this.city(var9);
      if (var6 != null) {
         new RoofOutline(this.width, this.height, this.pieces, this.flags, this.shallow, new boolean[this.flags.length]).build(var8);

         for (int var7 = 0; var7 < this.flags.length; var7++) {
            if (var6[var7]) {
               var2.take(var8, var7);
            }
         }
      }

      this.rims(var2, var3);
      RoofCaps.uncap(this.width, this.height, this.pieces, this.flags, var2);
      return var2;
   }

   private boolean[] city(boolean[] var1) {
      boolean[] var2 = null;

      for (int var3 = 0; var3 < this.flags.length; var3++) {
         RoofPiece var4 = this.pieces[var3];
         if (planar(var4) && var4.kind == 0 && (!var4.porch() || this.shallow[var3]) && !var1[var3]) {
            var2 = var2 == null ? new boolean[this.flags.length] : var2;

            for (int var5 = Math.max(0, var3 / this.width - 8); var5 <= Math.min(this.height - 1, var3 / this.width + 8); var5++) {
               for (int var6 = Math.max(0, var3 % this.width - 8); var6 <= Math.min(this.width - 1, var3 % this.width + 8); var6++) {
                  var2[var5 * this.width + var6] = true;
               }
            }
         }
      }

      return var2;
   }

   private void rims(RoofSurface var1, float[] var2) {
      for (int var3 = 0; var3 < this.flags.length; var3++) {
         byte var4 = var1.source(var3 % this.width, var3 / this.width);
         if (var4 == 0 && !planar(this.pieces[var3]) && (this.flags[var3] & 1) == 0) {
            int var5 = this.neighbour(var3, 0);
            int var6 = this.neighbour(var3, 1);
            RoofPiece var7 = this.top(var5) ? this.rimPlane(var3, 2, this.pieces[var5]) : null;
            RoofPiece var8 = this.top(var6) ? this.rimPlane(var3, 3, this.pieces[var6]) : null;
            int var9 = var5 < 0 ? -1 : this.neighbour(var5, 1);
            if (var7 == null && var8 == null && this.top(var9) && !planar(this.pieces[var5]) && !planar(this.pieces[var6])) {
               var7 = this.rimPlane(var6, 2, this.pieces[var9]);
               var8 = this.rimPlane(var5, 3, this.pieces[var9]);

               for (int var13 = 0; var13 < 9; var13++) {
                  var2[var13] = Math.min(point(var7, var13), point(var8, var13));
               }
            } else {
               if (var7 == null && var8 == null) {
                  continue;
               }

               for (int var10 = 0; var10 < 9; var10++) {
                  var2[var10] = Math.max(var7 == null ? 0.0F : point(var7, var10), var8 == null ? 0.0F : point(var8, var10));
               }
            }

            var1.set(var3, (byte)2, var2);
         }
      }
   }

   private RoofPiece rimPlane(int var1, int var2, RoofPiece var3) {
      int var4 = (var2 + 1) % 4;

      for (int var5 = 1; var5 <= 16; var5++) {
         for (int var9 : new int[]{var4, (var4 + 2) % 4}) {
            int var10 = var1;

            for (int var11 = 0; var11 < var5 && var10 >= 0; var11++) {
               var10 = this.neighbour(var10, var9);
            }

            RoofPiece var12 = var10 >= 0 ? this.pieces[var10] : null;
            if (planar(var12) && var12.kind != 5 && var12.kind != 6 && falls(var12, var2)) {
               return new RoofPiece(0, var2, -1, var12.perStorey, var12.step);
            }
         }
      }

      return new RoofPiece(0, var2, -1, var3.perStorey, var3.step);
   }

   private static float point(RoofPiece var0, int var1) {
      return var0.height(var1 % 3 / 2.0F - 0.5F, var1 / 3 / 2.0F - 0.5F);
   }

   private boolean top(int var1) {
      return var1 >= 0 && planar(this.pieces[var1]) && this.pieces[var1].kind == 5;
   }

   private boolean[] seat() {
      boolean[] var1 = new boolean[this.flags.length];
      RoofPiece[] var2 = (RoofPiece[])this.pieces.clone();

      for (int var3 = 0; var3 < this.flags.length; var3++) {
         RoofPiece var4 = var2[var3];
         int var5 = var3 - this.width - 1;
         if (planar(var4) && ridge(var4) && var3 % this.width != 0 && var5 >= 0 && !planar(var2[var5])) {
            int var6 = var4.step - var4.perStorey / 3;
            if (var6 >= 1 && !this.holds(var2, this.neighbour(var3, var4.facing)) && !this.holds(var2, this.neighbour(var3, var4.other))) {
               this.pieces[var5] = new RoofPiece(var4.kind, var4.facing, var4.other, var4.perStorey, var6);
               this.pieces[var3] = null;
               var1[var3] = true;
            }
         }
      }

      return var1;
   }

   private boolean holds(RoofPiece[] var1, int var2) {
      return var2 < 0 || planar(var1[var2]) || (this.flags[var2] & 4) != 0;
   }

   private static boolean falls(RoofPiece var0, int var1) {
      return var0.kind == 6 || var0.facing == var1 || var0.kind != 0 && var0.other == var1;
   }

   private static boolean ridge(RoofPiece var0) {
      return var0.kind == 1 && (var0.facing + 2) % 4 == var0.other;
   }

   private static boolean planar(RoofPiece var0) {
      return var0 != null && var0.planar();
   }

   private int neighbour(int var1, int var2) {
      int var3 = var1 % this.width + DX[var2];
      int var4 = var1 / this.width + DZ[var2];
      return var3 >= 0 && var4 >= 0 && var3 < this.width && var4 < this.height ? var4 * this.width + var3 : -1;
   }
}
