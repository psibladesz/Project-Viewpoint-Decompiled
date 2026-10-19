package viewpoint.render;

import java.util.Arrays;
import zombie.core.textures.TextureID;

public final class GroundArt implements FloorLayers {
   private final GroundTiles tiles;
   private final int[] starts;
   private final int[] layers;

   private GroundArt(GroundTiles var1, int[] var2, int[] var3) {
      this.tiles = var1;
      this.starts = var2;
      this.layers = var3;
   }

   @Override
   public int first(int var1) {
      return this.starts[var1];
   }

   @Override
   public int end(int var1) {
      return this.starts[var1 + 1];
   }

   @Override
   public TextureID atlas(int var1) {
      return this.tiles.atlas(this.layers[var1] >> 1);
   }

   @Override
   public float number(int var1, int var2) {
      return this.tiles.number(this.layers[var1] >> 1, var2);
   }

   @Override
   public boolean has(int var1, int var2) {
      return var2 == 2 && (this.layers[var1] & 1) != 0;
   }

   public long bytes() {
      return 32L + this.starts.length * 4L + this.layers.length * 4L;
   }

   public static final class Builder {
      private final int[] starts = new int[65];
      private int[] layers = new int[256];
      private int square;
      private int count;
      private GroundTiles tiles;

      public void begin(GroundTiles var1) {
         this.tiles = var1;
         this.square = 0;
         this.count = 0;
         this.starts[0] = 0;
      }

      public void layer(int var1, int var2, boolean var3) {
         while (this.square < var1) {
            this.starts[++this.square] = this.count;
         }

         if (this.count == this.layers.length) {
            this.layers = Arrays.copyOf(this.layers, this.count * 2);
         }

         this.layers[this.count++] = var2 << 1 | (var3 ? 1 : 0);
         this.starts[this.square + 1] = this.count;
      }

      public GroundArt build() {
         while (this.square < 64) {
            this.starts[++this.square] = this.count;
         }

         return this.count == 0 ? null : new GroundArt(this.tiles, (int[])this.starts.clone(), Arrays.copyOf(this.layers, this.count));
      }
   }
}
