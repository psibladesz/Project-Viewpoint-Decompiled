package viewpoint.world;

import java.util.ArrayList;
import java.util.Arrays;

final class RoofSections {
   private static final int WALK = 12;
   private static final int SPAN = 8;
   private static final float EPS = 0.001F;
   private static final byte OFF = 0;
   private static final byte MIRROR = 1;
   private static final byte ODD = 2;
   private static final byte WIDE = 3;
   private static final byte RIDGE = 4;
   private static final byte AGAINST = 5;
   private static final byte RUNS_ON = 6;
   private static final byte CAPPED = 7;
   private static final byte KINDS = 8;
   private static final byte EDGE = 0;
   private static final byte WALL = 1;
   private static final byte PARALLEL = 2;
   private static final byte LIMIT = 3;
   private static final byte WINDOW = 4;
   private static final int UNKNOWN = Integer.MIN_VALUE;
   private static final int NO_MIRROR = -1073741824;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   private final int width;
   private final int height;
   private final RoofPiece[] pieces;
   private final byte[] flags;
   private final boolean[] shallow;
   private final int[][] faceOf;
   private final float[] best;
   private boolean[] reads;
   private int[] far;
   private int[] from;
   private float[] cap;

   private RoofSections(int var1, int var2, RoofPiece[] var3, byte[] var4, boolean[] var5) {
      this.width = var1;
      this.height = var2;
      this.pieces = var3;
      this.flags = var4;
      this.shallow = var5;
      this.faceOf = new int[4][var1 * var2];
      this.best = new float[var1 * var2 * 9];

      for (int[] var9 : this.faceOf) {
         Arrays.fill(var9, -1);
      }

      Arrays.fill(this.best, Float.NaN);
   }

   static boolean[] build(int var0, int var1, RoofPiece[] var2, byte[] var3, boolean[] var4, RoofSurface var5) {
      RoofSections var6 = new RoofSections(var0, var1, var2, var3, var4);
      ArrayList var7 = RoofFace.of(var0, var2, var6::leads, var6.faceOf);
      boolean[] var8 = new boolean[var2.length];

      for (int var9 = 0; var9 < var7.size(); var9++) {
         RoofFace var10 = (RoofFace)var7.get(var9);
         var6.reads(var10);
         var6.walls(var10);
         var6.cover(var10, var9);

         for (int var14 : var10.squares) {
            var8[var14] |= var6.reads[RoofFace.along(var0, var14, var10.facing) - var10.a0];
         }
      }

      var6.apply(var5, var8);
      return var8;
   }

   private void reads(RoofFace var1) {
      int var2 = var1.top.length;
      boolean[] var3 = new boolean[var2];
      this.reads = new boolean[var2];

      for (int var4 = 0; var4 < var2; var4++) {
         int var5 = var1.square(this.width, this.height, var4, var1.top[var4]);
         int var6 = var1.square(this.width, this.height, var4, var1.bottom[var4]);
         boolean var7 = var1.rides || var1.stacked[var4] || var5 >= 0 && ridge(this.pieces[var5], var1.facing);
         var3[var4] = var7 && (var6 < 0 || !this.hidden(var6));
      }

      for (int var8 = 0; var8 < var2; var8++) {
         for (int var9 = Math.max(0, var8 - 8); var9 <= Math.min(var2 - 1, var8 + 8) && !this.reads[var8]; var9++) {
            this.reads[var8] = var3[var9];
         }
      }
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
         var6[var8] = 2 * var1.upper(var8) - var1.c + Math.round(var1.farEave(var8) / var1.rise);
         var3[var8] = var1.top[var8] == var1.upper(var8) ? this.look(var1, var8, var6[var8], var4, var5, var7) : 0;
         var4[var8] = mirror(var3[var8], var6[var8], var4[var8]);
      }

