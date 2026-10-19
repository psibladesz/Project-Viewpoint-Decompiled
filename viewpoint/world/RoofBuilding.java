package viewpoint.world;

import java.util.Arrays;

final class RoofBuilding {
   private static final int OLDER_PITCH = 3;
   private static final int REACH = 8;
   private static final float EPS = 0.001F;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};

   private RoofBuilding() {
   }

   static RoofSurface build(RoofColumns var0) {
      int var1 = var0.width * var0.height;
      RoofPiece[] var2 = (RoofPiece[])var0.pieces.clone();
      byte[] var3 = new byte[var1];

      for (int var4 = 0; var4 < var1; var4++) {
         var3[var4] = (byte)(!var0.in[var4] ? 0 : (Float.isNaN(var0.floor[var4]) ? 1 : 4));
      }

      boolean[] var9 = RoofShallow.level(var0.width, var0.height, var2, var3);
      RoofSurface var5 = RoofSurface.empty(var0.width, var0.height);
      float[] var6 = new float[9];

      for (int var7 = 0; var7 < var1; var7++) {
         if (planar(var2[var7])) {
            for (int var8 = 0; var8 < 9; var8++) {
               var6[var8] = var2[var7].height(var8 % 3 / 2.0F - 0.5F, var8 / 3 / 2.0F - 0.5F);
            }

            var5.set(var7, (byte)1, var6);
         }
      }

      BuildingSections.build(var0, var2, var9, var5);
      boolean[] var10 = new boolean[var1];

      for (int var11 = 0; var11 < var1; var11++) {
         var10[var11] = open(var0, var5, var11);
      }

      for (int var12 = 0; var12 < var1; var12++) {
         if (var10[var12] && var0.cap[var12] < Float.POSITIVE_INFINITY) {
            var5.lid(var12, var0.cap[var12]);
         }
      }

      outline(var0, var5, var9, rise(var2), var6);
      porches(var0, var5, var6);
      return var5;
   }

   private static void porches(RoofColumns var0, RoofSurface var1, float[] var2) {
      int var3 = var0.width * var0.height;
      int var4 = 0;
      int var5 = 0;
      int[] var6 = new int[var3];
      int[] var7 = new int[var3];
      Arrays.fill(var6, -1);

      for (int var8 = 0; var8 < var3; var8++) {
         if (planar(var0.pieces[var8]) && Float.isNaN(var0.floor[var8])) {
            var6[var8] = var8;
            var7[var5++] = var8;
         }
      }

      while (var4 < var5) {
         int var18 = var7[var4++];
         int var9 = var18 % var0.width;
         int var10 = var18 / var0.width;

         for (int var11 = 0; var11 < DX.length; var11++) {
            int var12 = var9 + DX[var11];
            int var13 = var10 + DZ[var11];
            int var14 = var13 * var0.width + var12;
            if (between(var0, var6, var12, var13, var11)) {
               int var15 = var6[var18];
               RoofPiece var16 = var0.pieces[var15];

               for (int var17 = 0; var17 < 9; var17++) {
                  var2[var17] = var16.height(var12 - var15 % var0.width + var17 % 3 / 2.0F - 0.5F, var13 - var15 / var0.width + var17 / 3 / 2.0F - 0.5F);
               }

               var1.set(var14, (byte)2, var2);
               var6[var14] = var15;
               var7[var5++] = var14;
            }
         }
      }
   }

   private static boolean between(RoofColumns var0, int[] var1, int var2, int var3, int var4) {
      int var5 = var3 * var0.width + var2;
      int var6 = var2 + DX[var4];
      int var7 = var3 + DZ[var4];
      int var8 = var7 * var0.width + var6;
      if (var2 >= 0 && var3 >= 0 && var2 < var0.width && var3 < var0.height && var1[var5] < 0 && var0.porch[var5]) {
         boolean var9 = var6 >= 0 && var7 >= 0 && var6 < var0.width && var7 < var0.height;
         return var9 && (!Float.isNaN(var0.floor[var8]) || var1[var8] >= 0 || var0.porch[var8] && wall(var0, var1, var6, var7, var4));
      } else {
         return false;
      }
   }

   private static boolean wall(RoofColumns var0, int[] var1, int var2, int var3, int var4) {
      int var5 = 0;

      while (var5 < var0.width + var0.height) {
         int var6 = (var3 + DZ[var4] * var5) * var0.width + var2 + DX[var4] * var5;
         int var7 = var2 + DX[var4] * var5;
         int var8 = var3 + DZ[var4] * var5;
         if (var7 >= 0 && var8 >= 0 && var7 < var0.width && var8 < var0.height) {
            if (Float.isNaN(var0.floor[var6]) && var1[var6] < 0) {
               if (!var0.porch[var6]) {
                  return false;
               }

               var5++;
               continue;
            }

            return true;
         }

         return false;
      }

      return false;
   }

   private static boolean planar(RoofPiece var0) {
      return var0 != null && var0.planar();
   }

   private static boolean open(RoofColumns var0, RoofSurface var1, int var2) {
      return var0.in[var2] && !var0.tiled[var2] && !Float.isNaN(var0.floor[var2]) && var1.source(var2 % var0.width, var2 / var0.width) == 0;
   }

   private static float rise(RoofPiece[] var0) {
      int[] var1 = new int[128];
      byte var2 = 3;

      for (RoofPiece var6 : var0) {
         if (var6 != null && var6.planar() && !var6.porch() && var6.kind != 5 && var6.kind != 7) {
            var1[var6.perStorey]++;
            var2 = var1[var6.perStorey] > var1[var2] ? var6.perStorey : var2;
         }
      }

      return 2.4494896F / var2;
   }

   private static void outline(RoofColumns var0, RoofSurface var1, boolean[] var2, float var3, float[] var4) {
      int var5 = var0.width * var0.height;
      boolean[] var6 = new boolean[var5];
      boolean var7 = halfStorey(var0);

      for (int var8 = 0; var8 < var5; var8++) {
         var6[var8] = open(var0, var1, var8);
      }

      for (int var9 = 0; var9 < var5; var9++) {
         if (var6[var9]) {
            outlineAt(var0, var1, var2, var6, var9, var3, var7, var4);
            var1.set(var9, (byte)2, var4);
         }
      }

      for (int var10 = 0; var10 < var5; var10++) {
         if (var6[var10]) {
            meet(var0, var1, var10, var4);
         }
      }
   }

   private static void outlineAt(RoofColumns var0, RoofSurface var1, boolean[] var2, boolean[] var3, int var4, float var5, boolean var6, float[] var7) {
      float var8 = var0.floor[var4] + (var6 ? 0.0F : 2.4494896F);
      float var9 = round(var0, var1, var3, var4);
      float var10 = Float.isNaN(var9) ? var8 + 2.4494896F : Math.max(var8, var9);
      RoofPiece var11 = nearest(var0, var2, var4);
      float var12 = var11 == null ? var5 : 2.4494896F / var11.perStorey;

      for (int var13 = 0; var13 < 9; var13++) {
         float var14 = var4 % var0.width + var13 % 3 / 2.0F;
         float var15 = var4 / var0.width + var13 / 3 / 2.0F;
         var7[var13] = Math.min(var10, var8 + var12 * edge(var0, var1, var4, var8, var14, var15));
      }
   }

   private static void meet(RoofColumns var0, RoofSurface var1, int var2, float[] var3) {
      int var4 = var2 % var0.width;
      int var5 = var2 / var0.width;
      boolean var6 = false;

      for (int var7 = 0; var7 < 9; var7++) {
         var3[var7] = var1.height(var4, var5, var7);
         float var8 = Float.NaN;

         for (int var9 = -1; var9 <= 1; var9++) {
            for (int var10 = -1; var10 <= 1; var10++) {
               int var11 = var7 % 3 - 2 * var10;
               int var12 = var7 / 3 - 2 * var9;
               int var13 = var4 + var10;
               int var14 = var5 + var9;
               if ((var10 | var9) != 0
                  && var11 >= 0
                  && var11 <= 2
                  && var12 >= 0
                  && var12 <= 2
                  && var13 >= 0
                  && var14 >= 0
                  && var13 < var0.width
                  && var14 < var0.height) {
                  RoofPiece var15 = var0.pieces[var14 * var0.width + var13];
                  float var16 = var1.height(var13, var14, var12 * 3 + var11);
                  if (var15 != null && var15.planar() && Math.abs(var16 - var3[var7]) < 2.4494896F / var15.perStorey) {
                     var8 = Float.isNaN(var8) ? var16 : Math.max(var8, var16);
                  }
               }
            }
         }

         var6 |= !Float.isNaN(var8);
         var3[var7] = Float.isNaN(var8) ? var3[var7] : var8;
      }

      if (var6) {
         var1.set(var2, (byte)2, var3);
      }
   }

   private static boolean halfStorey(RoofColumns var0) {
      byte var1 = 0;
      byte var2 = 0;

      for (int var3 = 0; var3 < var0.width * var0.height; var3++) {
         if (var0.pieces[var3] != null && !Float.isNaN(var0.floor[var3])) {
            int var4 = Math.round(var0.floor[var3] / 2.4494896F) + var0.reference;
            var1 += var0.pieceLevel[var3] == var4 ? 1 : 0;
            var2 += var0.pieceLevel[var3] == var4 + 1 ? 1 : 0;
         }
      }

      return var1 > var2;
   }

   private static float round(RoofColumns var0, RoofSurface var1, boolean[] var2, int var3) {
      float var4 = Float.NaN;
      int var5 = var3 % var0.width;
      int var6 = var3 / var0.width;

      for (int var7 = Math.max(0, var6 - 8); var7 <= Math.min(var0.height - 1, var6 + 8); var7++) {
         for (int var8 = Math.max(0, var5 - 8); var8 <= Math.min(var0.width - 1, var5 + 8); var8++) {
            if (!var2[var7 * var0.width + var8] && var1.stands(var8, var7)) {
               for (int var9 = 0; var9 < 9; var9++) {
                  float var10 = var1.height(var8, var7, var9);
                  var4 = Float.isNaN(var4) ? var10 : Math.max(var4, var10);
               }
            }
         }
      }

      return var4;
   }

   private static RoofPiece nearest(RoofColumns var0, boolean[] var1, int var2) {
      int var3 = var2 % var0.width;
      int var4 = var2 / var0.width;

      for (int var5 = 1; var5 <= 8; var5++) {
         for (int var6 = Math.max(0, var4 - var5); var6 <= Math.min(var0.height - 1, var4 + var5); var6++) {
            for (int var7 = Math.max(0, var3 - var5); var7 <= Math.min(var0.width - 1, var3 + var5); var7++) {
               RoofPiece var8 = Math.max(Math.abs(var7 - var3), Math.abs(var6 - var4)) == var5 ? var0.pieces[var6 * var0.width + var7] : null;
               if (var8 != null && var8.planar() && !var8.porch() && var8.kind != 5 && var8.kind != 6) {
                  return var8;
               }
            }
         }
      }

      return null;
   }

   private static float edge(RoofColumns var0, RoofSurface var1, int var2, float var3, float var4, float var5) {
      float var6 = 8.0F;
      int var7 = (int)Math.floor(var4);
      int var8 = (int)Math.floor(var5);

      for (int var9 = var8 - 8; var9 <= var8 + 8; var9++) {
         for (int var10 = var7 - 8; var10 <= var7 + 8; var10++) {
            int var11 = var9 * var0.width + var10;
            boolean var12 = var10 < 0
               || var9 < 0
               || var10 >= var0.width
               || var9 >= var0.height
               || !var0.in[var11]
               || !(var0.floor[var11] >= var0.floor[var2] - 0.001F)
               || var1.source(var10, var9) == 4 && var0.cap[var11] <= var3 + 0.001F;
            if (var12) {
               float var13 = Math.max(0.0F, Math.max(var10 - var4, var4 - (var10 + 1)));
               float var14 = Math.max(0.0F, Math.max(var9 - var5, var5 - (var9 + 1)));
               var6 = Math.min(var6, Math.max(var13, var14));
            }
         }
      }

      return var6;
   }
}
