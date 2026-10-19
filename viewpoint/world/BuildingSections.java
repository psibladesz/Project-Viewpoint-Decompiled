package viewpoint.world;

import java.util.ArrayList;
import java.util.Arrays;

final class BuildingSections {
   private static final int WALK = 12;
   private static final float EPS = 0.001F;
   private static final float LEVEL = 0.01F;
   private static final int SPAN = 8;
   private static final int OLDER_PITCH = 3;
   private static final byte OFF = 0;
   private static final byte MIRROR = 1;
   private static final byte ODD = 2;
   private static final byte WIDE = 3;
   private static final byte RIDGE = 4;
   private static final byte AGAINST = 5;
   private static final byte CAPPED = 6;
   private static final byte KINDS = 7;
   private static final byte EDGE = 0;
   private static final byte WALL = 1;
   private static final byte PARALLEL = 2;
   private static final byte LIMIT = 3;
   private static final byte WINDOW = 4;
   private static final byte STOREY = 5;
   private static final int UNKNOWN = Integer.MIN_VALUE;
   private static final int NO_MIRROR = -1073741824;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   private final int width;
   private final int height;
   private final RoofColumns c;
   private final RoofPiece[] pieces;
   private final boolean[] shallow;
   private final int[][] faceOf;
   private final float[] best;
   private int[] far;
   private int[] from;
   private float[] cap;
   private float faceFloor;
   private float roomFloor;

   private BuildingSections(RoofColumns var1, RoofPiece[] var2, boolean[] var3) {
      this.width = var1.width;
      this.height = var1.height;
      this.c = var1;
      this.pieces = var2;
      this.shallow = var3;
      this.faceOf = new int[4][this.width * this.height];
      this.best = new float[this.width * this.height * 9];

      for (int[] var7 : this.faceOf) {
         Arrays.fill(var7, -1);
      }

      Arrays.fill(this.best, Float.NaN);
   }

   static void build(RoofColumns var0, RoofPiece[] var1, boolean[] var2, RoofSurface var3) {
      BuildingSections var4 = new BuildingSections(var0, var1, var2);
      ArrayList var5 = RoofFace.of(var0.width, var1, var4::leads, var4.faceOf);

      for (int var6 = 0; var6 < var5.size(); var6++) {
         RoofFace var7 = (RoofFace)var5.get(var6);
         var4.faceFloor = var4.floorOf(var7);
         var4.roomFloor = var4.roomFloorOf(var7);
         var4.walls(var7);
         var4.cover(var7, var6);
      }

      var4.apply(var3);
   }

   private float floorOf(RoofFace var1) {
      int var2 = Integer.MAX_VALUE;

      for (int var6 : var1.squares) {
         var2 = Math.min(var2, this.c.pieceLevel[var6]);
      }

      return (var2 - this.c.reference) * 2.4494896F;
   }

   private float roomFloorOf(RoofFace var1) {
      float var2 = Float.POSITIVE_INFINITY;

      for (int var6 : var1.squares) {
         var2 = Float.isNaN(this.c.floor[var6]) ? var2 : Math.min(var2, this.c.floor[var6]);
      }

      return var2 == Float.POSITIVE_INFINITY ? Float.NEGATIVE_INFINITY : var2;
   }

   private float farEave(RoofFace var1, int var2) {
      return var1.perStorey == 3 && !this.risesFromFlat(var1, var2) ? var1.e(var2) : this.faceFloor;
   }

   private boolean risesFromFlat(RoofFace var1, int var2) {
      int var3 = var1.square(this.width, this.height, var2, var1.eave(var2));
      if (var3 < 0) {
         return false;
      }

      RoofPiece var4 = this.pieces[var3];
      float var5 = planar(var4) && var4.kind == 5 ? var4.height(0.0F, 0.0F) : (this.hidden(var3) ? this.c.cap[var3] : Float.POSITIVE_INFINITY);
      return Math.abs(var5 - var1.e(var2)) <= 0.01F;
   }

   private void walls(RoofFace var1) {
      int var2 = var1.top.length;
      byte[] var3 = new byte[var2];
      int[] var4 = new int[var2];
      int[] var5 = new int[var2];
      int[] var6 = new int[var2];
      float[] var7 = new float[var2];
      this.far = new int[var2];
      this.from = new int[var2];
      this.cap = new float[var2];

      for (int var8 = 0; var8 < var2; var8++) {
         var6[var8] = 2 * var1.upper(var8) - var1.c + Math.round(this.farEave(var1, var8) / var1.rise);
         var3[var8] = var1.top[var8] == var1.upper(var8) ? this.look(var1, var8, var6[var8], var4, var5, var7) : 0;
         var4[var8] = mirror(var3[var8], var6[var8], var4[var8]);
      }

      for (int var10 = 0; var10 < var2; var10++) {
         byte var9 = vote(var3, var10, false);
         var9 = var9 == 6 && var3[var10] != 0 && var3[var10] != 6 ? vote(var3, var10, true) : var9;
         this.decide(var1, var10, var9, var6[var10], var3, var4, var5, var7);
      }
   }

