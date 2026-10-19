package viewpoint.camera;

import viewpoint.core.CameraSquares;

final class Boom {
   static final float CLEARANCE = 0.15F;
   static final float CORNER_MARGIN = 0.05F;
   static final float MAX_CLEARANCE = 0.45F;
   static final float PROBE_YAW = (float)Math.toRadians(12.0);
   static final float PROBE_PITCH = (float)Math.toRadians(8.0);
   static final float PROBE_SHARE = 0.5F;
   static final float SPAN = 1.5F;
   private static final float SHORT = 0.001F;

   static float place(CameraSquares var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float[] var9) {
      float var10 = (float)Math.cos(var7);
      float var11 = (float)Math.sin(var7);
      float var12 = reach(var0, var1, var2, var3, var1 - var11 * var4, var2 + var10 * var4, var3 + var5);
      float var13 = var1 - var11 * var4 * var12;
      float var14 = var2 + var10 * var4 * var12;
      float var15 = var3 + var5 * var12;
      float var16 = var6 * leg(var0, var13, var14, var15, var6, var7, var8);
      float var17 = (float)Math.cos(var8);
      float var18 = (float)Math.sin(var8);
      var9[0] = var13 - var10 * var17 * var16;
      var9[1] = var14 - var11 * var17 * var16;
      var9[2] = var15 - var18 * var16;
      keepClear(var0, var9);
      return var16;
   }

   static float free(CameraSquares var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float var9 = (float)Math.cos(var7);
      float var10 = (float)Math.sin(var7);
      float var11 = reach(var0, var1, var2, var3, var1 - var10 * var4, var2 + var9 * var4, var3 + var5);
      float var12 = var1 - var10 * var4 * var11;
      float var13 = var2 + var9 * var4 * var11;
      float var14 = var3 + var5 * var11;
      float var15 = var6 * leg(var0, var12, var13, var14, var6, var7, var8);
      float[] var16 = new float[]{
         leg(var0, var12, var13, var14, var6, var7 - PROBE_YAW, var8),
         leg(var0, var12, var13, var14, var6, var7 + PROBE_YAW, var8),
         leg(var0, var12, var13, var14, var6, var7, var8 - PROBE_PITCH)
      };

      for (float var20 : var16) {
         var15 = Math.min(var15, var6 - (var6 - var20 * var6) * 0.5F);
      }

      return var15;
   }

   static float shoulder(CameraSquares var0, float var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = (float)Math.cos(var6);
      float var8 = (float)Math.sin(var6);
      return reach(var0, var1, var2, var3, var1 - var8 * var4, var2 + var7 * var4, var3 + var5);
   }

   static float span(CameraSquares var0, float var1, float var2, float var3, float var4) {
      float var5 = (float)Math.cos(var4);
      float var6 = (float)Math.sin(var4);
      float var7 = reach(var0, var1, var2, var3, var1 - var6 * 1.5F, var2 + var5 * 1.5F, var3);
      float var8 = reach(var0, var1, var2, var3, var1 + var6 * 1.5F, var2 - var5 * 1.5F, var3);
      return (var7 + var8) * 1.5F;
   }

   private static float leg(CameraSquares var0, float var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = (float)Math.cos(var6);
      float var8 = var1 - (float)Math.cos(var5) * var7 * var4;
      float var9 = var2 - (float)Math.sin(var5) * var7 * var4;
      return reach(var0, var1, var2, var3, var8, var9, var3 - (float)Math.sin(var6) * var4);
   }

   static float reach(CameraSquares var0, float var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = var4 - var1;
      float var8 = var5 - var2;
      float var9 = var6 - var3;
      float var10 = (float)Math.sqrt(var7 * var7 + var8 * var8 + var9 * var9);
      if (var10 < 0.001F) {
         return 1.0F;
      }

      int var11 = (int)Math.floor(var1);
      int var12 = (int)Math.floor(var2);
      int var13 = (int)Math.floor(var3 / 2.4494896F);
      int var14 = var7 > 0.0F ? 1 : -1;
      int var15 = var8 > 0.0F ? 1 : -1;
      int var16 = var9 > 0.0F ? 1 : -1;
      float var17 = crossing(var1, var7, var11, 1.0F);
      float var18 = crossing(var2, var8, var12, 1.0F);
      float var19 = crossing(var3, var9, var13, 2.4494896F);

      float var20;
      boolean var21;
      do {
         var20 = Math.min(var17, Math.min(var18, var19));
         if (var20 >= 1.0F) {
            return 1.0F;
         }

         if (var20 == var17) {
            var21 = blocked(var0, var11, var12, var13, var14, 0, 0, 2);
            var11 += var14;
            var17 += 1.0F / Math.abs(var7);
         } else if (var20 == var18) {
            var21 = blocked(var0, var11, var12, var13, 0, var15, 0, 1);
            var12 += var15;
            var18 += 1.0F / Math.abs(var8);
         } else {
            var21 = blocked(var0, var11, var12, var13, 0, 0, var16, 4);
            var13 += var16;
            var19 += 2.4494896F / Math.abs(var9);
         }
      } while (!var21);

      return Math.max(0.0F, var20 - 0.001F / var10);
   }

   private static float crossing(float var0, float var1, int var2, float var3) {
      return var1 == 0.0F ? Float.POSITIVE_INFINITY : ((var1 > 0.0F ? var2 + 1 : var2) * var3 - var0) / var1;
   }

   private static boolean blocked(CameraSquares var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      int var8 = var0.at(var1 + var4, var2 + var5, var3 + var6);
      int var9 = var4 + var5 + var6 > 0 ? var8 : var0.at(var1, var2, var3);
      return var8 < 0 || (var9 & var7) != 0;
   }

   private static void keepClear(CameraSquares var0, float[] var1) {
      float var2 = Math.min(0.45F, Math.max(0.15F, var0.nearCorner + 0.05F));
      int var3 = (int)Math.floor(var1[0]);
      int var4 = (int)Math.floor(var1[1]);
      int var5 = (int)Math.floor(var1[2] / 2.4494896F);
      float var6 = var5 * 2.4494896F;
      if (blocked(var0, var3, var4, var5, -1, 0, 0, 2)) {
         var1[0] = Math.max(var1[0], var3 + var2);
      }

      if (blocked(var0, var3, var4, var5, 1, 0, 0, 2)) {
         var1[0] = Math.min(var1[0], var3 + 1 - var2);
      }

      if (blocked(var0, var3, var4, var5, 0, -1, 0, 1)) {
         var1[1] = Math.max(var1[1], var4 + var2);
      }

      if (blocked(var0, var3, var4, var5, 0, 1, 0, 1)) {
         var1[1] = Math.min(var1[1], var4 + 1 - var2);
      }

      if (blocked(var0, var3, var4, var5, 0, 0, -1, 4)) {
         var1[2] = Math.max(var1[2], var6 + var2);
      }

      if (blocked(var0, var3, var4, var5, 0, 0, 1, 4)) {
         var1[2] = Math.min(var1[2], var6 + 2.4494896F - var2);
      }
   }

   private Boom() {
   }
}
