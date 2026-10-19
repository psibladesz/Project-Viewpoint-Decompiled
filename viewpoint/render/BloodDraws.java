package viewpoint.render;

public final class BloodDraws {
   public static final int MAX = 1024;
   static final int VEC4S = 6;
   static final int VALUES = 24;
   final float[] values = new float[24576];
   final int[] textures = new int[1024];
   int count;

   public void clear() {
      this.count = 0;
   }

   public boolean full() {
      return this.count == 1024;
   }

   public void add(int var1, float[] var2, float[] var3, float[] var4, float[] var5, float[] var6, float var7) {
      if (!this.full()) {
         int var8 = this.count * 24;
         System.arraycopy(var2, 0, this.values, var8, 3);
         this.values[var8 + 3] = var6[3];
         System.arraycopy(var3, 0, this.values, var8 + 4, 3);
         this.values[var8 + 7] = var6[0];
         System.arraycopy(var3, 3, this.values, var8 + 8, 3);
         this.values[var8 + 11] = var6[1];
         System.arraycopy(var4, 0, this.values, var8 + 12, 4);
         this.values[var8 + 16] = var4[4];
         this.values[var8 + 17] = var4[5];
         this.values[var8 + 18] = var6[2];
         this.values[var8 + 19] = var7;
         System.arraycopy(var5, 0, this.values, var8 + 20, 4);
         this.textures[this.count++] = var1;
      }
   }
}
