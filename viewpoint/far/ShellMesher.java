package viewpoint.far;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import viewpoint.platform.VertexFormat;
import viewpoint.render.FarRelief;
import viewpoint.render.GroundPage;
import viewpoint.render.GroundTiles;
import viewpoint.render.ShellGround;
import viewpoint.render.TreeAtlas;
import viewpoint.world.Cook;
import viewpoint.world.GridRoofs;
import viewpoint.world.Recipe;

final class ShellMesher {
   static final int FULL = 0;
   static final int LITE = 1;
   private static final float INSET = 0.2F;
   private static final int MAX_LAYER = 3;

   static ShellMesher.Result mesh(
      int var0,
      int var1,
      int var2,
      int var3,
      byte[] var4,
      int var5,
      int var6,
      FarTiles.Tile[] var7,
      short[][] var8,
      ShellFacades var9,
      byte[] var10,
      BiomeTrees.Table var11,
      BiomeTrees.Legend var12,
      byte[][] var13,
      int var14,
      boolean var15,
      GroundTiles var16,
      byte[] var17,
      FarTrees.Thinning var18,
      GridRoofs var19
   ) {
      Recipe var20 = new Recipe(0);
      byte[] var21 = new byte[8192];
      GroundCapture var22 = var16 == null ? null : new GroundCapture(var16, var2, var3);
      byte var23 = 8;
      int var24 = var22 == null ? 0 : 1;
      BiomeTrees.Ground var25 = new BiomeTrees.Ground(var2 * 64, var3 * 64, 64);
      ShellRoofs var26 = ShellRoofs.place(var20, var4, var5, var6, var7, var8, var2, var3, var19);
      read(var4, var5, var6, var2 * var23 - var24, var3 * var23 - var24, var23 + 2 * var24, (var13x, var14x, var15x, var16x) -> {
         FarTiles.Tile var17x = var13x < var7.length ? var7[var13x] : FarTiles.EMPTY;
         int var18x = var14x - var2 * 64;
         int var19x = var15x - var3 * 64;
         if (!var25.add(var13x, var7, var14x, var15x, var16x)) {
            if (var22 == null || var16x != 0 || var17x.shell != 2 || building(var8, var14x, var15x, 0) != 0 || !var22.add(var13x, var17x, var14x, var15x)) {
               if (var18x >= 0 && var19x >= 0 && var18x < 64 && var19x < 64) {
                  if (!var26.replaces(var17x, var14x, var15x, var16x)) {
                     if (var17x.shell != 0) {
                        add(var20, var17x, var7, var14x, var15x, var16x, var8, var9, var14, var21, var2, var3, var17, var18);
                     }
                  }
               }
            }
         }
      });
      var25.grow(var11, var10, var0, var1, var7, var12, var13, (var4x, var5x, var6x) -> {
         if (building(var8, var5x, var6x, 0) == 0) {
            tree(var20, var4x, var5x, var6x, 0, var14, var18);
         }
      }, var14 != 0 ? null : (var3x, var4x, var5x) -> plant(var20, var3x, var4x, var5x, 0, var8, var17));
      GroundPage var27 = var22 == null ? null : var22.build();
      if (var27 != null) {
         ShellGroundCuts.ground(var20, var2, var3, var14 == 0, var15);
      }

      return new ShellMesher.Result(Cook.vertices(var20), insides(var2, var3, var8, var17), var27);
   }

   private static FloatBuffer insides(int var0, int var1, short[][] var2, byte[] var3) {
      short[] var4 = new short[4096];
      boolean var5 = false;

      for (int var6 = 0; var6 < 64; var6++) {
         for (int var7 = 0; var7 < 64; var7++) {
            for (int var8 = var2.length - 1; var8 >= 0; var8--) {
               int var9 = building(var2, var0 * 64 + var7, var1 * 64 + var6, var8);
               if (var9 != 0 && open(var3, var9)) {
                  break;
               }

               if (var9 != 0) {
                  var4[var6 * 64 + var7] = (short)((var8 + 1) * 8 - 1);
                  var5 = true;
                  break;
               }
            }
         }
      }

      return FarMesher.boxes(var5 ? var4 : new short[1], var5 ? 64 : 1, 1, var0 * 64, var1 * 64, 0.2F, false, 0);
   }

