package viewpoint.render;

import java.util.Arrays;
import zombie.core.textures.Texture;

public final class FlameDraws {
   public static final int MAX = 192;
   static final int VALUES = 20;
   final float[] values = new float[3840];
   final Texture[] textures = new Texture[192];
   int count;

   public void clear() {
      Arrays.fill(this.textures, 0, this.count, null);
      this.count = 0;
   }

   public boolean full() {
      return this.count == 192;
   }

   public void add(Texture var1, float[] var2, float[] var3, float[] var4, float var5, float[] var6, boolean var7) {
      if (!this.full()) {
         int var8 = this.count * 20;
         System.arraycopy(var3, 0, this.values, var8, 3);
         this.values[var8 + 3] = var5;
         System.arraycopy(var4, 0, this.values, var8 + 4, 4);
         System.arraycopy(var2, 0, this.values, var8 + 8, 4);
         System.arraycopy(var6, 0, this.values, var8 + 12, 4);
         this.values[var8 + 16] = var7 ? 1.0F : 0.0F;
         this.textures[this.count++] = var1;
      }
   }
}
