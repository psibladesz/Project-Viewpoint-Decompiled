package viewpoint.light;

final class CarLamps {
   static int merge(float[][] var0, boolean[] var1, int var2) {
      int var3 = var2;

      for (int var4 = 0; var4 < 2; var4++) {
         boolean var5 = var4 == 1;
         int var6 = -1;
         int var7 = -1;

         for (int var8 = 0; var8 < var3; var8++) {
            if (var1[var8] == var5) {
               if (var6 < 0) {
                  var6 = var8;
               } else if (var7 < 0) {
                  var7 = var8;
               }
            }
         }

         if (var7 >= 0) {
            join(var0[var6], var0[var7]);
            var3 = drop(var0, var1, var3, var7);
         }
      }

      return var3;
   }

   private static void join(float[] var0, float[] var1) {
      float var2 = var1[0] - var0[0];
      float var3 = var1[1] - var0[1];
      float var4 = var1[2] - var0[2];
      float var5 = (float)Math.sqrt(var2 * var2 + var3 * var3 + var4 * var4);

      for (int var6 = 0; var6 < 3; var6++) {
         var0[0 + var6] = (var0[0 + var6] + var1[0 + var6]) / 2.0F;
         var0[8 + var6] = var0[8 + var6] + var1[8 + var6];
      }

      var0[3] = Math.max(var0[3], var1[3]);
      var0[19] = Math.max(var0[19], var1[19]) + var5 / 2.0F;
   }

   private static int drop(float[][] var0, boolean[] var1, int var2, int var3) {
      float[] var4 = var0[var3];

      for (int var5 = var3; var5 < var2 - 1; var5++) {
         var0[var5] = var0[var5 + 1];
         var1[var5] = var1[var5 + 1];
      }

      var0[var2 - 1] = var4;
      return var2 - 1;
   }

   private CarLamps() {
   }
}