   static int mode() {
      int var0 = (ShellGround.BAKED.get() ? 1 : 0)
         | (ShellGround.TEXELS_LITE.getInt() > 0 ? 2 : 0)
         | (ShellInteriors.FURNITURE.get() ? 4 : 0)
         | (FarRelief.cut() <= 64 ? 8 : 0);
      return (var0 * 31 + FarTrees.liteKeep()) * 31 + Relief.version();
   }

   private static boolean add(
      Recipe var0,
      FarTiles.Tile var1,
      FarTiles.Tile[] var2,
      int var3,
      int var4,
      int var5,
      short[][] var6,
      ShellFacades var7,
      int var8,
      byte[] var9,
      int var10,
      int var11,
      byte[] var12,
      FarTrees.Thinning var13
   ) {
      float var14 = var3 + 0.5F;
      float var15 = var4 + 0.5F;
      float var16 = var5 * 2.4494896F;
      int var17 = building(var6, var3, var4, var5);
      switch (var1.shell) {
         case 1:
            boolean var20 = var1.north >= 0 && face(var0, var1, var2, var3, var4, var5, var6, 0, var7, var12);
            boolean var21 = var1.west >= 0 && face(var0, var1, var2, var3, var4, var5, var6, 1, var7, var12);
            return var20 || var21;
         case 2:
            if ((var17 == 0 || open(var12, var17)) && (var5 != 0 || var8 == 0)) {
               int var18 = 0;
               if ((var1.flags & 2) != 0) {
                  int var19 = ((var4 - var11 * 64) * 64 + var3 - var10 * 64) * 2 + (var5 == 0 ? 0 : 1);
                  var9[var19]++;
                  var18 = 4 + 16 * Math.min(var9[var19], 3);
               }

               Recipe.place(var0, var1.page, var1.mesh, var1.map, var14, var15, var16, var18);
               return true;
            }

            return false;
         case 3:
            return thing(var0, var1, var3, var4, var5, var6, var8, var12);
         case 4:
            tree(var0, var1, var3, var4, var5, var8, var13);
            return true;
         case 5:
            return var5 != 0 && var8 == 0 && plant(var0, var1, var3, var4, var5, var6, var12);
         default:
            return false;
      }
   }

   private static boolean thing(Recipe var0, FarTiles.Tile var1, int var2, int var3, int var4, short[][] var5, int var6, byte[] var7) {
      boolean var8 = (var1.flags & 1) != 0;
      boolean var9 = var1.north == 1 || var1.west == 1;
      if (var6 != 0 && !var8 && !var9) {
         return false;
      }

      int var10 = building(var5, var2, var3, var4);
      boolean var11 = var10 == 0
         || var10 < (var7 == null ? 0 : var7.length) && var7[var10] == 2
         || var1.north == 1 && building(var5, var2, var3 - 1, var4) == 0
         || var1.west == 1 && building(var5, var2 - 1, var3, var4) == 0;
      if (var11) {
         Recipe.place(var0, var1.page, var1.mesh, var1.map, var2 + 0.5F, var3 + 0.5F, var4 * 2.4494896F, 0);
      }

      return var11;
   }

   private static boolean face(
      Recipe var0, FarTiles.Tile var1, FarTiles.Tile[] var2, int var3, int var4, int var5, short[][] var6, int var7, ShellFacades var8, byte[] var9
   ) {
      float var10 = var3 + 0.5F;
      float var11 = var4 + 0.5F;
      float var12 = var5 * 2.4494896F;
      int var13 = building(var6, var3, var4, var5);
      int var14 = var7 == 0 ? building(var6, var3, var4 - 1, var5) : building(var6, var3 - 1, var4, var5);
      boolean var15 = var13 == 0 || open(var9, var13);
      boolean var16 = var14 == 0 || open(var9, var14);
      if (!var15 && !var16) {
         return false;
      }

      float var17 = var7 == 0 ? 0.0F : 1.0F;
      float var18 = var7 == 0 ? 1.0F : 0.0F;
      if (var15) {
         Recipe.placeFace(var0, var1.page, var1.mesh, var1.map, var10, var11, var12, var17, var18, 0, 0.0F);
      }

      if (var16) {
         FarTiles.Tile var19 = var8.art(var1, var2, var13, var14, var5, var7);
         FarTiles.Tile var20 = var19 != null ? var19 : var1;
         float var21 = var19 != null ? 0.0F : var8.paint(var13) + 1.0F;
         Recipe.placeFace(var0, var20.page, var20.mesh, var20.map, var10, var11, var12, var17, var18, 1, var21);
      }

      return true;
   }

