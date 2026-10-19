package viewpoint.far;

import java.util.Arrays;

final class ReliefFit {
   private static final float BETWEEN = 12.0F;
   private static final int ROUNDS = 4;
   private static final float RAIL_GRADE = 0.02F;
   private static final float DEPART = 1.5F;
   private static final float STEEP = 1.1F;
   private static final float TOWN_REACH = 64.0F;
   private static final float TOWN_LEAST = 0.05F;
   private static final float TOWN_MOST = 0.2F;

   private ReliefFit() {
   }

   static ReliefFit.Result fit(ReliefData.Sources var0, ReliefRules var1) {
      int var2 = var0.baked().width();
      int var3 = var0.baked().height();
      byte var4 = 4;
      float[] var5 = townShare(var0, var2, var3, var4);
      float[] var6 = natural(var0, var1, var5, var2, var3, var4);
      ReliefWater.Bodies var7 = ReliefWater.of(var0.ground(), var6, var2, var3, var4, var1, var0.flows());
      float[] var8 = nearest(var7.level(), var2, var3);
      ReliefNetwork var9 = ReliefNetwork.built(var0.roads(), var6, var8, var0.ground(), var2, var3, var4, var1);
      ReliefNetwork.Roads var10 = var9.stamp();
      ReliefPads.Pads var11 = ReliefPads.of(var0.buildings(), var0.ground(), var10.band(), var6, var10.height(), var7.level(), var2, var3, var4, var1);
      var9.settle(RoadTies.of(var9.samples(), var11, var7.level(), var2, var3, var4, var1));
      var10 = var9.stamp();
      int[] var12 = new int[5];
      float[] var13 = fixed(var11, var10, var7, var2, var12);
      ReliefFit.Laid var14 = laid(var0, var6, var5, var13, var11, var10, var7, var1);
      ReliefData.Counts var15 = new ReliefData.Counts(var12[0], var11.buildings(), var11.height().length, var0.roads().streets.length);
      String var16 = report(var7, var10, var11, var12, var14);
      byte[] var17 = new byte[var2 * var3];
      return new ReliefFit.Result(var14.ground, var8, var17, var15, var16, var11, var10, var7, var14.paving, var14.apart, var6, var7.flow());
   }

   private static ReliefFit.Laid laid(
      ReliefData.Sources var0,
      float[] var1,
      float[] var2,
      float[] var3,
      ReliefPads.Pads var4,
      ReliefNetwork.Roads var5,
      ReliefWater.Bodies var6,
      ReliefRules var7
   ) {
      int var8 = var0.baked().width();
      int var9 = var0.baked().height();
      byte var10 = 4;
      boolean[] var11 = new boolean[var3.length];
      float[] var12 = (float[])var3.clone();
      float[] var13 = (float[])var1.clone();

      for (int var14 = 0; var14 < var3.length; var14++) {
         boolean var15 = !Float.isNaN(var5.height()[var14]);
         boolean var16 = var4.pad()[var14] >= 0;
         var11[var14] = var6.ground()[var14] && !var16 && !var15;
         var12[var14] = var11[var14] ? Float.NaN : var12[var14];
      }

      boolean[] var30 = graded(var0.ground(), var5.band(), var4.pad(), var8, var9, var10, false);
      ReliefSweeps.graded(var13, var30, var12, var8, var9, var7.metresASquare(var7.grade()) * var10, 1.5F);
      boolean[] var31 = graded(var0.ground(), var5.band(), var4.pad(), var8, var9, var10, true);
      ReliefSweeps.graded(var13, var31, var12, var8, var9, var7.metresASquare(0.02F) * var10, 1.5F);
      ReliefSweeps.between(var13, var5.height(), padHeights(var4), var8, var9, 12.0F / var10);
      float var32 = var7.metresASquare(var7.bank()) * var10;
      boolean[] var17 = either(var30, var31);
      float[] var18 = (float[])var3.clone();

      for (int var19 = 0; var19 < var18.length; var19++) {
         if (var17[var19] && (Float.isNaN(var18[var19]) || var11[var19])) {
            var18[var19] = var13[var19];
         }
      }

      boolean[] var33 = TownPaving.of(var0.ground(), var5.height(), var4, var6.ground(), var2, var18, var8, var9, var10);
      float var20 = var7.metresASquare(var7.grade()) * var10;
      boolean[] var21 = anchors(var18, var33, var17, var5, var4);
      byte[] var22 = TownPaving.apart(var18, var1, var33, var21, var8, var9, var20);
      int var23 = TownPaving.join(var18, var1, var33, var21, var8, var9, var20);
      int var24 = ReliefSweeps.bank(var13, var18, var17, var8, var9, var32, 4);
      int var25 = 0;

      for (int var29 : var22) {
         var25 += Integer.bitCount(var29);
      }

      return new ReliefFit.Laid(var13, var33, var22, count(var30), var23, var25, var24, cliffs(var13, var18, var8, var32 * 1.1F));
   }

