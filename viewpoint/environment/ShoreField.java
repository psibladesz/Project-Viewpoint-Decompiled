package viewpoint.environment;

import java.util.Arrays;

final class ShoreField {
   static final float STEPS_PER_SQUARE = 16.0F;
   private static final float DIAGONAL = (float)Math.sqrt(2.0);
   private static final float FAR = 1000000.0F;
   private static final float[] distance = new float[4096];

   static void compute(byte[] var0, byte[] var1) {
      byte var2 = 64;
      boolean var3 = false;

      for (int var4 = 0; var4 < distance.length; var4++) {
         distance[var4] = var0[var4] == 0 ? 0.0F : 1000000.0F;
         var3 |= var0[var4] != 0;
      }

      if (!var3) {
         Arrays.fill(var1, (byte)0);
      } else {
         for (int var6 = 0; var6 < var2; var6++) {
            for (int var5 = 0; var5 < var2; var5++) {
               relax(var5, var6, var5 - 1, var6, 1.0F);
               relax(var5, var6, var5, var6 - 1, 1.0F);
               relax(var5, var6, var5 - 1, var6 - 1, DIAGONAL);
               relax(var5, var6, var5 + 1, var6 - 1, DIAGONAL);
            }
         }

         for (int var7 = var2 - 1; var7 >= 0; var7--) {
            for (int var9 = var2 - 1; var9 >= 0; var9--) {
               relax(var9, var7, var9 + 1, var7, 1.0F);
               relax(var9, var7, var9, var7 + 1, 1.0F);
               relax(var9, var7, var9 + 1, var7 + 1, DIAGONAL);
               relax(var9, var7, var9 - 1, var7 + 1, DIAGONAL);
            }
         }

         for (int var8 = 0; var8 < distance.length; var8++) {
            var1[var8] = (byte)Math.min(255, Math.round(distance[var8] * 16.0F));
         }
      }
   }

   private static void relax(int var0, int var1, int var2, int var3, float var4) {
      byte var5 = 64;
      if (var2 >= 0 && var3 >= 0 && var2 < var5 && var3 < var5) {
         int var6 = var1 * var5 + var0;
         distance[var6] = Math.min(distance[var6], distance[var3 * var5 + var2] + var4);
      }
   }

   private ShoreField() {
   }
}
