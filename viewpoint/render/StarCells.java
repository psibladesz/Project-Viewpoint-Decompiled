package viewpoint.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class StarCells {
   static final int CELLS = 32;
   static final int SLOTS = 16;
   static final int WIDTH = 512;
   static final int HEIGHT = 192;
   static final double MARGIN = Math.toRadians(0.5);

   static int[] cell(double var0, double var2, double var4) {
      double var6 = Math.abs(var0);
      double var8 = Math.abs(var2);
      double var10 = Math.abs(var4);
      double var12;
      double var14;
      int var16;
      if (var6 >= var8 && var6 >= var10) {
         var16 = var0 > 0.0 ? 0 : 1;
         var12 = var2 / var6;
         var14 = var4 / var6;
      } else if (var8 >= var10) {
         var16 = var2 > 0.0 ? 2 : 3;
         var12 = var0 / var8;
         var14 = var4 / var8;
      } else {
         var16 = var4 > 0.0 ? 4 : 5;
         var12 = var0 / var10;
         var14 = var2 / var10;
      }

      int var17 = Math.min(Math.max((int)Math.floor((var12 * 0.5 + 0.5) * 32.0), 0), 31);
      int var18 = Math.min(Math.max((int)Math.floor((var14 * 0.5 + 0.5) * 32.0), 0), 31);
      return new int[]{var17 * 16, var16 * 32 + var18};
   }

   static float pack(double var0, double var2) {
      double var4 = Math.max(1.0, Math.round((var0 + 2.0) * 100.0));
      double var6 = Math.min(Math.max((var2 + 0.4) / 2.5, 0.0), 1.0) * 0.98;
      return (float)(var4 + var6);
   }

   static float[] image(List<StarCells.Star> var0) {
      ArrayList var1 = new ArrayList(6144);

      for (int var2 = 0; var2 < 6144; var2++) {
         var1.add(new ArrayList());
      }

      for (StarCells.Star var3 : var0) {
         for (int var5 : reach(var3)) {
            List var6 = (List)var1.get(var5);
            if (!var6.contains(var3)) {
               var6.add(var3);
            }
         }
      }

      float[] var11 = new float[393216];

      for (int var12 = 0; var12 < var1.size(); var12++) {
         List var13 = (List)var1.get(var12);
         var13.sort(Comparator.comparingDouble(StarCells.Star::magnitude));
         int var14 = var12 % 32 * 16;
         int var15 = var12 / 32;

         for (int var7 = 0; var7 < Math.min(16, var13.size()); var7++) {
            StarCells.Star var8 = (StarCells.Star)var13.get(var7);
            int var9 = (var15 * 512 + var14 + var7) * 4;
            var11[var9] = (float)var8.x();
            var11[var9 + 1] = (float)var8.y();
            var11[var9 + 2] = (float)var8.z();
            var11[var9 + 3] = pack(var8.magnitude(), var8.colour());
         }
      }

      return var11;
   }

   private static List<Integer> reach(StarCells.Star var0) {
      double[] var1 = tangent(var0);
      double[] var2 = new double[]{var0.y() * var1[2] - var0.z() * var1[1], var0.z() * var1[0] - var0.x() * var1[2], var0.x() * var1[1] - var0.y() * var1[0]};
      ArrayList var3 = new ArrayList(9);

      for (int var4 = -1; var4 <= 1; var4++) {
         for (int var5 = -1; var5 <= 1; var5++) {
            double var6 = var0.x() + MARGIN * (var4 * var1[0] + var5 * var2[0]);
            double var8 = var0.y() + MARGIN * (var4 * var1[1] + var5 * var2[1]);
            double var10 = var0.z() + MARGIN * (var4 * var1[2] + var5 * var2[2]);
            int[] var12 = cell(var6, var8, var10);
            int var13 = var12[1] * 32 + var12[0] / 16;
            if (!var3.contains(var13)) {
               var3.add(var13);
            }
         }
      }

      return var3;
   }

   private static double[] tangent(StarCells.Star var0) {
      double var1 = Math.abs(var0.x());
      double var3 = Math.abs(var0.y());
      double var5 = Math.abs(var0.z());
      double[] var7 = var1 <= var3 && var1 <= var5
         ? new double[]{0.0, -var0.z(), var0.y()}
         : (var3 <= var5 ? new double[]{-var0.z(), 0.0, var0.x()} : new double[]{-var0.y(), var0.x(), 0.0});
      double var8 = Math.sqrt(var7[0] * var7[0] + var7[1] * var7[1] + var7[2] * var7[2]);
      return new double[]{var7[0] / var8, var7[1] / var8, var7[2] / var8};
   }

   private StarCells() {
   }

   record Star(double x, double y, double z, double magnitude, double colour) {
      static StarCells.Star at(double var0, double var2, double var4, double var6) {
         double var8 = Math.toRadians(var0);
         double var10 = Math.toRadians(var2);
         return new StarCells.Star(Math.cos(var10) * Math.cos(var8), Math.cos(var10) * Math.sin(var8), Math.sin(var10), var4, var6);
      }
   }
}
