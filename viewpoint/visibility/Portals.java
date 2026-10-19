package viewpoint.visibility;

import java.util.Arrays;
import viewpoint.render.FarRelief;

final class Portals {
   static final int BUDGET = 65536;
   private static final float CLOSE = 0.75F;
   private static final float NEAR = 0.05F;
   private static final float SIDE = 0.05F;
   private static final int MAX_RECTS = 6;
   private float[] rects = new float[4096];
   private int[] rectSector = new int[1024];
   private int[] rectNext = new int[1024];
   private int rectCount;
   private int[] head = new int[0];
   private int[] count = new int[0];
   private int[] queue = new int[1024];
   private final float[] corners = new float[12];
   private final float[] clipped = new float[24];
   final float[] projected = new float[4];

   boolean run(SectorGraph var1, byte[] var2, int[] var3, Portals.View var4, boolean[] var5) {
      if (this.head.length < var1.sectors) {
         this.head = new int[var1.sectors];
         this.count = new int[var1.sectors];
      }

      Arrays.fill(this.head, 0, var1.sectors, -1);
      Arrays.fill(this.count, 0, var1.sectors, 0);
      this.rectCount = 0;
      int var6 = 0;
      int var7 = 0;

      for (int var11 : var3) {
         if (var11 >= 0 && !var1.exterior[var11]) {
            var6 = this.admit(var11, -var4.tanX, var4.tanX, -var4.tanY, var4.tanY, var6);
         }
      }

      for (int var19 = 0; var19 < var1.openings; var19++) {
         int var22 = var1.openA[var19];
         int var24 = var1.openB[var19];
         boolean var26 = var1.exterior[var22] && !var1.exterior[var24];
         boolean var12 = var1.exterior[var24] && !var1.exterior[var22];
         if ((var26 || var12) && passable(var1, var2, var19)) {
            var6 = this.through(var1, var19, var26, var4, -var4.tanX, var4.tanX, -var4.tanY, var4.tanY, var6);
         }
      }

      for (int var20 = 0; var20 < var6; var20++) {
         int var23 = this.queue[var20];
         int var25 = this.rectSector[var23];
         float var27 = this.rects[var23 * 4];
         float var28 = this.rects[var23 * 4 + 1];
         float var13 = this.rects[var23 * 4 + 2];
         float var14 = this.rects[var23 * 4 + 3];

         for (int var15 = var1.adjacentStart[var25]; var15 < var1.adjacentStart[var25 + 1]; var15++) {
            if (++var7 > 65536) {
               return false;
            }

            int var16 = var1.adjacent[var15];
            boolean var17 = var1.openA[var16] == var25;
            int var18 = var17 ? var1.openB[var16] : var1.openA[var16];
            if (!var1.exterior[var18] && passable(var1, var2, var16)) {
               var6 = this.through(var1, var16, var17, var4, var27, var28, var13, var14, var6);
            }
         }
      }

      for (int var21 = 0; var21 < var1.sectors; var21++) {
         var5[var21] = this.head[var21] >= 0;
      }

      return true;
   }

   private int through(SectorGraph var1, int var2, boolean var3, Portals.View var4, float var5, float var6, float var7, float var8, int var9) {
      int var10 = var3 ? var1.openB[var2] : var1.openA[var2];
      if (close(var1, var2, var4)) {
         return this.admit(var10, var5, var6, var7, var8, var9);
      } else {
         return onSide(var1, var2, var3, var4) && this.project(var1, var2, var4, var5, var6, var7, var8)
            ? this.admit(var10, this.projected[0], this.projected[1], this.projected[2], this.projected[3], var9)
            : var9;
      }
   }

   private static boolean passable(SectorGraph var0, byte[] var1, int var2) {
      return var0.openDoor[var2] < 0 || var1[var0.openDoor[var2]] != 1;
   }

   static boolean onSide(SectorGraph var0, int var1, boolean var2, Portals.View var3) {
      float[] var4 = var0.openPlace;
      int var5 = var1 * 5;

      float var6 = switch (var0.openKind[var1]) {
         case 0 -> var3.y - var4[var5 + 1];
         case 1 -> var3.x - var4[var5];
         default -> var4[var5 + 4] * 2.4494896F + var3.lift(var4, var5) - var3.h;
      };
      return var2 ? var6 >= -0.05F : var6 <= 0.05F;
   }