      for (int var10 = 0; var10 < var2; var10++) {
         byte var9 = vote(var3, var10, false);
         var9 = var9 == 7 && var3[var10] != 0 && var3[var10] != 7 ? vote(var3, var10, true) : var9;
         this.decide(var1, var10, var9, var6[var10], var3, var4, var5, var7);
      }
   }

   private static int mirror(byte var0, int var1, int var2) {
      return switch (var0) {
         case 1 -> var1;
         case 2 -> var1 - 1;
         default -> var2;
         case 4 -> var1 + 1;
         case 6 -> var2 == Integer.MIN_VALUE ? var1 : Math.min(var2, var1);
      };
   }

   private static byte vote(byte[] var0, int var1, boolean var2) {
      int[] var3 = new int[8];

      for (int var4 = Math.max(0, var1 - 8); var4 <= Math.min(var0.length - 1, var1 + 8); var4++) {
         var3[var0[var4]] = var3[var0[var4]] + (var2 && var0[var4] == 7 ? 0 : 1);
      }

      byte var6 = 1;

      for (int var5 = 1; var5 < 8; var5++) {
         var6 = (byte) (var3[var5] >= var3[var6] && var3[var5] > 0 ? var5 : var6);
      }

      return var6;
   }

   private byte look(RoofFace var1, int var2, int var3, int[] var4, int[] var5, float[] var6) {
      int var7 = var1.square(this.width, this.height, var2, var1.top[var2]);
      if (ridge(this.pieces[var7], var1.facing)) {
         return 4;
      } else {
         boolean var8 = this.runsOn(var1, var7);
         byte var9 = this.walk(var1, var7, var8, var8 ? Integer.MIN_VALUE : var3 - 2, var2, var4, var5, var6);
         if (var6[var2] < Float.POSITIVE_INFINITY) {
            return 7;
         } else if (var8) {
            return 6;
         } else {
            return (byte)(var9 == 1 ? 5 : (var9 == 3 ? 3 : (var9 == 0 && var4[var2] == var3 - 1 ? 2 : 1)));
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
      } else if (var3 == 5 || var3 == 7) {
         this.far[var2] = -1073741824;
         this.from[var2] = Integer.MAX_VALUE;
         this.cap[var2] = var3 == 5 ? var1.rise * (var1.c - var1.upper(var2)) : this.cap[var2];

         for (int var10 = Math.max(0, var2 - 8); var10 <= Math.min(var5.length - 1, var2 + 8); var10++) {
            if (var5[var10] == var3) {
               this.cap[var2] = var3 == 7 ? Math.min(this.cap[var2], var8[var10]) : this.cap[var2];
               this.from[var2] = Math.min(this.from[var2], var7[var10]);
            }
         }
      }
   }

   private boolean runsOn(RoofFace var1, int var2) {
      int var3 = this.neighbour(var2, (var1.facing + 2) % 4);
      float var4 = var1.rise * (var1.c - RoofFace.u(this.width, var2, var1.facing));
      return Math.abs(var4 - 2.4494896F) <= 0.001F && var3 >= 0 && this.room(var3);
   }

   private boolean room(int var1) {
      return !planar(this.pieces[var1]) && (this.flags[var1] & 10) != 0;
   }

   private byte walk(RoofFace var1, int var2, boolean var3, int var4, int var5, int[] var6, int[] var7, float[] var8) {
      int var9 = (var1.facing + 2) % 4;
      int var10 = var2;
      float var11 = var1.rise * (var1.c - RoofFace.u(this.width, var2, var1.facing));
      boolean var12 = var3;
      var8[var5] = Float.POSITIVE_INFINITY;

      for (int var13 = 0; var13 < 12; var13++) {
         int var14 = this.neighbour(var10, var9);
         byte var15 = var14 < 0 ? 4 : this.stop(var1, var14, var11, var12, var3, var8[var5], var4);
         if (var15 >= 0) {
            var6[var5] = var15 == 4 ? Integer.MIN_VALUE : RoofFace.u(this.width, var10, var1.facing);
            var7[var5] = RoofFace.u(this.width, var10, var1.facing);
            return var15;
         }

         var12 &= this.room(var14);
         var8[var5] = Math.min(var8[var5], this.flat(var14, var11));
         var10 = var14;
      }

      var6[var5] = Integer.MIN_VALUE;
      var7[var5] = RoofFace.u(this.width, var10, var1.facing);
      return 4;
   }

   private byte stop(RoofFace var1, int var2, float var3, boolean var4, boolean var5, float var6, int var7) {
      byte var8 = this.flags[var2];
      RoofPiece var9 = this.pieces[var2];
      boolean var10 = this.flat(var2, var3) < Float.POSITIVE_INFINITY;
      if (var8 != 0 && (!(var6 < Float.POSITIVE_INFINITY) || var10)) {
         if ((var8 & 16) == 0 && (!this.room(var2) || var4)) {
            if (!planar(var9) || var10 || !var5 && !parallel(var9, var1.facing)) {
               return (byte)(var6 == Float.POSITIVE_INFINITY && var7 != Integer.MIN_VALUE && RoofFace.u(this.width, var2, var1.facing) < var7 ? 3 : -1);
            } else {
               return 2;
            }
         } else {
            return 1;
         }
      } else {
         return 0;
      }
   }

   private float flat(int var1, float var2) {
      RoofPiece var3 = this.pieces[var1];
      float var4 = planar(var3) && var3.kind == 5 ? var3.height(0.0F, 0.0F) : ((this.flags[var1] & 32) != 0 ? 2.4494896F : Float.POSITIVE_INFINITY);
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
         if (this.reads[var3]) {
            int var4 = this.from[var3];

            for (int var5 = var1.eave(var3); var4 < var5; var4++) {
               int var6 = var1.square(this.width, this.height, var3, var4);
               if (var6 >= 0 && this.faceOf[var1.facing][var6] != var2 && this.raisable(var6, var1.facing)) {
                  this.points(var1, var3, var6);
               }
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
            var9 = Math.min(var9, var1.rise * (var1.u(var7, var8) - this.far[var2]) + var1.farEave(var2));
         }

         int var10 = var3 * 9 + var6;
         this.best[var10] = Float.isNaN(this.best[var10]) ? var9 : Math.max(this.best[var10], var9);
      }
   }

   private void apply(RoofSurface var1, boolean[] var2) {
      float[] var3 = new float[9];

      for (int var4 = 0; var4 < this.pieces.length; var4++) {
         int var5 = var4 % this.width;
         int var6 = var4 / this.width;
         int var7 = var4 * 9;
         if (!Float.isNaN(this.best[var7])) {
            if (this.hidden(var4)) {
               for (int var10 = 0; var10 < 9; var10++) {
                  var3[var10] = Math.max(0.0F, this.best[var7 + var10]);
               }

               var1.set(var4, (byte)2, var3);
               var2[var4] = true;
            } else if (var1.source(var5, var6) == 1) {
               boolean var8 = false;

               for (int var9 = 0; var9 < 9; var9++) {
                  var3[var9] = Math.max(var1.height(var5, var6, var9), this.best[var7 + var9]);
                  var8 |= var3[var9] > var1.height(var5, var6, var9);
               }

               if (var8) {
                  var1.set(var4, (byte)1, var3);
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
      return !planar(this.pieces[var1]) && (this.flags[var1] & 7) == 4;
   }

   private int neighbour(int var1, int var2) {
      int var3 = var1 % this.width + DX[var2];
      int var4 = var1 / this.width + DZ[var2];
      return var3 >= 0 && var4 >= 0 && var3 < this.width && var4 < this.height ? var4 * this.width + var3 : -1;
   }
}
