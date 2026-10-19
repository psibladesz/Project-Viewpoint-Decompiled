package viewpoint.light;

import java.util.Arrays;
import java.util.List;

final class LightModel {
   static final byte SKY_REACH = 15;
   static final byte NO_SKY = 16;
   static final int ROOM_R = 229;
   static final int ROOM_G = 216;
   static final int ROOM_B = 209;
   static final int ROOM_RADIUS = 7;
   static final int MAX_RADIUS = 20;
   private static final int[] STRAIGHT = new int[]{0, 2, 4, 6};

   static int levelsReached(int var0) {
      return (var0 - 1) / 3;
   }

   static byte[] skySteps(LightLayout var0) {
      byte[] var1 = new byte[var0.size()];
      skySteps(var0, var1, new int[var0.size()]);
      return var1;
   }

   static void skySteps(LightLayout var0, byte[] var1, int[] var2) {
      int var3 = 0;
      int var4 = 0;
      Arrays.fill(var1, 0, var0.size(), (byte)16);

      for (int var5 = 0; var5 < var0.size(); var5++) {
         if (var0.exists(var5) && var0.openAir(var5)) {
            var1[var5] = 0;
            var2[var4++] = var5;
         }
      }

      while (var3 < var4) {
         int var12 = var2[var3++];
         byte var6 = (byte)(var1[var12] + 1);
         if (var6 <= 15) {
            for (int var10 : STRAIGHT) {
               int var11 = var0.neighbour(var12, var10);
               if (var11 >= 0
                  && var0.exists(var11)
                  && var1[var11] > var6
                  && (var0.pass(var12, var10) != 4 || var0.pass(var11, LightLayout.REVERSE[var10]) != 4)) {
                  var1[var11] = var6;
                  var2[var4++] = var11;
               }
            }
         }
      }
   }

   static int[] sources(LightLayout var0, List<SourceRegister.Lamp> var1, List<SourceRegister.RoomLight> var2) {
      int[] var3 = new int[var0.size()];
      LightFlood var4 = new LightFlood(var0);
      int[] var5 = new int[1];

      for (SourceRegister.Lamp var7 : var1) {
         var5[0] = var0.index(var7.x(), var7.y(), var7.z());
         int var8 = Math.min(var7.radius(), 20);
         if (var5[0] >= 0 && var8 > 0) {
            var4.run(var5, 1, var8);
            brighten(var3, var4, 255.0 * var7.r(), 255.0 * var7.g(), 255.0 * var7.b(), var8);
         }
      }

      int[] var12 = new int[64];

      for (SourceRegister.RoomLight var14 : var2) {
         int var9 = 0;

         for (int var10 = var14.y(); var10 < var14.y() + var14.height(); var10++) {
            for (int var11 = var14.x(); var11 < var14.x() + var14.width(); var11++) {
               if (var9 == var12.length) {
                  var12 = Arrays.copyOf(var12, var9 * 2);
               }

               var12[var9++] = var0.index(var11, var10, var14.z());
            }
         }

         var4.run(var12, var9, 7.0);
         brighten(var3, var4, 229.0, 216.0, 209.0, 7.0);
      }

      return var3;
   }

   static int combine(int var0, int var1, int var2) {
      int var3 = 0;

      for (int var4 = 0; var4 <= 16; var4 += 8) {
         int var5 = Math.max(0, (var0 >> var4 & 0xFF) - 16 * var1);
         var3 |= Math.max(var5, var2 >> var4 & 0xFF) << var4;
      }

      return var3;
   }

   private static void brighten(int[] var0, LightFlood var1, double var2, double var4, double var6, double var8) {
      for (int var10 = 0; var10 < var1.touchedCount(); var10++) {
         int var11 = var1.touched(var10);
         var0[var11] = brighter(var0[var11], light(var1, var11, var2, var4, var6, var8));
      }
   }

   static int light(LightFlood var0, int var1, double var2, double var4, double var6, double var8) {
      double var10 = var0.transmission(var1);
      double var12 = 1.0 - var0.cost(var1) / var8;
      return (int)Math.floor(var2 * var10 * var12) | (int)Math.floor(var4 * var10 * var12) << 8 | (int)Math.floor(var6 * var10 * var12) << 16;
   }

   static int brighter(int var0, int var1) {
      return Math.max(var0 & 0xFF, var1 & 0xFF) | Math.max(var0 >> 8 & 0xFF, var1 >> 8 & 0xFF) << 8 | Math.max(var0 >> 16 & 0xFF, var1 >> 16 & 0xFF) << 16;
   }

   private LightModel() {
   }
}
