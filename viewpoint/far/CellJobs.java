package viewpoint.far;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentLinkedQueue;
import viewpoint.platform.Caches;
import viewpoint.render.FarGpu;
import zombie.iso.IsoLot;
import zombie.iso.LotHeader;
import zombie.iso.MapFiles;

final class CellJobs {
   private static final ConcurrentLinkedQueue<CellJobs.Done> DONE = new ConcurrentLinkedQueue<>();

   static CellJobs.Done poll() {
      return DONE.poll();
   }

   static boolean start(FarWorld.Slot var0, long var1) {
      int var3 = FarWorld.detail(var0.distance);
      int var4 = CellColours.size(var0.distance);
      if (var0.height != null && !CellColours.tooSmall(var0.colours, var0.distance)) {
         remesh(var0, var3);
         return true;
      }

      MapFiles var5 = mapFiles(var0.cellX, var0.cellY);
      LotHeader var6 = var5 == null ? null : (LotHeader)var5.infoHeaders.get(var0.cellX + "_" + var0.cellY + ".lotheader");
      String var7 = var5 == null ? null : (String)var5.infoFileNames.get("world_" + var0.cellX + "_" + var0.cellY + ".lotpack");
      if (var6 != null && var7 != null) {
         FarTiles.Tile[] var8 = FarTiles.table(var6, var1);
         if (var8 == null) {
            return false;
         }

         Pyramid var9 = Pyramid.of(var5.mapDirectoryAbsolutePath);
         Relief.want(var5.mapDirectoryAbsolutePath);
         BiomeTrees.Table var10 = BiomeTrees.table();
         int var11 = var6.minLevel;
         int var12 = var6.maxLevel;
         var0.lotpack = var7;
         var0.minLevel = var11;
         var0.maxLevel = var12;
         var0.tiles = var8;
         var0.legend = BiomeTrees.legend(var6);
         var0.busy = true;
         var0.used = Caches.tick();
         FarWorkers.jobs.incrementAndGet();
         FarWorkers.POOL
            .execute(
               () -> {
                  try {
                     byte[] var10x = Files.readAllBytes(new File(var7).toPath());
                     FarCell var11x = FarCell.read(
                        var0.cellX,
                        var0.cellY,
                        var10x,
                        var11,
                        var12,
                        var8,
                        var9.image(var0.cellX, var0.cellY),
                        BiomeTrees.image(var5.mapDirectoryAbsolutePath, var0.cellX, var0.cellY),
                        var10,
                        var0.legend
                     );
                     DONE.add(CellJobs.Done.read(var0, var11x, new FarGpu.Mesh(var3, FarMesher.mesh(var11x.height, var3)), var4));
                  } catch (Throwable var15) {
                     System.out.println("[Viewpoint] far world: reading cell " + var0.cellX + "," + var0.cellY + " failed: " + var15);
                     DONE.add(CellJobs.Done.failed(var0));
                  } finally {
                     FarWorkers.jobs.decrementAndGet();
                  }
               }
            );
         return true;
      } else {
         var0.busy = true;
         return true;
      }
   }

   private static void remesh(FarWorld.Slot var0, int var1) {
      short[] var2 = var0.height;
      var0.busy = true;
      var0.used = Caches.tick();
      FarWorkers.jobs.incrementAndGet();
      FarWorkers.POOL.execute(() -> {
         try {
            DONE.add(CellJobs.Done.meshed(var0, new FarGpu.Mesh(var1, FarMesher.mesh(var2, var1))));
         } catch (Throwable var7) {
            System.out.println("[Viewpoint] far world: meshing cell " + var0.cellX + "," + var0.cellY + " failed: " + var7);
            DONE.add(CellJobs.Done.failed(var0));
         } finally {
            FarWorkers.jobs.decrementAndGet();
         }
      });
   }

   static MapFiles mapFiles(int var0, int var1) {
      String var2 = "world_" + var0 + "_" + var1 + ".lotpack";

      for (MapFiles var4 : IsoLot.MapFiles) {
         if (var4.infoFileNames.containsKey(var2)) {
            return var4;
         }
      }

      return null;
   }

   private CellJobs() {
   }

   record Done(FarWorld.Slot slot, FarCell cell, FarGpu.Mesh mesh, FarGpu.Colours colours, FarGpu.Trees trees, boolean failed) {
      static CellJobs.Done read(FarWorld.Slot var0, FarCell var1, FarGpu.Mesh var2, int var3) {
         return new CellJobs.Done(
            var0,
            var1,
            var2,
            new FarGpu.Colours(CellColours.shrink(var1.top, var3), CellColours.shrink(var1.side, var3), var3),
            new FarGpu.Trees(var1.trees, var1.treesBelow),
            false
         );
      }

      static CellJobs.Done meshed(FarWorld.Slot var0, FarGpu.Mesh var1) {
         return new CellJobs.Done(var0, null, var1, null, null, false);
      }

      static CellJobs.Done failed(FarWorld.Slot var0) {
         return new CellJobs.Done(var0, null, null, null, null, true);
      }

      boolean uploaded() {
         return this.mesh.uploaded() && (this.colours == null || this.colours.uploaded()) && (this.trees == null || this.trees.uploaded());
      }
   }
}