   private static int cliffs(float[] var0, float[] var1, int var2, float var3) {
      byte var4 = 0;

      for (int var5 = 0; var5 < var0.length; var5++) {
         for (int var9 : new int[]{var5 % var2 + 1 < var2 ? var5 + 1 : -1, var5 + var2 < var0.length ? var5 + var2 : -1}) {
            boolean var10 = var9 >= 0 && (!Float.isNaN(var1[var5]) || !Float.isNaN(var1[var9]));
            var4 += var10 && Math.abs(var0[var5] - var0[var9]) > var3 ? 1 : 0;
         }
      }

      return var4;
   }

   private static boolean[] anchors(float[] var0, boolean[] var1, boolean[] var2, ReliefNetwork.Roads var3, ReliefPads.Pads var4) {
      boolean[] var5 = new boolean[var0.length];

      for (int var6 = 0; var6 < var5.length; var6++) {
         boolean var7 = var2[var6] || !Float.isNaN(var3.height()[var6]);
         var5[var6] = var7 && !var1[var6] && !var4.core()[var6] && !Float.isNaN(var0[var6]);
      }

      return var5;
   }

   private static String report(ReliefWater.Bodies var0, ReliefNetwork.Roads var1, ReliefPads.Pads var2, int[] var3, ReliefFit.Laid var4) {
      return String.format(
         "water bodies %d (%d running), nodes at water %d; roads: %d samples, %d nodes, steepest grade %.1f%% (its sharpest change %.2f%% over 8 squares; turns past the rule %d, at bridges %d, at junctions %d); %s, nodes %d, under buildings and kept from a road %d, yielded to a road %d; graded nodes %d; towns' paving %d nodes, %d moved to join it, %d steps kept apart; nodes no bank could lay %d; edges steeper than a bank %d",
         var0.bodies(),
         var0.running(),
         var3[0],
         var1.samples(),
         var3[1],
         var1.steepest() * 100.0F,
         var1.sharpest() * 100.0F,
         var1.breaks()[0],
         var1.breaks()[1],
         var1.breaks()[2],
         var2.report(),
         var3[2],
         var3[4],
         var3[3],
         var4.graded(),
         count(var4.paving()),
         var4.moved(),
         var4.parted(),
         var4.steep(),
         var4.cliffs()
      );
   }

   private static float[] natural(ReliefData.Sources var0, ReliefRules var1, float[] var2, int var3, int var4, int var5) {
      float[] var6 = var0.baked().metres();
      float[] var7 = ReliefSweeps.blurred(var6, var3, var4, Math.round((float)var1.smoothingSquares() / var5));
      if (var1.townSmoothingSquares() != var1.smoothingSquares()) {
         float[] var8 = ReliefSweeps.blurred(var6, var3, var4, Math.round((float)var1.townSmoothingSquares() / var5));

         for (int var9 = 0; var9 < var7.length; var9++) {
            var7[var9] += (var8[var9] - var7[var9]) * var2[var9];
         }
      }

      float[] var10 = TownSlope.shore(ReliefWater.of(var0.ground(), var7, var3, var4, var5, var1));
      TownSlope.cap(var7, var10, var2, var3, var4, var1.metresASquare(var1.townSlope()) * var5);
      return var7;
   }

   private static float[] townShare(ReliefData.Sources var0, int var1, int var2, int var3) {
      float[] var4 = new float[var1 * var2];
      ReliefGround var5 = var0.ground();
      byte var6 = 4;

      for (int var7 = 0; var7 < var4.length; var7++) {
         byte var8 = var5.at(var7 % var1 * var3 / var6, var7 / var1 * var3 / var6);
         var4[var7] = var8 != 2 && var8 != 4 ? 0.0F : 1.0F;
      }

      int[] var11 = var0.buildings();

      for (int var12 = 0; var12 + 4 <= var11.length; var12 += 4) {
         for (int var9 = Math.max(0, var11[var12 + 1] / var3); var9 <= Math.min(var2 - 1, var11[var12 + 3] / var3); var9++) {
            for (int var10 = Math.max(0, var11[var12] / var3); var10 <= Math.min(var1 - 1, var11[var12 + 2] / var3); var10++) {
               var4[var9 * var1 + var10] = 1.0F;
            }
         }
      }

      float[] var13 = ReliefSweeps.blurred(var4, var1, var2, Math.round(64.0F / var3));

      for (int var14 = 0; var14 < var13.length; var14++) {
         float var15 = Math.max(0.0F, Math.min(1.0F, (var13[var14] - 0.05F) / 0.15F));
         var13[var14] = var15 * var15 * (3.0F - 2.0F * var15);
      }

      return var13;
   }

