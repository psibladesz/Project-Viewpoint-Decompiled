package viewpoint.world;

import viewpoint.render.FarRelief;
import viewpoint.render.ReliefWall;

public final class WallCuts {
   public static final int FLOATS = 6;

   private WallCuts() {
   }

   public static float[] pieces(FarRelief.Field var0, double var1, double var3, float var5, float var6, float var7, float var8) {
      byte var9 = 4;
      int var10 = (int)Math.floor((var1 + var5) / var9);
      int var11 = (int)Math.floor((var3 + var6) / var9);
      int var12 = var0 == null ? 0 : var0.wall(var10, var11);
      float[] var13 = cuts(var12, 0, var10, var1, var5, var7);
      float[] var14 = cuts(var12, 1, var11, var3, var6, var8);
      float[] var15 = new float[(var13.length - 1) * (var14.length - 1) * 6];
      int var16 = 0;

      for (int var17 = 0; var17 + 1 < var14.length; var17++) {
         for (int var18 = 0; var18 + 1 < var13.length; var18++) {
            float var19 = var18 == 1 && var13.length > 2 ? facing(var0, var1 + var13[2], var3 + (var14[var17] + var14[var17 + 1]) * 0.5, 0) : 0.0F;
            float var20 = var17 == 1 && var14.length > 2 ? facing(var0, var1 + (var13[var18] + var13[var18 + 1]) * 0.5, var3 + var14[2], 1) : 0.0F;
            float var21 = (float)Math.hypot(var19, var20);
            var15[var16++] = var13[var18];
            var15[var16++] = var14[var17];
            var15[var16++] = var13[var18 + 1];
            var15[var16++] = var14[var17 + 1];
            var15[var16++] = var21 > 0.0F ? var19 / var21 : 0.0F;
            var15[var16++] = var21 > 0.0F ? var20 / var21 : 0.0F;
         }
      }

      return var15;
   }

   private static float[] cuts(int var0, int var1, int var2, double var3, float var5, float var6) {
      if (ReliefWall.state(var0, var1) == 0) {
         return new float[]{var5, var6};
      } else {
         float var7 = (float)(var2 * 4.0 + ReliefWall.line(var0, var1) - var3);
         if (!(var7 <= var5) && !(var7 > var6)) {
            return var7 < var6 ? new float[]{var5, var7 - 0.015625F, var7, var6} : new float[]{var5, var7 - 0.015625F, var7};
         } else {
            return new float[]{var5, var6};
         }
      }
   }

   private static float facing(FarRelief.Field var0, double var1, double var3, int var5) {
      double var6 = 0.5;
      float var8 = var5 == 0 ? var0.metresAt(var1 - var6, var3) : var0.metresAt(var1, var3 - var6);
      float var9 = var5 == 0 ? var0.metresAt(var1 + var6, var3) : var0.metresAt(var1, var3 + var6);
      return var9 < var8 ? -1.0F : 1.0F;
   }
}