   private static int mirror(byte var0, int var1, int var2) {
      return switch (var0) {
         case 1 -> var1;
         case 2 -> var1 - 1;
         default -> var2;
         case 4 -> var1 + 1;
      };
   }

   private static byte vote(byte[] var0, int var1, boolean var2) {
      int[] var3 = new int[7];

      for (int var4 = Math.max(0, var1 - 8); var4 <= Math.min(var0.length - 1, var1 + 8); var4++) {
         var3[var0[var4]] = var3[var0[var4]] + (var2 && var0[var4] == 6 ? 0 : 1);
      }

      byte var6 = 1;

      for (int var5 = 1; var5 < 7; var5++) {
         var6 = (byte) (var3[var5] >= var3[var6] && var3[var5] > 0 ? var5 : var6);
      }

      return var6;
   }

   private byte look(RoofFace var1, int var2, int var3, int[] var4, int[] var5, float[] var6) {
      int var7 = var1.square(this.width, this.height, var2, var1.top[var2]);
      if (ridge(this.pieces[var7], var1.facing)) {
         return 4;
      } else {
         byte var8 = this.walk(var1, var7, var3 - 2, var2, var4, var5, var6);
         if (var6[var2] < Float.POSITIVE_INFINITY) {
            return 6;
         } else {
            return (byte)(var8 == 1 ? 5 : (var8 == 3 ? 3 : (var8 == 0 && var4[var2] == var3 - 1 ? 2 : 1)));
         }
      }
   }

   private void decide(RoofFace var1, int var2, byte var3, int var4, byte[] var5, int[] var6, int[] var7, float[] var8) {
      int var9 = mode(var5, var6, var2, var3);
      this.far[var2] = var9 == Integer.MIN_VALUE ? var4 : var9;
      this.cap[var2] = Float.POSITIVE_INFINITY;
      this.from[var2] = this.far[var2];
      if (var3 == 3) {
         this.far[var2] = -1073741824;
         this.from[var2] = var1.top[var2];
      } else if (var3 == 5 || var3 == 6) {
         this.far[var2] = -1073741824;
         this.from[var2] = Integer.MAX_VALUE;
         this.cap[var2] = var3 == 5 ? var1.rise * (var1.c - var1.upper(var2)) : this.cap[var2];

         for (int var10 = Math.max(0, var2 - 8); var10 <= Math.min(var5.length - 1, var2 + 8); var10++) {
            if (var5[var10] == var3) {
               this.cap[var2] = var3 == 6 ? Math.min(this.cap[var2], var8[var10]) : this.cap[var2];
               this.from[var2] = Math.min(this.from[var2], var7[var10]);
            }
         }
      }
   }

   private byte walk(RoofFace var1, int var2, int var3, int var4, int[] var5, int[] var6, float[] var7) {
      int var8 = (var1.facing + 2) % 4;
      int var9 = var2;
      byte var10 = 0;
      float var11 = var1.rise * (var1.c - RoofFace.u(this.width, var2, var1.facing));
      var7[var4] = Float.POSITIVE_INFINITY;

      for (int var12 = 0; var12 < 12; var12++) {
         int var13 = this.neighbour(var9, var8);
         byte var14 = var13 < 0 ? 4 : this.stop(var1, var13, var11, var1.e(var4), var7[var4], var3);
         if (var14 >= 0) {
            var5[var4] = var14 == 4 ? Integer.MIN_VALUE : RoofFace.u(this.width, var9, var1.facing);
            var6[var4] = var7[var4] < Float.POSITIVE_INFINITY ? var10 : RoofFace.u(this.width, var9, var1.facing);
            return var14;
         }

         var10 = (byte)(var7[var4] == Float.POSITIVE_INFINITY ? RoofFace.u(this.width, var13, var1.facing) + 1 : var10);
         var7[var4] = Math.min(var7[var4], this.flat(var13, var11));
         var9 = var13;
      }

      var5[var4] = Integer.MIN_VALUE;
      var6[var4] = var7[var4] < Float.POSITIVE_INFINITY ? var10 : RoofFace.u(this.width, var9, var1.facing);
      return 4;
   }

   private byte stop(RoofFace var1, int var2, float var3, float var4, float var5, int var6) {
      RoofPiece var7 = this.pieces[var2];
      boolean var8 = this.flat(var2, var3) < Float.POSITIVE_INFINITY;
      if (this.c.in[var2] && (!(var5 < Float.POSITIVE_INFINITY) || var8)) {
         if (this.hidden(var2) && this.c.cap[var2] <= var4 + 0.001F) {
            return 0;
         } else if (this.c.floor[var2] < this.roomFloor - 0.001F) {
            return 5;
         } else if (this.c.floor[var2] > this.roomFloor + 0.001F && var3 + 0.001F < this.c.floor[var2] + 2.4494896F) {
            return 1;
         } else if (planar(var7) && !var8 && parallel(var7, var1.facing)) {
            return 2;
         } else {
            return (byte)(var5 == Float.POSITIVE_INFINITY && RoofFace.u(this.width, var2, var1.facing) < var6 ? 3 : -1);
         }
      } else {
         return 0;
      }
   }

