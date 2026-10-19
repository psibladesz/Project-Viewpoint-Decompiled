package viewpoint.light;

import java.util.Arrays;
import viewpoint.platform.LongMap;
import viewpoint.render.BandLight;

final class BandGrids {
   private static final int BAND_CHUNKS = 35;
   private static final LongMap<LevelLight> shown = new LongMap<>(4096);
   private static final int[] rings = new int[1226];
   private static int centreX = Integer.MIN_VALUE;
   private static int centreY;
   private static int reach;

   static void update(int var0, int var1) {
      if (!MapLight.band()) {
         if (centreX != Integer.MIN_VALUE) {
            clear();
         }
      } else {
         if (var0 != centreX || var1 != centreY) {
            centreX = var0;
            centreY = var1;
            fit();
         }

         for (LevelLight var3 : LightJobs.worked) {
            if (inside(var3.chunkX, var3.chunkY, var3.level)) {
               show(var3);
            }
         }

         if (shown.size() > BandLight.share()) {
            fit();
         }
      }
   }

   static void dropped(int var0, int var1, int var2) {
      if (shown.remove(LightLayouts.key(var0, var1, var2)) != null) {
         BandLight.hide(var0, var1, var2);
      }
   }

   static int reachChunks() {
      return (int)Math.sqrt(Math.max(reach, 0));
   }

   static void clear() {
      shown.clear();
      centreX = Integer.MIN_VALUE;
      BandLight.clear();
   }

   private static void fit() {
      Arrays.fill(rings, 0);
      LightJobs.lights(var0x -> {
         int var1 = distance(var0x.chunkX, var0x.chunkY);
         if (var1 < rings.length && onLevels(var0x.level)) {
            rings[var1]++;
         }
      });
      long var0 = BandLight.share();
      long var2 = 0L;
      reach = -1;

      while (reach + 1 < rings.length && var2 + rings[reach + 1] <= var0) {
         var2 += rings[++reach];
      }

      shown.removeIf(var0x -> {
         boolean var1 = !inside(var0x.chunkX, var0x.chunkY, var0x.level);
         if (var1) {
            BandLight.hide(var0x.chunkX, var0x.chunkY, var0x.level);
         }

         return var1;
      });
      LightJobs.lights(var0x -> {
         if (inside(var0x.chunkX, var0x.chunkY, var0x.level) && shown.get(LightLayouts.key(var0x.chunkX, var0x.chunkY, var0x.level)) == null) {
            show(var0x);
         }
      });
   }

   private static void show(LevelLight var0) {
      shown.put(LightLayouts.key(var0.chunkX, var0.chunkY, var0.level), var0);
      BandLight.show(var0.chunkX, var0.chunkY, var0.level, var0.grid);
   }

   private static boolean inside(int var0, int var1, int var2) {
      return distance(var0, var1) <= reach && onLevels(var2);
   }

   private static int distance(int var0, int var1) {
      int var2 = var0 - centreX;
      int var3 = var1 - centreY;
      return var2 * var2 + var3 * var3;
   }

   private static boolean onLevels(int var0) {
      return var0 >= 0 && var0 < 32;
   }

   private BandGrids() {
   }
}
