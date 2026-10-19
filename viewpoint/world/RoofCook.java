package viewpoint.world;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

final class RoofCook {
   private static final float INSET = 0.96875F;
   private static final float FLAT = 0.02F;
   private static final float STEP = 0.01F;
   private static final float STEP_IN = 0.25F;
   private static final float ALONG = 0.01F;
   private static final int[] DX = new int[]{0, 1, 0, -1};
   private static final int[] DZ = new int[]{1, 0, -1, 0};
   private static final int[][] SIDE_POINTS = new int[][]{{6, 7, 8}, {2, 5, 8}, {0, 1, 2}, {0, 3, 6}};
   private static final int TRIANGLES = 8;
   private static final int STEP_TRIANGLES = 16;
   private static final int EAVE_TRIANGLES = 32;
   private static final AtomicLong cooked = new AtomicLong();

   static long takeTriangles() {
      return cooked.getAndSet(0L);
   }

   static float[] vertices(RoofSurface var0, RoofWindow var1, Recipe.Op var2) {
      int var3 = var1.innerSquare(var2.dx, var2.dy);
      int var4 = var1.square(var3);
      int var5 = var1.lenders[var3];
      boolean var6 = (var2.face & 4) != 0;
      RoofPiece var7 = var6 ? (var5 < 0 ? null : var1.art(var5)) : var1.art(var4);
      if (var7 == null) {
         return new float[0];
      }

      int var8 = var4 % var1.width;
      int var9 = var4 / var1.width;
      int var10 = var1.lifts[var4] != 0 ? 98304 : 0;
      RoofCook.Square var11 = new RoofCook.Square(var2, var7, var1.pieces[var4], var6, var10, var8 + 0.5F, var9 + 0.5F);
      var0.triangles(var4, var11);
      if ((var2.face & 2) != 0 && var0.built(var8, var9)) {
         var11.steps(var0, var8, var9);
      }

      RoofEaves.square(var0, var1.flags, var8, var9, var11);
      cooked.addAndGet(var11.n / 42);
      return Arrays.copyOf(var11.vertices, var11.n);
   }

   static boolean covers(RoofPiece var0, float var1, float var2) {
      if (var0 != null && var0.kind != 0 && var0.kind != 5 && var0.kind != 7) {
         int var3 = face(var0, var1, var2);
         return var3 == 0 || var3 == 1;
      } else {
         return true;
      }
   }

   static int face(RoofPiece var0, float var1, float var2) {
      if (var0.kind == 0 || var0.kind == 7) {
         return var0.facing;
      }

      if (var0.kind == 6) {
         return Math.abs(var2) >= Math.abs(var1) ? (var2 > 0.0F ? 0 : 2) : (var1 > 0.0F ? 1 : 3);
      }

      if (var0.kind != 1 && var0.kind != 2) {
         return -1;
      }

      float var3 = var0.plane(var0.facing, var1, var2);
      float var4 = var0.plane(var0.other, var1, var2);
      boolean var5 = var0.kind == 1 ? var3 <= var4 : var3 >= var4;
      return var5 ? var0.facing : var0.other;
   }

   private static float within(float var0) {
      return var0 > 0.5F ? var0 - 0.5F : (var0 < -0.5F ? var0 + 0.5F : var0);
   }

   static int facing(float var0, float var1) {
      if (var0 * var0 + var1 * var1 < 4.0E-4F) {
         return -1;
      } else if (Math.abs(var1) >= Math.abs(var0)) {
         return var1 > 0.0F ? 0 : 2;
      } else {
         return var0 > 0.0F ? 1 : 3;
      }
   }

   private RoofCook() {
   }

   private static final class Square implements RoofSurface.Triangles, RoofEaves.Sink {
      final Recipe.Op op;
      final RoofPiece art;
      final RoofPiece own;
      final boolean lent;
      final int flags;
      final float middleX;
      final float middleZ;
      int shade;
      float inX;
      float inZ;
      final float[] vertices = new float[2352];
      final float[] normal = new float[3];
      int n;

      Square(Recipe.Op var1, RoofPiece var2, RoofPiece var3, boolean var4, int var5, float var6, float var7) {
         this.op = var1;
         this.art = var2;
         this.own = var3;
         this.lent = var4;
         this.flags = var5;
         this.middleX = var6;
         this.middleZ = var7;
      }

      @Override
      public void triangle(int var1, float[] var2, float[] var3, float[] var4) {
         this.normal(var2, var3, var4);
         int var5 = RoofCook.facing(this.normal[0], this.normal[2]);
         boolean var6 = var5 == -1 || var5 == 0 || var5 == 1;
         float var7 = (var2[0] + var3[0] + var4[0]) / 3.0F - this.middleX;
         float var8 = (var2[2] + var3[2] + var4[2]) / 3.0F - this.middleZ;
         boolean var9 = var6 && RoofCook.covers(this.own, var7, var8);
         if ((this.op.face & (var9 ? 1 : 2)) != 0) {
            int var10 = this.lent && var5 != -1 && this.art.facing != -1 ? Math.floorMod(this.art.facing - var5, 4) : 0;
            float var11 = var7;
            float var12 = var8;

            for (int var13 = 0; var13 < var10; var13++) {
               float var14 = var11;
               var11 = var12;
               var12 = -var14;
            }

            this.shade = this.lent && RoofCook.face(this.art, var11, var12) == 1 ? 163840 : 0;
            this.vertex(var2, var10, 0.0F);
            this.vertex(var3, var10, 0.0F);
            this.vertex(var4, var10, 0.0F);
         }
      }