   private static boolean plant(Recipe var0, FarTiles.Tile var1, int var2, int var3, int var4, short[][] var5, byte[] var6) {
      int var7 = building(var5, var2, var3, var4);
      if (var7 == 0 || var7 < (var6 == null ? 0 : var6.length) && var6[var7] == 2) {
         float[] var8 = var1.card;
         int var9 = (var1.flags & 32) != 0 ? 2 : 0;
         Recipe.card(var0, var1.page, var2 + 0.5F, var3 + 0.5F, var4 * 2.4494896F, var8[0], var8[1], var8[2], var8[3], var1.map, 512 | var9);
         return true;
      } else {
         return false;
      }
   }

   private static void tree(Recipe var0, FarTiles.Tile var1, int var2, int var3, int var4, int var5, FarTrees.Thinning var6) {
      if (var6.keeps(var2, var3)) {
         float var7 = var2 + 0.5F;
         float var8 = var3 + 0.5F;
         float var9 = var4 * 2.4494896F;
         int var10 = VertexFormat.rooted(var2, var3);
         TreeAtlas.Slot var11 = var1.billboard;
         if (var5 == 1 && var11 != null) {
            float var13 = var6.grow();
            Recipe.billboard(var0, var1.page, var7, var8, var9, var11.halfWidth * var13, var11.top * var13, var11.rect, var10);
         } else {
            Recipe.place(var0, var1.page, var1.mesh, var1.map, var7, var8, var9, var10);
            FarTiles.Tile var12 = var1.leaves;
            if (var12 != null) {
               Recipe.place(var0, var12.page, var12.mesh, var12.map, var7, var8, var9, var10 | 2);
            }
         }
      }
   }

   private static boolean open(byte[] var0, int var1) {
      return var0 != null && var1 < var0.length && var0[var1] != 0;
   }

   static int building(short[][] var0, int var1, int var2, int var3) {
      return var1 >= 0 && var2 >= 0 && var1 < 256 && var2 < 256 && var3 >= 0 && var3 < var0.length && var0[var3] != null ? var0[var3][var2 * 256 + var1] : 0;
   }

   static void read(byte[] var0, int var1, int var2, int var3, int var4, int var5, ShellMesher.Visit var6) {
      ByteBuffer var7 = ByteBuffer.wrap(var0).order(ByteOrder.LITTLE_ENDIAN);
      int var8 = var0.length >= 8 && var0[0] == 76 && var0[1] == 79 && var0[2] == 84 && var0[3] == 80 ? var7.getInt(4) : 0;
      int var9 = (var8 >= 1 ? 8 : 0) + 4;
      int var10 = Math.max(var1, -32);
      int var11 = Math.min(var2, 31);

      for (int var12 = Math.max(0, var3); var12 < Math.min(32, var3 + var5); var12++) {
         for (int var13 = Math.max(0, var4); var13 < Math.min(32, var4 + var5); var13++) {
            var7.position(var7.getInt(var9 + (var12 * 32 + var13) * 8));
            int var14 = 0;

            for (int var15 = var10; var15 <= var11; var15++) {
               for (int var16 = 0; var16 < 8; var16++) {
                  for (int var17 = 0; var17 < 8; var17++) {
                     if (var14 > 0) {
                        var14--;
                     } else {
                        int var18 = var7.getInt();
                        if (var18 == -1) {
                           var14 = var7.getInt();
                           if (var14 > 0) {
                              var14--;
                              continue;
                           }
                        }

                        if (var18 > 1) {
                           var7.getInt();

                           for (int var19 = 1; var19 < var18; var19++) {
                              int var20 = var7.getInt();
                              if (var15 >= 0 && var20 >= 0) {
                                 var6.tile(var20, var12 * 8 + var16, var13 * 8 + var17, var15);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   record Result(Cook.Cooked shell, FloatBuffer inside, GroundPage ground) {
   }

   interface Visit {
      void tile(int var1, int var2, int var3, int var4);
   }
}
