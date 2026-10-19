package viewpoint.light;

import java.util.List;

final class LightWork {
   private static final int START = 20800;
   LightLayout layout;
   LightFlood flood;
   byte[] steps;
   int[] queue;
   int[] values;
   int[] starts = new int[64];

   LightLayout layout(int var1, int var2, int var3, int var4, int var5, int var6, List<LightLayouts.Level> var7) {
      int var8 = var4 - var1 + 1;
      int var9 = var5 - var2 + 1;
      int var10 = var6 - var3 + 1;
      if (this.layout == null || var8 * var9 * var10 > this.layout.capacity()) {
         int var11 = Math.max(20800, var8 * var9 * var10);
         this.layout = new LightLayout(var11);
         this.flood = new LightFlood(this.layout);
         this.steps = new byte[var11];
         this.queue = new int[var11];
         this.values = new int[var11];
      }

      this.layout.box(var1, var2, var3, var8, var9, var10);

      for (LightLayouts.Level var12 : var7) {
         this.layout.chunk(var12);
      }

      return this.layout;
   }

   int[] starts(int var1) {
      if (this.starts.length < var1) {
         this.starts = new int[var1];
      }

      return this.starts;
   }
}
