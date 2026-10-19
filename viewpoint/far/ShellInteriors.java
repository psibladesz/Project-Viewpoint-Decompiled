package viewpoint.far;

import java.util.Arrays;
import viewpoint.platform.LiveSettings;

final class ShellInteriors {
   static final int[] NONE = new int[0];
   static final byte WALLS = 1;
   static final byte FURNISHED = 2;
   static final LiveSettings.Toggle FURNITURE = LiveSettings.toggle(
      "lod.interiorFurniture", "Furniture in buildings across the grid's edge", "World/Levels of detail", false
   );
   private static int[] scratch = new int[64];

   static int[] bounds(short[][] var0, int var1) {
      int[] var2 = new int[(var1 + 1) * 4];

      for (int var3 = 0; var3 <= var1; var3++) {
         var2[var3 * 4] = var2[var3 * 4 + 1] = Integer.MAX_VALUE;
         var2[var3 * 4 + 2] = var2[var3 * 4 + 3] = Integer.MIN_VALUE;
      }

      for (short[] var6 : var0) {
         for (int var7 = 0; var6 != null && var7 < var6.length; var7++) {
            short var8 = var6[var7];
            if (var8 > 0 && var8 <= var1) {
               int var9 = var7 % 256;
               int var10 = var7 / 256;
               var2[var8 * 4] = Math.min(var2[var8 * 4], var9);
               var2[var8 * 4 + 1] = Math.min(var2[var8 * 4 + 1], var10);
               var2[var8 * 4 + 2] = Math.max(var2[var8 * 4 + 2], var9);
               var2[var8 * 4 + 3] = Math.max(var2[var8 * 4 + 3], var10);
            }
         }
      }

      return var2;
   }

   static int[] select(int[] var0, int var1, int var2, int var3, int var4, float var5, float var6, float var7, int[] var8) {
      int var9 = 0;
      int var10 = var3 * 64;
      int var11 = var4 * 64;
      int var12 = var10 + 64 - 1;
      int var13 = var11 + 64 - 1;

      for (int var14 = 1; var14 < var0.length / 4; var14++) {
         int var15 = var14 * 4;
         if (var0[var15 + 2] >= var10 && var0[var15] <= var12 && var0[var15 + 3] >= var11 && var0[var15 + 1] <= var13) {
            float var16 = var1 * 256 + var0[var15] - var5;
            float var17 = var16 + var0[var15 + 2] + 1.0F - var0[var15];
            float var18 = var2 * 256 + var0[var15 + 1] - var6;
            float var19 = var18 + var0[var15 + 3] + 1.0F - var0[var15 + 1];
            float var20 = Math.max(0.0F, Math.max(var16, -var17));
            float var21 = Math.max(0.0F, Math.max(var18, -var19));
            float var22 = Math.max(Math.abs(var16), Math.abs(var17));
            float var23 = Math.max(Math.abs(var18), Math.abs(var19));
            float var24 = Arrays.binarySearch(var8, var14) >= 0 ? 32.0F : 0.0F;
            float var25 = var7 + var24;
            float var26 = Math.max(0.0F, var7 - var24);
            if (var20 * var20 + var21 * var21 < var25 * var25 && var22 * var22 + var23 * var23 > var26 * var26) {
               if (var9 == scratch.length) {
                  scratch = Arrays.copyOf(scratch, var9 * 2);
               }

               scratch[var9++] = var14;
            }
         }
      }

      return Arrays.equals(scratch, 0, var9, var8, 0, var8.length) ? var8 : (var9 == 0 ? NONE : Arrays.copyOf(scratch, var9));
   }

   static byte[] mask(int[] var0, boolean var1) {
      if (var0.length == 0) {
         return null;
      }

      byte[] var2 = new byte[var0[var0.length - 1] + 1];

      for (int var6 : var0) {
         var2[var6] = (byte)(var1 ? 2 : 1);
      }

      return var2;
   }

   private ShellInteriors() {
   }
}
