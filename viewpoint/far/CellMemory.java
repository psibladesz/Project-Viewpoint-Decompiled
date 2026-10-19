package viewpoint.far;

import viewpoint.platform.Caches;

final class CellMemory implements Caches.Cache {
   @Override
   public long bytes() {
      long var1 = 0L;

      for (FarWorld.Slot var4 : FarWorld.slots.values()) {
         var1 += bytes(var4);
      }

      return var1;
   }

   @Override
   public void list(Caches.Survey var1) {
      for (FarWorld.Slot var3 : FarWorld.slots.values()) {
         long var4 = bytes(var3);
         if (var4 > 0L) {
            var1.add(var3.used, var4);
         }
      }
   }

   @Override
   public void evictBefore(long var1) {
      for (FarWorld.Slot var4 : FarWorld.slots.values()) {
         if (var4.used < var1) {
            var4.height = null;
            var4.biomes = null;
            var4.rooms = null;
            var4.facades = null;
            var4.roofs = null;
            var4.groundTiles = null;
            var4.buildingBounds = null;
         }
      }
   }

   private static long bytes(FarWorld.Slot var0) {
      long var1 = (var0.height == null ? 0L : var0.height.length * 2L)
         + (var0.biomes == null ? 0L : var0.biomes.length)
         + (var0.facades == null ? 0L : var0.facades.bytes())
         + (var0.roofs == null ? 0L : var0.roofs.bytes())
         + (var0.groundTiles == null ? 0L : var0.groundTiles.bytes())
         + (var0.buildingBounds == null ? 0L : var0.buildingBounds.length * 4L);
      if (var0.rooms != null) {
         for (short[] var6 : var0.rooms) {
            var1 += var6 == null ? 0L : var6.length * 2L;
         }
      }

      return var1;
   }
}
