package viewpoint.world;

import java.util.Arrays;

public final class RoofSurface {
   public static final byte NONE = 0;
   public static final byte DECLARED = 1;
   public static final byte INFERRED = 2;
   public static final byte MOVED = 3;
   public static final byte LID = 4;
   public static final int POINTS = 9;
   private static final int[] RING = new int[]{0, 3, 6, 7, 8, 5, 2, 1};
   private static final int CENTRE = 4;
   private static final float FLAT = 1.0E-4F;
   public final int width;
   public final int height;
   private final float[] heights;
   private final byte[] sources;

   RoofSurface(int var1, int var2, float[] var3, byte[] var4) {
      this.width = var1;
      this.height = var2;
      this.heights = var3;
      this.sources = var4;
   }

   static RoofSurface empty(int var0, int var1) {
      float[] var2 = new float[var0 * var1 * 9];
      Arrays.fill(var2, Float.NaN);
      return new RoofSurface(var0, var1, var2, new byte[var0 * var1]);
   }

   public byte source(int var1, int var2) {
      return this.sources[var2 * this.width + var1];
   }

   public boolean built(int var1, int var2) {
      byte var3 = this.sources[var2 * this.width + var1];
      return var3 == 1 || var3 == 2;
   }

   public boolean stands(int var1, int var2) {
      return this.built(var1, var2) || this.sources[var2 * this.width + var1] == 4;
   }

   public float height(int var1, int var2, int var3) {
      return this.heights[(var2 * this.width + var1) * 9 + var3];
   }

   void set(int var1, byte var2, float[] var3) {
      this.sources[var1] = var2;
      System.arraycopy(var3, 0, this.heights, var1 * 9, 9);
   }

   RoofSurface copy() {
      return new RoofSurface(this.width, this.height, (float[])this.heights.clone(), (byte[])this.sources.clone());
   }

   void take(RoofSurface var1, int var2) {
      this.sources[var2] = var1.sources[var2];
      System.arraycopy(var1.heights, var2 * 9, this.heights, var2 * 9, 9);
   }

   void moved(int var1) {
      this.sources[var1] = 3;
   }

   void lid(int var1, float var2) {
      this.sources[var1] = 4;
      Arrays.fill(this.heights, var1 * 9, (var1 + 1) * 9, var2);
   }

   public void triangles(RoofSurface.Triangles var1) {
      float[] var2 = new float[3];
      float[] var3 = new float[3];
      float[] var4 = new float[3];

      for (int var5 = 0; var5 < this.width * this.height; var5++) {
         this.triangles(var5, var1, var2, var3, var4);
      }
   }

   public void triangles(int var1, RoofSurface.Triangles var2) {
      this.triangles(var1, var2, new float[3], new float[3], new float[3]);
   }

   private void triangles(int var1, RoofSurface.Triangles var2, float[] var3, float[] var4, float[] var5) {
      if (this.sources[var1] == 1 || this.sources[var1] == 2) {
         if (this.planar(var1)) {
            var2.triangle(var1, this.point(var1, 0, var3), this.point(var1, 6, var4), this.point(var1, 8, var5));
            var2.triangle(var1, this.point(var1, 0, var3), this.point(var1, 8, var4), this.point(var1, 2, var5));
         } else {
            for (int var6 = 0; var6 < RING.length; var6++) {
               this.point(var1, 4, var3);
               this.point(var1, RING[var6], var4);
               this.point(var1, RING[(var6 + 1) % RING.length], var5);
               var2.triangle(var1, var3, var4, var5);
            }
         }
      }
   }

   private boolean planar(int var1) {
      int var2 = var1 * 9;
      float var3 = this.heights[var2];
      float var4 = this.heights[var2 + 2] - var3;
      float var5 = this.heights[var2 + 6] - var3;

      for (int var6 = 0; var6 < 9; var6++) {
         float var7 = var3 + var4 * (var6 % 3) / 2.0F + var5 * (var6 / 3) / 2.0F;
         if (Math.abs(this.heights[var2 + var6] - var7) > 1.0E-4F) {
            return false;
         }
      }

      return true;
   }

   private float[] point(int var1, int var2, float[] var3) {
      var3[0] = var1 % this.width + var2 % 3 / 2.0F;
      var3[1] = this.heights[var1 * 9 + var2];
      var3[2] = var1 / this.width + var2 / 3 / 2.0F;
      return var3;
   }

   public interface Triangles {
      void triangle(int var1, float[] var2, float[] var3, float[] var4);
   }
}