   private float flat(int var1, float var2) {
      RoofPiece var3 = this.pieces[var1];
      float var4 = planar(var3) && var3.kind == 5 ? var3.height(0.0F, 0.0F) : this.c.cap[var1];
      return var2 <= var4 + 0.001F ? var4 : Float.POSITIVE_INFINITY;
   }

   private static int mode(byte[] var0, int[] var1, int var2, byte var3) {
      int var4 = Integer.MIN_VALUE;
      byte var5 = 0;

      for (int var6 = Math.max(0, var2 - 8); var6 <= Math.min(var1.length - 1, var2 + 8); var6++) {
         if (var0[var6] == var3 && var1[var6] != Integer.MIN_VALUE) {
            byte var7 = 0;

            for (int var8 = Math.max(0, var2 - 8); var8 <= Math.min(var1.length - 1, var2 + 8); var8++) {
               var7 += var0[var8] == var3 && var1[var8] == var1[var6] ? 1 : 0;
            }

            if (var7 > var5 || var7 == var5 && var1[var6] > var4) {
               var4 = var1[var6];
               var5 = var7;
            }
         }
      }

      return var4;
   }

   private void cover(RoofFace var1, int var2) {
      for (int var3 = 0; var3 < var1.top.length; var3++) {
         int var4 = this.from[var3];

         for (int var5 = var1.eave(var3); var4 < var5; var4++) {
            int var6 = var1.square(this.width, this.height, var3, var4);
            if (var6 >= 0 && this.faceOf[var1.facing][var6] != var2 && this.raisable(var6, var1.facing)) {
               this.points(var1, var3, var6);
            }
         }
      }
   }

   private void points(RoofFace var1, int var2, int var3) {
      int var4 = var3 % this.width;
      int var5 = var3 / this.width;

      for (int var6 = 0; var6 < 9; var6++) {
         float var7 = var4 + var6 % 3 / 2.0F;
         float var8 = var5 + var6 / 3 / 2.0F;
         float var9 = Math.min(var1.height(var2, var7, var8), this.cap[var2]);
         if (this.far[var2] != -1073741824) {
            var9 = Math.min(var9, var1.rise * (var1.u(var7, var8) - this.far[var2]) + this.farEave(var1, var2));
         }

         int var10 = var3 * 9 + var6;
         var9 = Math.max(var9, this.faceFloor);
         this.best[var10] = Float.isNaN(this.best[var10]) ? var9 : Math.max(this.best[var10], var9);
      }
   }

   private void apply(RoofSurface var1) {
      float[] var2 = new float[9];
      byte var3 = 4;

      for (int var4 = 0; var4 < this.pieces.length; var4++) {
         int var5 = var4 % this.width;
         int var6 = var4 / this.width;
         int var7 = var4 * 9;
         if (!Float.isNaN(this.best[var7])) {
            if (this.hidden(var4)) {
               float var10 = this.best[var7 + var3];
               boolean var11 = this.c.cap[var4] < Float.POSITIVE_INFINITY && var10 <= this.c.cap[var4] + 0.001F;
               if (!(var10 < this.c.floor[var4] - 0.001F) && !var11) {
                  System.arraycopy(this.best, var7, var2, 0, 9);
                  var1.set(var4, (byte)2, var2);
               }
            } else if (var1.source(var5, var6) == 1) {
               boolean var8 = false;

               for (int var9 = 0; var9 < 9; var9++) {
                  var2[var9] = Math.max(var1.height(var5, var6, var9), this.best[var7 + var9]);
                  var8 |= var2[var9] > var1.height(var5, var6, var9);
               }

               if (var8) {
                  var1.set(var4, (byte)1, var2);
               }
            }
         }
      }
   }

   private boolean raisable(int var1, int var2) {
      return this.hidden(var1) || this.leads(var1) && !parallel(this.pieces[var1], var2);
   }

   private boolean leads(int var1) {
      RoofPiece var2 = this.pieces[var1];
      return planar(var2) && var2.kind != 5 && (!var2.porch() || this.shallow[var1]);
   }

   private static boolean planar(RoofPiece var0) {
      return var0 != null && var0.planar();
   }

   private static boolean ridge(RoofPiece var0, int var1) {
      return (var0.kind == 1 || var0.kind == 6) && RoofFace.has(var0, var1) && RoofFace.has(var0, (var1 + 2) % 4);
   }

   private static boolean parallel(RoofPiece var0, int var1) {
      return var0.kind != 5 && (RoofFace.has(var0, var1) || RoofFace.has(var0, (var1 + 2) % 4));
   }

   private boolean hidden(int var1) {
      return this.c.in[var1] && !this.c.tiled[var1] && this.pieces[var1] == null && !Float.isNaN(this.c.floor[var1]);
   }

   private int neighbour(int var1, int var2) {
      int var3 = var1 % this.width + DX[var2];
      int var4 = var1 / this.width + DZ[var2];
      return var3 >= 0 && var4 >= 0 && var3 < this.width && var4 < this.height ? var4 * this.width + var3 : -1;
   }
}
