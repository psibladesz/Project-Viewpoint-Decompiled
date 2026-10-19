package viewpoint.far;

import java.util.function.Consumer;
import viewpoint.world.GridRoofs;
import viewpoint.world.MapRoofs;
import viewpoint.world.Recipe;
import viewpoint.world.RoofBuildings;
import viewpoint.world.RoofPiece;
import viewpoint.world.RoofSurface;
import viewpoint.world.RoofWindow;

final class ShellRoofs {
   private static final int MARGIN = 16;
   private static final int SIDE = 64;
   private static final int CHUNK = 8;
   private final int x0;
   private final int y0;
   private final boolean[][] drawn;

   private ShellRoofs(int var1, int var2, boolean[][] var3) {
      this.x0 = var1;
      this.y0 = var2;
      this.drawn = var3;
   }

   static GridRoofs cell(byte[] var0, int var1, int var2, FarTiles.Tile[] var3, short[][] var4) {
      Consumer<ShellMesher.Visit> var5 = var3x -> ShellMesher.read(var0, var1, var2, 0, 0, 32, var3x);
      return GridRoofs.build(levels(var5, Math.max(var2, 0) + 2, var3, var4));
   }

   static RoofBuildings.Levels levels(Consumer<ShellMesher.Visit> var0, int var1, FarTiles.Tile[] var2, short[][] var3) {
      int var4 = 65536;
      RoofPiece[][] var5 = new RoofPiece[var1][var4];
      boolean[][] var6 = new boolean[var1][var4];
      boolean[][] var7 = new boolean[var1][var4];
      boolean[][] var8 = new boolean[var1][var4];
      boolean[][] var9 = new boolean[var1][var4];
      var0.accept((var6x, var7x, var8x, var9x) -> {
         FarTiles.Tile var10x = var6x < var2.length ? var2[var6x] : FarTiles.EMPTY;
         int var11x = var8x * 256 + var7x;
         if (var9x >= 0 && var9x < var1) {
            var6[var9x][var11x] = var6[var9x][var11x] | MapRoofs.tiled(var10x.roofBits);
            var7[var9x][var11x] = var7[var9x][var11x] | MapRoofs.floor(var10x.roofBits);
            var8[var9x][var11x] = var8[var9x][var11x] | var10x.flatRoof;
            RoofPiece var12x = var5[var9x][var11x];
            if (var10x.roof != null && (var12x == null || var10x.roof.over(var12x))) {
               var5[var9x][var11x] = var10x.roof;
            }
         }
      });

      for (int var10 = 0; var10 < Math.min(var3.length, var1); var10++) {
         for (int var11 = 0; var3[var10] != null && var11 < var4; var11++) {
            var9[var10][var11] = var3[var10][var11] != 0;
         }
      }

      boolean[][] var12 = new boolean[var1][var4];
      return new RoofBuildings.Levels(256, 256, var5, var6, var7, var8, var9, var12);
   }

   static ShellRoofs place(Recipe var0, byte[] var1, int var2, int var3, FarTiles.Tile[] var4, short[][] var5, int var6, int var7, GridRoofs var8) {
      int var9 = var6 * 64;
      int var10 = var7 * 64;
      int var11 = Math.max(0, var9 - 16);
      int var12 = Math.max(0, var10 - 16);
      int var13 = Math.min(256, var9 + 64 + 16) - var11;
      int var14 = Math.min(256, var10 + 64 + 16) - var12;
      MapRoofs.Level[] var15 = new MapRoofs.Level[Math.max(var3, 0) + 2];
      int var16 = (var9 - 16) / 8;
      int var17 = (var10 - 16) / 8;
      byte var18 = 12;
      ShellMesher.read(
              var1,
              var2,
              var3,
              var16,
              var17,
              var18,
              (var6x, var7x, var8x, var9x) -> {
                 FarTiles.Tile var10x = var6x < var4.length ? var4[var6x] : FarTiles.EMPTY;
                 int var11x = var7x - var11;
                 int var12x = var8x - var12;
                 if ((var10x.roofBits != 0 || var10x.roof != null) && var11x >= 0 && var12x >= 0 && var11x < var13 && var12x < var14) {
                    level(var15, var9x, var13 * var14)
                            .tile(var12x * var13 + var11x, var10x.roofBits, var10x.flatRoof, var10x.roof, var10x.page, var10x.map, var10x.kind == 2 ? var10x.colour : -1);
                 }
              }
      );
      rooms(var15, var5, var11, var12, var13, var14);

      for (int var19 = var15.length - 2; var19 >= 0; var19--) {
         if (var15[var19 + 1] != null) {
            level(var15, var19, var13 * var14).under(var15[var19 + 1]);
         }
      }

      RoofWindow.Inner var25 = new RoofWindow.Inner(var9 - var11, var10 - var12, 64, var9, var10);
      boolean[][] var20 = new boolean[var15.length][];

      for (int var21 = 1; var21 < var15.length; var21++) {
         if (var15[var21] != null || var15[var21 - 1] != null) {
            MapRoofs.Level var22 = level(var15, var21, var13 * var14);
            MapRoofs.Level var23 = var21 + 1 < var15.length ? var15[var21 + 1] : null;
            RoofSurface var24 = var8 == null ? null : var8.window(var21, var11, var12, var13, var14);
            var20[var21] = MapRoofs.place(var0, var15[var21 - 1], var22, var23, var13, var14, var25, var21 * 2.4494896F, var24);
         }
      }

      return new ShellRoofs(var9, var10, var20);
   }

   boolean replaces(FarTiles.Tile var1, int var2, int var3, int var4) {
      boolean[] var5 = var1.roof != null && var4 < this.drawn.length ? this.drawn[var4] : null;
      return var5 != null && var5[(var3 - this.y0) * 64 + var2 - this.x0];
   }

   private static void rooms(MapRoofs.Level[] var0, short[][] var1, int var2, int var3, int var4, int var5) {
      for (int var6 = 0; var6 < Math.min(var1.length, var0.length); var6++) {
         if (var1[var6] != null) {
            for (int var7 = 0; var7 < var5; var7++) {
               for (int var8 = 0; var8 < var4; var8++) {
                  if (var1[var6][(var3 + var7) * 256 + var2 + var8] != 0) {
                     level(var0, var6, var4 * var5).room(var7 * var4 + var8);
                  }
               }
            }
         }
      }
   }

   private static MapRoofs.Level level(MapRoofs.Level[] var0, int var1, int var2) {
      if (var0[var1] == null) {
         var0[var1] = new MapRoofs.Level(var2);
      }

      return var0[var1];
   }
}