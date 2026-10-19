package viewpoint.render;

import zombie.core.textures.TextureID;

public final class GroundTiles {
   private final TextureID[] atlases;
   private final float[] numbers;

   public GroundTiles(TextureID[] var1, float[] var2) {
      this.atlases = var1;
      this.numbers = var2;
   }

   public long bytes() {
      return 32L + this.atlases.length * 8L + this.numbers.length * 4L;
   }

   public boolean paints(int var1) {
      return var1 >= 0 && var1 < this.atlases.length && this.atlases[var1] != null;
   }

   TextureID atlas(int var1) {
      return this.atlases[var1];
   }

   float number(int var1, int var2) {
      return this.numbers[var1 * 15 + var2];
   }
}
