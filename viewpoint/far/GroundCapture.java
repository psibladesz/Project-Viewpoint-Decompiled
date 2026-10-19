package viewpoint.far;

import java.util.Arrays;
import viewpoint.render.GroundArt;
import viewpoint.render.GroundPage;
import viewpoint.render.GroundTiles;

final class GroundCapture {
   private static final int SIDE = 80;
   private final GroundTiles tiles;
   private final int x0;
   private final int y0;
   private final int[] head = new int[6400];
   private final int[] tail = new int[6400];
   private final boolean[] based = new boolean[6400];
   private int[] next = new int[4096];
   private int[] value = new int[4096];
   private int count;

   GroundCapture(GroundTiles var1, int var2, int var3) {
      this.tiles = var1;
      this.x0 = var2 * 64 - 8;
      this.y0 = var3 * 64 - 8;
      Arrays.fill(this.head, -1);
   }

   boolean add(int var1, FarTiles.Tile var2, int var3, int var4) {
      int var5 = var3 - this.x0;
      int var6 = var4 - this.y0;
      if (this.tiles.paints(var1) && var5 >= 0 && var6 >= 0 && var5 < 80 && var6 < 80) {
         int var7 = var6 * 80 + var5;
         boolean var8 = !this.based[var7] && (var2.flags & 2) != 0 && (var2.flags & 4) == 0;
         this.based[var7] = this.based[var7] | var8;
         if (this.count == this.value.length) {
            this.next = Arrays.copyOf(this.next, this.count * 2);
            this.value = Arrays.copyOf(this.value, this.count * 2);
         }

         this.value[this.count] = var1 << 1 | (var8 ? 1 : 0);
         this.next[this.count] = -1;
         if (this.head[var7] < 0) {
            this.head[var7] = this.count;
         } else {
            this.next[this.tail[var7]] = this.count;
         }

         this.tail[var7] = this.count++;
         return true;
      } else {
         return false;
      }
   }

   GroundPage build() {
      GroundArt[] var1 = new GroundArt[100];
      GroundArt.Builder var2 = new GroundArt.Builder();
      boolean var3 = false;

      for (int var4 = 0; var4 < 10; var4++) {
         for (int var5 = 0; var5 < 10; var5++) {
            var2.begin(this.tiles);

            for (int var6 = 0; var6 < 8; var6++) {
               for (int var7 = 0; var7 < 8; var7++) {
                  int var8 = (var4 * 8 + var6) * 80 + var5 * 8 + var7;

                  for (int var9 = this.head[var8]; var9 >= 0; var9 = this.next[var9]) {
                     var2.layer(var6 * 8 + var7, this.value[var9] >> 1, (this.value[var9] & 1) != 0);
                  }
               }
            }

            GroundArt var10 = var1[var4 * 10 + var5] = var2.build();
            boolean var11 = var5 > 0 && var4 > 0 && var5 < 9 && var4 < 9;
            var3 |= var11 && var10 != null;
         }
      }

      return var3 ? new GroundPage(var1) : null;
   }
}
