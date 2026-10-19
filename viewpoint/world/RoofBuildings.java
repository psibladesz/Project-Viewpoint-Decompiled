package viewpoint.world;

public final class RoofBuildings {
   private static final int HANG = 2;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};

   private RoofBuildings() {
   }

   public static RoofSurface[] build(RoofBuildings.Levels var0) {
      RoofBuildings.Levels var1 = new RoofBuildings.Levels(
         var0.width(), var0.height(), seated(var0), var0.tiled(), var0.floor(), var0.flat(), var0.room(), var0.porch()
      );
      int var2 = var1.width() * var1.height();
      int var3 = var1.room().length;
      RoofSurface[] var4 = new RoofSurface[var3];

      for (int var5 = 0; var5 < var3; var5++) {
         var4[var5] = RoofSurface.empty(var1.width(), var1.height());
      }

      int[] var12 = new int[var2];
      int[] var6 = new int[var2];
      float[] var7 = new float[9];
      int var8 = 0;

      for (int var9 = 0; var9 < var12.length; var9++) {
         if (var12[var9] == 0 && roofed(var1, var9)) {
            int[] var10 = label(var1, var12, var6, var9, ++var8);
            RoofColumns var11 = RoofColumns.of(var1, var12, var8, var10[0], var10[1], var10[2], var10[3]);
            place(var11, RoofBuilding.build(var11), var4, var7);
         }
      }

      return var4;
   }

   public static int[] labels(RoofBuildings.Levels var0) {
      int var1 = var0.width() * var0.height();
      int[] var2 = new int[var1];
      int[] var3 = new int[var1];
      int var4 = 0;

      for (int var5 = 0; var5 < var1; var5++) {
         if (var2[var5] == 0 && roofed(var0, var5)) {
            label(var0, var2, var3, var5, ++var4);
         }
      }

      return var2;
   }

   private static RoofPiece[][] seated(RoofBuildings.Levels var0) {
      int var1 = var0.width();
      int var2 = var0.pieces().length;
      RoofPiece[][] var3 = new RoofPiece[var2][];

      for (int var4 = 0; var4 < var2; var4++) {
         var3[var4] = (RoofPiece[])var0.pieces()[var4].clone();
      }

      for (int var15 = 1; var15 < var2; var15++) {
         for (int var5 = 0; var5 < var1 * var0.height(); var5++) {
            boolean var6 = carried(var0, var15, var5);
            RoofPiece var7 = var6 ? RoofLevels.lowered(var0.pieces()[var15 + 1][var5], 1) : var0.pieces()[var15][var5];
            int var8 = var5 - var1 - 1;
            if (var7 != null && ridge(var7) && var5 % var1 != 0 && var8 >= 0 && !pieced(var0, var15, var8)) {
               int var9 = var7.step - var7.perStorey / 3;
               int var10 = neighbour(var0, var5, var7.facing);
               int var11 = neighbour(var0, var5, var7.other);
               boolean var12 = !held(var0, var15, var10) && !held(var0, var15, var11);
               boolean var13 = !var6 || !held(var0, var15 + 1, var10) && !held(var0, var15 + 1, var11);
               RoofPiece var14 = var6 ? null : var0.pieces()[var15 - 1][var5];
               if (var9 >= 1 && (var12 && var13 || var14 != null && var14.planar() && !var14.porch())) {
                  seat(var0, var3, var15, var5, var7, hang(var0, var15, var5, var7));
                  var3[var6 ? var15 + 1 : var15][var5] = null;
               }
            }
         }
      }

      hung(var0, var3, 0);
      hung(var0, var3, 1);
      return var3;
   }

   private static void hung(RoofBuildings.Levels var0, RoofPiece[][] var1, int var2) {
      int var3 = var0.width();
      int var4 = var0.height();

      for (int var5 = var2 == 0 ? var4 - 1 : var3 - 1; var5 >= 0; var5--) {
         for (int var6 = 0; var6 < (var2 == 0 ? var3 : var4); var6++) {
            int var7 = var2 == 0 ? var5 * var3 + var6 : var6 * var3 + var5;
            int var8 = var7 - var3 - 1;

            for (int var9 = 0; var9 < var1.length && var7 % var3 > 0 && var8 >= 0; var9++) {
               RoofPiece var10 = var1[var9][var7];
               if (var10 != null && var10.planar() && !ridge(var10) && RoofFace.has(var10, var2) && var10.kind != 7) {
                  int var11 = var10.step + var10.perStorey * var9 + RoofFace.u(var3, var7, var2);
                  int var12 = var10.step - var10.perStorey / 3;

                  int var13;
                  for (var13 = var9; var12 < 1 && var13 > 0; var13--) {
                     var12 += var10.perStorey;
                  }

                  int var14 = var12 + var10.perStorey * var13 + RoofFace.u(var3, var8, var2);
                  if (var12 >= 1
                     && !meets(var0, var1, neighbour(var0, var7, var2), var2, var10.perStorey, var11)
                     && !planarAt(var1, var13, var8)
                     && meets(var0, var1, neighbour(var0, var8, var2), var2, var10.perStorey, var14)) {
                     var1[var13][var8] = new RoofPiece(var10.kind, var10.facing, var10.other, var10.perStorey, var12);
                     var1[var9][var7] = null;
                  }
               }
            }
         }
      }
   }

   private static boolean meets(RoofBuildings.Levels var0, RoofPiece[][] var1, int var2, int var3, int var4, int var5) {
      for (int var6 = 0; var2 >= 0 && var6 < var1.length; var6++) {
         RoofPiece var7 = var1[var6][var2];
         if (var7 != null
            && var7.planar()
            && var7.kind != 7
            && var7.perStorey == var4
            && RoofFace.has(var7, var3)
            && var7.step + var4 * var6 + RoofFace.u(var0.width(), var2, var3) == var5) {
            return true;
         }
      }

      return false;
   }

   private static boolean planarAt(RoofPiece[][] var0, int var1, int var2) {
      return var0[var1][var2] != null && var0[var1][var2].planar();
   }

   private static int hang(RoofBuildings.Levels var0, int var1, int var2, RoofPiece var3) {
      for (int var4 = 1; var4 <= 2; var4++) {
         int[] var5 = seat(var0, var1, var2, var3, var4);
         if (var5 != null) {
            for (int var9 : new int[]{var3.facing, var3.other}) {
               int var10 = neighbour(var0, var5[0], var9);
               RoofPiece var11 = var10 < 0 ? null : var0.pieces()[var5[1]][var10];
               boolean var12 = var11 != null && var11.planar() && var11.perStorey == var3.perStorey && RoofFace.has(var11, var9);
               if (var12 && var11.step + RoofFace.u(var0.width(), var10, var9) == var5[2] + RoofFace.u(var0.width(), var5[0], var9)) {
                  return var4;
               }
            }
         }
      }

      return 1;
   }

   private static int[] seat(RoofBuildings.Levels var0, int var1, int var2, RoofPiece var3, int var4) {
      int var5 = var2 - var4 * (var0.width() + 1);
      int var6 = var3.step - var4 * var3.perStorey / 3;

      int var7;
      for (var7 = var1; var6 < 1; var7--) {
         var6 += var3.perStorey;
      }

      return var2 % var0.width() >= var4 && var5 >= 0 && var7 >= 0 && !pieced(var0, var7, var5) ? new int[]{var5, var7, var6} : null;
   }

   private static void seat(RoofBuildings.Levels var0, RoofPiece[][] var1, int var2, int var3, RoofPiece var4, int var5) {
      int[] var6 = seat(var0, var2, var3, var4, var5);
      var1[var6[1]][var6[0]] = new RoofPiece(var4.kind, var4.facing, var4.other, var4.perStorey, var6[2]);
   }

   private static boolean carried(RoofBuildings.Levels var0, int var1, int var2) {
      return var1 + 1 < var0.pieces().length
         && var0.pieces()[var1][var2] == null
         && MapRoofs.carries(var0.pieces()[var1 + 1][var2], var0.floor()[var1][var2], var0.room()[var1][var2]);
   }

   private static boolean held(RoofBuildings.Levels var0, int var1, int var2) {
      return var2 < 0 ? true : pieced(var0, var1, var2) || var0.room()[var1 - 1][var2] && !var0.floor()[var1][var2];
   }

   private static boolean pieced(RoofBuildings.Levels var0, int var1, int var2) {
      RoofPiece var3 = var0.pieces()[var1][var2];
      return var3 != null && var3.planar() || carried(var0, var1, var2);
   }

   private static boolean ridge(RoofPiece var0) {
      return var0.kind == 1 && (var0.facing + 2) % 4 == var0.other;
   }

   private static int neighbour(RoofBuildings.Levels var0, int var1, int var2) {
      int var3 = var1 % var0.width() + DX[var2];
      int var4 = var1 / var0.width() + DZ[var2];
      return var3 >= 0 && var4 >= 0 && var3 < var0.width() && var4 < var0.height() ? var4 * var0.width() + var3 : -1;
   }

   private static boolean roofed(RoofBuildings.Levels var0, int var1) {
      for (int var2 = 0; var2 < var0.room().length; var2++) {
         if (var0.room()[var2][var1] || var0.tiled()[var2][var1] || var0.porch()[var2][var1]) {
            return true;
         }
      }

      return false;
   }

   private static int[] label(RoofBuildings.Levels var0, int[] var1, int[] var2, int var3, int var4) {
      int var5 = var0.width();
      int var6 = 0;
      int var7 = 0;
      int[] var8 = new int[]{var3 % var5, var3 / var5, var3 % var5, var3 / var5};
      var1[var3] = var4;
      var2[var7++] = var3;

      while (var6 < var7) {
         int var9 = var2[var6++];
         int var10 = var9 % var5;
         int var11 = var9 / var5;
         var8[0] = Math.min(var8[0], var10);
         var8[1] = Math.min(var8[1], var11);
         var8[2] = Math.max(var8[2], var10);
         var8[3] = Math.max(var8[3], var11);
         int[] var12 = new int[]{
            var10 > 0 ? var9 - 1 : -1, var10 < var5 - 1 ? var9 + 1 : -1, var11 > 0 ? var9 - var5 : -1, var11 < var0.height() - 1 ? var9 + var5 : -1
         };

         for (int var16 : var12) {
            if (var16 >= 0 && var1[var16] == 0 && roofed(var0, var16)) {
               var1[var16] = var4;
               var2[var7++] = var16;
            }
         }
      }

      return var8;
   }

   private static void place(RoofColumns var0, RoofSurface var1, RoofSurface[] var2, float[] var3) {
      int var4 = var2[0].width;

      for (int var5 = 0; var5 < var0.width * var0.height; var5++) {
         byte var6 = var1.source(var5 % var0.width, var5 / var0.width);
         byte var7 = var0.level[var5];
         if (var6 != 0 && var7 >= 0 && var7 < var2.length) {
            float var8 = (var0.reference - var7) * 2.4494896F;

            for (int var9 = 0; var9 < 9; var9++) {
               var3[var9] = var1.height(var5 % var0.width, var5 / var0.width, var9) + var8;
            }

            if (var6 == 4) {
               var2[var7].lid(var0.grid(var4, var5), var3[0]);
            } else {
               var2[var7].set(var0.grid(var4, var5), var6, var3);
            }
         }
      }
   }

   public record Levels(
      int width, int height, RoofPiece[][] pieces, boolean[][] tiled, boolean[][] floor, boolean[][] flat, boolean[][] room, boolean[][] porch
   ) {
   }
}