   private static boolean close(SectorGraph var0, int var1, Portals.View var2) {
      float[] var3 = var0.openPlace;
      int var4 = var1 * 5;
      float var5 = 2.4494896F;
      float var6 = var3[var4 + 4] * var5 + var2.lift(var3, var4);
      float var7 = clamp(var2.x, Math.min(var3[var4], var3[var4 + 2]), Math.max(var3[var4], var3[var4 + 2]));
      float var8 = clamp(var2.y, Math.min(var3[var4 + 1], var3[var4 + 3]), Math.max(var3[var4 + 1], var3[var4 + 3]));
      float var9 = var0.openKind[var1] == 2 ? var6 : clamp(var2.h, var6, var6 + var5);
      float var10 = var2.x - var7;
      float var11 = var2.y - var8;
      float var12 = var2.h - var9;
      return var10 * var10 + var11 * var11 + var12 * var12 < 0.5625F;
   }

   boolean project(SectorGraph var1, int var2, Portals.View var3, float var4, float var5, float var6, float var7) {
      this.outlineOf(var1, var2, var3);
      int var8 = this.clip(var3);
      if (var8 == 0) {
         return false;
      }

      float var9 = Float.MAX_VALUE;
      float var10 = -Float.MAX_VALUE;
      float var11 = Float.MAX_VALUE;
      float var12 = -Float.MAX_VALUE;

      for (int var13 = 0; var13 < var8; var13++) {
         float var14 = this.clipped[var13 * 3] - var3.x;
         float var15 = this.clipped[var13 * 3 + 1] - var3.y;
         float var16 = this.clipped[var13 * 3 + 2] - var3.h;
         float var17 = var14 * var3.forward[0] + var15 * var3.forward[1] + var16 * var3.forward[2];
         float var18 = (var14 * var3.right[0] + var15 * var3.right[1] + var16 * var3.right[2]) / var17;
         float var19 = (var14 * var3.up[0] + var15 * var3.up[1] + var16 * var3.up[2]) / var17;
         var9 = Math.min(var9, var18);
         var10 = Math.max(var10, var18);
         var11 = Math.min(var11, var19);
         var12 = Math.max(var12, var19);
      }

      this.projected[0] = Math.max(var9, var4);
      this.projected[1] = Math.min(var10, var5);
      this.projected[2] = Math.max(var11, var6);
      this.projected[3] = Math.min(var12, var7);
      return this.projected[0] < this.projected[1] && this.projected[2] < this.projected[3];
   }

   private void outlineOf(SectorGraph var1, int var2, Portals.View var3) {
      float[] var4 = var1.openPlace;
      int var5 = var2 * 5;
      float var6 = 2.4494896F;
      float var7 = var4[var5 + 4] * var6 + var3.lift(var4, var5);
      boolean var8 = var1.openKind[var2] == 2;
      float[] var9 = this.corners;
      var9[0] = var4[var5];
      var9[1] = var4[var5 + 1];
      var9[2] = var7;
      var9[3] = var4[var5 + 2];
      var9[4] = var8 ? var4[var5 + 1] : var4[var5 + 3];
      var9[5] = var7;
      var9[6] = var4[var5 + 2];
      var9[7] = var4[var5 + 3];
      var9[8] = var8 ? var7 : var7 + var6;
      var9[9] = var4[var5];
      var9[10] = var8 ? var4[var5 + 3] : var4[var5 + 1];
      var9[11] = var8 ? var7 : var7 + var6;
   }

