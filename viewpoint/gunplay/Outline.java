package viewpoint.gunplay;

final class Outline {
   static final float CELL = 0.0125F;
   static final int STADIUM = 5;
   private static final int SAMPLES = 2;
   final float left;
   final float bottom;
   final int columns;
   final int rows;
   final float[] covered;
   final double[] across;
   final double[] up;
   private final float[] stadiums;

   private Outline(float[] var1) {
      this.stadiums = var1;
      float var2 = Float.MAX_VALUE;
      float var3 = Float.MAX_VALUE;
      float var4 = -Float.MAX_VALUE;
      float var5 = -Float.MAX_VALUE;

      for (int var6 = 0; var6 < var1.length; var6 += 5) {
         float var7 = var1[var6 + 4];
         var2 = Math.min(var2, Math.min(var1[var6], var1[var6 + 2]) - var7);
         var4 = Math.max(var4, Math.max(var1[var6], var1[var6 + 2]) + var7);
         var3 = Math.min(var3, Math.min(var1[var6 + 1], var1[var6 + 3]) - var7);
         var5 = Math.max(var5, Math.max(var1[var6 + 1], var1[var6 + 3]) + var7);
      }

      this.left = var2;
      this.bottom = var3;
      this.columns = Math.max(1, (int)Math.ceil((var4 - var2) / 0.0125F));
      this.rows = Math.max(1, (int)Math.ceil((var5 - var3) / 0.0125F));
      this.covered = new float[this.columns * this.rows];
      this.across = new double[this.columns];
      this.up = new double[this.rows];
      float var14 = 0.25F;

      for (int var15 = 0; var15 < this.rows; var15++) {
         for (int var8 = 0; var8 < this.columns; var8++) {
            float var9 = 0.0F;

            for (int var10 = 0; var10 < 2; var10++) {
               for (int var11 = 0; var11 < 2; var11++) {
                  float var12 = this.left + (var8 + (var11 + 0.5F) / 2.0F) * 0.0125F;
                  float var13 = this.bottom + (var15 + (var10 + 0.5F) / 2.0F) * 0.0125F;
                  if (this.covers(var12, var13)) {
                     var9 += var14;
                  }
               }
            }

            this.covered[var15 * this.columns + var8] = var9;
         }
      }
   }

   static Outline of(float[] var0) {
      return new Outline((float[])var0.clone());
   }

   boolean covers(double var1, double var3) {
      for (int var5 = 0; var5 < this.stadiums.length; var5 += 5) {
         double var6 = this.stadiums[var5];
         double var8 = this.stadiums[var5 + 1];
         double var10 = this.stadiums[var5 + 2] - var6;
         double var12 = this.stadiums[var5 + 3] - var8;
         double var14 = var10 * var10 + var12 * var12;
         double var16 = var14 > 0.0 ? Math.min(1.0, Math.max(0.0, ((var1 - var6) * var10 + (var3 - var8) * var12) / var14)) : 0.0;
         double var18 = var6 + var16 * var10 - var1;
         double var20 = var8 + var16 * var12 - var3;
         double var22 = this.stadiums[var5 + 4];
         if (var18 * var18 + var20 * var20 <= var22 * var22) {
            return true;
         }
      }

      return false;
   }
}