      @Override
      public void slope(float[] var1, float[] var2, float[] var3, int var4) {
         this.inX = RoofCook.DX[var4] * 0.5F;
         this.inZ = RoofCook.DZ[var4] * 0.5F;
         this.triangle(-1, var1, var2, var3);
         this.inX = 0.0F;
         this.inZ = 0.0F;
      }

      @Override
      public void trim(float[] var1, float[] var2, float[] var3, float var4, float var5, float var6) {
         if ((this.op.face & 2) != 0) {
            this.normal[0] = var4;
            this.normal[1] = var5;
            this.normal[2] = var6;
            float var7 = 1.4999767E7F;
            this.vertex(var1, 0, var7);
            this.vertex(var2, 0, var7);
            this.vertex(var3, 0, var7);
         }
      }

      void steps(RoofSurface var1, int var2, int var3) {
         float[] var4 = new float[3];
         float[] var5 = new float[3];
         this.shade = this.lent && RoofCook.face(this.art, 0.0F, 0.0F) == 1 ? 163840 : 0;
         float[] var6 = new float[3];
         float[] var7 = new float[3];
         float[] var8 = new float[3];
         float[] var9 = new float[3];

         for (int var10 = 0; var10 < RoofCook.DX.length; var10++) {
            int var11 = var2 + RoofCook.DX[var10];
            int var12 = var3 + RoofCook.DZ[var10];
            if (var11 >= 0 && var12 >= 0 && var11 < var1.width && var12 < var1.height && var1.stands(var11, var12)) {
               int[] var13 = RoofCook.SIDE_POINTS[var10];
               int[] var14 = RoofCook.SIDE_POINTS[(var10 + 2) % 4];
               boolean var15 = false;

               for (int var16 = 0; var16 < 3; var16++) {
                  var4[var16] = var1.height(var2, var3, var13[var16]);
                  var5[var16] = Math.min(var4[var16], var1.height(var11, var12, var14[var16]));
                  var15 |= var4[var16] - var5[var16] > 0.01F;
               }

               if (var15) {
                  this.normal[0] = RoofCook.DX[var10];
                  this.normal[1] = 0.0F;
                  this.normal[2] = RoofCook.DZ[var10];
                  this.inX = RoofCook.DX[var10] * 0.25F;
                  this.inZ = RoofCook.DZ[var10] * 0.25F;
                  float var19 = Math.max(var4[0], Math.max(var4[1], var4[2])) - Math.min(var4[0], Math.min(var4[1], var4[2]));
                  float var17 = var19 > 0.01F ? this.op.fill : 0.0F;
                  if (!(var19 > 0.01F) || (this.op.face >> 3 & 1 << var10) == 0) {
                     for (int var18 = 0; var18 < 2; var18++) {
                        point(var2, var3, var13[var18], var5[var18], var6);
                        point(var2, var3, var13[var18 + 1], var5[var18 + 1], var7);
                        point(var2, var3, var13[var18], var4[var18], var8);
                        point(var2, var3, var13[var18 + 1], var4[var18 + 1], var9);
                        this.vertex(var6, 0, var17);
                        this.vertex(var7, 0, var17);
                        this.vertex(var9, 0, var17);
                        this.vertex(var6, 0, var17);
                        this.vertex(var9, 0, var17);
                        this.vertex(var8, 0, var17);
                     }
                  }
               }
            }
         }

         this.inX = 0.0F;
         this.inZ = 0.0F;
      }

      private static void point(int var0, int var1, int var2, float var3, float[] var4) {
         var4[0] = var0 + var2 % 3 / 2.0F;
         var4[1] = var3;
         var4[2] = var1 + var2 / 3 / 2.0F;
      }

      private void vertex(float[] var1, int var2, float var3) {
         float var4 = var1[0] - this.middleX;
         float var5 = var1[2] - this.middleZ;
         float var6 = var3 != 0.0F ? 0.25F : var4 - this.inX;
         float var7 = var3 != 0.0F ? 0.25F : var5 - this.inZ;

         for (int var8 = 0; var8 < var2; var8++) {
            float var9 = var6;
            var6 = var7;
            var7 = -var9;
         }

         var6 = RoofCook.within(var6) * 0.96875F;
         var7 = RoofCook.within(var7) * 0.96875F;
         float var14 = this.art.height(var6, var7);
         float var15 = 32.0F * (var6 - var7 + 1.0F);
         float var10 = 112.0F + 16.0F * (var6 + var7) - 39.191833F * var14;
         float[] var11 = this.op.map;
         this.n = Cook.put(
            this.vertices,
            this.n,
            -(this.op.dx + var4),
            this.op.height + var1[1],
            -(this.op.dy + var5),
            var11[4] + var15 * var11[6],
            var11[5] + var10 * var11[7],
            var11[0],
            var11[1],
            var11[2],
            var11[3],
            -this.normal[0],
            this.normal[1],
            -this.normal[2],
            var3,
            this.flags | (var3 != 0.0F ? 0 : this.shade)
         );
      }

      private void normal(float[] var1, float[] var2, float[] var3) {
         float var4 = var2[0] - var1[0];
         float var5 = var2[1] - var1[1];
         float var6 = var2[2] - var1[2];
         float var7 = var3[0] - var1[0];
         float var8 = var3[1] - var1[1];
         float var9 = var3[2] - var1[2];
         float var10 = var5 * var9 - var6 * var8;
         float var11 = var6 * var7 - var4 * var9;
         float var12 = var4 * var8 - var5 * var7;
         float var13 = (var11 < 0.0F ? -1.0F : 1.0F) / (float)Math.sqrt(var10 * var10 + var11 * var11 + var12 * var12);
         this.normal[0] = var10 * var13;
         this.normal[1] = var11 * var13;
         this.normal[2] = var12 * var13;
      }
   }
}
