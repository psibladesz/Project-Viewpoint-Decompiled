package viewpoint.world;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import viewpoint.packs.PackLoads;
import viewpoint.render.PackModel;
import viewpoint.render.PackModels;

final class ModelWaits {
   private static final IdentityHashMap<PackModel, ArrayList<ChunkCache.Level>> byModel = new IdentityHashMap<>();
   private static final IdentityHashMap<ChunkCache.Level, ArrayList<PackModel>> byLevel = new IdentityHashMap<>();
   private static int lastRoom;

   static void gathered(ChunkCache.Level var0, List<PackModel> var1) {
      if (var1 == null) {
         byLevel.remove(var0);
      } else {
         byLevel.put(var0, new ArrayList<>(var1));

         for (PackModel var3 : var1) {
            ArrayList<ChunkCache.Level> var4 = byModel.computeIfAbsent(var3, var0x -> new ArrayList<>());
            if (!var4.contains(var0)) {
               var4.add(var0);
            }
         }
      }
   }

   static void frame() {
      PackLoads.frame();
      roomAgain();

      PackModel var0;
      while ((var0 = PackModels.settled()) != null) {
         ArrayList<ChunkCache.Level> var1 = byModel.remove(var0);

         for (int var2 = 0; var1 != null && var2 < var1.size(); var2++) {
            ChunkCache.Level var3 = var1.get(var2);
            ArrayList<PackModel> var4 = byLevel.get(var3);
            if (var4 != null && !var3.evicted) {
               var4.remove(var0);
               if (!onTheirWay(var4)) {
                  byLevel.remove(var3);
                  ChunkCache.mark(var3, 4);
               }
            } else {
               byLevel.remove(var3);
            }
         }
      }
   }

   private static void roomAgain() {
      int var0 = PackModels.roomAgain();
      if (var0 != lastRoom) {
         lastRoom = var0;
         Iterator<Entry<ChunkCache.Level, ArrayList<PackModel>>> var1 = byLevel.entrySet().iterator();

         while (var1.hasNext()) {
            Entry<ChunkCache.Level, ArrayList<PackModel>> var2 = var1.next();
            ChunkCache.Level var3 = var2.getKey();
            boolean var4 = false;

            for (PackModel var6 : var2.getValue()) {
               var4 |= var6.state() == 5;
            }

            if (var4 || var3.evicted) {
               var1.remove();
            }

            if (var4 && !var3.evicted) {
               ChunkCache.mark(var3, 4);
            }
         }
      }
   }

   static void clear() {
      byModel.clear();
      byLevel.clear();
      PackLoads.clear();
   }

   private static boolean onTheirWay(List<PackModel> var0) {
      for (PackModel var2 : var0) {
         int var3 = var2.state();
         if (var3 == 1 || var3 == 2) {
            return true;
         }
      }

      return false;
   }

   private ModelWaits() {
   }
}