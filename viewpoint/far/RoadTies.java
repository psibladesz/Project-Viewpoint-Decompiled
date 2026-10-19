package viewpoint.far;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

final class RoadTies {
   private static final float DRIVEWAY_REACH = 16.0F;
   private static final float REACH = 32.0F;
   private static final float AIM = 0.95F;

   private RoadTies() {
   }

   static RoadGrades.Ties of(ReliefNetwork.Samples var0, ReliefPads.Pads var1, float[] var2, int var3, int var4, int var5, ReliefRules var6) {
      Map<Long, Float> var7 = roadPad(var0, var1, var3, var4, var5, var6);
      Map<Long, Float> var8 = padPad(var1, var3, var4, var5, var6);
      long var9 = var1.height().length;
      int[] var11 = new int[var7.size() * 2];
      int[] var12 = new int[var8.size() * 2];
      float[] var13 = new float[var7.size()];
      float[] var14 = new float[var8.size()];
      int var15 = 0;

      for (Entry<Long, Float> var17 : var7.entrySet()) {
         var11[2 * var15] = (int)((Long)var17.getKey() / var9);
         var11[2 * var15 + 1] = (int)((Long)var17.getKey() % var9);
         var13[var15++] = (Float)var17.getValue();
      }

      var15 = 0;

      for (Entry<Long, Float> var21 : var8.entrySet()) {
         var12[2 * var15] = (int)((Long)var21.getKey() / var9);
         var12[2 * var15 + 1] = (int)((Long)var21.getKey() % var9);
         var14[var15++] = (Float)var21.getValue();
      }

      float[] var20 = ceilings(var1, var2, var3, var4, var5, var6);
      return new RoadGrades.Ties(var1.height(), var11, var13, var12, var14, var1.floor(), var20);
   }

   private static float[] ceilings(ReliefPads.Pads var0, float[] var1, int var2, int var3, int var4, ReliefRules var5) {
      float[] var6 = new float[var1.length];
      float[] var7 = new float[var1.length];

      for (int var8 = 0; var8 < var1.length; var8++) {
         boolean var9 = !Float.isNaN(var1[var8]);
         var6[var8] = var9 ? 0.0F : Float.POSITIVE_INFINITY;
         var7[var8] = var9 ? var1[var8] : 0.0F;
      }

      ReliefSweeps.nearest(var6, var7, var2, var3);
      float var12 = var5.metresASquare(var5.bank()) * 0.95F;
      float[] var13 = new float[var0.height().length];
      Arrays.fill(var13, Float.POSITIVE_INFINITY);

      for (int var10 = 0; var10 < var1.length; var10++) {
         int var11 = var0.pad()[var10];
         if (var11 >= 0 && var6[var10] * var4 <= 32.0F) {
            var13[var11] = Math.min(var13[var11], var7[var10] + var5.freeboard() + var12 * var6[var10] * var4);
         }
      }

      return var13;
   }

   private static Map<Long, Float> roadPad(ReliefNetwork.Samples var0, ReliefPads.Pads var1, int var2, int var3, int var4, ReliefRules var5) {
      float var6 = var5.metresASquare(var5.bank()) * 0.95F;
      float var7 = var5.metresASquare(var5.driveway()) * 0.95F;
      int var8 = (int)Math.ceil(32.0F / var4);
      long var9 = var1.height().length;
      HashMap<Long, Float> var11 = new HashMap<>();

      for (int var12 = 0; var12 < var0.count(); var12++) {
         float var13 = var0.xs()[var12];
         float var14 = var0.ys()[var12];
         float var15 = var0.widths()[var12] * 0.5F;
         int var16 = Math.round(var13 / var4);
         int var17 = Math.round(var14 / var4);

         for (int var18 = Math.max(0, var17 - var8); var18 <= Math.min(var3 - 1, var17 + var8); var18++) {
            for (int var19 = Math.max(0, var16 - var8); var19 <= Math.min(var2 - 1, var16 + var8); var19++) {
               int var20 = var1.pad()[var18 * var2 + var19];
               float var21 = (float)Math.hypot(var19 * var4 - var13, var18 * var4 - var14);
               if (var20 >= 0 && var21 <= 32.0F && var21 > var15) {
                  float var22 = var21 - var15;
                  float var23 = (var22 <= 16.0F ? var7 : var6) * var22;
                  var11.merge(var0.root()[var12] * var9 + var20, var23, Math::min);
               }
            }
         }
      }

      return var11;
   }

   private static Map<Long, Float> padPad(ReliefPads.Pads var0, int var1, int var2, int var3, ReliefRules var4) {
      float var5 = var4.metresASquare(var4.bank()) * 0.95F;
      int var6 = (int)Math.ceil(32.0F / var3);
      int var7 = var1;
      int[] var8 = var0.pad();
      long var9 = var0.height().length;
      HashMap<Long, Float> var11 = new HashMap<>();

      for (int var12 = 0; var12 < var8.length; var12++) {
         int var13 = var8[var12];
         int var14 = var12 % var7;
         int var15 = var12 / var7;
         boolean var16 = var13 >= 0
                 && (
                 var14 == 0
                         || var14 == var7 - 1
                         || var15 == 0
                         || var15 == var2 - 1
                         || var8[var12 - 1] != var13
                         || var8[var12 + 1] != var13
                         || var8[var12 - var7] != var13
                         || var8[var12 + var7] != var13
         );

         for (int var17 = Math.max(0, var15 - var6); var16 && var17 <= Math.min(var2 - 1, var15 + var6); var17++) {
            for (int var18 = Math.max(0, var14 - var6); var18 <= Math.min(var7 - 1, var14 + var6); var18++) {
               int var19 = var8[var17 * var7 + var18];
               float var20 = (float)Math.hypot((var18 - var14) * var3, (var17 - var15) * var3);
               if (var19 > var13 && var20 <= 32.0F) {
                  var11.merge(var13 * var9 + var19, var5 * var20, Math::min);
               }
            }
         }
      }

      return var11;
   }
}