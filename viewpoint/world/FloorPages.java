package viewpoint.world;

import java.util.HashMap;
import viewpoint.platform.Caches;
import viewpoint.render.FloorArt;
import viewpoint.render.FloorPage;

final class FloorPages {
   private static final HashMap<Long, FloorPages.Held> held = new HashMap<>();
   private static long bytes;

   static void put(int var0, int var1, int var2, FloorArt var3) {
      if (var3 == null) {
         remove(var0, var1, var2);
      } else {
         long var4 = FloorPage.key(var0, var1, var2);
         FloorPages.Held var6 = held.get(var4);
         if (var6 == null || !var6.art.sameArt(var3)) {
            if (var6 == null) {
               var6 = new FloorPages.Held();
               held.put(var4, var6);
            }

            bytes = bytes + (var3.bytes() - (var6.art == null ? 0L : var6.art.bytes()));
            var6.art = var3;
            repage(var0, var1, var2);
         }
      }
   }

   static void remove(int var0, int var1, int var2) {
      FloorPages.Held var3 = held.remove(FloorPage.key(var0, var1, var2));
      if (var3 != null) {
         bytes = bytes - var3.art.bytes();
         repage(var0, var1, var2);
      }
   }

   static FloorPage page(int var0, int var1, int var2) {
      FloorPages.Held var3 = held.get(FloorPage.key(var0, var1, var2));
      return var3 == null ? null : var3.page;
   }

   static void clear() {
      held.clear();
      bytes = 0L;
   }

   private static void repage(int var0, int var1, int var2) {
      for (int var3 = -1; var3 <= 1; var3++) {
         for (int var4 = -1; var4 <= 1; var4++) {
            FloorPages.Held var5 = held.get(FloorPage.key(var0 + var4, var1 + var3, var2));
            if (var5 != null) {
               var5.page = new FloorPage(var0 + var4, var1 + var3, var2, var5.art, neighbours(var0 + var4, var1 + var3, var2));
            }
         }
      }
   }

   private static FloorArt[] neighbours(int var0, int var1, int var2) {
      FloorArt[] var3 = new FloorArt[FloorPage.NEIGHBOURS.length / 2];

      for (int var4 = 0; var4 < var3.length; var4++) {
         FloorPages.Held var5 = held.get(FloorPage.key(var0 + FloorPage.NEIGHBOURS[var4 * 2], var1 + FloorPage.NEIGHBOURS[var4 * 2 + 1], var2));
         var3[var4] = var5 == null ? null : var5.art;
      }

      return var3;
   }

   private FloorPages() {
   }

   static {
      Caches.register(new Caches.Cache() {
         @Override
         public long bytes() {
            return FloorPages.bytes;
         }

         @Override
         public void list(Caches.Survey var1) {
         }

         @Override
         public void evictBefore(long var1) {
         }
      });
   }

   private static final class Held {
      FloorArt art;
      FloorPage page;
   }
}
