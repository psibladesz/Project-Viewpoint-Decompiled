package viewpoint.far;

import java.io.File;
import java.util.Arrays;

final class ReliefGround {
   static final byte OTHER = 0;
   static final byte WATER = 1;
   static final byte PAVED = 2;
   static final byte NO_CELL = 3;
   static final byte GRAVEL = 4;
   static final byte RAIL = 5;
   static final int SQUARES_A_PIXEL = 4;
   private static final int ASPHALT = 8421504;
   private static final int CONCRETE = 10526884;
   private static final int GRAVEL_COLOUR = 14274487;
   private static final int RAIL_COLOUR = 4799019;
   private static final int BUILT_FLOOR = 9329494;
   final int width;
   final int height;
   final byte[] kind;

   ReliefGround(int var1, int var2, byte[] var3) {
      this.width = var1;
      this.height = var2;
      this.kind = var3;
   }

   static ReliefGround read(String var0, int var1, int var2) {
      byte[] var3 = new byte[var1 * var2];
      Arrays.fill(var3, (byte)3);
      Pyramid var4 = Pyramid.of(var0);
      byte var5 = 64;
      int[] var6 = new int[var5 * var5];
      int[] var7 = new int[var5 * var5];
      int[] var8 = new int[var5 * var5];
      int[] var9 = new int[var5 * var5];

      for (int var10 = 0; var10 * var5 < var2; var10++) {
         for (int var11 = 0; var11 * var5 < var1; var11++) {
            if (new File(var0, var11 + "_" + var10 + ".lotheader").isFile()) {
               int[] var12 = var4.image(var11, var10);
               if (var12 != null) {
                  count(var12, var5, var6, var7, var8, var9);
                  byte var13 = 8;

                  for (int var14 = 0; var14 < var5 && var10 * var5 + var14 < var2; var14++) {
                     for (int var15 = 0; var15 < var5 && var11 * var5 + var15 < var1; var15++) {
                        int var16 = var14 * var5 + var15;
                        var3[(var10 * var5 + var14) * var1 + var11 * var5 + var15] = (byte)(var6[var16] >= var13
                           ? 1
                           : (var7[var16] >= var13 ? 2 : (var9[var16] * 2 >= var13 ? 5 : (var7[var16] + var8[var16] >= var13 ? 4 : 0))));
                     }
                  }
               }
            }
         }
      }

      return new ReliefGround(var1, var2, var3);
   }

   private static void count(int[] var0, int var1, int[] var2, int[] var3, int[] var4, int[] var5) {
      Arrays.fill(var2, 0);
      Arrays.fill(var3, 0);
      Arrays.fill(var4, 0);
      Arrays.fill(var5, 0);

      for (int var6 = 0; var6 < 256; var6++) {
         for (int var7 = 0; var7 < 256; var7++) {
            int var8 = var0[var6 * 256 + var7] & 16777215;
            int var9 = var6 / 4 * var1 + var7 / 4;
            if (ReliefWater.water(var8)) {
               var2[var9]++;
            } else if (var8 == 8421504 || var8 == 10526884 || var8 == 9329494) {
               var3[var9]++;
            } else if (var8 == 14274487) {
               var4[var9]++;
            } else if (var8 == 4799019) {
               var5[var9]++;
            }
         }
      }
   }

   byte at(int var1, int var2) {
      return var1 >= 0 && var2 >= 0 && var1 < this.width && var2 < this.height ? this.kind[var2 * this.width + var1] : 3;
   }
}
