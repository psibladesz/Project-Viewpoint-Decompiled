package viewpoint.render;

final class FarFootprint {
   private static final float GROWN = 8.0F;
   private static final int BUILT = 128;
   private static final int BLOCK_CHUNKS = 8;
   private static final int CELL_BLOCKS = 4;
   float x0;
   float z0;
   float x1;
   float z1;

   boolean block(SceneData var1, FarGpu.BlockDraw var2) {
      int var3 = var2.worldX + var2.blockX;
      int var4 = var2.worldY + var2.blockY;
      int var5 = Math.floorDiv(var3, 8);
      int var6 = Math.floorDiv(var4, 8);
      int var7 = Integer.MAX_VALUE;
      int var8 = Integer.MAX_VALUE;
      int var9 = Integer.MIN_VALUE;
      int var10 = Integer.MIN_VALUE;

      for (int var11 = 0; var11 < 8; var11++) {
         for (int var12 = 0; var12 < 8; var12++) {
            if (!built(var1, var5 + var12, var6 + var11)) {
               var7 = Math.min(var7, var12);
               var8 = Math.min(var8, var11);
               var9 = Math.max(var9, var12);
               var10 = Math.max(var10, var11);
            }
         }
      }

      if (var9 < 0) {
         return false;
      }

      int var13 = var2.blockX;
      int var14 = var2.blockY;
      this.set(
         var2.originX, var2.originZ, var13 + var7 * 8, var14 + var8 * 8, var13 + (var9 + 1) * 8, var14 + (var10 + 1) * 8, var13, var14, var13 + 64, var14 + 64
      );
      return true;
   }

   boolean cell(SceneData var1, FarGpu.CellDraw var2) {
      int var3 = Math.floorDiv(var2.worldX, 64);
      int var4 = Math.floorDiv(var2.worldY, 64);
      int var5 = Integer.MAX_VALUE;
      int var6 = Integer.MAX_VALUE;
      int var7 = Integer.MIN_VALUE;
      int var8 = Integer.MIN_VALUE;

      for (int var9 = 0; var9 < 4; var9++) {
         for (int var10 = 0; var10 < 4; var10++) {
            if (!shellStands(var1, var3 + var10, var4 + var9) && !withinNear(var1, var2, var10, var9)) {
               var5 = Math.min(var5, var10);
               var6 = Math.min(var6, var9);
               var7 = Math.max(var7, var10);
               var8 = Math.max(var8, var9);
            }
         }
      }

      if (var7 < 0) {
         return false;
      }

      this.set(var2.originX, var2.originZ, var5 * 64, var6 * 64, (var7 + 1) * 64, (var8 + 1) * 64, 0, 0, 256, 256);
      return true;
   }

   private void set(float var1, float var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
      float var11 = Math.max(var7, var3 - 8.0F);
      float var12 = Math.max(var8, var4 - 8.0F);
      float var13 = Math.min(var9, var5 + 8.0F);
      float var14 = Math.min(var10, var6 + 8.0F);
      this.x0 = var1 - var13;
      this.x1 = var1 - var11;
      this.z0 = var2 - var14;
      this.z1 = var2 - var12;
   }

   static boolean reaches(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      if (var0 <= 0.0F) {
         return true;
      }

      float var8 = (var3 + var5) * 0.5F;
      float var9 = (var4 + var6) * 0.5F;
      float var10 = var7 * var1;
      float var11 = var7 * var2;
      float var12 = (var5 - var3) * 0.5F + var0;
      float var13 = (var6 - var4) * 0.5F + var0;
      return Math.max(enters(var8, var10, var12), enters(var9, var11, var13)) <= Math.min(leaves(var8, var10, var12), leaves(var9, var11, var13));
   }

   private static float enters(float var0, float var1, float var2) {
      if (var1 == 0.0F) {
         return Math.abs(var0) <= var2 ? 0.0F : 2.0F;
      } else {
         return Math.max(0.0F, Math.min((-var2 - var0) / var1, (var2 - var0) / var1));
      }
   }

   private static float leaves(float var0, float var1, float var2) {
      if (var1 == 0.0F) {
         return Math.abs(var0) <= var2 ? 1.0F : -1.0F;
      } else {
         return Math.min(1.0F, Math.max((-var2 - var0) / var1, (var2 - var0) / var1));
      }
   }

   private static boolean built(SceneData var0, int var1, int var2) {
      int var3 = var1 - var0.farMaskX;
      int var4 = var2 - var0.farMaskY;
      int var5 = var0.farMaskSize;
      return var3 >= 0 && var4 >= 0 && var3 < var5 && var4 < var5 && (var0.farMask[var4 * var5 + var3] & 255) >= 128;
   }

   private static boolean shellStands(SceneData var0, int var1, int var2) {
      int var3 = var1 - var0.shellMaskX;
      int var4 = var2 - var0.shellMaskY;
      if (var3 >= 0 && var4 >= 0 && var3 < 53 && var4 < 53) {
         int var5 = (var4 * 53 + var3) * 3;
         return var0.shellMask[var5] != 0 || var0.shellMask[var5 + 2] != 0;
      } else {
         return false;
      }
   }

   private static boolean withinNear(SceneData var0, FarGpu.CellDraw var1, int var2, int var3) {
      float var4 = var0.nearReach - 8.0F;
      float var5 = 0.0F;

      for (int var6 = 0; var6 < 4; var6++) {
         float var7 = var1.originX - (var2 + (var6 & 1)) * 64;
         float var8 = var1.originZ - (var3 + (var6 >> 1)) * 64;
         var5 = Math.max(var5, var7 * var7 + var8 * var8);
      }

      return var4 > 0.0F && var5 < var4 * var4;
   }
}
