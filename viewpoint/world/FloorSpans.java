package viewpoint.world;

import viewpoint.render.FarRelief;
import viewpoint.render.FloorArt;
import viewpoint.visibility.Edges;
import viewpoint.visibility.Owner;
import zombie.iso.IsoChunk;
import zombie.iso.IsoGridSquare;

final class FloorSpans {
   private static final int SIZE = 8;
   private static final int SQUARES = 64;
   private static final int[] group = new int[64];
   private static final boolean[] present = new boolean[64];
   private static final boolean[] covered = new boolean[64];
   private static final int[] rectangles = new int[256];

   static void emit(Recipe.Batch var0, IsoChunk var1, int var2, FloorArt var3, float var4) {
      for (int var5 = 0; var5 < 64; var5++) {
         present[var5] = var3.present(var5);
      }

      groups(var1, var2);
      int var16 = rectangles(present, group, rectangles, FarRelief.cut());
      FarRelief.Field var6 = FarRelief.walls();
      double var7 = var1.wx * 8.0;
      double var9 = var1.wy * 8.0;

      for (int var11 = 0; var11 < var16; var11++) {
         int var12 = rectangles[var11 * 4];
         int var13 = rectangles[var11 * 4 + 1];
         WorldMesher.owner = Owner.of(var13 * 8 + var12, 3);
         float[] var14 = WallCuts.pieces(var6, var7, var9, var12, var13, rectangles[var11 * 4 + 2], rectangles[var11 * 4 + 3]);

         for (int var15 = 0; var15 < var14.length; var15 += 6) {
            WorldMesher.raw(var0, rectangle(var14[var15], var14[var15 + 1], var14[var15 + 2], var14[var15 + 3], var4, var14[var15 + 4], var14[var15 + 5]));
         }
      }

      WorldMesher.owner = -1;
   }

   static int rectangles(boolean[] var0, int[] var1, int[] var2, int var3) {
      boolean[] var4 = new boolean[64];
      int var5 = 0;

      for (int var6 = 0; var6 < 64; var6++) {
         if (!var4[var6] && var0[var6]) {
            int var7 = var6 % 8;
            int var8 = var6 / 8;
            int var9 = var7 + 1;
            int var10 = var8 + 1;

            while (var9 < 8 && var9 % var3 != 0 && joins(var6, var8 * 8 + var9, var0, var1, var4)) {
               var9++;
            }

            while (var10 < 8 && var10 % var3 != 0 && rowJoins(var6, var7, var9, var10, var0, var1, var4)) {
               var10++;
            }

            for (int var11 = var8; var11 < var10; var11++) {
               for (int var12 = var7; var12 < var9; var12++) {
                  var4[var11 * 8 + var12] = true;
               }
            }

            var2[var5 * 4] = var7;
            var2[var5 * 4 + 1] = var8;
            var2[var5 * 4 + 2] = var9;
            var2[var5 * 4 + 3] = var10;
            var5++;
         }
      }

      return var5;
   }

   private static boolean joins(int var0, int var1, boolean[] var2, int[] var3, boolean[] var4) {
      return !var4[var1] && var2[var1] && var3[var1] == var3[var0];
   }

   private static boolean rowJoins(int var0, int var1, int var2, int var3, boolean[] var4, int[] var5, boolean[] var6) {
      for (int var7 = var1; var7 < var2; var7++) {
         if (!joins(var0, var3 * 8 + var7, var4, var5, var6)) {
            return false;
         }
      }

      return true;
   }

   private static void groups(IsoChunk var0, int var1) {
      int var2 = 0;

      while (var2 < 64) {
         group[var2] = var2++;
      }

      for (int var5 = 0; var5 < 64; var5++) {
         int var3 = var5 % 8;
         int var4 = var5 / 8;
         if (present[var5]) {
            if (var3 > 0 && present[var5 - 1] && sameSectors(var0, var1, var3, var4, false)) {
               union(var5, var5 - 1);
            }

            if (var4 > 0 && present[var5 - 8] && sameSectors(var0, var1, var3, var4, true)) {
               union(var5, var5 - 8);
            }
         }
      }

      for (int var6 = 0; var6 < 64; var6++) {
         group[var6] = find(var6);
      }
   }

   private static boolean sameSectors(IsoChunk var0, int var1, int var2, int var3, boolean var4) {
      int var5 = var4 ? var2 : var2 - 1;
      int var6 = var4 ? var3 - 1 : var3;
      if (!joined(var0.getGridSquare(var2, var3, var1), var0.getGridSquare(var5, var6, var1), var4)) {
         return false;
      }

      IsoGridSquare var7 = var0.getGridSquare(var2, var3, var1 - 1);
      IsoGridSquare var8 = var0.getGridSquare(var5, var6, var1 - 1);
      return var7 == null && var8 == null || joined(var7, var8, var4);
   }

   private static boolean joined(IsoGridSquare var0, IsoGridSquare var1, boolean var2) {
      return var0 != null && var1 != null && room(var0) == room(var1) && Edges.edge(var0, var2) == 0;
   }

   private static long room(IsoGridSquare var0) {
      return var0.getRoom() != null && !var0.isOutside() ? var0.getRoomID() : -1L;
   }

   private static int find(int var0) {
      while (group[var0] != var0) {
         group[var0] = group[group[var0]];
         var0 = group[var0];
      }

      return var0;
   }

   private static void union(int var0, int var1) {
      group[find(var0)] = find(var1);
   }

   private static float[] rectangle(float var0, float var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = 10.0F;
      float[] var8 = new float[]{(var0 + 1.0F) / var7, (var1 + 1.0F) / var7, (var2 + 1.0F) / var7, (var3 + 1.0F) / var7};
      return Recipe.floorVertices(var0, var1, var2, var3, var4, var8, var5, var6);
   }

   private FloorSpans() {
   }
}
