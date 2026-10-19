package viewpoint.far;

import java.util.Arrays;
import viewpoint.render.FarRelief;
import viewpoint.world.Recipe;
import viewpoint.world.WallCuts;

final class ShellGroundCuts {
   private static final float TOLERANCE = 0.2F;
   private static final int CELL = FarMesher.RELIEF_SPLIT / 4;
   private static final int BLOCK = 16;
   private static final float[] GROUND_RECT = new float[]{0.015151516F, 0.015151516F, 0.9848485F, 0.9848485F};

   private ShellGroundCuts() {
   }

   static void ground(Recipe var0, int var1, int var2, boolean var3, boolean var4) {
      ReliefData var5 = Relief.data();
      if (FarRelief.cut() > 64) {
         emit(var0, var1, var2, 0, 0, 16, null, null);
      } else if (var3 && var5 != null && !var4) {
         cut(var0, var5, var1, var2);
      } else {
         int var6 = var3 ? 1 : CELL;

         for (int var7 = 0; var7 < 16; var7 += var6) {
            for (int var8 = 0; var8 < 16; var8 += var6) {
               emit(var0, var1, var2, var8, var7, var6, null, var3 && var5 != null ? var5.grid : null);
            }
         }
      }
   }

   private static void cut(Recipe var0, ReliefData var1, int var2, int var3) {
      int var4 = var2 * 16;
      int var5 = var3 * 16;
      int[] var6 = new int[768];
      int var7 = 0;

      for (int var8 = 0; var8 < 16; var8 += CELL) {
         for (int var9 = 0; var9 < 16; var9 += CELL) {
            var7 = leaves(var1, var4 + var9, var5 + var8, CELL, var6, var7);
         }
      }

      boolean[] var10 = new boolean[289];
      mark(var10, var6, var7, var4, var5);

      for (int var11 = 0; var11 <= 16; var11++) {
         var10[var11] = var10[272 + var11] = true;
         var10[var11 * 17] = var10[var11 * 17 + 16] = true;
      }

      for (int var12 = 0; var12 < var7; var12++) {
         emit(var0, var2, var3, var6[var12 * 3] - var4, var6[var12 * 3 + 1] - var5, var6[var12 * 3 + 2], var10, var1.grid);
      }
   }

   private static int leaves(ReliefData var0, int var1, int var2, int var3, int[] var4, int var5) {
      if (var3 > 1 && (!flat(var0, var1, var2, var3) || walled(var0, var1, var2, var3))) {
         int var6 = var3 / 2;
         var5 = leaves(var0, var1, var2, var6, var4, var5);
         var5 = leaves(var0, var1 + var6, var2, var6, var4, var5);
         var5 = leaves(var0, var1, var2 + var6, var6, var4, var5);
         return leaves(var0, var1 + var6, var2 + var6, var6, var4, var5);
      } else {
         var4[var5 * 3] = var1;
         var4[var5 * 3 + 1] = var2;
         var4[var5 * 3 + 2] = var3;
         return var5 + 1;
      }
   }

