package viewpoint.world;

import java.util.Arrays;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;

final class RoofGables {
   static final int PAINT_LENT = 16;
   static final int PAINT_OWN = 32;
   static final int STEPS = 64;
   private static final float STEP = 0.01F;
   private static final float ALONG = 0.01F;
   private static final int SIDES = 4;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   private static final int[][] POINTS = new int[][]{{6, 7, 8}, {2, 5, 8}, {0, 1, 2}, {0, 3, 6}};
   private static final int REACH = 16;
   private static final float LOW = 0.01F;
   private static final float PIXEL_UP = 0.02551552F;
   private static final float INSET = 0.96875F;
   static final float PAINT_AT = 0.25F;
   private static final int VERTICES = 48;

   static int open(RoofWindow var0, byte[] var1, int var2, int[] var3) {
      Arrays.fill(var3, -1);
      byte var4 = 0;

      for (int var5 = 0; var5 < 4; var5++) {
         if (outer(var0, var2, var5) && !ended(var0, var1, var2, var5)) {
            boolean var6 = walled(var0, var1, var2, var5);
            if (!var6 && !eave(var0.pieces[var2], var5)) {
               int var7 = farEnd(var0, var1, var2, var5);
               var3[var5] = var7 >= 0 && tiledAlong(var0, var1, var2, var5) ? var7 : -1;
               var4 |= var3[var5] < 0 && !overRooms(var0, var2, var5) ? 0 : 1 << var5;
            } else {
               var4 |= !var6 && !overRooms(var0, var2, var5) ? 0 : 1 << var5;
            }
         }
      }

      return var4;
   }

   static int colour(IsoCell var0, int var1, int var2, int var3, int var4) {
      int var5 = (Integer.numberOfTrailingZeros(var4) + 2) % 4;

      for (int var6 = 0; var6 <= 16; var6++) {
         IsoGridSquare var7 = var0.getGridSquare(var1 + DX[var5] * var6, var2 + DZ[var5] * var6, var3 - 1);
         if (var7 != null && var7.getRoom() != null) {
            return Facades.outside(var7);
         }
      }

      return 9210501;
   }

   static boolean roofed(RoofWindow var0, int var1) {
      byte var2 = var0.flags[var1];
      return var0.pieces[var1] != null || (var2 & 65) != 0 || (var2 & 6) == 4;
   }

   private static boolean outer(RoofWindow var0, int var1, int var2) {
      int var3 = var1 % var0.width + DX[var2];
      int var4 = var1 / var0.width + DZ[var2];
      int var5 = var4 * var0.width + var3;
      return roofed(var0, var1) && var3 >= 0 && var4 >= 0 && var3 < var0.width && var4 < var0.height && !roofed(var0, var5) && (var0.flags[var5] & 16) == 0;
   }

   private static boolean gableEdge(RoofWindow var0, byte[] var1, int var2, int var3) {
      return outer(var0, var2, var3) && !walled(var0, var1, var2, var3) && !eave(var0.pieces[var2], var3);
   }

   private static boolean ended(RoofWindow var0, byte[] var1, int var2, int var3) {
      int var4 = var2 + DZ[var3] * var0.width + DX[var3];
      byte var5 = var1[edgeSquare(var2, var4, var3)];
      return (var5 & 128) != 0 && (var5 & gable(var3)) != 0;
   }

   private static boolean walled(RoofWindow var0, byte[] var1, int var2, int var3) {
      int var4 = var2 + DZ[var3] * var0.width + DX[var3];
      return (var1[edgeSquare(var2, var4, var3)] & wall(var3)) != 0;
   }