   private int clip(Portals.View var1) {
      int var2 = 0;

      for (int var3 = 0; var3 < 4; var3++) {
         int var4 = (var3 + 1) % 4;
         float var5 = this.ahead(var1, var3) - 0.05F;
         float var6 = this.ahead(var1, var4) - 0.05F;
         if (var5 >= 0.0F) {
            System.arraycopy(this.corners, var3 * 3, this.clipped, var2 * 3, 3);
            var2++;
         }

         if (var5 >= 0.0F != var6 >= 0.0F) {
            float var7 = var5 / (var5 - var6);

            for (int var8 = 0; var8 < 3; var8++) {
               this.clipped[var2 * 3 + var8] = this.corners[var3 * 3 + var8] + (this.corners[var4 * 3 + var8] - this.corners[var3 * 3 + var8]) * var7;
            }

            var2++;
         }
      }

      return var2;
   }

   private float ahead(Portals.View var1, int var2) {
      return (this.corners[var2 * 3] - var1.x) * var1.forward[0]
         + (this.corners[var2 * 3 + 1] - var1.y) * var1.forward[1]
         + (this.corners[var2 * 3 + 2] - var1.h) * var1.forward[2];
   }

   private int admit(int var1, float var2, float var3, float var4, float var5, int var6) {
      for (int var7 = this.head[var1]; var7 >= 0; var7 = this.rectNext[var7]) {
         if (this.rects[var7 * 4] <= var2 && this.rects[var7 * 4 + 1] >= var3 && this.rects[var7 * 4 + 2] <= var4 && this.rects[var7 * 4 + 3] >= var5) {
            return var6;
         }
      }

      if (this.count[var1] >= 6) {
         int var8 = this.head[var1];
         var2 = Math.min(var2, this.rects[var8 * 4]);
         var3 = Math.max(var3, this.rects[var8 * 4 + 1]);
         var4 = Math.min(var4, this.rects[var8 * 4 + 2]);
         var5 = Math.max(var5, this.rects[var8 * 4 + 3]);
      }

      int var9 = this.rectCount++;
      this.grow();
      this.rects[var9 * 4] = var2;
      this.rects[var9 * 4 + 1] = var3;
      this.rects[var9 * 4 + 2] = var4;
      this.rects[var9 * 4 + 3] = var5;
      this.rectSector[var9] = var1;
      this.rectNext[var9] = this.head[var1];
      this.head[var1] = var9;
      this.count[var1]++;
      if (var6 == this.queue.length) {
         this.queue = Arrays.copyOf(this.queue, var6 * 2);
      }

      this.queue[var6] = var9;
      return var6 + 1;
   }

   private void grow() {
      if (this.rectCount > this.rectSector.length) {
         this.rects = Arrays.copyOf(this.rects, this.rectSector.length * 8);
         this.rectSector = Arrays.copyOf(this.rectSector, this.rectSector.length * 2);
         this.rectNext = Arrays.copyOf(this.rectNext, this.rectNext.length * 2);
      }
   }

   private static float clamp(float var0, float var1, float var2) {
      return var0 < var1 ? var1 : Math.min(var0, var2);
   }

   static final class View {
      float x;
      float y;
      float h;
      final float[] forward = new float[3];
      final float[] right = new float[3];
      final float[] up = new float[3];
      float tanX;
      float tanY;
      FarRelief.Lift relief;

      float lift(float[] var1, int var2) {
         return this.relief == null ? 0.0F : this.relief.scene((var1[var2] + var1[var2 + 2]) * 0.5, (var1[var2 + 1] + var1[var2 + 3]) * 0.5);
      }

      void look(float var1, float var2) {
         float var3 = (float)Math.cos(var2);
         this.forward[0] = (float)Math.cos(var1) * var3;
         this.forward[1] = (float)Math.sin(var1) * var3;
         this.forward[2] = (float)Math.sin(var2);
         float var4 = this.forward[1];
         float var5 = -this.forward[0];
         float var6 = (float)Math.sqrt(var4 * var4 + var5 * var5);
         this.right[0] = var6 > 1.0E-6F ? var4 / var6 : 1.0F;
         this.right[1] = var6 > 1.0E-6F ? var5 / var6 : 0.0F;
         this.right[2] = 0.0F;
         this.up[0] = this.right[1] * this.forward[2];
         this.up[1] = -this.right[0] * this.forward[2];
         this.up[2] = this.right[0] * this.forward[1] - this.right[1] * this.forward[0];
      }
   }
}