   static float[] fixed(ReliefPads.Pads var0, ReliefNetwork.Roads var1, ReliefWater.Bodies var2, int var3, int[] var4) {
      float[] var5 = new float[var0.pad().length];
      Arrays.fill(var5, Float.NaN);

      for (int var6 = 0; var6 < var5.length; var6++) {
         boolean var7 = !Float.isNaN(var1.height()[var6]);
         boolean var8 = var0.pad()[var6] >= 0;
         if (var0.core()[var6]) {
            var5[var6] = var0.height()[var0.pad()[var6]];
            var4[2]++;
            var4[4] += var7 ? 1 : 0;
         } else if (var7) {
            var5[var6] = var1.height()[var6];
            var4[1]++;
            var4[3] += var8 ? 1 : 0;
         } else if (var8) {
            float var9 = besideRoad(var1.height(), var6, var3);
            float var10 = var0.height()[var0.pad()[var6]];
            var5[var6] = Float.isNaN(var9) ? var10 : (var10 + var9) * 0.5F;
            var4[2]++;
         } else if (var2.ground()[var6]) {
            var5[var6] = var2.level()[var6];
            var4[0]++;
         }
      }

      return var5;
   }

   private static boolean[] graded(ReliefGround var0, boolean[] var1, int[] var2, int var3, int var4, int var5, boolean var6) {
      boolean[] var7 = new boolean[var3 * var4];
      byte var8 = 4;

      for (int var9 = 0; var9 < var0.height; var9++) {
         for (int var10 = 0; var10 < var0.width; var10++) {
            int var11 = var9 * var0.width + var10;
            byte var12 = var0.kind[var11];
            boolean var13 = var12 == 4 || var12 == 2 && !var1[var11];
            if (var6 ? var12 == 5 && !var1[var11] : var13) {
               for (int var14 = var9 * var8 / var5; var14 <= Math.min(var4 - 1, ((var9 + 1) * var8 - 1) / var5 + 1); var14++) {
                  for (int var15 = var10 * var8 / var5; var15 <= Math.min(var3 - 1, ((var10 + 1) * var8 - 1) / var5 + 1); var15++) {
                     var7[var14 * var3 + var15] = var7[var14 * var3 + var15] | var2[var14 * var3 + var15] < 0;
                  }
               }
            }
         }
      }

      return var7;
   }

   private static float besideRoad(float[] var0, int var1, int var2) {
      int var3 = var1 % var2;
      int var4 = 0;
      float var5 = 0.0F;

      for (int var9 : new int[]{var3 > 0 ? var1 - 1 : -1, var3 < var2 - 1 ? var1 + 1 : -1, var1 - var2, var1 + var2}) {
         if (var9 >= 0 && var9 < var0.length && !Float.isNaN(var0[var9])) {
            var5 += var0[var9];
            var4++;
         }
      }

      return var4 > 0 ? var5 / var4 : Float.NaN;
   }

   private static float[] padHeights(ReliefPads.Pads var0) {
      float[] var1 = new float[var0.pad().length];

      for (int var2 = 0; var2 < var1.length; var2++) {
         var1[var2] = var0.pad()[var2] >= 0 ? var0.height()[var0.pad()[var2]] : Float.NaN;
      }

      return var1;
   }

   private static boolean[] either(boolean[] var0, boolean[] var1) {
      boolean[] var2 = (boolean[])var0.clone();

      for (int var3 = 0; var3 < var2.length; var3++) {
         var2[var3] |= var1[var3];
      }

      return var2;
   }

   private static int count(boolean[] var0) {
      byte var1 = 0;

      for (boolean var5 : var0) {
         var1 += var5 ? 1 : 0;
      }

      return var1;
   }

   private static float[] nearest(float[] var0, int var1, int var2) {
      float[] var3 = new float[var0.length];
      float[] var4 = new float[var0.length];

      for (int var5 = 0; var5 < var0.length; var5++) {
         boolean var6 = !Float.isNaN(var0[var5]);
         var3[var5] = var6 ? 0.0F : Float.POSITIVE_INFINITY;
         var4[var5] = var6 ? var0[var5] : 0.0F;
      }

      ReliefSweeps.nearest(var3, var4, var1, var2);
      return var4;
   }

   private record Laid(float[] ground, boolean[] paving, byte[] apart, int graded, int moved, int parted, int steep, int cliffs) {
   }

   record Result(
      float[] ground,
      float[] water,
      byte[] walls,
      ReliefData.Counts counts,
      String report,
      ReliefPads.Pads pads,
      ReliefNetwork.Roads roads,
      ReliefWater.Bodies bodies,
      boolean[] paving,
      byte[] apart,
      float[] natural,
      byte[] flow
   ) {
   }
}