   private static boolean tiledAlong(RoofWindow var0, byte[] var1, int var2, int var3) {
      int var4 = DZ[var3] != 0 ? 1 : 0;
      int var5 = 1 - var4;
      int var6 = var2 % var0.width;
      int var7 = var2 / var0.width;

      for (int var8 = -1; var8 <= 1; var8 += 2) {
         for (int var9 = 1; var9 <= 16; var9++) {
            int var10 = var6 + var4 * var8 * var9;
            int var11 = var7 + var5 * var8 * var9;
            int var12 = var11 * var0.width + var10;
            if (var10 < 0 || var11 < 0 || var10 >= var0.width || var11 >= var0.height || !gableEdge(var0, var1, var12, var3)) {
               break;
            }

            if (farEnd(var0, var1, var12, var3) < 0) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean overRooms(RoofWindow var0, int var1, int var2) {
      int var3 = (var2 + 2) % 4;
      int var4 = var1 % var0.width;
      int var5 = var1 / var0.width;

      for (int var6 = 0; var6 <= 16; var6++) {
         int var7 = var4 + DX[var3] * var6;
         int var8 = var5 + DZ[var3] * var6;
         int var9 = var8 * var0.width + var7;
         if (var7 < 0 || var8 < 0 || var7 >= var0.width || var8 >= var0.height || !roofed(var0, var9)) {
            return false;
         }

         if ((var0.flags[var9] & 4) != 0) {
            return true;
         }
      }

      return false;
   }

   private static boolean eave(RoofPiece var0, int var1) {
      if (var0 == null) {
         return false;
      }

      for (int var5 : POINTS[var1]) {
         if (var0.height(var5 % 3 / 2.0F - 0.5F, var5 / 3 / 2.0F - 0.5F) > 0.01F) {
            return false;
         }
      }

      return true;
   }

   private static int farEnd(RoofWindow var0, byte[] var1, int var2, int var3) {
      int var4 = (var3 + 2) % 4;
      int var5 = var2 % var0.width;
      int var6 = var2 / var0.width;
      int var7 = var2;

      for (int var8 = 1; var8 <= 16; var8++) {
         int var9 = var5 + DX[var4] * var8;
         int var10 = var6 + DZ[var4] * var8;
         if (var9 < 0 || var10 < 0 || var9 >= var0.width || var10 >= var0.height) {
            return -1;
         }

         int var11 = var10 * var0.width + var9;
         if (!roofed(var0, var11)) {
            int var12 = edgeSquare(var7, var11, var4);
            return (var1[var12] & gable(var4)) != 0 ? var12 : -1;
         }

         if (!same(var0.pieces[var11], var0.pieces[var2])) {
            return -1;
         }

         var7 = var11;
      }

      return -1;
   }

   private static int edgeSquare(int var0, int var1, int var2) {
      return var2 != 2 && var2 != 3 ? var1 : var0;
   }

   private static int wall(int var0) {
      return across(var0) ? 8 : 16;
   }

   private static int gable(int var0) {
      return across(var0) ? 32 : 64;
   }

   static int inner(RoofWindow var0, int var1) {
      byte var2 = 0;

      for (int var3 = 0; var3 < 4; var3++) {
         int var4 = var1 % var0.width + DX[var3];
         int var5 = var1 / var0.width + DZ[var3];
         boolean var6 = var4 >= 0 && var5 >= 0 && var4 < var0.width && var5 < var0.height;
         var2 |= var6 && roofed(var0, var5 * var0.width + var4) ? 1 << var3 : 0;
      }

      return var2;
   }

   static boolean across(int var0) {
      return var0 == 2 || var0 == 0;
   }

   private static boolean same(RoofPiece var0, RoofPiece var1) {
      return var0 != null && var1 != null
         ? var0.kind == var1.kind
            && var0.facing == var1.facing
            && var0.other == var1.other
            && var0.perStorey == var1.perStorey
            && var0.step == var1.step
            && var0.rise == var1.rise
            && var0.middle == var1.middle
         : var0 == var1;
   }

   static float[] vertices(RoofSurface var0, RoofWindow var1, Recipe.Op var2) {
      int var3 = var1.innerSquare(var2.dx, var2.dy);
      int var4 = var1.square(var3);
      int var5 = var4 % var1.width;
      int var6 = var4 / var1.width;
      if (!var0.built(var5, var6)) {
         return new float[0];
      }

      RoofGables.Faces var7 = new RoofGables.Faces(var2, paintArt(var1, var3, var4, var2.face));

      for (int var8 = 0; var8 < 4; var8++) {
         int var9 = var5 + DX[var8];
         int var10 = var6 + DZ[var8];
         boolean var11 = var9 >= 0 && var10 >= 0 && var9 < var1.width && var10 < var1.height;
         if ((var2.face & 1 << var8) != 0) {
            if ((var2.face & 64) != 0 && var11 && roofed(var1, var10 * var1.width + var9)) {
               if (var0.stands(var9, var10)) {
                  var7.step(var0, var5, var6, var9, var10, var8);
               }
            } else if (!var11 || !var0.built(var9, var10)) {
               var7.side(var0, var5, var6, var8);
            }
         }
      }

      return Arrays.copyOf(var7.vertices, var7.n);
   }

   private static RoofPiece paintArt(RoofWindow var0, int var1, int var2, int var3) {
      if ((var3 & 16) != 0) {
         return var0.art(var0.lenders[var1]);
      } else {
         return (var3 & 32) != 0 ? var0.art(var2) : null;
      }
   }

   private RoofGables() {
   }

   private static final class Faces {
      final Recipe.Op op;
      final RoofPiece paint;
      final float[] vertices = new float[672];
      int n;

      Faces(Recipe.Op var1, RoofPiece var2) {
         this.op = var1;
         this.paint = var2;
      }

      void side(RoofSurface var1, int var2, int var3, int var4) {
         int[] var5 = RoofGables.POINTS[var4];
         float[] var6 = new float[3];

         for (int var7 = 0; var7 < 3; var7++) {
            var6[var7] = Math.max(0.0F, var1.height(var2, var3, var5[var7]));
         }

         if (!(Math.max(var6[0], Math.max(var6[1], var6[2])) <= 0.01F)) {
            this.strips(var5, new float[3], var6, var4);
         }
      }

      void step(RoofSurface var1, int var2, int var3, int var4, int var5, int var6) {
         int[] var7 = RoofGables.POINTS[var6];
         int[] var8 = RoofGables.POINTS[(var6 + 2) % 4];
         float[] var9 = new float[3];
         float[] var10 = new float[3];
         boolean var11 = false;

         for (int var12 = 0; var12 < 3; var12++) {
            var9[var12] = var1.height(var2, var3, var7[var12]);
            var10[var12] = Math.min(var9[var12], var1.height(var4, var5, var8[var12]));
            var11 |= var9[var12] - var10[var12] > 0.01F;
         }

         float var13 = Math.max(var9[0], Math.max(var9[1], var9[2])) - Math.min(var9[0], Math.min(var9[1], var9[2]));
         if (var11 && var13 > 0.01F) {
            this.strips(var7, var10, var9, var6);
         }
      }

      private void strips(int[] var1, float[] var2, float[] var3, int var4) {
         for (int var5 = 0; var5 < 2; var5++) {
            int var6 = var1[var5];
            int var7 = var1[var5 + 1];
            if (var3[var5 + 1] > var2[var5 + 1]) {
               this.vertex(var6, var2[var5], var4);
               this.vertex(var7, var2[var5 + 1], var4);
               this.vertex(var7, var3[var5 + 1], var4);
            }

            if (var3[var5] > var2[var5]) {
               this.vertex(var6, var2[var5], var4);
               this.vertex(var7, var3[var5 + 1], var4);
               this.vertex(var6, var3[var5], var4);
            }
         }
      }

      private void vertex(int var1, float var2, int var3) {
         float var4 = var1 % 3 / 2.0F - 0.5F;
         float var5 = var1 / 3 / 2.0F - 0.5F;
         float var6;
         float var7;
         if (this.paint != null) {
            var6 = 32.0F;
            var7 = 120.0F - 39.191833F * this.paint.height(0.25F, 0.25F);
         } else {
            boolean var8 = RoofGables.across(var3);
            float var9 = var8 ? var4 * 0.96875F : -0.46875F;
            float var10 = var8 ? -0.4375F : var5 * 0.96875F;
            float var11 = Math.max(0.02551552F, Math.min(2.4494896F, var2) - 0.02551552F);
            var6 = 32.0F * (var9 - var10 + 1.0F);
            var7 = 112.0F + 16.0F * (var9 + var10) - 39.191833F * var11;
         }

         float[] var12 = this.op.map;
         this.n = Cook.put(
            this.vertices,
            this.n,
            -(this.op.dx + var4),
            this.op.height + var2,
            -(this.op.dy + var5),
            var12[4] + var6 * var12[6],
            var12[5] + var7 * var12[7],
            var12[0],
            var12[1],
            var12[2],
            var12[3],
            -RoofGables.DX[var3],
            0.0F,
            -RoofGables.DZ[var3],
            this.op.fill,
            0
         );
      }
   }
}
