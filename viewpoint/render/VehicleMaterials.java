package viewpoint.render;

import java.util.Arrays;
import zombie.core.textures.Texture;

public final class VehicleMaterials {
   public static final int SINGLE_UV = 0;
   public static final int MULTI_UV = 1;
   public static final int NO_PAINT = 2;
   public static final int TEXTURES = 7;
   public static final int MATRICES = 12;
   static final int VALUES = 197;
   static final int RUST = 192;
   static final int PAINT = 193;
   static final int KIND = 196;
   Texture[] textures = new Texture[112];
   float[] values = new float[3152];
   int count;

   void reset() {
      Arrays.fill(this.textures, 0, this.count * 7, null);
      this.count = 0;
   }

   public int add(Texture[] var1, float[][] var2, float var3, float var4, float var5, float var6, int var7) {
      if (this.count * 197 == this.values.length) {
         this.textures = Arrays.copyOf(this.textures, this.count * 2 * 7);
         this.values = Arrays.copyOf(this.values, this.count * 2 * 197);
      }

      int var8 = this.count++;
      System.arraycopy(var1, 0, this.textures, var8 * 7, 7);

      for (int var9 = 0; var9 < 12; var9++) {
         System.arraycopy(var2[var9], 0, this.values, var8 * 197 + var9 * 16, 16);
      }

      this.values[var8 * 197 + 192] = var3;
      this.values[var8 * 197 + 193] = var4;
      this.values[var8 * 197 + 193 + 1] = var5;
      this.values[var8 * 197 + 193 + 2] = var6;
      this.values[var8 * 197 + 196] = var7;
      return var8;
   }

   public int count() {
      return this.count;
   }
}
