package viewpoint.world;

public final class TileMesh {
   public static final int STRIDE = 8;
   static final TileMesh EMPTY = new TileMesh(new float[0]);
   public final float[] data;
   public final int vertCount;
   final long hash;
   final float top;
   public final float[] bounds = new float[6];

   TileMesh(float[] var1) {
      this.data = var1;
      this.vertCount = var1.length / 8;
      this.hash = Recipe.mix(-7046029254386353131L, var1);

      for (int var2 = 0; var2 < var1.length; var2 += 8) {
         for (int var3 = 0; var3 < 3; var3++) {
            this.bounds[var3] = var2 == 0 ? var1[var3] : Math.min(this.bounds[var3], var1[var2 + var3]);
            this.bounds[var3 + 3] = var2 == 0 ? var1[var3] : Math.max(this.bounds[var3 + 3], var1[var2 + var3]);
         }
      }

      this.top = Math.max(0.0F, this.bounds[4]);
   }
}