   private static boolean flat(ReliefData var0, int var1, int var2, int var3) {
      float var4 = var0.node(var1, var2);
      float var5 = var0.node(var1 + var3, var2);
      float var6 = var0.node(var1, var2 + var3);
      float var7 = var0.node(var1 + var3, var2 + var3);

      for (int var8 = 0; var8 <= var3; var8++) {
         for (int var9 = 0; var9 <= var3; var9++) {
            float var10 = (float)var9 / var3;
            float var11 = (float)var8 / var3;
            float var12 = (var4 * (1.0F - var10) + var5 * var10) * (1.0F - var11) + (var6 * (1.0F - var10) + var7 * var10) * var11;
            if (Math.abs(var0.node(var1 + var9, var2 + var8) - var12) > 0.2F) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean walled(ReliefData var0, int var1, int var2, int var3) {
      for (int var4 = var2; var4 < var2 + var3; var4++) {
         for (int var5 = var1; var5 < var1 + var3; var5++) {
            if (var0.grid.wall(var5, var4) != 0) {
               return true;
            }
         }
      }

      return false;
   }

   private static void mark(boolean[] var0, int[] var1, int var2, int var3, int var4) {
      for (int var5 = 0; var5 < var2; var5++) {
         int var6 = var1[var5 * 3] - var3;
         int var7 = var1[var5 * 3 + 1] - var4;
         int var8 = var1[var5 * 3 + 2];

         for (int var9 = 0; var9 < 4; var9++) {
            var0[(var7 + (var9 >> 1) * var8) * 17 + var6 + (var9 & 1) * var8] = true;
         }
      }
   }

   private static void emit(Recipe var0, int var1, int var2, int var3, int var4, int var5, boolean[] var6, FarRelief.Field var7) {
      float var8 = 4.0F;
      float var9 = var1 * 64;
      float var10 = var2 * 64;
      if (var5 == 1 && var7 != null && var7.wall(var1 * 16 + var3, var2 * 16 + var4) != 0) {
         walled(var0, var7, var9, var10, var3 * var8, var4 * var8, (var3 + 1) * var8, (var4 + 1) * var8);
      } else {
         float[] var11 = new float[]{
            along(GROUND_RECT[0], GROUND_RECT[2], var3 * var8),
            along(GROUND_RECT[1], GROUND_RECT[3], var4 * var8),
            along(GROUND_RECT[0], GROUND_RECT[2], (var3 + var5) * var8),
            along(GROUND_RECT[1], GROUND_RECT[3], (var4 + var5) * var8)
         };
         float[] var12 = ring(var3, var4, var5, var6);
         float var13 = var9 + var3 * var8;
         float var14 = var10 + var4 * var8;
         float var15 = var9 + (var3 + var5) * var8;
         float var16 = var10 + (var4 + var5) * var8;
         if (var12.length == 8) {
            Recipe.bakedFloor(var0, var13, var14, var15, var16, 0.0F, var11);
         } else {
            for (int var17 = 0; var17 < var12.length; var17 += 2) {
               var12[var17] = var9 + var12[var17] * var8;
               var12[var17 + 1] = var10 + var12[var17 + 1] * var8;
            }

            Recipe.bakedFloorFan(var0, var12, var13, var14, var15, var16, 0.0F, var11);
         }
      }
   }

   private static float[] ring(int var0, int var1, int var2, boolean[] var3) {
      float[] var4 = new float[8 * var2];
      int var5 = 0;
      int[][] var6 = new int[][]{{var0, var1, 1, 0}, {var0 + var2, var1, 0, 1}, {var0 + var2, var1 + var2, -1, 0}, {var0, var1 + var2, 0, -1}};

      for (int[] var10 : var6) {
         for (int var11 = 0; var11 < var2; var11++) {
            int var12 = var10[0] + var10[2] * var11;
            int var13 = var10[1] + var10[3] * var11;
            if (var11 == 0 || var3 != null && var3[var13 * 17 + var12]) {
               var4[var5++] = var12;
               var4[var5++] = var13;
            }
         }
      }

      return Arrays.copyOf(var4, var5);
   }

   private static void walled(Recipe var0, FarRelief.Field var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      float[] var8 = WallCuts.pieces(var1, var2, var3, var4, var5, var6, var7);

      for (int var9 = 0; var9 < var8.length; var9 += 6) {
         float[] var10 = new float[]{
            along(GROUND_RECT[0], GROUND_RECT[2], var8[var9]),
            along(GROUND_RECT[1], GROUND_RECT[3], var8[var9 + 1]),
            along(GROUND_RECT[0], GROUND_RECT[2], var8[var9 + 2]),
            along(GROUND_RECT[1], GROUND_RECT[3], var8[var9 + 3])
         };
         Recipe.bakedFloor(
            var0, var2 + var8[var9], var3 + var8[var9 + 1], var2 + var8[var9 + 2], var3 + var8[var9 + 3], 0.0F, var10, var8[var9 + 4], var8[var9 + 5]
         );
      }
   }

   private static float along(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2 / 64.0F;
   }
}
