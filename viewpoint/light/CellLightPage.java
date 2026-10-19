package viewpoint.light;

import java.util.Arrays;

public final class CellLightPage {
   public static final int TEXELS = 128;
   public static final int LAMP_INTS = 4;
   public static final int ROOM_INTS = 5;
   public static final int ROOM_LEVELS = 8;

   public static byte[] build(int var0, int var1, int[] var2, int[] var3) {
      byte[] var4 = new byte[65536];
      int var5 = var0 * 256;
      int var6 = var1 * 256;
      byte var7 = 2;

      for (int var8 = 0; var8 + 4 <= var2.length; var8 += 4) {
         lamp(var4, var5, var6, var7, var2, var8);
      }

      for (int var9 = 0; var9 + 5 <= var3.length; var9 += 5) {
         room(var4, var5, var6, var7, var3, var9);
      }

      return var4;
   }

   public static int[] cards(int var0, int var1, int[] var2) {
      int[] var3 = new int[var2.length / 4 * 4];
      byte var4 = 0;
      int var5 = var0 * 256;
      int var6 = var1 * 256;

      for (int var7 = 0; var7 + 4 <= var2.length; var7 += 4) {
         int var8 = var2[var7];
         int var9 = var2[var7 + 1];
         int var10 = var2[var7 + 3];
         int var11 = (int)engine(var10) | (int)engine(var10 >> 8) << 8 | (int)engine(var10 >> 16) << 16;
         if (var8 >= var5 && var9 >= var6 && var8 < var5 + 256 && var9 < var6 + 256 && var11 != 0) {
            var3[var4] = var8;
            var3[var4 + 1] = var9;
            var3[var4 + 2] = var2[var7 + 2];
            var3[var4 + 3] = var11;
            var4 += 4;
         }
      }

      return Arrays.copyOf(var3, var4);
   }

   private static void lamp(byte[] var0, int var1, int var2, int var3, int[] var4, int var5) {
      int var6 = var4[var5];
      int var7 = var4[var5 + 1];
      int var8 = var4[var5 + 2];
      int var9 = var4[var5 + 3];
      int var10 = Math.min(var9 >>> 24 & 63, 20);
      double var11 = 3.0 * Math.max(0, var8);
      if (var10 > 0 && !(var11 >= var10)) {
         double var13 = engine(var9);
         double var15 = engine(var9 >> 8);
         double var17 = engine(var9 >> 16);
         int var19 = Math.max(0, Math.floorDiv(var6 - var10 - var1, var3));
         int var20 = Math.min(127, (var6 + var10 - var1) / var3);
         int var21 = Math.max(0, Math.floorDiv(var7 - var10 - var2, var3));
         int var22 = Math.min(127, (var7 + var10 - var2) / var3);

         for (int var23 = var21; var23 <= var22; var23++) {
            for (int var24 = var19; var24 <= var20; var24++) {
               double var25 = Math.abs(var1 + (var24 + 0.5) * var3 - (var6 + 0.5));
               double var27 = Math.abs(var2 + (var23 + 0.5) * var3 - (var7 + 0.5));
               double var29 = Math.max(var25, var27) + (LightFlood.DIAGONAL - 1.0) * Math.min(var25, var27) + var11;
               if (var29 < var10) {
                  double var31 = 1.0 - var29 / var10;
                  int var33 = (var23 * 128 + var24) * 4;
                  brighten(var0, var33, (int)Math.floor(var13 * var31));
                  brighten(var0, var33 + 1, (int)Math.floor(var15 * var31));
                  brighten(var0, var33 + 2, (int)Math.floor(var17 * var31));
               }
            }
         }
      }
   }

   private static void room(byte[] var0, int var1, int var2, int var3, int[] var4, int var5) {
      int var6 = var4[var5] - var1;
      int var7 = var4[var5 + 1] - var2;
      int var8 = var4[var5 + 2];
      int var9 = var4[var5 + 3];
      int var10 = var4[var5 + 4];
      if (var8 >= 0 && var8 < 8) {
         for (int var11 = Math.max(0, Math.floorDiv(var7, var3)); var11 <= Math.min(127, (var7 + var10 - 1) / var3); var11++) {
            for (int var12 = Math.max(0, Math.floorDiv(var6, var3)); var12 <= Math.min(127, (var6 + var9 - 1) / var3); var12++) {
               var0[(var11 * 128 + var12) * 4 + 3] = (byte)(var0[(var11 * 128 + var12) * 4 + 3] | (byte)(1 << var8));
            }
         }
      }
   }

   private static double engine(int var0) {
      return 255.0 * Math.min(1.0, (var0 & 0xFF) / 255.0 * 2.0);
   }

   private static void brighten(byte[] var0, int var1, int var2) {
      var0[var1] = (byte)Math.max(var0[var1] & 255, Math.min(255, var2));
   }

   private CellLightPage() {
   }
}
