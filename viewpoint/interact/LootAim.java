package viewpoint.interact;

final class LootAim {
   static final int BOX = 9;
   static final int FLAT = 8;
   static final float SOLID = -1.0F;
   static final float KEEP = 0.08F;
   static final float TAKE_OVER = 0.25F;
   private static final float GRAZE = 1000.0F;
   private static final float[] turned = new float[6];

   static float enter(float[] var0, float[] var1, int var2, float var3) {
      int var4 = var2 * 9;
      float[] var5 = unturn(var0, var1, var4);
      float var6 = 0.0F;
      float var7 = Float.MAX_VALUE;

      for (int var8 = 0; var8 < 3; var8++) {
         float var9 = var5[var8];
         float var10 = var5[var8 + 3];
         float var11 = var1[var4 + var8] - var3;
         float var12 = var1[var4 + 3 + var8] + var3;
         if (Math.abs(var10) < 1.0E-6F) {
            if (var9 < var11 || var9 > var12) {
               return -1.0F;
            }
         } else {
            float var13 = (var11 - var9) / var10;
            float var14 = (var12 - var9) / var10;
            var6 = Math.max(var6, Math.min(var13, var14));
            var7 = Math.min(var7, Math.max(var13, var14));
            if (var6 > var7) {
               return -1.0F;
            }
         }
      }

      return var6;
   }

   static int pick(float[] var0, float[] var1, int var2, int var3, float var4) {
      int var5 = -1;
      float var6 = Float.MAX_VALUE;

      for (int var7 = 0; var7 < var2; var7++) {
         float var8 = rank(var0, var1, var7, 0.0F, var4);
         if (var8 >= 0.0F && var8 < var6) {
            var5 = var7;
            var6 = var8;
         }
      }

      if (var3 >= 0 && var3 < var2 && var3 != var5) {
         float var9 = rank(var0, var1, var3, 0.08F, var4);
         if (var9 >= 0.0F && (var5 < 0 || var9 <= var6 + 0.25F)) {
            return var3;
         }
      }

      return var5;
   }

   private static float rank(float[] var0, float[] var1, int var2, float var3, float var4) {
      float var5 = enter(var0, var1, var2, var3);
      if (var5 < 0.0F) {
         return -1.0F;
      } else {
         int var6 = var2 * 9;
         int var7 = (int)var1[var6 + 8];
         if (var7 < 0) {
            return var5 >= var4 ? var5 : -1.0F;
         } else {
            float var8 = crossing(var0, var1, var6, var7, var3);
            if (var8 >= var4) {
               return var8;
            } else {
               return var5 >= var4 ? var5 + 1000.0F : -1.0F;
            }
         }
      }
   }

   private static float crossing(float[] var0, float[] var1, int var2, int var3, float var4) {
      float[] var5 = unturn(var0, var1, var2);
      float var6 = var5[var3 + 3];
      if (Math.abs(var6) < 1.0E-6F) {
         return -1.0F;
      }

      float var7 = ((var1[var2 + var3] + var1[var2 + 3 + var3]) * 0.5F - var5[var3]) / var6;

      for (int var8 = 0; var8 < 3 && var7 >= 0.0F; var8++) {
         float var9 = var5[var8] + var7 * var5[var8 + 3];
         if (var8 != var3 && (var9 < var1[var2 + var8] - var4 || var9 > var1[var2 + 3 + var8] + var4)) {
            return -1.0F;
         }
      }

      return var7;
   }

   private static float[] unturn(float[] var0, float[] var1, int var2) {
      float var3 = var1[var2 + 6];
      float var4 = var1[var2 + 7];
      if (var4 == 0.0F && var3 == 1.0F) {
         return var0;
      }

      float var5 = (var1[var2] + var1[var2 + 3]) * 0.5F;
      float var6 = (var1[var2 + 1] + var1[var2 + 4]) * 0.5F;
      float var7 = var0[0] - var5;
      float var8 = var0[1] - var6;
      turned[0] = var5 + var7 * var3 + var8 * var4;
      turned[1] = var6 - var7 * var4 + var8 * var3;
      turned[2] = var0[2];
      turned[3] = var0[3] * var3 + var0[4] * var4;
      turned[4] = -var0[3] * var4 + var0[4] * var3;
      turned[5] = var0[5];
      return turned;
   }

   private LootAim() {
   }
}
