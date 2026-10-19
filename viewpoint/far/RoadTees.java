package viewpoint.far;

import java.util.ArrayList;
import java.util.List;

final class RoadTees {
   private static final float KERB = 2.0F;
   private static final float ENDS = 0.5F;

   private RoadTees() {
   }

   static List<RoadTees.Tee> of(ReliefRoads var0) {
      float[][] var1 = var0.streets;
      float[][] var2 = new float[var1.length][];
      ArrayList var3 = new ArrayList();

      for (int var4 = 0; var4 < var1.length; var4++) {
         var2[var4] = box(var1[var4]);
      }

      for (int var15 = 0; var15 < var1.length; var15++) {
         float[] var5 = var1[var15];

         for (int var6 = 0; var5.length >= 5 && var6 < 2; var6++) {
            float var7 = var6 == 0 ? var5[1] : var5[var5.length - 2];
            float var8 = var6 == 0 ? var5[2] : var5[var5.length - 1];
            RoadTees.Tee var9 = null;
            float var10 = Float.POSITIVE_INFINITY;

            for (int var11 = 0; var11 < var1.length; var11++) {
               float var12 = var1[var11][0] * 0.5F + 2.0F;
               if (var11 != var15 && within(var2[var11], var7, var8, var12)) {
                  for (int var13 = 1; var13 + 3 < var1[var11].length; var13 += 2) {
                     float[] var14 = foot(var1[var11], var13, var7, var8);
                     if (var14[1] <= var12 && var14[1] < var10 && !atEnd(var1[var11], var13, var14[0])) {
                        var10 = var14[1];
                        var9 = new RoadTees.Tee(var15, var6, var11, var13, var14[0]);
                     }
                  }
               }
            }

            if (var9 != null) {
               var3.add(onward(var9, var1[var9.other()]));
            }
         }
      }

      return var3;
   }

   private static float[] foot(float[] var0, int var1, float var2, float var3) {
      float var4 = var0[var1];
      float var5 = var0[var1 + 1];
      float var6 = var0[var1 + 2] - var4;
      float var7 = var0[var1 + 3] - var5;
      float var8 = var6 * var6 + var7 * var7;
      float var9 = var8 > 0.0F ? Math.max(0.0F, Math.min(1.0F, ((var2 - var4) * var6 + (var3 - var5) * var7) / var8)) : 0.0F;
      return new float[]{var9, (float)Math.hypot(var2 - (var4 + var6 * var9), var3 - (var5 + var7 * var9))};
   }

   private static boolean atEnd(float[] var0, int var1, float var2) {
      float var3 = (float)Math.hypot(var0[var1 + 2] - var0[var1], var0[var1 + 3] - var0[var1 + 1]);
      return var1 == 1 && var2 * var3 < 0.5F || var1 + 4 >= var0.length && (1.0F - var2) * var3 < 0.5F;
   }

   private static RoadTees.Tee onward(RoadTees.Tee var0, float[] var1) {
      return var0.t() >= 1.0F && var0.point() + 4 < var1.length ? new RoadTees.Tee(var0.street(), var0.end(), var0.other(), var0.point() + 2, 0.0F) : var0;
   }

   private static float[] box(float[] var0) {
      float[] var1 = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

      for (int var2 = 1; var2 + 1 < var0.length; var2 += 2) {
         var1[0] = Math.min(var1[0], var0[var2]);
         var1[1] = Math.min(var1[1], var0[var2 + 1]);
         var1[2] = Math.max(var1[2], var0[var2]);
         var1[3] = Math.max(var1[3], var0[var2 + 1]);
      }

      return var1;
   }

   private static boolean within(float[] var0, float var1, float var2, float var3) {
      return var1 >= var0[0] - var3 && var2 >= var0[1] - var3 && var1 <= var0[2] + var3 && var2 <= var0[3] + var3;
   }

   record Tee(int street, int end, int other, int point, float t) {
   }
}
