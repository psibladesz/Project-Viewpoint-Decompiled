package viewpoint.render;

import java.util.Arrays;
import zombie.core.Core;
import zombie.core.textures.Texture;
import zombie.core.textures.TextureID;

public final class FloorArt implements FloorLayers {
   public static final int SIZE = 8;
   public static final int SQUARES = 64;
   public static final int U0 = 0;
   public static final int V0 = 1;
   public static final int U1 = 2;
   public static final int V1 = 3;
   public static final int WIDTH = 4;
   public static final int HEIGHT = 5;
   public static final int LEFT = 6;
   public static final int TOP = 7;
   public static final int SPAN_X = 8;
   public static final int SPAN_Y = 9;
   public static final int R = 10;
   public static final int G = 11;
   public static final int B = 12;
   public static final int ALPHA = 13;
   public static final int TEXELS = 14;
   public static final int NUMBERS = 15;
   public static final int FLIP = 1;
   public static final int OPAQUE = 2;
   public static final int MATERIAL_SHIFT = 2;
   private static final float ISO_HALF_WIDTH = 32.0F;
   private static final float ISO_HALF_HEIGHT = 16.0F;
   private static final float SPAN_LEAST = 0.001F;
   private final int[] starts;
   private final TextureID[] atlases;
   private final float[] numbers;
   private final byte[] flags;
   private final float texels;

   public static void numbers(float[] var0, Texture var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
      float var10 = 32.0F * Core.tileScale;
      float var11 = 16.0F * Core.tileScale;
      var0[0] = var1.getXStart();
      var0[1] = var1.getYStart();
      var0[2] = var1.getXEnd();
      var0[3] = var1.getYEnd();
      var0[4] = var1.getWidth();
      var0[5] = var1.getHeight();
      var0[6] = var2 / var10;
      var0[7] = var3 / var11;
      var0[8] = var1.getWidth() * var4 / var10;
      var0[9] = var1.getHeight() * var5 / var11;
      var0[10] = var6;
      var0[11] = var7;
      var0[12] = var8;
      var0[13] = var9;
      float var12 = Math.abs(var0[2] - var0[0]) * var1.getWidthHW() / Math.max(var0[8], 0.001F);
      float var13 = Math.abs(var0[3] - var0[1]) * var1.getHeightHW() / Math.max(var0[9], 0.001F);
      var0[14] = (float)Math.sqrt(2.0 * var12 * var13);
   }

   private FloorArt(int[] var1, TextureID[] var2, float[] var3, byte[] var4) {
      this.starts = var1;
      this.atlases = var2;
      this.numbers = var3;
      this.flags = var4;
      float var5 = 0.0F;

      for (int var6 = 0; var6 < var4.length; var6++) {
         if ((var4[var6] & 2) != 0) {
            var5 = Math.max(var5, var3[var6 * 15 + 14]);
         }
      }

      this.texels = var5;
   }

   public float texels() {
      return this.texels;
   }

   @Override
   public int first(int var1) {
      return this.starts[var1];
   }

   @Override
   public int end(int var1) {
      return this.starts[var1 + 1];
   }

   public boolean present(int var1) {
      return this.starts[var1 + 1] > this.starts[var1];
   }

   public int layers() {
      return this.starts[64];
   }

   @Override
   public TextureID atlas(int var1) {
      return this.atlases[var1];
   }

   @Override
   public float number(int var1, int var2) {
      return this.numbers[var1 * 15 + var2];
   }

   @Override
   public boolean has(int var1, int var2) {
      return (this.flags[var1] & var2) != 0;
   }

   @Override
   public int material(int var1) {
      return (this.flags[var1] & 0xFF) >> 2;
   }

   public static int withMaterial(int var0, int var1) {
      return var0 | var1 << 2;
   }

   public boolean sameArt(FloorArt var1) {
      if (var1 == this) {
         return true;
      }

      if (var1 != null && Arrays.equals(this.starts, var1.starts) && Arrays.equals(this.flags, var1.flags)) {
         for (int var2 = 0; var2 < this.atlases.length; var2++) {
            if (this.atlases[var2] != var1.atlases[var2]) {
               return false;
            }
         }

         return Arrays.equals(this.numbers, var1.numbers);
      } else {
         return false;
      }
   }

   public long bytes() {
      return 64L + this.starts.length * 4L + this.atlases.length * 8L + this.numbers.length * 4L + this.flags.length;
   }

   public static final class Builder {
      private final int[] starts = new int[65];
      private TextureID[] atlases = new TextureID[256];
      private float[] numbers = new float[3840];
      private byte[] flags = new byte[256];
      private int square;
      private int layers;

      public void begin() {
         this.square = 0;
         this.layers = 0;
         this.starts[0] = 0;
      }

      public void square(int var1) {
         while (this.square < var1) {
            this.starts[++this.square] = this.layers;
         }
      }

      public void layer(TextureID var1, float[] var2, int var3) {
         if (this.layers == this.atlases.length) {
            this.atlases = Arrays.copyOf(this.atlases, this.layers * 2);
            this.numbers = Arrays.copyOf(this.numbers, this.layers * 2 * 15);
            this.flags = Arrays.copyOf(this.flags, this.layers * 2);
         }

         this.atlases[this.layers] = var1;
         System.arraycopy(var2, 0, this.numbers, this.layers * 15, 15);
         this.flags[this.layers] = (byte)var3;
         this.layers++;
         this.starts[this.square + 1] = this.layers;
      }

      public FloorArt build() {
         this.square(64);
         return this.layers == 0
            ? null
            : new FloorArt(
               (int[])this.starts.clone(),
               Arrays.copyOf(this.atlases, this.layers),
               Arrays.copyOf(this.numbers, this.layers * 15),
               Arrays.copyOf(this.flags, this.layers)
            );
      }
   }
}
